package com.barberia.modules.sales.service.impl;

import com.barberia.common.PageResponse;
import com.barberia.exception.BusinessException;
import com.barberia.exception.ConflictException;
import com.barberia.exception.ResourceNotFoundException;
import com.barberia.modules.appointments.entity.EstadoCita;
import com.barberia.modules.appointments.repository.CitaRepository;
import com.barberia.modules.audit.AuditAction;
import com.barberia.modules.audit.AuditPublisher;
import com.barberia.modules.customers.repository.ClienteRepository;
import com.barberia.modules.products.entity.Producto;
import com.barberia.modules.products.repository.ProductoRepository;
import com.barberia.modules.sales.dto.VentaRequest;
import com.barberia.modules.sales.dto.VentaResponse;
import com.barberia.modules.sales.entity.*;
import com.barberia.modules.sales.mapper.VentaMapper;
import com.barberia.modules.sales.repository.VentaRepository;
import com.barberia.modules.services.repository.ServicioRepository;
import com.barberia.modules.workers.repository.BarberoRepository;
import com.barberia.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class VentaServiceImpl {

    private final VentaRepository ventaRepository;
    private final BarberoRepository barberoRepository;
    private final ClienteRepository clienteRepository;
    private final ProductoRepository productoRepository;
    private final ServicioRepository servicioRepository;
    private final CitaRepository citaRepository;
    private final VentaMapper ventaMapper;
    private final AuditPublisher auditPublisher;

    @Transactional(readOnly = true)
    public PageResponse<VentaResponse> listar(EstadoVenta estado, LocalDateTime desde,
                                               LocalDateTime hasta, Pageable pageable) {
        return PageResponse.from(
                ventaRepository.findWithFilters(estado, desde, hasta, pageable)
                        .map(ventaMapper::toResponse));
    }

    @Transactional(readOnly = true)
    public VentaResponse obtenerPorId(Integer id) {
        return ventaMapper.toResponse(findOrThrow(id));
    }

    @Transactional(readOnly = true)
    public PageResponse<VentaResponse> listarPorBarbero(Integer barberoId, Pageable pageable) {
        return PageResponse.from(
                ventaRepository.findByBarberoId(barberoId, pageable).map(ventaMapper::toResponse));
    }

    @Transactional(readOnly = true)
    public PageResponse<VentaResponse> listarPorCliente(Integer clienteId, Pageable pageable) {
        return PageResponse.from(
                ventaRepository.findByClienteId(clienteId, pageable).map(ventaMapper::toResponse));
    }

    @Transactional
    public VentaResponse crear(VentaRequest request) {
        var barbero = barberoRepository.findById(request.getBarberoId())
                .orElseThrow(() -> new ResourceNotFoundException("Barbero", request.getBarberoId()));
        if (!Boolean.TRUE.equals(barbero.getEstado())) {
            throw new BusinessException("El barbero seleccionado no está activo");
        }

        String clienteNombre = null;
        if (request.getClienteId() != null) {
            clienteNombre = clienteRepository.findById(request.getClienteId())
                    .map(c -> c.getNombreCompleto())
                    .orElseThrow(() -> new ResourceNotFoundException("Cliente", request.getClienteId()));
        }

        if (request.getCitaId() != null && ventaRepository.existsByCitaId(request.getCitaId())) {
            throw new ConflictException("Esta cita ya fue convertida en venta anteriormente");
        }

        Map<Integer, Producto> productosCache = new HashMap<>();
        boolean hayServicios = false;
        boolean hayProductos = false;
        BigDecimal subtotal = BigDecimal.ZERO;

        for (VentaRequest.VentaItemRequest item : request.getItems()) {
            if (item.getTipo() == TipoItem.SERVICIO) {
                var servicio = servicioRepository.findById(item.getItemId())
                        .orElseThrow(() -> new ResourceNotFoundException("Servicio", item.getItemId()));
                if (!Boolean.TRUE.equals(servicio.getEstado())) {
                    throw new BusinessException("El servicio '" + servicio.getNombre() + "' no está activo");
                }
                hayServicios = true;
            } else {
                Producto producto = productoRepository.findById(item.getItemId())
                        .orElseThrow(() -> new ResourceNotFoundException("Producto", item.getItemId()));
                if (!Boolean.TRUE.equals(producto.getEstado())) {
                    throw new BusinessException("El producto '" + producto.getNombre() + "' no está activo");
                }
                int stockDisponible = producto.getStockActual();
                int yaReservado = productosCache.containsKey(item.getItemId()) ? 0 : 0;
                if (stockDisponible < item.getCantidad() + yaReservado) {
                    throw new BusinessException("Stock insuficiente para '" + producto.getNombre() +
                            "'. Disponible: " + stockDisponible + ", solicitado: " + item.getCantidad());
                }
                productosCache.put(item.getItemId(), producto);
                hayProductos = true;
            }
            BigDecimal itemSubtotal = item.getPrecio().multiply(BigDecimal.valueOf(item.getCantidad()));
            subtotal = subtotal.add(itemSubtotal);
        }

        TipoVenta tipoVenta = determinarTipo(hayServicios, hayProductos);
        BigDecimal descuento = request.getDescuento() != null ? request.getDescuento() : BigDecimal.ZERO;
        BigDecimal total = subtotal.subtract(descuento);
        if (total.compareTo(BigDecimal.ZERO) < 0) total = BigDecimal.ZERO;

        CustomUserDetails userDetails = getCurrentUser();

        Venta venta = Venta.builder()
                .clienteId(request.getClienteId())
                .clienteNombre(clienteNombre)
                .barberoId(request.getBarberoId())
                .barberoNombre(barbero.getNombreCompleto())
                .usuarioRegistroId(userDetails != null ? userDetails.getId() : null)
                .usuarioRegistroNombre(userDetails != null ? userDetails.getNombreCompleto() : "Sistema")
                .tipoVenta(tipoVenta)
                .metodoPago(request.getMetodoPago())
                .subtotal(subtotal)
                .descuento(descuento)
                .total(total)
                .estado(EstadoVenta.COMPLETADA)
                .citaId(request.getCitaId())
                .notas(request.getNotas())
                .build();

        for (VentaRequest.VentaItemRequest itemReq : request.getItems()) {
            BigDecimal itemSubtotal = itemReq.getPrecio().multiply(BigDecimal.valueOf(itemReq.getCantidad()));
            VentaItem item = VentaItem.builder()
                    .venta(venta)
                    .tipo(itemReq.getTipo())
                    .itemId(itemReq.getItemId())
                    .nombre(itemReq.getNombre())
                    .precio(itemReq.getPrecio())
                    .cantidad(itemReq.getCantidad())
                    .subtotal(itemSubtotal)
                    .build();
            venta.getItems().add(item);
        }

        Venta saved = ventaRepository.save(venta);

        if (request.getCitaId() != null) {
            citaRepository.findById(request.getCitaId()).ifPresent(cita -> {
                cita.setEstado(EstadoCita.ATENDIDA);
                cita.setVentaId(saved.getId());
                citaRepository.save(cita);
            });
        }

        String actor = userDetails != null ? userDetails.getUsername() : "SISTEMA";
        auditPublisher.publish(AuditAction.SALE_CREATE, actor,
                "VENTA", saved.getId().toString(),
                "Venta creada total=" + total + " barbero=" + barbero.getNombreCompleto());
        log.info("Venta registrada ID={} barbero={} total={}", saved.getId(),
                barbero.getNombreCompleto(), total);
        return ventaMapper.toResponse(saved);
    }

    @Transactional
    public VentaResponse anular(Integer id) {
        Venta venta = findOrThrow(id);
        if (venta.getEstado() == EstadoVenta.ANULADA) {
            throw new BusinessException("La venta ya está anulada");
        }
        venta.setEstado(EstadoVenta.ANULADA);
        Venta saved = ventaRepository.save(venta);
        CustomUserDetails userDetails = getCurrentUser();
        String actor = userDetails != null ? userDetails.getUsername() : "SISTEMA";
        auditPublisher.publish(AuditAction.SALE_CANCEL, actor,
                "VENTA", id.toString(), "Venta anulada");
        log.info("Venta anulada ID={}", id);
        return ventaMapper.toResponse(saved);
    }

    private TipoVenta determinarTipo(boolean hayServicios, boolean hayProductos) {
        if (hayServicios && hayProductos) return TipoVenta.MIXTA;
        if (hayProductos) return TipoVenta.PRODUCTO;
        return TipoVenta.SERVICIO;
    }

    private CustomUserDetails getCurrentUser() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails ud) return ud;
        return null;
    }

    private Venta findOrThrow(Integer id) {
        return ventaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Venta", id));
    }
}
