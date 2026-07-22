package com.barberia.modules.sales.controller;

import com.barberia.common.ApiResponse;
import com.barberia.common.PageResponse;
import com.barberia.modules.sales.dto.ConfirmarVentaRequest;
import com.barberia.modules.sales.dto.VentaItemsUpdateRequest;
import com.barberia.modules.sales.dto.VentaRequest;
import com.barberia.modules.sales.dto.VentaResponse;
import com.barberia.modules.sales.dto.VentaUpdateRequest;
import com.barberia.modules.sales.entity.EstadoVenta;
import com.barberia.modules.sales.service.impl.VentaServiceImpl;
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
import java.time.LocalDateTime;
import java.time.LocalTime;

@RestController
@RequestMapping("/api/sales")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Ventas", description = "Registro y consulta de ventas")
public class VentaController {

    private final VentaServiceImpl ventaBiz;

    @Operation(summary = "Listar ventas con filtros opcionales")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','CASHIER','RECEPTION')")
    public ResponseEntity<ApiResponse<PageResponse<VentaResponse>>> listar(
            @RequestParam(required = false) EstadoVenta estado,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        LocalDateTime desdeTs = desde != null ? desde.atStartOfDay() : null;
        LocalDateTime hastaTs = hasta != null ? hasta.atTime(LocalTime.MAX) : null;
        PageRequest pageable = PageRequest.of(page, Math.min(size, 100), Sort.by("fecha").descending());
        return ResponseEntity.ok(ApiResponse.ok(ventaBiz.listar(estado, desdeTs, hastaTs, pageable)));
    }

    @Operation(summary = "Obtener venta por ID")
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<VentaResponse>> obtenerPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.ok(ventaBiz.obtenerPorId(id)));
    }

    @Operation(summary = "Ventas por barbero",
               description = "Usado por \"Mis Ventas\" (BARBER) -- cada venta trae calculada la comisión que " +
                       "le corresponde a ese barbero, no el total de la venta. Rango de fechas opcional.")
    @GetMapping("/by-worker/{workerId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CASHIER') or " +
                  "(hasRole('BARBER') and #workerId == authentication.principal.usuario.barberoId)")
    public ResponseEntity<ApiResponse<PageResponse<VentaResponse>>> porBarbero(
            @PathVariable Integer workerId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        LocalDateTime desdeTs = desde != null ? desde.atStartOfDay() : null;
        LocalDateTime hastaTs = hasta != null ? hasta.atTime(LocalTime.MAX) : null;
        PageRequest pageable = PageRequest.of(page, size, Sort.by("fecha").descending());
        return ResponseEntity.ok(ApiResponse.ok(ventaBiz.listarPorBarbero(workerId, desdeTs, hastaTs, pageable)));
    }

    @Operation(summary = "Ventas por cliente")
    @GetMapping("/by-customer/{customerId}")
    @PreAuthorize("hasAnyRole('ADMIN','CASHIER')")
    public ResponseEntity<ApiResponse<PageResponse<VentaResponse>>> porCliente(
            @PathVariable Integer customerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("fecha").descending());
        return ResponseEntity.ok(ApiResponse.ok(ventaBiz.listarPorCliente(customerId, pageable)));
    }

    @Operation(summary = "Registrar venta",
               description = "El backend calcula totales y descuenta stock. Los triggers de BD también se ejecutan.")
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<VentaResponse>> crear(
            @Valid @RequestBody VentaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(ventaBiz.crear(request)));
    }

    @Operation(summary = "Venta rápida (alias de POST /api/sales)")
    @PostMapping("/quick-sale")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<VentaResponse>> ventaRapida(
            @Valid @RequestBody VentaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(ventaBiz.crear(request)));
    }

    @Operation(summary = "Editar una venta ya registrada",
               description = "Solo ADMIN. Permite corregir cliente, barbero, método de pago, descuento, notas " +
                       "e ítems (productos/servicios) de una venta existente. El subtotal y el total se " +
                       "recalculan en el backend a partir de los ítems recibidos.")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<VentaResponse>> actualizar(
            @PathVariable Integer id,
            @Valid @RequestBody VentaUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Venta actualizada", ventaBiz.actualizar(id, request)));
    }

    @Operation(summary = "Editar los ítems de la propia solicitud de venta (BARBER)",
               description = "Permite al barbero agregar o quitar servicios/productos de una solicitud " +
                       "PENDIENTE que él mismo creó (ej. el cliente pide algo más a mitad de la atención), " +
                       "mientras recepción todavía no la aceptó y cobró. No permite cambiar cliente, método " +
                       "de pago ni barbero -- para eso está PUT /{id} (solo ADMIN).")
    @PatchMapping("/{id}/items")
    @PreAuthorize("hasRole('BARBER')")
    public ResponseEntity<ApiResponse<VentaResponse>> actualizarItems(
            @PathVariable Integer id,
            @Valid @RequestBody VentaItemsUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Ítems actualizados", ventaBiz.actualizarItemsPropios(id, request)));
    }

    @Operation(summary = "Eliminar una venta definitivamente",
               description = "Solo ADMIN. A diferencia de anular (que solo cambia el estado), esto borra la " +
                       "venta de la base de datos. Si venía de una cita, la desvincula.")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> eliminar(@PathVariable Integer id) {
        ventaBiz.eliminar(id);
        return ResponseEntity.ok(ApiResponse.ok("Venta eliminada", null));
    }

    @Operation(summary = "Anular venta")
    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN','CASHIER')")
    public ResponseEntity<ApiResponse<VentaResponse>> anular(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.ok("Venta anulada", ventaBiz.anular(id)));
    }

    @Operation(summary = "Aceptar y cobrar una solicitud de venta creada por un barbero",
               description = "Pasa la venta de PENDIENTE a COMPLETADA: recién ahí se registra el ingreso, " +
                       "se suma el sello de fidelización y se marca la cita como atendida.")
    @PatchMapping("/{id}/confirm")
    @PreAuthorize("hasAnyRole('ADMIN','CASHIER','RECEPTION')")
    public ResponseEntity<ApiResponse<VentaResponse>> confirmar(
            @PathVariable Integer id,
            @Valid @RequestBody ConfirmarVentaRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Venta aceptada y cobrada",
                ventaBiz.confirmar(id, request.getPagos(), request.getClienteId())));
    }
}
