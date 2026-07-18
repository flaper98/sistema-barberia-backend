package com.barberia.modules.barbershop.controller;

import com.barberia.common.ApiResponse;
import com.barberia.modules.barbershop.dto.ConfiguracionBarberiaRequest;
import com.barberia.modules.barbershop.dto.ConfiguracionBarberiaResponse;
import com.barberia.modules.barbershop.service.impl.ConfiguracionBarberiaServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/config/barbershop")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Configuración", description = "Configuración de la barbería")
public class ConfiguracionBarberiaController {

    private final ConfiguracionBarberiaServiceImpl service;

    @Operation(summary = "Obtener configuración de la barbería")
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ConfiguracionBarberiaResponse>> obtener() {
        return ResponseEntity.ok(ApiResponse.ok(service.obtener()));
    }

    @Operation(summary = "Actualizar configuración de la barbería")
    @PutMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ConfiguracionBarberiaResponse>> actualizar(
            @Valid @RequestBody ConfiguracionBarberiaRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Configuración actualizada", service.actualizar(request)));
    }
}
