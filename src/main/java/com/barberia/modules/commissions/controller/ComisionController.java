package com.barberia.modules.commissions.controller;

import com.barberia.common.ApiResponse;
import com.barberia.common.PageResponse;
import com.barberia.modules.commissions.dto.*;
import com.barberia.modules.commissions.entity.EstadoCorte;
import com.barberia.modules.commissions.service.impl.ComisionServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/commissions")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Comisiones", description = "Comisiones, propinas y descuentos por consumo de los barberos")
public class ComisionController {

    private static final String VER_PROPIA = "hasAnyRole('ADMIN','CASHIER','RECEPTION') or " +
            "(hasRole('BARBER') and #barberoId == authentication.principal.usuario.barberoId)";
    private static final String EDITAR = "hasAnyRole('ADMIN','CASHIER','RECEPTION')";
    // El detalle por item (ventas/comisiones de un barbero, con fecha) es
    // mas sensible que el resumen -- ADMIN siempre puede, un barbero puede
    // ver el suyo propio, y CASHIER/RECEPTION necesitan el permiso
    // explicito "Ver detalle" del modulo COMISIONES (arranca apagado, ver
    // RolPermisoSeeder -- el administrador lo habilita desde Usuarios ->
    // Roles y permisos si quiere).
    private static final String VER_DETALLE = "@rolPermisoService.puedeVerDetalleComisiones(authentication.principal, #barberoId)";

    private final ComisionServiceImpl comisionService;

    @Operation(summary = "Resumen de comisión pendiente de un barbero",
               description = "Lo que se le debe si se generara un corte ahora mismo para ese rango. Por defecto, el mes actual.")
    @GetMapping("/summary/{barberoId}")
    @PreAuthorize(VER_PROPIA)
    public ResponseEntity<ApiResponse<ResumenComisionResponse>> resumen(
            @PathVariable Integer barberoId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        if (desde == null) desde = LocalDate.now().withDayOfMonth(1);
        if (hasta == null) hasta = LocalDate.now();
        return ResponseEntity.ok(ApiResponse.ok(comisionService.obtenerResumenPendiente(barberoId, desde, hasta)));
    }

    @Operation(summary = "Detalle de comisión por ítem de un barbero",
               description = "Cuanto vendio y cuanto le corresponde de CADA servicio/producto/paquete puntual " +
                       "en ese rango, no solo el total agrupado por categoría. Por defecto, el mes actual.")
    @GetMapping("/summary/{barberoId}/items")
    @PreAuthorize(VER_DETALLE)
    public ResponseEntity<ApiResponse<List<DetalleComisionResponse>>> resumenDetallado(
            @PathVariable Integer barberoId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        if (desde == null) desde = LocalDate.now().withDayOfMonth(1);
        if (hasta == null) hasta = LocalDate.now();
        return ResponseEntity.ok(ApiResponse.ok(comisionService.obtenerDetalleComision(barberoId, desde, hasta)));
    }

    @Operation(summary = "Registrar una propina para un barbero")
    @PostMapping("/tips")
    @PreAuthorize(EDITAR)
    public ResponseEntity<ApiResponse<PropinaResponse>> registrarPropina(@Valid @RequestBody PropinaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(comisionService.registrarPropina(request)));
    }

    @Operation(summary = "Eliminar una propina (solo si todavía no está en un corte)")
    @DeleteMapping("/tips/{id}")
    @PreAuthorize(EDITAR)
    public ResponseEntity<ApiResponse<Void>> eliminarPropina(@PathVariable Long id) {
        comisionService.eliminarPropina(id);
        return ResponseEntity.ok(ApiResponse.ok("Propina eliminada", null));
    }

    @Operation(summary = "Listar propinas de un barbero")
    @GetMapping("/tips/{barberoId}")
    @PreAuthorize(VER_PROPIA)
    public ResponseEntity<ApiResponse<PageResponse<PropinaResponse>>> listarPropinas(
            @PathVariable Integer barberoId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.ok(comisionService.listarPropinas(barberoId, PageRequest.of(page, size))));
    }

    @Operation(summary = "Registrar el consumo personal de un producto por un barbero",
               description = "Descuenta el stock (movimiento tipo CONSUMO) y resta el monto de la comisión del barbero.")
    @PostMapping("/consumption")
    @PreAuthorize(EDITAR)
    public ResponseEntity<ApiResponse<DescuentoConsumoResponse>> registrarConsumo(@Valid @RequestBody DescuentoConsumoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(comisionService.registrarConsumo(request)));
    }

    @Operation(summary = "Eliminar un descuento por consumo (solo si todavía no está en un corte)")
    @DeleteMapping("/consumption/{id}")
    @PreAuthorize(EDITAR)
    public ResponseEntity<ApiResponse<Void>> eliminarDescuento(@PathVariable Long id) {
        comisionService.eliminarDescuento(id);
        return ResponseEntity.ok(ApiResponse.ok("Descuento eliminado", null));
    }

    @Operation(summary = "Listar descuentos por consumo de un barbero")
    @GetMapping("/consumption/{barberoId}")
    @PreAuthorize(VER_PROPIA)
    public ResponseEntity<ApiResponse<PageResponse<DescuentoConsumoResponse>>> listarDescuentos(
            @PathVariable Integer barberoId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.ok(comisionService.listarDescuentos(barberoId, PageRequest.of(page, size))));
    }

    @Operation(summary = "Generar un corte de comisión para un barbero",
               description = "Cierra el periodo pedido: calcula comisión de servicios/productos y agrupa propinas/descuentos sueltos de ese rango.")
    @PostMapping("/cuts")
    @PreAuthorize(EDITAR)
    public ResponseEntity<ApiResponse<CorteComisionResponse>> generarCorte(@Valid @RequestBody GenerarCorteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(comisionService.generarCorte(request)));
    }

    @Operation(summary = "Marcar un corte como pagado",
               description = "Genera automáticamente un egreso en Finanzas por el total. Un corte pagado no se puede editar ni volver a generar.")
    @PatchMapping("/cuts/{id}/pay")
    @PreAuthorize(EDITAR)
    public ResponseEntity<ApiResponse<CorteComisionResponse>> marcarComoPagado(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Corte marcado como pagado", comisionService.marcarComoPagado(id)));
    }

    @Operation(summary = "Listar cortes de comisión de un barbero")
    @GetMapping("/cuts")
    @PreAuthorize(VER_PROPIA)
    public ResponseEntity<ApiResponse<PageResponse<CorteComisionResponse>>> listarCortes(
            @RequestParam Integer barberoId,
            @RequestParam(required = false) EstadoCorte estado,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("fechaHasta").descending());
        return ResponseEntity.ok(ApiResponse.ok(comisionService.listarCortes(barberoId, estado, pageable)));
    }

    // ─── Tasas de comisión por ítem del catálogo (no por barbero) ──────────

    @Operation(summary = "Listar tasas de comisión de servicios")
    @GetMapping("/rates/services")
    @PreAuthorize(EDITAR)
    public ResponseEntity<ApiResponse<List<TasaComisionResponse>>> listarTasasServicios() {
        return ResponseEntity.ok(ApiResponse.ok(comisionService.listarTasasServicios()));
    }

    @Operation(summary = "Actualizar el % de comisión de un servicio")
    @PatchMapping("/rates/services/{id}")
    @PreAuthorize(EDITAR)
    public ResponseEntity<ApiResponse<TasaComisionResponse>> actualizarTasaServicio(
            @PathVariable Integer id, @Valid @RequestBody ActualizarTasaRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Tasa actualizada", comisionService.actualizarTasaServicio(id, request.getPorcentajeComision())));
    }

    @Operation(summary = "Listar tasas de comisión de productos")
    @GetMapping("/rates/products")
    @PreAuthorize(EDITAR)
    public ResponseEntity<ApiResponse<List<TasaComisionResponse>>> listarTasasProductos() {
        return ResponseEntity.ok(ApiResponse.ok(comisionService.listarTasasProductos()));
    }

    @Operation(summary = "Actualizar el % de comisión de un producto")
    @PatchMapping("/rates/products/{id}")
    @PreAuthorize(EDITAR)
    public ResponseEntity<ApiResponse<TasaComisionResponse>> actualizarTasaProducto(
            @PathVariable Integer id, @Valid @RequestBody ActualizarTasaRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Tasa actualizada", comisionService.actualizarTasaProducto(id, request.getPorcentajeComision())));
    }

    @Operation(summary = "Listar tasas de comisión de paquetes")
    @GetMapping("/rates/packages")
    @PreAuthorize(EDITAR)
    public ResponseEntity<ApiResponse<List<TasaComisionResponse>>> listarTasasPaquetes() {
        return ResponseEntity.ok(ApiResponse.ok(comisionService.listarTasasPaquetes()));
    }

    @Operation(summary = "Actualizar el % de comisión de un paquete")
    @PatchMapping("/rates/packages/{id}")
    @PreAuthorize(EDITAR)
    public ResponseEntity<ApiResponse<TasaComisionResponse>> actualizarTasaPaquete(
            @PathVariable Integer id, @Valid @RequestBody ActualizarTasaRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Tasa actualizada", comisionService.actualizarTasaPaquete(id, request.getPorcentajeComision())));
    }
}
