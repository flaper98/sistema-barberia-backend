package com.barberia.modules.sales.service.impl;

import com.barberia.common.PageResponse;
import com.barberia.exception.BusinessException;
import com.barberia.exception.ConflictException;
import com.barberia.exception.ResourceNotFoundException;
import com.barberia.modules.appointments.entity.EstadoCita;
import com.barberia.modules.appointments.repository.CitaRepository;
import com.barberia.modules.audit.AuditAction;
import com.barberia.modules.audit.AuditPublisher;
import com.barberia.modules.customers.entity.Cliente;
import com.barberia.modules.customers.repository.ClienteRepository;
import com.barberia.modules.finance.entity.CategoriaFinanza;
import com.barberia.modules.finance.entity.Finanza;
import com.barberia.modules.finance.entity.TipoFinanza;
import com.barberia.modules.finance.repository.FinanzaRepository;
import com.barberia.modules.loyalty.dto.AgregarSelloRequest;
import com.barberia.modules.loyalty.service.impl.FidelizacionServiceImpl;
import com.barberia.modules.products.entity.Producto;
import com.barberia.modules.products.repository.ProductoRepository;
import com.barberia.modules.sales.dto.VentaRequest;
import com.barberia.modules.sales.dto.VentaResponse;
import com.barberia.modules.sales.dto.VentaUpdateRequest;
import com.barberia.modules.sales.entity.*;
import com.barberia.modules.sales.mapper.VentaMapper;
import com.barberia.modules.sales.repository.VentaRepository;
import com.barberia.modules.services.repository.PaqueteRepository;
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
    private final PaqueteRepository paqueteRepository;
    private final CitaRepository citaRepository;
    private final FinanzaRepository finanzaRepository;
    private final FidelizacionServiceImpl fidelizacionService;
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

        Cliente cliente = null;
        String clienteNombre = null;
        if (request.getClienteId() != null) {
            cliente = clienteRepository.findById(request.getClienteId())
                    .orElseThrow(() -> new ResourceNotFoundException("Cliente", request.getClienteId()));
            clienteNombre = cliente.getNombreCompleto();
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
            } else if (item.getTipo() == TipoItem.PAQUETE) {
                var paquete = paqueteRepository.findById(item.getItemId())
                        .orElseThrow(() -> new ResourceNotFoundException("Paquete", item.getItemId()));
                if (!Boolean.TRUE.equals(paquete.getEstado())) {
                    throw new BusinessException("El paquete '" + paquete.getNombre() + "' no está activo");
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
        boolean esSolicitudDeBarbero = userDetails != null && "BARBER".equals(userDetails.getRol());
        EstadoVenta estadoInicial = esSolicitudDeBarbero ? EstadoVenta.PENDIENTE : EstadoVenta.COMPLETADA;

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
                .estado(estadoInicial)
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
                if (saved.getEstado() == EstadoVenta.COMPLETADA) cita.setEstado(EstadoCita.ATENDIDA);
                cita.setVentaId(saved.getId());
                citaRepository.save(cita);
            });
        }

        if (saved.getEstado() == EstadoVenta.COMPLETADA) {
            registrarIngresoAutomatico(saved);
            if (saved.getClienteId() != null && hayServicios) {
                AgregarSelloRequest selloRequest = new AgregarSelloRequest();
                selloRequest.setVentaId(saved.getId());
                selloRequest.setMotivo("Servicio atendido - Venta #" + saved.getId());
                fidelizacionService.agregarSello(saved.getClienteId(), selloRequest);
            }
            if (cliente != null) {
                cliente.setUltimaVisita(saved.getFecha().toLocalDate());
                clienteRepository.save(cliente);
            }
        }

        String actor = userDetails != null ? userDetails.getUsername() : "SISTEMA";
        auditPublisher.publish(AuditAction.SALE_CREATE, actor,
                "VENTA", saved.getId().toString(),
                (esSolicitudDeBarbero ? "Solicitud de venta creada (pendiente de cobro) total=" : "Venta creada total=")
                        + total + " barbero=" + barbero.getNombreCompleto());
        log.info("Venta {} ID={} barbero={} total={}", esSolicitudDeBarbero ? "solicitada" : "registrada",
                saved.getId(), barbero.getNombreCompleto(), total);
        return ventaMapper.toResponse(saved);
    }

    @Transactional
    public VentaResponse confirmar(Integer id, MetodoPago metodoPago) {
        Venta venta = findOrThrow(id);
        if (venta.getEstado() != EstadoVenta.PENDIENTE) {
            throw new BusinessException("Esta venta no está pendiente de cobro");
        }

        if (metodoPago != null) venta.setMetodoPago(metodoPago);
        venta.setEstado(EstadoVenta.COMPLETADA);
        Venta saved = ventaRepository.save(venta);

        if (saved.getCitaId() != null) {
            citaRepository.findById(saved.getCitaId()).ifPresent(cita -> {
                cita.setEstado(EstadoCita.ATENDIDA);
                citaRepository.save(cita);
            });
        }

        registrarIngresoAutomatico(saved);

        boolean hayServicios = saved.getTipoVenta() == TipoVenta.SERVICIO || saved.getTipoVenta() == TipoVenta.MIXTA;
        if (saved.getClienteId() != null && hayServicios) {
            AgregarSelloRequest selloRequest = new AgregarSelloRequest();
            selloRequest.setVentaId(saved.getId());
            selloRequest.setMotivo("Servicio atendido - Venta #" + saved.getId());
            fidelizacionService.agregarSello(saved.getClienteId(), selloRequest);
        }

        if (saved.getClienteId() != null) {
            clienteRepository.findById(saved.getClienteId()).ifPresent(cliente -> {
                cliente.setUltimaVisita(saved.getFecha().toLocalDate());
                clienteRepository.save(cliente);
            });
        }

        CustomUserDetails userDetails = getCurrentUser();
        String actor = userDetails != null ? userDetails.getUsername() : "SISTEMA";
        auditPublisher.publish(AuditAction.SALE_CREATE, actor,
                "VENTA", saved.getId().toString(),
                "Venta aceptada y cobrada total=" + saved.getTotal());
        log.info("Venta ID={} aceptada y cobrada por {}", saved.getId(), actor);
        return ventaMapper.toResponse(saved);
    }

    // Edicion de los datos generales de una venta ya registrada (solo ADMIN,
    // ver @PreAuthorize en el controller). A proposito NO permite tocar los
    // items -- para eso hay que anular y volver a registrar: recalcular stock
    // y sellos de fidelizacion ya aplicados por cambios de item es mucho mas
    // riesgoso que corregir datos generales.
    @Transactional
    public VentaResponse actualizar(Integer id, VentaUpdateRequest request) {
        Venta venta = findOrThrow(id);
        if (venta.getEstado() == EstadoVenta.ANULADA) {
            throw new BusinessException("No se puede editar una venta anulada");
        }

        Cliente nuevoCliente = clienteRepository.findById(request.getClienteId())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", request.getClienteId()));
        var nuevoBarbero = barberoRepository.findById(request.getBarberoId())
                .orElseThrow(() -> new ResourceNotFoundException("Barbero", request.getBarberoId()));

        // Si la venta no tenia cliente y se le agrega uno ahora, hay que darle
        // el sello de fidelizacion que se salteo en su momento. Si el cliente
        // cambia de uno a otro (caso raro), los sellos ya sumados se quedan
        // como estan -- revertirlos de forma segura excede el alcance de esta
        // edicion "solo datos generales".
        boolean clienteAgregado = venta.getClienteId() == null;

        venta.setClienteId(request.getClienteId());
        venta.setClienteNombre(nuevoCliente.getNombreCompleto());
        venta.setBarberoId(request.getBarberoId());
        venta.setBarberoNombre(nuevoBarbero.getNombreCompleto());
        venta.setMetodoPago(request.getMetodoPago());
        venta.setNotas(request.getNotas());

        BigDecimal descuento = request.getDescuento() != null ? request.getDescuento() : BigDecimal.ZERO;
        BigDecimal nuevoTotal = venta.getSubtotal().subtract(descuento);
        if (nuevoTotal.compareTo(BigDecimal.ZERO) < 0) nuevoTotal = BigDecimal.ZERO;
        venta.setDescuento(descuento);
        venta.setTotal(nuevoTotal);

        Venta saved = ventaRepository.save(venta);

        if (saved.getEstado() == EstadoVenta.COMPLETADA) {
            // El ingreso automatico en Finanzas tiene que reflejar el nuevo
            // total/cliente si ya se habia registrado.
            finanzaRepository.findByVentaId(saved.getId()).ifPresent(finanza -> {
                finanza.setMonto(saved.getTotal());
                finanza.setDescripcion("Venta #" + saved.getId()
                        + (saved.getClienteNombre() != null ? " - " + saved.getClienteNombre() : ""));
                finanzaRepository.save(finanza);
            });

            boolean hayServicios = saved.getTipoVenta() == TipoVenta.SERVICIO || saved.getTipoVenta() == TipoVenta.MIXTA;
            if (clienteAgregado && hayServicios) {
                AgregarSelloRequest selloRequest = new AgregarSelloRequest();
                selloRequest.setVentaId(saved.getId());
                selloRequest.setMotivo("Servicio atendido - Venta #" + saved.getId() + " (cliente agregado al editar)");
                fidelizacionService.agregarSello(saved.getClienteId(), selloRequest);
            }
            clienteRepository.findById(saved.getClienteId()).ifPresent(cliente -> {
                cliente.setUltimaVisita(saved.getFecha().toLocalDate());
                clienteRepository.save(cliente);
            });
        }

        CustomUserDetails userDetails = getCurrentUser();
        String actor = userDetails != null ? userDetails.getUsername() : "SISTEMA";
        auditPublisher.publish(AuditAction.SALE_UPDATE, actor,
                "VENTA", saved.getId().toString(), "Venta editada por administrador");
        log.info("Venta ID={} editada por {}", saved.getId(), actor);
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
        finanzaRepository.deleteByVentaId(id);
        CustomUserDetails userDetails = getCurrentUser();
        String actor = userDetails != null ? userDetails.getUsername() : "SISTEMA";
        auditPublisher.publish(AuditAction.SALE_CANCEL, actor,
                "VENTA", id.toString(), "Venta anulada");
        log.info("Venta anulada ID={}", id);
        return ventaMapper.toResponse(saved);
    }

    private void registrarIngresoAutomatico(Venta venta) {
        Finanza finanza = Finanza.builder()
                .tipo(TipoFinanza.INGRESO)
                .categoria(CategoriaFinanza.VENTA)
                .descripcion("Venta #" + venta.getId()
                        + (venta.getClienteNombre() != null ? " - " + venta.getClienteNombre() : ""))
                .monto(venta.getTotal())
                .fecha(venta.getFecha().toLocalDate())
                .usuarioId(venta.getUsuarioRegistroId())
                .usuarioNombre(venta.getUsuarioRegistroNombre())
                .referencia("VENTA-" + venta.getId())
                .ventaId(venta.getId())
                .build();
        finanzaRepository.save(finanza);
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
