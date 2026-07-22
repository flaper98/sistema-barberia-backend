package com.barberia.modules.notifications.controller;

import com.barberia.common.ApiResponse;
import com.barberia.common.PageResponse;
import com.barberia.modules.notifications.dto.NotificacionResponse;
import com.barberia.modules.notifications.service.NotificacionService;
import com.barberia.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Notificaciones del usuario autenticado -- siempre "las mias", no hay
 * endpoint para ver las de otro usuario (ni ADMIN). No pasa por la matriz de
 * permisos (no es un modulo de negocio), solo requiere estar logueado.
 */
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Notificaciones", description = "Notificaciones internas del usuario autenticado")
public class NotificacionController {

    private final NotificacionService notificacionService;

    @Operation(summary = "Listar mis notificaciones")
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<NotificacionResponse>>> listar(
            @AuthenticationPrincipal CustomUserDetails principal,
            @RequestParam(defaultValue = "false") boolean soloNoLeidas,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageRequest pageable = PageRequest.of(page, Math.min(size, 50));
        return ResponseEntity.ok(ApiResponse.ok(
                notificacionService.listar(principal.getId(), soloNoLeidas, pageable)));
    }

    @Operation(summary = "Cantidad de notificaciones no leidas")
    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<Map<String, Long>>> contarNoLeidas(
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(ApiResponse.ok(
                Map.of("count", notificacionService.contarNoLeidas(principal.getId()))));
    }

    @Operation(summary = "Marcar una notificacion como leida")
    @PatchMapping("/{id}/read")
    public ResponseEntity<ApiResponse<Void>> marcarLeida(
            @AuthenticationPrincipal CustomUserDetails principal,
            @PathVariable Long id) {
        notificacionService.marcarLeida(principal.getId(), id);
        return ResponseEntity.ok(ApiResponse.ok("Notificación marcada como leída", null));
    }

    @Operation(summary = "Marcar todas mis notificaciones como leidas")
    @PatchMapping("/read-all")
    public ResponseEntity<ApiResponse<Void>> marcarTodasLeidas(
            @AuthenticationPrincipal CustomUserDetails principal) {
        notificacionService.marcarTodasLeidas(principal.getId());
        return ResponseEntity.ok(ApiResponse.ok("Notificaciones marcadas como leídas", null));
    }
}
