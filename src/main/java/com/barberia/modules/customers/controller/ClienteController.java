package com.barberia.modules.customers.controller;

import com.barberia.common.ApiResponse;
import com.barberia.common.PageResponse;
import com.barberia.modules.customers.dto.ClienteRequest;
import com.barberia.modules.customers.dto.ClienteResponse;
import com.barberia.modules.customers.service.ClienteService;
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

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Clientes", description = "Gestión de clientes de la barbería")
public class ClienteController {

    private final ClienteService clienteService;

    @Operation(summary = "Listar clientes activos con búsqueda")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','CASHIER','RECEPTION','BARBER')")
    public ResponseEntity<ApiResponse<PageResponse<ClienteResponse>>> listar(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageRequest pageable = PageRequest.of(page, Math.min(size, 100),
                Sort.by("apellido", "nombre"));
        return ResponseEntity.ok(ApiResponse.ok(clienteService.listarClientes(search, pageable)));
    }

    @Operation(summary = "Obtener cliente por ID")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','CASHIER','RECEPTION','BARBER')")
    public ResponseEntity<ApiResponse<ClienteResponse>> obtenerPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.ok(clienteService.obtenerPorId(id)));
    }

    @Operation(summary = "Crear cliente")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','CASHIER','RECEPTION')")
    public ResponseEntity<ApiResponse<ClienteResponse>> crear(
            @Valid @RequestBody ClienteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(clienteService.crear(request)));
    }

    @Operation(summary = "Actualizar cliente")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','CASHIER','RECEPTION')")
    public ResponseEntity<ApiResponse<ClienteResponse>> actualizar(
            @PathVariable Integer id,
            @Valid @RequestBody ClienteRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Cliente actualizado",
                clienteService.actualizar(id, request)));
    }

    @Operation(summary = "Deshabilitar cliente")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> eliminar(@PathVariable Integer id) {
        clienteService.eliminar(id);
        return ResponseEntity.ok(ApiResponse.ok("Cliente deshabilitado correctamente"));
    }

    @Operation(summary = "Listar clientes con recompensas disponibles")
    @GetMapping("/with-rewards")
    @PreAuthorize("hasAnyRole('ADMIN','CASHIER','RECEPTION')")
    public ResponseEntity<ApiResponse<PageResponse<ClienteResponse>>> conRecompensas(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("recompensasDisponibles").descending());
        return ResponseEntity.ok(ApiResponse.ok(clienteService.listarConRecompensas(pageable)));
    }
}
