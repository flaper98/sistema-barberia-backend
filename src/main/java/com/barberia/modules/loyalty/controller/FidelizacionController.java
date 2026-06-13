package com.barberia.modules.loyalty.controller;

import com.barberia.common.ApiResponse;
import com.barberia.common.PageResponse;
import com.barberia.modules.loyalty.dto.AgregarSelloRequest;
import com.barberia.modules.loyalty.dto.FidelizacionCuentaResponse;
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

    private final FidelizacionServiceImpl fidelizacionBiz;

    @Operation(summary = "Ver cuenta de fidelización de un cliente")
    @GetMapping("/customers/{customerId}")
    @PreAuthorize("hasAnyRole('ADMIN','CASHIER','RECEPTION','BARBER')")
    public ResponseEntity<ApiResponse<FidelizacionCuentaResponse>> obtenerCuenta(
            @PathVariable Integer customerId) {
        return ResponseEntity.ok(ApiResponse.ok(fidelizacionBiz.obtenerCuenta(customerId)));
    }

    @Operation(summary = "Agregar sello a un cliente",
               description = "Usa el stored procedure sp_agregar_sello. Verifica automáticamente si corresponde recompensa.")
    @PostMapping("/customers/{customerId}/add-stamp")
    @PreAuthorize("hasAnyRole('ADMIN','CASHIER','BARBER')")
    public ResponseEntity<ApiResponse<SelloResponse>> agregarSello(
            @PathVariable Integer customerId,
            @Valid @RequestBody AgregarSelloRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(fidelizacionBiz.agregarSello(customerId, request)));
    }

    @Operation(summary = "Canjear recompensa de un cliente",
               description = "Usa el stored procedure sp_canjear_recompensa. El cliente debe tener recompensas disponibles.")
    @PostMapping("/customers/{customerId}/redeem")
    @PreAuthorize("hasAnyRole('ADMIN','CASHIER','RECEPTION')")
    public ResponseEntity<ApiResponse<SelloResponse>> canjear(
            @PathVariable Integer customerId,
            @RequestBody Map<String, String> body) {
        String motivo = body.getOrDefault("motivo", "Canje de recompensa");
        return ResponseEntity.ok(ApiResponse.ok(fidelizacionBiz.canjearRecompensa(customerId, motivo)));
    }

    @Operation(summary = "Ver catálogo de recompensas activas")
    @GetMapping("/rewards")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<Recompensa>>> listarRecompensas() {
        return ResponseEntity.ok(ApiResponse.ok(fidelizacionBiz.listarRecompensas()));
    }

    @Operation(summary = "Clientes con recompensas pendientes de canjear")
    @GetMapping("/customers-with-rewards")
    @PreAuthorize("hasAnyRole('ADMIN','CASHIER','RECEPTION')")
    public ResponseEntity<ApiResponse<PageResponse<FidelizacionCuentaResponse>>> clientesConRecompensas(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageRequest pageable = PageRequest.of(page, size,
                Sort.by("recompensasDisponibles").descending());
        return ResponseEntity.ok(ApiResponse.ok(fidelizacionBiz.listarClientesConRecompensas(pageable)));
    }

    @Operation(summary = "Historial de movimientos de fidelización de un cliente")
    @GetMapping("/customers/{customerId}/history")
    @PreAuthorize("hasAnyRole('ADMIN','CASHIER','RECEPTION')")
    public ResponseEntity<ApiResponse<PageResponse<?>>> historial(
            @PathVariable Integer customerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("fecha").descending());
        return ResponseEntity.ok(ApiResponse.ok(fidelizacionBiz.historialMovimientos(customerId, pageable)));
    }
}
