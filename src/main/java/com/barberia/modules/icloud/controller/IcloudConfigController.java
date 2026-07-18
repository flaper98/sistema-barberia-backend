package com.barberia.modules.icloud.controller;

import com.barberia.common.ApiResponse;
import com.barberia.modules.icloud.dto.IcloudConfigRequest;
import com.barberia.modules.icloud.dto.IcloudConfigResponse;
import com.barberia.modules.icloud.service.IcloudConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/config/icloud")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Sincronización iCloud", description = "Configuración de sincronización de clientes con iCloud (CardDAV)")
public class IcloudConfigController {

    private final IcloudConfigService service;

    @Operation(summary = "Obtener configuración de iCloud (nunca incluye la contraseña)")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<IcloudConfigResponse>> obtener() {
        return ResponseEntity.ok(ApiResponse.ok(service.obtener()));
    }

    @Operation(summary = "Guardar configuración de iCloud",
               description = "La contraseña de aplicación se cifra antes de guardarse. Si se omite, se conserva la anterior.")
    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<IcloudConfigResponse>> actualizar(@Valid @RequestBody IcloudConfigRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Configuración de iCloud actualizada", service.actualizar(request)));
    }
}
