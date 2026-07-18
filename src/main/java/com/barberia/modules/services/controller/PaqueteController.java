package com.barberia.modules.services.controller;

import com.barberia.common.ApiResponse;
import com.barberia.modules.services.dto.PaqueteRequest;
import com.barberia.modules.services.dto.PaqueteResponse;
import com.barberia.modules.services.service.PaqueteService;
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
@RequestMapping("/api/packages")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Paquetes", description = "Promociones / combos de servicios a precio fijo")
public class PaqueteController {

    private final PaqueteService paqueteService;

    @Operation(summary = "Listar paquetes activos")
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<PaqueteResponse>>> listarActivos() {
        return ResponseEntity.ok(ApiResponse.ok(paqueteService.listarActivos()));
    }

    @Operation(summary = "Listar todos los paquetes, incluidos inactivos")
    @GetMapping("/all")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<PaqueteResponse>>> listarTodos() {
        return ResponseEntity.ok(ApiResponse.ok(paqueteService.listarTodos()));
    }

    @Operation(summary = "Crear paquete")
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PaqueteResponse>> crear(@Valid @RequestBody PaqueteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(paqueteService.crear(request)));
    }

    @Operation(summary = "Actualizar paquete")
    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PaqueteResponse>> actualizar(
            @PathVariable Integer id, @Valid @RequestBody PaqueteRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Paquete actualizado", paqueteService.actualizar(id, request)));
    }

    @Operation(summary = "Activar o desactivar paquete")
    @PatchMapping("/{id}/status")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> cambiarEstado(
            @PathVariable Integer id, @RequestBody Map<String, Boolean> body) {
        Boolean estado = body.get("estado");
        if (estado == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("El campo 'estado' es obligatorio"));
        }
        paqueteService.cambiarEstado(id, estado);
        return ResponseEntity.ok(ApiResponse.ok("Estado actualizado correctamente"));
    }
}
