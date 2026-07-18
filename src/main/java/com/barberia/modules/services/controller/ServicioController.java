package com.barberia.modules.services.controller;

import com.barberia.common.ApiResponse;
import com.barberia.common.PageResponse;
import com.barberia.modules.services.dto.ServicioRequest;
import com.barberia.modules.services.dto.ServicioResponse;
import com.barberia.modules.services.service.impl.ServicioServiceImpl;
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
@RequestMapping("/api/services")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Servicios", description = "Catálogo de servicios de la barbería")
public class ServicioController {

    private final ServicioServiceImpl servicioBiz;

    @Operation(summary = "Listar servicios activos")
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PageResponse<ServicioResponse>>> listar(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String categoria,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageRequest pageable = PageRequest.of(page, Math.min(size, 100), Sort.by("nombre"));
        return ResponseEntity.ok(ApiResponse.ok(servicioBiz.listar(search, pageable)));
    }

    @Operation(summary = "Obtener servicio por ID")
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ServicioResponse>> obtenerPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.ok(servicioBiz.obtenerPorId(id)));
    }

    @Operation(summary = "Crear servicio")
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ServicioResponse>> crear(
            @Valid @RequestBody ServicioRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(servicioBiz.crear(request)));
    }

    @Operation(summary = "Actualizar servicio")
    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ServicioResponse>> actualizar(
            @PathVariable Integer id,
            @Valid @RequestBody ServicioRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Servicio actualizado", servicioBiz.actualizar(id, request)));
    }

    @Operation(summary = "Activar o desactivar servicio")
    @PatchMapping("/{id}/status")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> cambiarEstado(
            @PathVariable Integer id,
            @RequestBody Map<String, Boolean> body) {
        servicioBiz.cambiarEstado(id, body.getOrDefault("estado", true));
        return ResponseEntity.ok(ApiResponse.ok("Estado actualizado correctamente"));
    }

    @Operation(summary = "Eliminar servicio")
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> eliminar(@PathVariable Integer id) {
        servicioBiz.cambiarEstado(id, false);
        return ResponseEntity.ok(ApiResponse.ok("Servicio eliminado correctamente"));
    }
}
