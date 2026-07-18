package com.barberia.modules.loyalty.controller;

import com.barberia.common.ApiResponse;
import com.barberia.exception.ResourceNotFoundException;
import com.barberia.modules.audit.AuditAction;
import com.barberia.modules.audit.AuditPublisher;
import com.barberia.modules.loyalty.dto.FidelizacionConfigRequest;
import com.barberia.modules.loyalty.dto.FidelizacionConfigResponse;
import com.barberia.modules.loyalty.entity.FidelizacionConfig;
import com.barberia.modules.loyalty.repository.FidelizacionConfigRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/loyalty/config")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Configuración de Fidelización", description = "Configuración del sistema de sellos y recompensas")
public class FidelizacionConfigController {

    private final FidelizacionConfigRepository configRepository;
    private final AuditPublisher auditPublisher;

    @Operation(summary = "Obtener configuración actual del sistema de fidelización")
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<FidelizacionConfigResponse>> obtener() {
        FidelizacionConfig config = configRepository.findById(1)
                .orElseThrow(() -> new ResourceNotFoundException("FidelizacionConfig", 1));
        return ResponseEntity.ok(ApiResponse.ok(toResponse(config)));
    }

    @Operation(summary = "Actualizar configuración del sistema de fidelización")
    @PutMapping
    @PreAuthorize("isAuthenticated()")
    @Transactional
    public ResponseEntity<ApiResponse<FidelizacionConfigResponse>> actualizar(
            @Valid @RequestBody FidelizacionConfigRequest request) {
        FidelizacionConfig config = configRepository.findById(1)
                .orElseThrow(() -> new ResourceNotFoundException("FidelizacionConfig", 1));

        config.setSellosNecesarios(request.getSellosNecesarios());
        config.setSellosPorGanancia(request.getSellosPorGanancia());
        config.setDescripcionRecompensa(request.getDescripcionRecompensa());
        config.setActivo(request.getActivo());

        configRepository.save(config);
        auditPublisher.publish(AuditAction.LOYALTY_CONFIG_UPDATE, "ADMIN",
                "FIDELIZACION_CONFIG", "1",
                "sellosNecesarios=" + config.getSellosNecesarios());
        return ResponseEntity.ok(ApiResponse.ok("Configuración de fidelización actualizada", toResponse(config)));
    }

    private FidelizacionConfigResponse toResponse(FidelizacionConfig c) {
        return FidelizacionConfigResponse.builder()
                .id(c.getId())
                .sellosNecesarios(c.getSellosNecesarios())
                .sellosPorGanancia(c.getSellosPorGanancia())
                .descripcionRecompensa(c.getDescripcionRecompensa())
                .activo(c.getActivo())
                .updatedAt(c.getUpdatedAt())
                .build();
    }
}
