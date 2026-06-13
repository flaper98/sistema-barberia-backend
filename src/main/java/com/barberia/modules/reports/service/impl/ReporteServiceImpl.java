package com.barberia.modules.reports.service.impl;

import com.barberia.modules.finance.dto.ResumenFinancieroResponse;
import com.barberia.modules.finance.entity.TipoFinanza;
import com.barberia.modules.finance.repository.FinanzaRepository;
import com.barberia.modules.reports.dto.*;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReporteServiceImpl {

    private final FinanzaRepository finanzaRepository;
    private final EntityManager em;

    @Transactional(readOnly = true)
    public DashboardResponse getDashboard() {
        LocalDate hoy = LocalDate.now();
        LocalDate inicioMes = hoy.withDayOfMonth(1);

        BigDecimal ingresosHoy = calcularIngresosVentasHoy();
        Long ventasHoy = contarVentasHoy();
        Long citasHoy = contarCitasHoy();
        Long citasPendientes = contarCitasPendientes();
        Integer stockBajo = countProductosStockBajo();
        Long clientesConRecompensa = contarClientesConRecompensa();

        BigDecimal ingresosDelMes = finanzaRepository.sumByTipoAndFecha(TipoFinanza.INGRESO, inicioMes, hoy);
        BigDecimal egresosDelMes  = finanzaRepository.sumByTipoAndFecha(TipoFinanza.EGRESO,  inicioMes, hoy);
        BigDecimal utilidad = ingresosDelMes.subtract(egresosDelMes);

        List<DashboardResponse.BarberoResumenDto> topBarberos = getTopBarberos(inicioMes, hoy);

        return DashboardResponse.builder()
                .ingresosHoy(ingresosHoy)
                .ventasHoy(ventasHoy)
                .citasHoy(citasHoy)
                .citasPendientes(citasPendientes)
                .productosStockBajo(stockBajo)
                .clientesConRecompensa(clientesConRecompensa)
                .ingresosDelMes(ingresosDelMes)
                .egresosDelMes(egresosDelMes)
                .utilidadDelMes(utilidad)
                .topBarberos(topBarberos)
                .build();
    }

    private BigDecimal calcularIngresosVentasHoy() {
        List<?> result = em.createNativeQuery(
                "SELECT COALESCE(SUM(total), 0) FROM ventas " +
                "WHERE DATE(fecha) = CURDATE() AND estado = 'COMPLETADA'")
                .getResultList();
        if (!result.isEmpty() && result.get(0) != null) {
            return (BigDecimal) result.get(0);
        }
        return BigDecimal.ZERO;
    }

    private Long contarVentasHoy() {
        List<?> result = em.createNativeQuery(
                "SELECT COUNT(*) FROM ventas WHERE DATE(fecha) = CURDATE() AND estado = 'COMPLETADA'")
                .getResultList();
        return result.isEmpty() ? 0L : ((Number) result.get(0)).longValue();
    }

    private Long contarCitasHoy() {
        List<?> result = em.createNativeQuery(
                "SELECT COUNT(*) FROM citas WHERE fecha = CURDATE()")
                .getResultList();
        return result.isEmpty() ? 0L : ((Number) result.get(0)).longValue();
    }

    private Long contarCitasPendientes() {
        List<?> result = em.createNativeQuery(
                "SELECT COUNT(*) FROM citas WHERE fecha >= CURDATE() AND estado IN ('PENDIENTE','CONFIRMADA')")
                .getResultList();
        return result.isEmpty() ? 0L : ((Number) result.get(0)).longValue();
    }

    private Long contarClientesConRecompensa() {
        List<?> result = em.createNativeQuery(
                "SELECT COUNT(*) FROM clientes WHERE recompensas_disponibles > 0 AND estado = 1")
                .getResultList();
        return result.isEmpty() ? 0L : ((Number) result.get(0)).longValue();
    }

    private Integer countProductosStockBajo() {
        List<?> result = em.createNativeQuery(
                "SELECT COUNT(*) FROM productos WHERE estado = 1 AND stock_actual <= stock_minimo")
                .getResultList();
        return result.isEmpty() ? 0 : ((Number) result.get(0)).intValue();
    }

    @SuppressWarnings("unchecked")
    private List<DashboardResponse.BarberoResumenDto> getTopBarberos(LocalDate desde, LocalDate hasta) {
        List<Object[]> rows = em.createNativeQuery(
                "SELECT b.id, CONCAT(b.nombre, ' ', b.apellido), b.porcentaje_comision, " +
                "COUNT(v.id), COALESCE(SUM(v.total), 0) " +
                "FROM barberos b " +
                "LEFT JOIN ventas v ON v.barbero_id = b.id AND v.estado = 'COMPLETADA' " +
                "AND DATE(v.fecha) BETWEEN :desde AND :hasta " +
                "WHERE b.estado = 1 " +
                "GROUP BY b.id, b.nombre, b.apellido, b.porcentaje_comision " +
                "ORDER BY SUM(v.total) DESC LIMIT 5")
                .setParameter("desde", desde)
                .setParameter("hasta", hasta)
                .getResultList();

        List<DashboardResponse.BarberoResumenDto> list = new ArrayList<>();
        for (Object[] row : rows) {
            BigDecimal ingresos = row[4] != null ? (BigDecimal) row[4] : BigDecimal.ZERO;
            BigDecimal pct = row[2] != null ? (BigDecimal) row[2] : BigDecimal.ZERO;
            BigDecimal comision = ingresos.multiply(pct)
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

            list.add(DashboardResponse.BarberoResumenDto.builder()
                    .id(((Number) row[0]).intValue())
                    .nombre((String) row[1])
                    .ventas(((Number) row[3]).longValue())
                    .ingresos(ingresos)
                    .comision(comision)
                    .build());
        }
        return list;
    }

    // -------------------------------------------------------------------------
    // Reporte: Ventas con filtros
    // -------------------------------------------------------------------------
    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<VentaReporteDto> getReporteVentas(LocalDate desde, LocalDate hasta, String estado) {
        String sql = "SELECT v.id, v.fecha, v.cliente_nombre, v.barbero_nombre, " +
                     "v.subtotal, v.descuento, v.total, v.metodo_pago, v.estado, " +
                     "COUNT(vi.id) AS items_count " +
                     "FROM ventas v " +
                     "LEFT JOIN venta_items vi ON vi.venta_id = v.id " +
                     "WHERE DATE(v.fecha) BETWEEN :desde AND :hasta " +
                     (estado != null ? "AND v.estado = :estado " : "") +
                     "GROUP BY v.id, v.fecha, v.cliente_nombre, v.barbero_nombre, " +
                     "v.subtotal, v.descuento, v.total, v.metodo_pago, v.estado " +
                     "ORDER BY v.fecha DESC";

        var query = em.createNativeQuery(sql)
                .setParameter("desde", desde)
                .setParameter("hasta", hasta);
        if (estado != null) query.setParameter("estado", estado);

        List<Object[]> rows = query.getResultList();
        List<VentaReporteDto> list = new ArrayList<>();
        for (Object[] r : rows) {
            list.add(VentaReporteDto.builder()
                    .id(((Number) r[0]).intValue())
                    .fecha(r[1] != null ? ((java.sql.Timestamp) r[1]).toLocalDateTime() : null)
                    .clienteNombre((String) r[2])
                    .barberoNombre((String) r[3])
                    .subtotal(r[4] != null ? (BigDecimal) r[4] : BigDecimal.ZERO)
                    .descuento(r[5] != null ? (BigDecimal) r[5] : BigDecimal.ZERO)
                    .total(r[6] != null ? (BigDecimal) r[6] : BigDecimal.ZERO)
                    .metodoPago((String) r[7])
                    .estado((String) r[8])
                    .itemsCount(r[9] != null ? ((Number) r[9]).intValue() : 0)
                    .build());
        }
        return list;
    }

    // -------------------------------------------------------------------------
    // Reporte: Ventas por barbero
    // -------------------------------------------------------------------------
    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<VentasPorBarberoDto> getVentasPorBarbero(LocalDate desde, LocalDate hasta) {
        List<Object[]> rows = em.createNativeQuery(
                "SELECT b.id, CONCAT(b.nombre, ' ', b.apellido), b.porcentaje_comision, " +
                "COUNT(v.id), COALESCE(SUM(v.total), 0), b.servicios_realizados " +
                "FROM barberos b " +
                "LEFT JOIN ventas v ON v.barbero_id = b.id AND v.estado = 'COMPLETADA' " +
                "AND DATE(v.fecha) BETWEEN :desde AND :hasta " +
                "GROUP BY b.id, b.nombre, b.apellido, b.porcentaje_comision, b.servicios_realizados " +
                "ORDER BY SUM(v.total) DESC")
                .setParameter("desde", desde)
                .setParameter("hasta", hasta)
                .getResultList();

        List<VentasPorBarberoDto> list = new ArrayList<>();
        for (Object[] r : rows) {
            BigDecimal ingresos = r[4] != null ? (BigDecimal) r[4] : BigDecimal.ZERO;
            BigDecimal pct = r[2] != null ? (BigDecimal) r[2] : BigDecimal.ZERO;
            BigDecimal comision = ingresos.multiply(pct)
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            list.add(VentasPorBarberoDto.builder()
                    .barberoId(((Number) r[0]).intValue())
                    .barberoNombre((String) r[1])
                    .porcentajeComision(pct)
                    .totalVentas(((Number) r[3]).longValue())
                    .ingresos(ingresos)
                    .comision(comision)
                    .serviciosRealizados(r[5] != null ? ((Number) r[5]).longValue() : 0L)
                    .build());
        }
        return list;
    }

    // -------------------------------------------------------------------------
    // Reporte: Top servicios
    // -------------------------------------------------------------------------
    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<TopServicioDto> getTopServicios(LocalDate desde, LocalDate hasta, int limit) {
        List<Object[]> rows = em.createNativeQuery(
                "SELECT s.id, s.nombre, s.categoria, COUNT(vi.id), " +
                "COALESCE(SUM(vi.precio_unitario * vi.cantidad), 0), " +
                "COALESCE(AVG(vi.precio_unitario), 0) " +
                "FROM servicios s " +
                "JOIN venta_items vi ON vi.servicio_id = s.id AND vi.tipo = 'SERVICIO' " +
                "JOIN ventas v ON v.id = vi.venta_id AND v.estado = 'COMPLETADA' " +
                "AND DATE(v.fecha) BETWEEN :desde AND :hasta " +
                "GROUP BY s.id, s.nombre, s.categoria " +
                "ORDER BY COUNT(vi.id) DESC " +
                "LIMIT :lim")
                .setParameter("desde", desde)
                .setParameter("hasta", hasta)
                .setParameter("lim", limit)
                .getResultList();

        List<TopServicioDto> list = new ArrayList<>();
        for (Object[] r : rows) {
            list.add(TopServicioDto.builder()
                    .servicioId(((Number) r[0]).intValue())
                    .nombre((String) r[1])
                    .categoria((String) r[2])
                    .vecesVendido(((Number) r[3]).longValue())
                    .ingresos(r[4] != null ? (BigDecimal) r[4] : BigDecimal.ZERO)
                    .precioPromedio(r[5] != null ? (BigDecimal) r[5] : BigDecimal.ZERO)
                    .build());
        }
        return list;
    }

    // -------------------------------------------------------------------------
    // Reporte: Top productos
    // -------------------------------------------------------------------------
    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<TopProductoDto> getTopProductos(LocalDate desde, LocalDate hasta, int limit) {
        List<Object[]> rows = em.createNativeQuery(
                "SELECT p.id, p.nombre, p.categoria, COALESCE(SUM(vi.cantidad), 0), " +
                "COALESCE(SUM(vi.precio_unitario * vi.cantidad), 0), p.stock_actual " +
                "FROM productos p " +
                "JOIN venta_items vi ON vi.producto_id = p.id AND vi.tipo = 'PRODUCTO' " +
                "JOIN ventas v ON v.id = vi.venta_id AND v.estado = 'COMPLETADA' " +
                "AND DATE(v.fecha) BETWEEN :desde AND :hasta " +
                "GROUP BY p.id, p.nombre, p.categoria, p.stock_actual " +
                "ORDER BY SUM(vi.cantidad) DESC " +
                "LIMIT :lim")
                .setParameter("desde", desde)
                .setParameter("hasta", hasta)
                .setParameter("lim", limit)
                .getResultList();

        List<TopProductoDto> list = new ArrayList<>();
        for (Object[] r : rows) {
            list.add(TopProductoDto.builder()
                    .productoId(((Number) r[0]).intValue())
                    .nombre((String) r[1])
                    .categoria((String) r[2])
                    .unidadesVendidas(((Number) r[3]).longValue())
                    .ingresos(r[4] != null ? (BigDecimal) r[4] : BigDecimal.ZERO)
                    .stockActual(r[5] != null ? ((Number) r[5]).intValue() : 0)
                    .build());
        }
        return list;
    }

    // -------------------------------------------------------------------------
    // Reporte: Citas
    // -------------------------------------------------------------------------
    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<CitaReporteDto> getReporteCitas(LocalDate desde, LocalDate hasta, String estado) {
        String sql = "SELECT c.id, c.fecha, c.hora, c.cliente_nombre, " +
                     "CONCAT(b.nombre, ' ', b.apellido), c.estado, c.total_estimado, " +
                     "(SELECT COUNT(*) FROM cita_servicios cs WHERE cs.cita_id = c.id) " +
                     "FROM citas c " +
                     "JOIN barberos b ON b.id = c.barbero_id " +
                     "WHERE c.fecha BETWEEN :desde AND :hasta " +
                     (estado != null ? "AND c.estado = :estado " : "") +
                     "ORDER BY c.fecha DESC, c.hora DESC";

        var query = em.createNativeQuery(sql)
                .setParameter("desde", desde)
                .setParameter("hasta", hasta);
        if (estado != null) query.setParameter("estado", estado);

        List<Object[]> rows = query.getResultList();
        List<CitaReporteDto> list = new ArrayList<>();
        for (Object[] r : rows) {
            list.add(CitaReporteDto.builder()
                    .id(((Number) r[0]).intValue())
                    .fecha(r[1] != null ? ((java.sql.Date) r[1]).toLocalDate() : null)
                    .hora(r[2] != null ? ((java.sql.Time) r[2]).toLocalTime() : null)
                    .clienteNombre((String) r[3])
                    .barberoNombre((String) r[4])
                    .estado((String) r[5])
                    .totalEstimado(r[6] != null ? (BigDecimal) r[6] : BigDecimal.ZERO)
                    .serviciosCount(r[7] != null ? ((Number) r[7]).intValue() : 0)
                    .build());
        }
        return list;
    }

    // -------------------------------------------------------------------------
    // Reporte: Movimientos de inventario
    // -------------------------------------------------------------------------
    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<InventarioReporteDto> getReporteInventario(LocalDate desde, LocalDate hasta, String tipo) {
        String sql = "SELECT m.id, m.created_at, p.nombre, m.tipo_movimiento, m.cantidad, " +
                     "m.stock_anterior, m.stock_nuevo, m.motivo, m.usuario_nombre " +
                     "FROM movimientos_stock m " +
                     "JOIN productos p ON p.id = m.producto_id " +
                     "WHERE DATE(m.created_at) BETWEEN :desde AND :hasta " +
                     (tipo != null ? "AND m.tipo_movimiento = :tipo " : "") +
                     "ORDER BY m.created_at DESC";

        var query = em.createNativeQuery(sql)
                .setParameter("desde", desde)
                .setParameter("hasta", hasta);
        if (tipo != null) query.setParameter("tipo", tipo);

        List<Object[]> rows = query.getResultList();
        List<InventarioReporteDto> list = new ArrayList<>();
        for (Object[] r : rows) {
            list.add(InventarioReporteDto.builder()
                    .id(((Number) r[0]).intValue())
                    .fecha(r[1] != null ? ((java.sql.Timestamp) r[1]).toLocalDateTime() : null)
                    .productoNombre((String) r[2])
                    .tipoMovimiento((String) r[3])
                    .cantidad(r[4] != null ? ((Number) r[4]).intValue() : 0)
                    .stockAnterior(r[5] != null ? ((Number) r[5]).intValue() : 0)
                    .stockNuevo(r[6] != null ? ((Number) r[6]).intValue() : 0)
                    .motivo((String) r[7])
                    .usuarioNombre((String) r[8])
                    .build());
        }
        return list;
    }

    // -------------------------------------------------------------------------
    // Reporte: Resumen de fidelización
    // -------------------------------------------------------------------------
    @Transactional(readOnly = true)
    public LoyaltyReporteDto getReporteLoyalty() {
        List<?> r1 = em.createNativeQuery(
                "SELECT COUNT(*) FROM fidelizacion_cuentas").getResultList();
        List<?> r2 = em.createNativeQuery(
                "SELECT COUNT(*) FROM fidelizacion_cuentas WHERE recompensas_disponibles > 0").getResultList();
        List<?> r3 = em.createNativeQuery(
                "SELECT COALESCE(SUM(total_sellos_acumulados), 0) FROM fidelizacion_cuentas").getResultList();
        List<?> r4 = em.createNativeQuery(
                "SELECT COALESCE(SUM(recompensas_canjeadas), 0) FROM fidelizacion_cuentas").getResultList();
        List<?> r5 = em.createNativeQuery(
                "SELECT COALESCE(SUM(recompensas_disponibles), 0) FROM fidelizacion_cuentas").getResultList();
        List<?> r6 = em.createNativeQuery(
                "SELECT COALESCE(AVG(total_sellos_acumulados), 0) FROM fidelizacion_cuentas").getResultList();

        return LoyaltyReporteDto.builder()
                .totalClientesConCuenta(toL(r1))
                .clientesConRecompensas(toL(r2))
                .totalSellosOtorgados(toL(r3))
                .totalRecompensasCanjeadas(toL(r4))
                .totalRecompensasPendientes(toL(r5))
                .promedioSellosPorCliente(r6.isEmpty() || r6.get(0) == null
                        ? 0.0 : ((Number) r6.get(0)).doubleValue())
                .build();
    }

    private Long toL(List<?> r) {
        return r.isEmpty() || r.get(0) == null ? 0L : ((Number) r.get(0)).longValue();
    }

    // -------------------------------------------------------------------------
    // Reporte financiero con periodo en texto
    // -------------------------------------------------------------------------
    @Transactional(readOnly = true)
    public ResumenFinancieroResponse getResumenFinanciero(LocalDate desde, LocalDate hasta) {
        BigDecimal ingresos = finanzaRepository.sumByTipoAndFecha(TipoFinanza.INGRESO, desde, hasta);
        BigDecimal egresos  = finanzaRepository.sumByTipoAndFecha(TipoFinanza.EGRESO,  desde, hasta);
        return ResumenFinancieroResponse.builder()
                .totalIngresos(ingresos)
                .totalEgresos(egresos)
                .utilidad(ingresos.subtract(egresos))
                .periodo(desde + " / " + hasta)
                .build();
    }
}
