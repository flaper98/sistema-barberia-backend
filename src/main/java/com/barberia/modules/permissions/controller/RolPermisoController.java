package com.barberia.modules.permissions.controller;

import com.barberia.common.ApiResponse;
import com.barberia.modules.permissions.dto.PermisoFlags;
import com.barberia.modules.permissions.dto.RolPermisoItem;
import com.barberia.modules.permissions.entity.Modulo;
import com.barberia.modules.permissions.service.RolPermisoService;
import com.barberia.modules.users.entity.Rol;
import com.barberia.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/role-permissions")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Permisos por rol", description = "Qué sección del sistema puede ver/editar cada rol")
public class RolPermisoController {

    private final RolPermisoService rolPermisoService;

    @Operation(summary = "Obtener la matriz completa de permisos (BARBER, CASHIER, RECEPTION)")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<Rol, Map<Modulo, PermisoFlags>>>> obtenerMatriz() {
        return ResponseEntity.ok(ApiResponse.ok(rolPermisoService.obtenerMatriz()));
    }

    @Operation(summary = "Guardar la matriz de permisos")
    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> actualizarMatriz(@Valid @RequestBody List<RolPermisoItem> items) {
        rolPermisoService.actualizarMatriz(items);
        return ResponseEntity.ok(ApiResponse.ok("Permisos actualizados correctamente"));
    }

    @Operation(summary = "Permisos del rol del usuario autenticado (para pintar el menú)")
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Map<Modulo, PermisoFlags>>> obtenerMisPermisos(
            @AuthenticationPrincipal CustomUserDetails principal) {
        return ResponseEntity.ok(ApiResponse.ok(rolPermisoService.obtenerParaRol(principal.getUsuario().getRol())));
    }
}
