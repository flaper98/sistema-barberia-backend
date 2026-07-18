package com.barberia.modules.products.controller;

import com.barberia.common.ApiResponse;
import com.barberia.modules.products.dto.CategoriaRequest;
import com.barberia.modules.products.dto.CategoriaResponse;
import com.barberia.modules.products.service.CategoriaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products/categories")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Categorías de productos", description = "Administración de categorías de productos")
public class CategoriaController {

    private final CategoriaService categoriaService;

    @Operation(summary = "Listar categorías activas (para selects)")
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<CategoriaResponse>>> listarActivas() {
        return ResponseEntity.ok(ApiResponse.ok(categoriaService.listarActivas()));
    }

    @Operation(summary = "Listar todas las categorías, incluidas inactivas")
    @GetMapping("/all")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<CategoriaResponse>>> listarTodas() {
        return ResponseEntity.ok(ApiResponse.ok(categoriaService.listarTodas()));
    }

    @Operation(summary = "Crear categoría")
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<CategoriaResponse>> crear(@Valid @RequestBody CategoriaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(categoriaService.crear(request)));
    }

    @Operation(summary = "Renombrar categoría", description = "Actualiza también los productos que ya la tenían asignada")
    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<CategoriaResponse>> actualizar(
            @PathVariable Integer id, @Valid @RequestBody CategoriaRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Categoría actualizada", categoriaService.actualizar(id, request)));
    }

    @Operation(summary = "Activar o desactivar categoría")
    @PatchMapping("/{id}/status")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> cambiarEstado(
            @PathVariable Integer id, @RequestBody Map<String, Boolean> body) {
        Boolean estado = body.get("estado");
        if (estado == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("El campo 'estado' es obligatorio"));
        }
        categoriaService.cambiarEstado(id, estado);
        return ResponseEntity.ok(ApiResponse.ok("Estado actualizado correctamente"));
    }
}
