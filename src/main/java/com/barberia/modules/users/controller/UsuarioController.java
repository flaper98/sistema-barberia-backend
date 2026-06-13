package com.barberia.modules.users.controller;

import com.barberia.common.ApiResponse;
import com.barberia.common.PageResponse;
import com.barberia.modules.users.dto.UsuarioRequest;
import com.barberia.modules.users.dto.UsuarioResponse;
import com.barberia.modules.users.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
@RequestMapping("/api/users")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Usuarios", description = "Gestión de usuarios del sistema")
public class UsuarioController {

    private final UsuarioService usuarioService;

    @Operation(summary = "Listar usuarios", description = "Solo ADMIN puede listar todos los usuarios")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse<UsuarioResponse>>> listar(
            @Parameter(description = "Búsqueda por nombre, apellido o email")
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageRequest pageable = PageRequest.of(page, Math.min(size, 100),
                Sort.by("apellido", "nombre"));
        return ResponseEntity.ok(ApiResponse.ok(usuarioService.listarUsuarios(search, pageable)));
    }

    @Operation(summary = "Obtener usuario por ID")
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UsuarioResponse>> obtenerPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.ok(usuarioService.obtenerPorId(id)));
    }

    @Operation(summary = "Crear usuario", description = "Solo ADMIN puede crear usuarios")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UsuarioResponse>> crear(
            @Valid @RequestBody UsuarioRequest request) {
        UsuarioResponse response = usuarioService.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(response));
    }

    @Operation(summary = "Actualizar usuario")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UsuarioResponse>> actualizar(
            @PathVariable Integer id,
            @Valid @RequestBody UsuarioRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Usuario actualizado", usuarioService.actualizar(id, request)));
    }

    @Operation(summary = "Cambiar estado del usuario (activar/desactivar)")
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> cambiarEstado(
            @PathVariable Integer id,
            @RequestBody Map<String, Boolean> body) {
        Boolean estado = body.get("estado");
        if (estado == null) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("El campo 'estado' es obligatorio"));
        }
        usuarioService.cambiarEstado(id, estado);
        return ResponseEntity.ok(ApiResponse.ok("Estado actualizado correctamente"));
    }

    @Operation(summary = "Eliminar usuario (deshabilitación lógica)")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> eliminar(@PathVariable Integer id) {
        usuarioService.eliminar(id);
        return ResponseEntity.ok(ApiResponse.ok("Usuario deshabilitado correctamente"));
    }
}
