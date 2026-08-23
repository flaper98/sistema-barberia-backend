package com.barberia.modules.whatsapp.controller;

import com.barberia.common.ApiResponse;
import com.barberia.modules.whatsapp.dto.WhatsAppConfigRequest;
import com.barberia.modules.whatsapp.dto.WhatsAppConfigResponse;
import com.barberia.modules.whatsapp.service.WhatsAppConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/config/whatsapp")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "WhatsApp", description = "Configuración del envío automático de bienvenida por WhatsApp (API oficial de Meta)")
public class WhatsAppConfigController {

    private final WhatsAppConfigService service;

    @Operation(summary = "Obtener configuración de WhatsApp (nunca incluye el access token)")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<WhatsAppConfigResponse>> obtener() {
        return ResponseEntity.ok(ApiResponse.ok(service.obtener()));
    }

    @Operation(summary = "Guardar configuración de WhatsApp",
               description = "El access token se cifra antes de guardarse. Si se omite, se conserva el anterior.")
    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<WhatsAppConfigResponse>> actualizar(@Valid @RequestBody WhatsAppConfigRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Configuración de WhatsApp actualizada", service.actualizar(request)));
    }
}
