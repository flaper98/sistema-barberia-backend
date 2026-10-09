package com.barberia.modules.reports.controller;

import com.barberia.common.ApiResponse;
import com.barberia.modules.reports.dto.*;
import com.barberia.modules.reports.service.impl.ReporteServiceImpl;
import com.barberia.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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

    @Operation(summary = "Dashboard general — métricas del día y del mes",
               description = "Para ADMIN/CASHIER muestra datos de toda la barbería; para BARBER, solo los propios")
    @GetMapping("/dashboard")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<DashboardResponse>> dashboard(
            @AuthenticationPrincipal CustomUserDetails principal) {
        Integer barberoScopeId = "BARBER".equals(principal.getRol()) ? principal.getUsuario().getBarberoId() : null;
        return ResponseEntity.ok(ApiResponse.ok(reporteService.getDashboard(barberoScopeId)));
    }

    @Operation(summary = "Reporte de ventas en un rango de fechas",
               description = "Filtra por estado: COMPLETADA, ANULADA. Un BARBER siempre ve solo sus propias ventas, sin importar el barberoId enviado")
    @GetMapping("/sales")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<VentaReporteDto>>> ventas(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) Integer barberoId,
            @AuthenticationPrincipal CustomUserDetails principal) {
        Integer scopedBarberoId = "BARBER".equals(principal.getRol())
                ? principal.getUsuario().getBarberoId()
                : barberoId;
        return ResponseEntity.ok(ApiResponse.ok(
                reporteService.getReporteVentas(desde, hasta, estado, scopedBarberoId)));
    }

    @Operation(summary = "Resumen de ventas agrupado por barbero en un rango de fechas",
               description = "Un BARBER siempre ve solo su propia fila, sin importar el barberoId enviado")
    @GetMapping("/sales-by-worker")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<VentasPorBarberoDto>>> ventasPorBarbero(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(required = false) Integer barberoId,
            @AuthenticationPrincipal CustomUserDetails principal) {
        Integer scopedBarberoId = "BARBER".equals(principal.getRol())
                ? principal.getUsuario().getBarberoId()
                : barberoId;
        return ResponseEntity.ok(ApiResponse.ok(
                reporteService.getVentasPorBarbero(desde, hasta, scopedBarberoId)));
    }

    @Operation(summary = "Top N servicios más vendidos en un rango de fechas",
               description = "Un BARBER siempre ve solo sus propios servicios, sin importar el barberoId enviado")
    @GetMapping("/top-services")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<TopServicioDto>>> topServicios(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) Integer barberoId,
            @AuthenticationPrincipal CustomUserDetails principal) {
        Integer scopedBarberoId = "BARBER".equals(principal.getRol())
                ? principal.getUsuario().getBarberoId()
                : barberoId;
        return ResponseEntity.ok(ApiResponse.ok(
                reporteService.getTopServicios(desde, hasta, limit, scopedBarberoId)));
    }

    @Operation(summary = "Top N productos más vendidos en un rango de fechas",
               description = "Un BARBER siempre ve solo sus propios productos vendidos, sin importar el barberoId enviado")
    @GetMapping("/top-products")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<TopProductoDto>>> topProductos(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) Integer barberoId,
            @AuthenticationPrincipal CustomUserDetails principal) {
        Integer scopedBarberoId = "BARBER".equals(principal.getRol())
                ? principal.getUsuario().getBarberoId()
                : barberoId;
        return ResponseEntity.ok(ApiResponse.ok(
                reporteService.getTopProductos(desde, hasta, limit, scopedBarberoId)));
    }

    @Operation(summary = "Reporte de citas en un rango de fechas",
               description = "Filtra por estado: PENDIENTE, CONFIRMADA, ATENDIDA, CANCELADA, NO_ASISTIO. Un BARBER siempre ve solo sus propias citas, sin importar el barberoId enviado")
    @GetMapping("/appointments")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<CitaReporteDto>>> citas(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) Integer barberoId,
            @AuthenticationPrincipal CustomUserDetails principal) {
        Integer scopedBarberoId = "BARBER".equals(principal.getRol())
                ? principal.getUsuario().getBarberoId()
                : barberoId;
        return ResponseEntity.ok(ApiResponse.ok(
                reporteService.getReporteCitas(desde, hasta, estado, scopedBarberoId)));
    }

    @Operation(summary = "Reporte de movimientos de inventario en un rango de fechas",
               description = "Filtra por tipo: ENTRADA, SALIDA, AJUSTE")
    @GetMapping("/inventory")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<InventarioReporteDto>>> inventario(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(required = false) String tipo) {
        return ResponseEntity.ok(ApiResponse.ok(
                reporteService.getReporteInventario(desde, hasta, tipo)));
    }

    @Operation(summary = "Reporte financiero — ingresos y egresos en un rango de fechas")
    @GetMapping("/finance")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<com.barberia.modules.finance.dto.ResumenFinancieroResponse>> finanzas(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return ResponseEntity.ok(ApiResponse.ok(
                reporteService.getResumenFinanciero(desde, hasta)));
    }

    @Operation(summary = "Resumen de fidelización — sellos, recompensas y métricas globales")
    @GetMapping("/loyalty")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<LoyaltyReporteDto>> loyalty() {
        return ResponseEntity.ok(ApiResponse.ok(reporteService.getReporteLoyalty()));
    }

    @Operation(summary = "Ingresos agrupados por método de pago en un rango de fechas")
    @GetMapping("/income-by-payment-method")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<IngresosPorMetodoPagoDto>>> ingresosPorMetodoPago(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return ResponseEntity.ok(ApiResponse.ok(
                reporteService.getIngresosPorMetodoPago(desde, hasta)));
    }
}
