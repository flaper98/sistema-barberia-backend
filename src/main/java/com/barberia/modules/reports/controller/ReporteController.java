package com.barberia.modules.reports.controller;

import com.barberia.common.ApiResponse;
import com.barberia.modules.reports.dto.*;
import com.barberia.modules.reports.service.impl.ReporteServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Reportes", description = "Reportes y estadísticas del negocio")
public class ReporteController {

    private final ReporteServiceImpl reporteService;

    @Operation(summary = "Dashboard general — métricas del día y del mes")
    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyRole('ADMIN','CASHIER')")
    public ResponseEntity<ApiResponse<DashboardResponse>> dashboard() {
        return ResponseEntity.ok(ApiResponse.ok(reporteService.getDashboard()));
    }

    @Operation(summary = "Reporte de ventas en un rango de fechas",
               description = "Filtra por estado: COMPLETADA, ANULADA")
    @GetMapping("/sales")
    @PreAuthorize("hasAnyRole('ADMIN','CASHIER')")
    public ResponseEntity<ApiResponse<List<VentaReporteDto>>> ventas(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(required = false) String estado) {
        return ResponseEntity.ok(ApiResponse.ok(
                reporteService.getReporteVentas(desde, hasta, estado)));
    }

    @Operation(summary = "Resumen de ventas agrupado por barbero en un rango de fechas")
    @GetMapping("/sales-by-worker")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<VentasPorBarberoDto>>> ventasPorBarbero(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return ResponseEntity.ok(ApiResponse.ok(
                reporteService.getVentasPorBarbero(desde, hasta)));
    }

    @Operation(summary = "Top N servicios más vendidos en un rango de fechas")
    @GetMapping("/top-services")
    @PreAuthorize("hasAnyRole('ADMIN','CASHIER')")
    public ResponseEntity<ApiResponse<List<TopServicioDto>>> topServicios(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(ApiResponse.ok(
                reporteService.getTopServicios(desde, hasta, limit)));
    }

    @Operation(summary = "Top N productos más vendidos en un rango de fechas")
    @GetMapping("/top-products")
    @PreAuthorize("hasAnyRole('ADMIN','CASHIER')")
    public ResponseEntity<ApiResponse<List<TopProductoDto>>> topProductos(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(ApiResponse.ok(
                reporteService.getTopProductos(desde, hasta, limit)));
    }

    @Operation(summary = "Reporte de citas en un rango de fechas",
               description = "Filtra por estado: PENDIENTE, CONFIRMADA, ATENDIDA, CANCELADA, NO_ASISTIO")
    @GetMapping("/appointments")
    @PreAuthorize("hasAnyRole('ADMIN','CASHIER','RECEPTION')")
    public ResponseEntity<ApiResponse<List<CitaReporteDto>>> citas(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(required = false) String estado) {
        return ResponseEntity.ok(ApiResponse.ok(
                reporteService.getReporteCitas(desde, hasta, estado)));
    }

    @Operation(summary = "Reporte de movimientos de inventario en un rango de fechas",
               description = "Filtra por tipo: ENTRADA, SALIDA, AJUSTE")
    @GetMapping("/inventory")
    @PreAuthorize("hasAnyRole('ADMIN','CASHIER')")
    public ResponseEntity<ApiResponse<List<InventarioReporteDto>>> inventario(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(required = false) String tipo) {
        return ResponseEntity.ok(ApiResponse.ok(
                reporteService.getReporteInventario(desde, hasta, tipo)));
    }

    @Operation(summary = "Reporte financiero — ingresos y egresos en un rango de fechas")
    @GetMapping("/finance")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<com.barberia.modules.finance.dto.ResumenFinancieroResponse>> finanzas(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return ResponseEntity.ok(ApiResponse.ok(
                reporteService.getResumenFinanciero(desde, hasta)));
    }

    @Operation(summary = "Resumen de fidelización — sellos, recompensas y métricas globales")
    @GetMapping("/loyalty")
    @PreAuthorize("hasAnyRole('ADMIN','CASHIER')")
    public ResponseEntity<ApiResponse<LoyaltyReporteDto>> loyalty() {
        return ResponseEntity.ok(ApiResponse.ok(reporteService.getReporteLoyalty()));
    }
}
