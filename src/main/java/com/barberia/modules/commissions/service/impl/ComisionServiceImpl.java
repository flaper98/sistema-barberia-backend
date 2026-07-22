package com.barberia.modules.commissions.service.impl;

import com.barberia.common.PageResponse;
import com.barberia.exception.BusinessException;
import com.barberia.exception.ResourceNotFoundException;
import com.barberia.modules.audit.AuditAction;
import com.barberia.modules.audit.AuditPublisher;
import com.barberia.modules.commissions.dto.*;
import com.barberia.modules.commissions.entity.CorteComision;
import com.barberia.modules.commissions.entity.DescuentoConsumo;
import com.barberia.modules.commissions.entity.EstadoCorte;
import com.barberia.modules.commissions.entity.Propina;
import com.barberia.modules.commissions.repository.CorteComisionRepository;
import com.barberia.modules.commissions.repository.DescuentoConsumoRepository;
import com.barberia.modules.commissions.repository.PropinaRepository;
import com.barberia.modules.finance.entity.CategoriaFinanza;
import com.barberia.modules.finance.entity.Finanza;
import com.barberia.modules.finance.entity.TipoFinanza;
import com.barberia.modules.finance.repository.FinanzaRepository;
import com.barberia.modules.inventory.dto.MovimientoRequest;
import com.barberia.modules.inventory.entity.TipoMovimiento;
import com.barberia.modules.inventory.service.impl.InventarioServiceImpl;
import com.barberia.modules.products.entity.Producto;
import com.barberia.modules.products.repository.ProductoRepository;
import com.barberia.modules.services.entity.Paquete;
import com.barberia.modules.services.entity.Servicio;
import com.barberia.modules.services.repository.PaqueteRepository;
import com.barberia.modules.services.repository.ServicioRepository;
import com.barberia.modules.workers.entity.Barbero;
import com.barberia.modules.workers.repository.BarberoRepository;
import com.barberia.security.CustomUserDetails;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

// Los barberos cobran por comision (no sueldo fijo): el % NO es una tasa
// fija por barbero -- lo define el administrador PARA CADA servicio y
// PARA CADA producto del catalogo (ver Tasas de comision, al final de
// esta clase), porque no todos pagan lo mismo. A eso se le suman
// propinas y se le restan descuentos por consumo personal. Un "corte"
// cierra un periodo y queda fijo para siempre -- ver EstadoCorte.
@Slf4j
@Service
@RequiredArgsConstructor
public class ComisionServiceImpl {

    private final BarberoRepository barberoRepository;
    private final ServicioRepository servicioRepository;
    private final ProductoRepository productoRepository;
    private final PaqueteRepository paqueteRepository;
    private final PropinaRepository propinaRepository;
    private final DescuentoConsumoRepository descuentoRepository;
    private final CorteComisionRepository corteRepository;
    private final FinanzaRepository finanzaRepository;
    private final InventarioServiceImpl inventarioService;
    private final AuditPublisher auditPublisher;
    private final EntityManager em;

    @Transactional(readOnly = true)
    public ResumenComisionResponse obtenerResumenPendiente(Integer barberoId, LocalDate desde, LocalDate hasta) {
        Barbero barbero = findBarberoOrThrow(barberoId);
        BasesYComisiones calc = calcularBasesYComisiones(barberoId, desde, hasta);

        BigDecimal propinas = propinaRepository.findByBarberoIdAndCorteIdIsNullAndFechaBetween(barberoId, desde, hasta)
                .stream().map(Propina::getMonto).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal descuentos = descuentoRepository.findByBarberoIdAndCorteIdIsNullAndFechaBetween(barberoId, desde, hasta)
                .stream().map(DescuentoConsumo::getMontoTotal).reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal total = calc.comisionServicios().add(calc.comisionProductos()).add(propinas).subtract(descuentos);
        if (total.compareTo(BigDecimal.ZERO) < 0) total = BigDecimal.ZERO;

        return ResumenComisionResponse.builder()
                .barberoId(barberoId)
                .barberoNombre(barbero.getNombreCompleto())
                .fechaDesde(desde)
                .fechaHasta(hasta)
                .baseServicios(calc.baseServicios())
                .baseProductos(calc.baseProductos())
                .comisionServicios(calc.comisionServicios())
                .comisionProductos(calc.comisionProductos())
                .propinasPendientes(propinas)
                .descuentosPendientes(descuentos)
                .totalEstimado(total)
                .build();
    }

    // Desglose "por item": no el total agrupado de servicios/productos,
    // sino cada venta puntual con su fecha -- para que el barbero pueda ver
    // de donde y de CUANDO sale cada sol de su comision, no solo un numero
    // agrupado sin poder rastrearlo a una venta real.
    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<DetalleComisionResponse> obtenerDetalleComision(Integer barberoId, LocalDate desde, LocalDate hasta) {
        findBarberoOrThrow(barberoId);
        List<Object[]> filas = em.createNativeQuery(
                "SELECT vi.tipo, vi.item_id, vi.nombre, vi.cantidad, vi.subtotal, v.fecha, " +
                "CASE WHEN vi.tipo = 'SERVICIO' THEN vi.subtotal * COALESCE(s.porcentaje_comision, 0) / 100 " +
                "     WHEN vi.tipo = 'PAQUETE'  THEN vi.subtotal * COALESCE(pk.porcentaje_comision, 0) / 100 " +
                "     WHEN vi.tipo = 'PRODUCTO' THEN vi.subtotal * COALESCE(p.porcentaje_comision, 0) / 100 " +
                "     ELSE 0 END AS comision " +
                "FROM ventas v " +
                "JOIN venta_items vi ON vi.venta_id = v.id " +
                "LEFT JOIN servicios s ON s.id = vi.item_id AND vi.tipo = 'SERVICIO' " +
                "LEFT JOIN paquetes pk ON pk.id = vi.item_id AND vi.tipo = 'PAQUETE' " +
                "LEFT JOIN productos p ON p.id = vi.item_id AND vi.tipo = 'PRODUCTO' " +
                "WHERE v.barbero_id = :bid AND v.estado = 'COMPLETADA' " +
                "AND DATE(v.fecha) BETWEEN :desde AND :hasta " +
                "ORDER BY v.fecha DESC")
                .setParameter("bid", barberoId)
                .setParameter("desde", desde)
                .setParameter("hasta", hasta)
                .getResultList();

        return filas.stream().map(row -> DetalleComisionResponse.builder()
                .tipo((String) row[0])
                .itemId(((Number) row[1]).intValue())
                .nombre((String) row[2])
                .cantidad(((Number) row[3]).intValue())
                .base((BigDecimal) row[4])
                .fecha(((java.sql.Timestamp) row[5]).toLocalDateTime())
                .comision(((BigDecimal) row[6]).setScale(2, java.math.RoundingMode.HALF_UP))
                .build())
                .toList();
    }

    @Transactional
    public PropinaResponse registrarPropina(PropinaRequest request) {
        Barbero barbero = findBarberoOrThrow(request.getBarberoId());
        CustomUserDetails userDetails = getCurrentUser();

        Propina propina = Propina.builder()
                .barberoId(barbero.getId())
                .monto(request.getMonto())
                .fecha(request.getFecha() != null ? request.getFecha() : LocalDate.now())
                .notas(request.getNotas())
                .usuarioRegistroId(userDetails != null ? userDetails.getId() : null)
                .usuarioRegistroNombre(userDetails != null ? userDetails.getNombreCompleto() : "Sistema")
                .build();
        propina = propinaRepository.save(propina);

        String actor = userDetails != null ? userDetails.getUsername() : "SISTEMA";
        auditPublisher.publish(AuditAction.TIP_ADD, actor, "BARBERO", barbero.getId().toString(),
                "Propina registrada S/ " + request.getMonto() + " para " + barbero.getNombreCompleto());
        log.info("Propina registrada barbero={} monto={}", barbero.getNombreCompleto(), request.getMonto());

        return toResponse(propina, barbero.getNombreCompleto());
    }

    @Transactional
    public void eliminarPropina(Long id) {
        Propina propina = propinaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Propina", id));
        if (propina.getCorteId() != null) {
            throw new BusinessException("No se puede eliminar una propina que ya está incluida en un corte pagado/generado");
        }
        propinaRepository.delete(propina);
    }

    @Transactional(readOnly = true)
    public PageResponse<PropinaResponse> listarPropinas(Integer barberoId, Pageable pageable) {
        Barbero barbero = findBarberoOrThrow(barberoId);
        return PageResponse.from(
                propinaRepository.findByBarberoIdOrderByFechaDesc(barberoId, pageable)
                        .map(p -> toResponse(p, barbero.getNombreCompleto())));
    }

    // El descuento se valora al precio de venta del producto (lo mismo que
    // le cobrarian a un cliente) -- ademas genera un movimiento de stock
    // real (tipo CONSUMO), reusando la validacion de stock ya existente en
    // InventarioServiceImpl en vez de duplicarla.
    @Transactional
    public DescuentoConsumoResponse registrarConsumo(DescuentoConsumoRequest request) {
        Barbero barbero = findBarberoOrThrow(request.getBarberoId());
        Producto producto = productoRepository.findById(request.getProductoId())
                .orElseThrow(() -> new ResourceNotFoundException("Producto", request.getProductoId()));

        MovimientoRequest movRequest = new MovimientoRequest();
        movRequest.setProductoId(producto.getId());
        movRequest.setTipoMovimiento(TipoMovimiento.CONSUMO);
        movRequest.setCantidad(request.getCantidad());
        movRequest.setMotivo("Consumo de " + barbero.getNombreCompleto()
                + (request.getMotivo() != null && !request.getMotivo().isBlank() ? " - " + request.getMotivo() : ""));
        inventarioService.registrarMovimiento(movRequest);

        BigDecimal montoTotal = producto.getPrecioVenta().multiply(BigDecimal.valueOf(request.getCantidad()));
        CustomUserDetails userDetails = getCurrentUser();

        DescuentoConsumo descuento = DescuentoConsumo.builder()
                .barberoId(barbero.getId())
                .productoId(producto.getId())
                .productoNombre(producto.getNombre())
                .cantidad(request.getCantidad())
                .precioUnitario(producto.getPrecioVenta())
                .montoTotal(montoTotal)
                .fecha(LocalDate.now())
                .motivo(request.getMotivo())
                .usuarioRegistroId(userDetails != null ? userDetails.getId() : null)
                .usuarioRegistroNombre(userDetails != null ? userDetails.getNombreCompleto() : "Sistema")
                .build();
        descuento = descuentoRepository.save(descuento);

        String actor = userDetails != null ? userDetails.getUsername() : "SISTEMA";
        auditPublisher.publish(AuditAction.CONSUMPTION_DISCOUNT_ADD, actor, "BARBERO", barbero.getId().toString(),
                "Consumo registrado: " + request.getCantidad() + "x " + producto.getNombre()
                        + " (S/ " + montoTotal + ") para " + barbero.getNombreCompleto());
        log.info("Descuento por consumo registrado barbero={} producto={} monto={}",
                barbero.getNombreCompleto(), producto.getNombre(), montoTotal);

        return toResponse(descuento, barbero.getNombreCompleto());
    }

    @Transactional
    public void eliminarDescuento(Long id) {
        DescuentoConsumo descuento = descuentoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Descuento", id));
        if (descuento.getCorteId() != null) {
            throw new BusinessException("No se puede eliminar un descuento que ya está incluido en un corte pagado/generado");
        }
        descuentoRepository.delete(descuento);
    }

    @Transactional(readOnly = true)
    public PageResponse<DescuentoConsumoResponse> listarDescuentos(Integer barberoId, Pageable pageable) {
        Barbero barbero = findBarberoOrThrow(barberoId);
        return PageResponse.from(
                descuentoRepository.findByBarberoIdOrderByFechaDesc(barberoId, pageable)
                        .map(d -> toResponse(d, barbero.getNombreCompleto())));
    }

    // Genera un corte: recalcula todo desde cero para el rango pedido y
    // marca las propinas/descuentos de ese rango como ya incluidos (para
    // que un proximo corte no los vuelva a contar).
    @Transactional
    public CorteComisionResponse generarCorte(GenerarCorteRequest request) {
        if (request.getFechaHasta().isBefore(request.getFechaDesde())) {
            throw new BusinessException("La fecha hasta no puede ser anterior a la fecha desde");
        }
        if (corteRepository.existsOverlapping(request.getBarberoId(), request.getFechaDesde(), request.getFechaHasta())) {
            throw new BusinessException("Ya existe un corte para este barbero que se superpone con ese rango de fechas");
        }
        Barbero barbero = findBarberoOrThrow(request.getBarberoId());

        BasesYComisiones calc = calcularBasesYComisiones(barbero.getId(), request.getFechaDesde(), request.getFechaHasta());

        List<Propina> propinas = propinaRepository.findByBarberoIdAndCorteIdIsNullAndFechaBetween(
                barbero.getId(), request.getFechaDesde(), request.getFechaHasta());
        List<DescuentoConsumo> descuentos = descuentoRepository.findByBarberoIdAndCorteIdIsNullAndFechaBetween(
                barbero.getId(), request.getFechaDesde(), request.getFechaHasta());

        BigDecimal totalPropinas = propinas.stream().map(Propina::getMonto).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalDescuentos = descuentos.stream().map(DescuentoConsumo::getMontoTotal).reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalAPagar = calc.comisionServicios().add(calc.comisionProductos()).add(totalPropinas).subtract(totalDescuentos);
        if (totalAPagar.compareTo(BigDecimal.ZERO) < 0) totalAPagar = BigDecimal.ZERO;

        CustomUserDetails userDetails = getCurrentUser();
        CorteComision corte = CorteComision.builder()
                .barberoId(barbero.getId())
                .fechaDesde(request.getFechaDesde())
                .fechaHasta(request.getFechaHasta())
                .baseServicios(calc.baseServicios())
                .baseProductos(calc.baseProductos())
                .comisionServicios(calc.comisionServicios())
                .comisionProductos(calc.comisionProductos())
                .totalPropinas(totalPropinas)
                .totalDescuentos(totalDescuentos)
                .totalAPagar(totalAPagar)
                .estado(EstadoCorte.PENDIENTE)
                .notas(request.getNotas())
                .usuarioRegistroId(userDetails != null ? userDetails.getId() : null)
                .usuarioRegistroNombre(userDetails != null ? userDetails.getNombreCompleto() : "Sistema")
                .build();
        corte = corteRepository.save(corte);

        if (!propinas.isEmpty()) {
            propinaRepository.asignarCorte(propinas.stream().map(Propina::getId).toList(), corte.getId());
        }
        if (!descuentos.isEmpty()) {
            descuentoRepository.asignarCorte(descuentos.stream().map(DescuentoConsumo::getId).toList(), corte.getId());
        }

        String actor = userDetails != null ? userDetails.getUsername() : "SISTEMA";
        auditPublisher.publish(AuditAction.COMMISION_CUT_CREATE, actor, "BARBERO", barbero.getId().toString(),
                "Corte generado " + request.getFechaDesde() + " a " + request.getFechaHasta()
                        + " total=S/ " + totalAPagar + " para " + barbero.getNombreCompleto());
        log.info("Corte de comision generado ID={} barbero={} total={}", corte.getId(), barbero.getNombreCompleto(), totalAPagar);

        return toResponse(corte, barbero.getNombreCompleto());
    }

    @Transactional
    public CorteComisionResponse marcarComoPagado(Long corteId) {
        CorteComision corte = corteRepository.findById(corteId)
                .orElseThrow(() -> new ResourceNotFoundException("Corte de comisión", corteId));
        if (corte.getEstado() == EstadoCorte.PAGADO) {
            throw new BusinessException("Este corte ya está marcado como pagado");
        }
        Barbero barbero = findBarberoOrThrow(corte.getBarberoId());

        corte.setEstado(EstadoCorte.PAGADO);
        corte.setFechaPago(LocalDateTime.now());
        corte = corteRepository.save(corte);

        CustomUserDetails userDetails = getCurrentUser();
        Finanza finanza = Finanza.builder()
                .tipo(TipoFinanza.EGRESO)
                .categoria(CategoriaFinanza.COMISION)
                .descripcion("Comisión pagada - " + barbero.getNombreCompleto()
                        + " (" + corte.getFechaDesde() + " a " + corte.getFechaHasta() + ")")
                .monto(corte.getTotalAPagar())
                .fecha(LocalDate.now())
                .usuarioId(userDetails != null ? userDetails.getId() : null)
                .usuarioNombre(userDetails != null ? userDetails.getNombreCompleto() : "Sistema")
                .referencia("CORTE-" + corte.getId())
                .build();
        finanzaRepository.save(finanza);

        String actor = userDetails != null ? userDetails.getUsername() : "SISTEMA";
        auditPublisher.publish(AuditAction.COMMISION_CUT_PAY, actor, "BARBERO", barbero.getId().toString(),
                "Corte ID=" + corte.getId() + " marcado como pagado, total=S/ " + corte.getTotalAPagar());
        log.info("Corte de comision ID={} marcado como pagado, total={}", corte.getId(), corte.getTotalAPagar());

        return toResponse(corte, barbero.getNombreCompleto());
    }

    @Transactional(readOnly = true)
    public PageResponse<CorteComisionResponse> listarCortes(Integer barberoId, EstadoCorte estado, Pageable pageable) {
        return PageResponse.from(
                corteRepository.findWithFilters(barberoId, estado, pageable)
                        .map(c -> toResponse(c, nombreBarbero(c.getBarberoId()))));
    }

    // ─── Tasas de comision (por servicio / producto / paquete) ──────────────

    @Transactional(readOnly = true)
    public List<TasaComisionResponse> listarTasasServicios() {
        return servicioRepository.findByEstadoTrue().stream()
                .sorted((a, b) -> a.getNombre().compareToIgnoreCase(b.getNombre()))
                .map(s -> TasaComisionResponse.builder()
                        .id(s.getId()).nombre(s.getNombre()).precio(s.getPrecio())
                        .porcentajeComision(s.getPorcentajeComision())
                        .build())
                .toList();
    }

    @Transactional
    public TasaComisionResponse actualizarTasaServicio(Integer id, BigDecimal porcentaje) {
        Servicio servicio = servicioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Servicio", id));
        servicio.setPorcentajeComision(porcentaje);
        servicio = servicioRepository.save(servicio);
        log.info("Tasa de comisión actualizada: servicio {} -> {}%", servicio.getNombre(), porcentaje);
        return TasaComisionResponse.builder()
                .id(servicio.getId()).nombre(servicio.getNombre()).precio(servicio.getPrecio())
                .porcentajeComision(servicio.getPorcentajeComision())
                .build();
    }

    @Transactional(readOnly = true)
    public List<TasaComisionResponse> listarTasasProductos() {
        return productoRepository.findByEstadoTrue(org.springframework.data.domain.Pageable.unpaged()).stream()
                .sorted((a, b) -> a.getNombre().compareToIgnoreCase(b.getNombre()))
                .map(p -> TasaComisionResponse.builder()
                        .id(p.getId()).nombre(p.getNombre()).precio(p.getPrecioVenta())
                        .porcentajeComision(p.getPorcentajeComision())
                        .build())
                .toList();
    }

    @Transactional
    public TasaComisionResponse actualizarTasaProducto(Integer id, BigDecimal porcentaje) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto", id));
        producto.setPorcentajeComision(porcentaje);
        producto = productoRepository.save(producto);
        log.info("Tasa de comisión actualizada: producto {} -> {}%", producto.getNombre(), porcentaje);
        return TasaComisionResponse.builder()
                .id(producto.getId()).nombre(producto.getNombre()).precio(producto.getPrecioVenta())
                .porcentajeComision(producto.getPorcentajeComision())
                .build();
    }

    @Transactional(readOnly = true)
    public List<TasaComisionResponse> listarTasasPaquetes() {
        return paqueteRepository.findByEstadoTrueOrderByPrecio().stream()
                .map(pk -> TasaComisionResponse.builder()
                        .id(pk.getId()).nombre(pk.getNombre()).precio(pk.getPrecio())
                        .porcentajeComision(pk.getPorcentajeComision())
                        .build())
                .toList();
    }

    @Transactional
    public TasaComisionResponse actualizarTasaPaquete(Integer id, BigDecimal porcentaje) {
        Paquete paquete = paqueteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Paquete", id));
        paquete.setPorcentajeComision(porcentaje);
        paquete = paqueteRepository.save(paquete);
        log.info("Tasa de comisión actualizada: paquete {} -> {}%", paquete.getNombre(), porcentaje);
        return TasaComisionResponse.builder()
                .id(paquete.getId()).nombre(paquete.getNombre()).precio(paquete.getPrecio())
                .porcentajeComision(paquete.getPorcentajeComision())
                .build();
    }

    // ─── Helpers ───────────────────────────────────────────────────────────

    private record BasesYComisiones(BigDecimal baseServicios, BigDecimal baseProductos,
                                     BigDecimal comisionServicios, BigDecimal comisionProductos) {}

    // Suma, por barbero y rango de fechas, cuanto vendio en servicios/
    // productos (base, al precio de venta) y cuanto le corresponde de
    // comision -- esto ultimo pesado por la tasa de CADA item vendido
    // (join contra servicios/paquetes/productos segun el tipo de cada
    // linea), no por una tasa unica del barbero.
    @SuppressWarnings("unchecked")
    private BasesYComisiones calcularBasesYComisiones(Integer barberoId, LocalDate desde, LocalDate hasta) {
        List<Object[]> resultado = em.createNativeQuery(
                "SELECT " +
                "COALESCE(SUM(CASE WHEN vi.tipo IN ('SERVICIO','PAQUETE') THEN vi.subtotal ELSE 0 END), 0), " +
                "COALESCE(SUM(CASE WHEN vi.tipo = 'PRODUCTO' THEN vi.subtotal ELSE 0 END), 0), " +
                "COALESCE(SUM(CASE WHEN vi.tipo = 'SERVICIO' THEN vi.subtotal * COALESCE(s.porcentaje_comision, 0) / 100 " +
                "                   WHEN vi.tipo = 'PAQUETE'  THEN vi.subtotal * COALESCE(pk.porcentaje_comision, 0) / 100 " +
                "                   ELSE 0 END), 0), " +
                "COALESCE(SUM(CASE WHEN vi.tipo = 'PRODUCTO' THEN vi.subtotal * COALESCE(p.porcentaje_comision, 0) / 100 ELSE 0 END), 0) " +
                "FROM ventas v " +
                "JOIN venta_items vi ON vi.venta_id = v.id " +
                "LEFT JOIN servicios s ON s.id = vi.item_id AND vi.tipo = 'SERVICIO' " +
                "LEFT JOIN paquetes pk ON pk.id = vi.item_id AND vi.tipo = 'PAQUETE' " +
                "LEFT JOIN productos p ON p.id = vi.item_id AND vi.tipo = 'PRODUCTO' " +
                "WHERE v.barbero_id = :bid AND v.estado = 'COMPLETADA' " +
                "AND DATE(v.fecha) BETWEEN :desde AND :hasta")
                .setParameter("bid", barberoId)
                .setParameter("desde", desde)
                .setParameter("hasta", hasta)
                .getResultList();

        if (resultado.isEmpty() || resultado.get(0) == null) {
            return new BasesYComisiones(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
        }
        Object[] row = resultado.get(0);
        return new BasesYComisiones(
                row[0] != null ? (BigDecimal) row[0] : BigDecimal.ZERO,
                row[1] != null ? (BigDecimal) row[1] : BigDecimal.ZERO,
                row[2] != null ? ((BigDecimal) row[2]).setScale(2, java.math.RoundingMode.HALF_UP) : BigDecimal.ZERO,
                row[3] != null ? ((BigDecimal) row[3]).setScale(2, java.math.RoundingMode.HALF_UP) : BigDecimal.ZERO);
    }

    private String nombreBarbero(Integer barberoId) {
        return barberoRepository.findById(barberoId).map(Barbero::getNombreCompleto).orElse("Barbero");
    }

    private Barbero findBarberoOrThrow(Integer id) {
        return barberoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Barbero", id));
    }

    private CustomUserDetails getCurrentUser() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails ud) return ud;
        return null;
    }

    private PropinaResponse toResponse(Propina p, String barberoNombre) {
        return PropinaResponse.builder()
                .id(p.getId()).barberoId(p.getBarberoId()).barberoNombre(barberoNombre)
                .monto(p.getMonto()).fecha(p.getFecha()).notas(p.getNotas())
                .usuarioRegistroNombre(p.getUsuarioRegistroNombre()).corteId(p.getCorteId())
                .createdAt(p.getCreatedAt())
                .build();
    }

    private DescuentoConsumoResponse toResponse(DescuentoConsumo d, String barberoNombre) {
        return DescuentoConsumoResponse.builder()
                .id(d.getId()).barberoId(d.getBarberoId()).barberoNombre(barberoNombre)
                .productoId(d.getProductoId()).productoNombre(d.getProductoNombre())
                .cantidad(d.getCantidad()).precioUnitario(d.getPrecioUnitario()).montoTotal(d.getMontoTotal())
                .fecha(d.getFecha()).motivo(d.getMotivo())
                .usuarioRegistroNombre(d.getUsuarioRegistroNombre()).corteId(d.getCorteId())
                .createdAt(d.getCreatedAt())
                .build();
    }

    private CorteComisionResponse toResponse(CorteComision c, String barberoNombre) {
        return CorteComisionResponse.builder()
                .id(c.getId()).barberoId(c.getBarberoId()).barberoNombre(barberoNombre)
                .fechaDesde(c.getFechaDesde()).fechaHasta(c.getFechaHasta())
                .baseServicios(c.getBaseServicios()).baseProductos(c.getBaseProductos())
                .comisionServicios(c.getComisionServicios()).comisionProductos(c.getComisionProductos())
                .totalPropinas(c.getTotalPropinas()).totalDescuentos(c.getTotalDescuentos())
                .totalAPagar(c.getTotalAPagar()).estado(c.getEstado().name())
                .fechaPago(c.getFechaPago()).notas(c.getNotas())
                .usuarioRegistroNombre(c.getUsuarioRegistroNombre()).createdAt(c.getCreatedAt())
                .build();
    }
}
