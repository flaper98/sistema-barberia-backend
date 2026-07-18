package com.barberia.modules.finance.controller;

import com.barberia.common.ApiResponse;
import com.barberia.common.PageResponse;
import com.barberia.modules.finance.dto.FinanzaRequest;
import com.barberia.modules.finance.dto.FinanzaResponse;
import com.barberia.modules.finance.dto.ResumenFinancieroResponse;
import com.barberia.modules.finance.entity.CategoriaFinanza;
import com.barberia.modules.finance.entity.TipoFinanza;
import com.barberia.modules.finance.service.impl.FinanzaServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/finance")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Finanzas", description = "Gestión de ingresos y egresos")
public class FinanzaController {

    private final FinanzaServiceImpl finanzaBiz;

    @Operation(summary = "Resumen financiero del período", description = "Calcula ingresos, egresos y utilidad")
    @GetMapping("/summary")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ResumenFinancieroResponse>> resumen(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        LocalDate hoy = LocalDate.now();
        if (desde == null) desde = hoy.withDayOfMonth(1);
        if (hasta == null) hasta = hoy.withDayOfMonth(hoy.lengthOfMonth());
        return ResponseEntity.ok(ApiResponse.ok(finanzaBiz.obtenerResumen(desde, hasta)));
    }

    @Operation(summary = "Listar movimientos financieros con filtros")
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PageResponse<FinanzaResponse>>> listar(
            @RequestParam(required = false) TipoFinanza tipo,
            @RequestParam(required = false) CategoriaFinanza categoria,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageRequest pageable = PageRequest.of(page, Math.min(size, 100), Sort.by("fecha").descending());
        return ResponseEntity.ok(ApiResponse.ok(finanzaBiz.listar(tipo, categoria, desde, hasta, pageable)));
    }

    @Operation(summary = "Registrar ingreso o egreso manual",
               description = "Los ingresos de ventas se registran automáticamente al completar la venta; usar este endpoint para egresos y otros ingresos manuales")
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<FinanzaResponse>> registrar(
            @Valid @RequestBody FinanzaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(finanzaBiz.registrar(request)));
    }

    @Operation(summary = "Listar solo ingresos")
    @GetMapping("/incomes")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PageResponse<FinanzaResponse>>> ingresos(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("fecha").descending());
        return ResponseEntity.ok(ApiResponse.ok(
                finanzaBiz.listar(TipoFinanza.INGRESO, null, desde, hasta, pageable)));
    }

    @Operation(summary = "Listar solo egresos")
    @GetMapping("/expenses")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PageResponse<FinanzaResponse>>> egresos(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("fecha").descending());
        return ResponseEntity.ok(ApiResponse.ok(
                finanzaBiz.listar(TipoFinanza.EGRESO, null, desde, hasta, pageable)));
    }
}
