package com.barberia.modules.loyalty.controller;

import com.barberia.common.ApiResponse;
import com.barberia.common.PageResponse;
import com.barberia.modules.loyalty.dto.ActualizarReglaFidelizacionRequest;
import com.barberia.modules.loyalty.dto.AgregarSelloRequest;
import com.barberia.modules.loyalty.dto.FidelizacionCuentaResponse;
import com.barberia.modules.loyalty.dto.ReglaFidelizacionResponse;
import com.barberia.modules.loyalty.dto.SelloResponse;
import com.barberia.modules.loyalty.entity.Recompensa;
import com.barberia.modules.loyalty.service.impl.FidelizacionServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/loyalty")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Fidelización", description = "Gestión de sellos, recompensas y fidelización de clientes")
public class FidelizacionController {

    // Igual criterio que ComisionController: configurar el catalogo (que
    // aplica sello) es cosa de ADMIN/CASHIER/RECEPTION, no del barbero.
    private static final String EDITAR = "hasAnyRole('ADMIN','CASHIER','RECEPTION')";

    private final FidelizacionServiceImpl fidelizacionBiz;

    @Operation(summary = "Ver cuenta de fidelización de un cliente")
    @GetMapping("/customers/{customerId}")
    @PreAuthorize("hasAnyRole('ADMIN','CASHIER','RECEPTION','BARBER')")
    public ResponseEntity<ApiResponse<FidelizacionCuentaResponse>> obtenerCuenta(
            @PathVariable Integer customerId) {
        return ResponseEntity.ok(ApiResponse.ok(fidelizacionBiz.obtenerCuenta(customerId)));
    }

    @Operation(summary = "Agregar sello a un cliente",
               description = "Suma un sello y verifica automáticamente si corresponde una nueva recompensa.")
    @PostMapping("/customers/{customerId}/add-stamp")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<SelloResponse>> agregarSello(
            @PathVariable Integer customerId,
            @Valid @RequestBody AgregarSelloRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(fidelizacionBiz.agregarSello(customerId, request)));
    }

    @Operation(summary = "Quitar un sello de un cliente",
               description = "Para corregir un sello agregado por error. Quita de a uno.")
    @PostMapping("/customers/{customerId}/remove-stamp")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<SelloResponse>> quitarSello(
            @PathVariable Integer customerId,
            @RequestBody(required = false) Map<String, String> body) {
        String motivo = body != null ? body.get("motivo") : null;
        return ResponseEntity.ok(ApiResponse.ok(fidelizacionBiz.quitarSello(customerId, motivo)));
    }

    @Operation(summary = "Canjear recompensa de un cliente",
               description = "El cliente debe tener recompensas disponibles.")
    @PostMapping("/customers/{customerId}/redeem")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<SelloResponse>> canjear(
            @PathVariable Integer customerId,
            @RequestBody Map<String, String> body) {
        String motivo = body.getOrDefault("motivo", "Canje de recompensa");
        return ResponseEntity.ok(ApiResponse.ok(fidelizacionBiz.canjearRecompensa(customerId, motivo)));
    }

    @Operation(summary = "Editar la descripción de la recompensa pendiente de un cliente",
               description = "Permite personalizar qué premio le corresponde a este cliente en particular " +
                       "(ej. '50% descuento' en vez del premio por defecto). Enviar vacío para volver al default.")
    @PatchMapping("/customers/{customerId}/reward-description")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<FidelizacionCuentaResponse>> actualizarDescripcionRecompensa(
            @PathVariable Integer customerId,
            @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(ApiResponse.ok("Recompensa actualizada",
                fidelizacionBiz.actualizarDescripcionRecompensa(customerId, body.get("descripcion"))));
    }

    @Operation(summary = "Ver catálogo de recompensas activas")
    @GetMapping("/rewards")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<Recompensa>>> listarRecompensas() {
        return ResponseEntity.ok(ApiResponse.ok(fidelizacionBiz.listarRecompensas()));
    }

    @Operation(summary = "Clientes con recompensas pendientes de canjear")
    @GetMapping("/customers-with-rewards")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PageResponse<FidelizacionCuentaResponse>>> clientesConRecompensas(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageRequest pageable = PageRequest.of(page, size,
                Sort.by("recompensasDisponibles").descending());
        return ResponseEntity.ok(ApiResponse.ok(fidelizacionBiz.listarClientesConRecompensas(pageable)));
    }

    @Operation(summary = "Listar todos los clientes con su progreso de fidelización")
    @GetMapping("/customers")
    @PreAuthorize("hasAnyRole('ADMIN','CASHIER','RECEPTION','BARBER')")
    public ResponseEntity<ApiResponse<PageResponse<FidelizacionCuentaResponse>>> todasLasCuentas(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("nombre").ascending());
        return ResponseEntity.ok(ApiResponse.ok(fidelizacionBiz.listarTodasLasCuentas(pageable, search)));
    }

    @Operation(summary = "Historial de movimientos de fidelización de un cliente")
    @GetMapping("/customers/{customerId}/history")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PageResponse<?>>> historial(
            @PathVariable Integer customerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("fecha").descending());
        return ResponseEntity.ok(ApiResponse.ok(fidelizacionBiz.historialMovimientos(customerId, pageable)));
    }

    // ─── Reglas de fidelización por ítem del catálogo (no por cliente) ─────

    @Operation(summary = "Listar reglas de fidelización de servicios")
    @GetMapping("/rules/services")
    @PreAuthorize(EDITAR)
    public ResponseEntity<ApiResponse<List<ReglaFidelizacionResponse>>> listarReglasServicios() {
        return ResponseEntity.ok(ApiResponse.ok(fidelizacionBiz.listarReglasServicios()));
    }

    @Operation(summary = "Definir si un servicio suma sello de fidelización")
    @PatchMapping("/rules/services/{id}")
    @PreAuthorize(EDITAR)
    public ResponseEntity<ApiResponse<ReglaFidelizacionResponse>> actualizarReglaServicio(
            @PathVariable Integer id, @Valid @RequestBody ActualizarReglaFidelizacionRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Regla actualizada", fidelizacionBiz.actualizarReglaServicio(id, request)));
    }

    @Operation(summary = "Listar reglas de fidelización de productos")
    @GetMapping("/rules/products")
    @PreAuthorize(EDITAR)
    public ResponseEntity<ApiResponse<List<ReglaFidelizacionResponse>>> listarReglasProductos() {
        return ResponseEntity.ok(ApiResponse.ok(fidelizacionBiz.listarReglasProductos()));
    }

    @Operation(summary = "Definir si un producto suma sello de fidelización")
    @PatchMapping("/rules/products/{id}")
    @PreAuthorize(EDITAR)
    public ResponseEntity<ApiResponse<ReglaFidelizacionResponse>> actualizarReglaProducto(
            @PathVariable Integer id, @Valid @RequestBody ActualizarReglaFidelizacionRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Regla actualizada", fidelizacionBiz.actualizarReglaProducto(id, request)));
    }

    @Operation(summary = "Listar reglas de fidelización de paquetes")
    @GetMapping("/rules/packages")
    @PreAuthorize(EDITAR)
    public ResponseEntity<ApiResponse<List<ReglaFidelizacionResponse>>> listarReglasPaquetes() {
        return ResponseEntity.ok(ApiResponse.ok(fidelizacionBiz.listarReglasPaquetes()));
    }

    @Operation(summary = "Definir si un paquete suma sello de fidelización")
    @PatchMapping("/rules/packages/{id}")
    @PreAuthorize(EDITAR)
    public ResponseEntity<ApiResponse<ReglaFidelizacionResponse>> actualizarReglaPaquete(
            @PathVariable Integer id, @Valid @RequestBody ActualizarReglaFidelizacionRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Regla actualizada", fidelizacionBiz.actualizarReglaPaquete(id, request)));
    }
}
