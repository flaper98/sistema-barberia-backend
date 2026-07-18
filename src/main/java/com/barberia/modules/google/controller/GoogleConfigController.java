package com.barberia.modules.google.controller;

import com.barberia.common.ApiResponse;
import com.barberia.modules.google.dto.GoogleConfigRequest;
import com.barberia.modules.google.dto.GoogleConfigResponse;
import com.barberia.modules.google.service.GoogleConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/config/google")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Sincronización Google Contacts", description = "Configuración de sincronización de clientes con Google Contacts (CardDAV)")
public class GoogleConfigController {

    private final GoogleConfigService service;

    @Operation(summary = "Obtener configuración de Google (nunca incluye la contraseña)")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<GoogleConfigResponse>> obtener() {
        return ResponseEntity.ok(ApiResponse.ok(service.obtener()));
    }

    @Operation(summary = "Guardar configuración de Google",
               description = "La contraseña de aplicación se cifra antes de guardarse. Si se omite, se conserva la anterior.")
    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<GoogleConfigResponse>> actualizar(@Valid @RequestBody GoogleConfigRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Configuración de Google actualizada", service.actualizar(request)));
    }
}
