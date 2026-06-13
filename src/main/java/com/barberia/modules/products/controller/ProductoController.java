package com.barberia.modules.products.controller;

import com.barberia.common.ApiResponse;
import com.barberia.common.PageResponse;
import com.barberia.modules.products.dto.ProductoRequest;
import com.barberia.modules.products.dto.ProductoResponse;
import com.barberia.modules.products.service.impl.ProductoServiceImpl;
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

import java.util.Map;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Productos", description = "Catálogo de productos e inventario")
public class ProductoController {

    private final ProductoServiceImpl productoBiz;

    @Operation(summary = "Listar productos activos")
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PageResponse<ProductoResponse>>> listar(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageRequest pageable = PageRequest.of(page, Math.min(size, 100), Sort.by("nombre"));
        return ResponseEntity.ok(ApiResponse.ok(productoBiz.listar(search, pageable)));
    }

    @Operation(summary = "Obtener producto por ID")
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ProductoResponse>> obtenerPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.ok(productoBiz.obtenerPorId(id)));
    }

    @Operation(summary = "Productos con stock bajo o agotado")
    @GetMapping("/low-stock")
    @PreAuthorize("hasAnyRole('ADMIN','CASHIER')")
    public ResponseEntity<ApiResponse<PageResponse<ProductoResponse>>> stockBajo(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageRequest pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.ok(productoBiz.listarConStockBajo(pageable)));
    }

    @Operation(summary = "Crear producto", description = "Solo ADMIN")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProductoResponse>> crear(
            @Valid @RequestBody ProductoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(productoBiz.crear(request)));
    }

    @Operation(summary = "Actualizar producto", description = "Solo ADMIN")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProductoResponse>> actualizar(
            @PathVariable Integer id,
            @Valid @RequestBody ProductoRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Producto actualizado", productoBiz.actualizar(id, request)));
    }

    @Operation(summary = "Activar o desactivar producto")
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> cambiarEstado(
            @PathVariable Integer id,
            @RequestBody Map<String, Boolean> body) {
        productoBiz.cambiarEstado(id, body.getOrDefault("estado", true));
        return ResponseEntity.ok(ApiResponse.ok("Estado actualizado correctamente"));
    }
}
