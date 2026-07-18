package com.barberia.modules.inventory.controller;

import com.barberia.common.ApiResponse;
import com.barberia.common.PageResponse;
import com.barberia.modules.inventory.dto.MovimientoRequest;
import com.barberia.modules.inventory.dto.MovimientoResponse;
import com.barberia.modules.inventory.service.impl.InventarioServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Inventario", description = "Movimientos de stock de productos")
public class InventarioController {

    private final InventarioServiceImpl inventarioBiz;

    @Operation(summary = "Listar movimientos de stock. Filtrar por productoId si se desea.")
    @GetMapping("/movements")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PageResponse<MovimientoResponse>>> listar(
            @RequestParam(required = false) Integer productoId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageRequest pageable = PageRequest.of(page, Math.min(size, 100),
                Sort.by("createdAt").descending());
        return ResponseEntity.ok(ApiResponse.ok(inventarioBiz.listarMovimientos(productoId, pageable)));
    }

    @Operation(summary = "Registrar movimiento manual de stock (entrada, salida o ajuste)")
    @PostMapping("/movements")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<MovimientoResponse>> registrar(
            @Valid @RequestBody MovimientoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(inventarioBiz.registrarMovimiento(request)));
    }
}
