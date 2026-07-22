package com.barberia.modules.customers.controller;

import com.barberia.common.ApiResponse;
import com.barberia.common.PageResponse;
import com.barberia.modules.customers.dto.ClienteRequest;
import com.barberia.modules.customers.dto.ClienteResponse;
import com.barberia.modules.customers.dto.ClienteSelectorResponse;
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
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Clientes", description = "Gestión de clientes de la barbería")
public class ClienteController {

    private final ClienteService clienteService;

    // Whitelist de columnas ordenables: nunca se pasa el "sortBy" del cliente
    // directo a Sort.by (evita exponer nombres de propiedades internas o
    // tirar un error feo si mandan un valor invalido).
    private static final Map<String, String[]> SORT_FIELDS = Map.of(
            "id", new String[]{"id"},
            "nombre", new String[]{"nombre", "apellido"},
            "apellido", new String[]{"apellido", "nombre"},
            "telefono", new String[]{"telefono"},
            "sellos", new String[]{"cantidadSellos"},
            "recompensas", new String[]{"recompensasDisponibles"},
            "ultimaVisita", new String[]{"ultimaVisita"}
    );

    @Operation(summary = "Buscar clientes para un selector (ej. adjuntar uno a una solicitud de venta)",
               description = "Versión mínima sin teléfono ni sellos de fidelización -- disponible para " +
                       "cualquier usuario autenticado, sin depender del permiso del módulo Clientes " +
                       "(igual que /api/workers/for-sale).")
    @GetMapping("/for-sale")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<ClienteSelectorResponse>>> listarParaSelector(
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(ApiResponse.ok(clienteService.listarParaSelector(search)));
    }

    @Operation(summary = "Listar clientes activos con búsqueda y filtros",
               description = "Todos los filtros son opcionales y se aplican del lado del servidor " +
                       "(no hace falta traer todos los clientes al frontend para filtrar ahi).")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','CASHIER','RECEPTION','BARBER')")
    public ResponseEntity<ApiResponse<PageResponse<ClienteResponse>>> listar(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Integer sellosMin,
            @RequestParam(required = false) String recompensa,
            @RequestParam(required = false) String inactividad,
            @RequestParam(required = false) String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        String[] campos = sortBy != null ? SORT_FIELDS.getOrDefault(sortBy, new String[]{"id"}) : new String[]{"id"};
        Sort sort = "desc".equalsIgnoreCase(sortDir) ? Sort.by(campos).descending() : Sort.by(campos).ascending();
        PageRequest pageable = PageRequest.of(page, Math.min(size, 200), sort);

        Boolean conRecompensa = "con".equals(recompensa) ? Boolean.TRUE
                : "sin".equals(recompensa) ? Boolean.FALSE : null;
        Boolean nuncaVisito = "nunca".equals(inactividad) ? Boolean.TRUE : null;
        LocalDate inactivoDesde = null;
        if (StringUtils.hasText(inactividad) && !"nunca".equals(inactividad)) {
            inactivoDesde = LocalDate.now().minusDays(Long.parseLong(inactividad));
        }

        return ResponseEntity.ok(ApiResponse.ok(clienteService.buscarConFiltros(
                search, sellosMin, conRecompensa, nuncaVisito, inactivoDesde, pageable)));
    }

    @Operation(summary = "Obtener cliente por ID")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','CASHIER','RECEPTION','BARBER')")
    public ResponseEntity<ApiResponse<ClienteResponse>> obtenerPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.ok(clienteService.obtenerPorId(id)));
    }

    @Operation(summary = "Código que le tocará al próximo cliente que se registre",
               description = "Solo informativo (se muestra en el formulario de alta) -- el código real " +
                       "definitivo es el que asigna la base de datos al guardar.")
    @GetMapping("/next-code")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Integer>> proximoCodigo() {
        return ResponseEntity.ok(ApiResponse.ok(clienteService.obtenerProximoCodigo()));
    }

    @Operation(summary = "Crear cliente")
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ClienteResponse>> crear(
            @Valid @RequestBody ClienteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(clienteService.crear(request)));
    }

    @Operation(summary = "Actualizar cliente")
    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ClienteResponse>> actualizar(
            @PathVariable Integer id,
            @Valid @RequestBody ClienteRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Cliente actualizado",
                clienteService.actualizar(id, request)));
    }

    @Operation(summary = "Deshabilitar cliente")
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> eliminar(@PathVariable Integer id) {
        clienteService.eliminar(id);
        return ResponseEntity.ok(ApiResponse.ok("Cliente deshabilitado correctamente"));
    }

    @Operation(summary = "Listar clientes con recompensas disponibles")
    @GetMapping("/with-rewards")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PageResponse<ClienteResponse>>> conRecompensas(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("recompensasDisponibles").descending());
        return ResponseEntity.ok(ApiResponse.ok(clienteService.listarConRecompensas(pageable)));
    }
}
