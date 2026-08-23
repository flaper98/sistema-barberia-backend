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
import com.barberia.modules.inventory.dto.MovimientoRequest;
import com.barberia.modules.inventory.entity.TipoMovimiento;
import com.barberia.modules.inventory.service.impl.InventarioServiceImpl;
import com.barberia.modules.loyalty.dto.AgregarSelloRequest;
import com.barberia.modules.loyalty.service.impl.FidelizacionServiceImpl;
import com.barberia.modules.notifications.entity.TipoNotificacion;
import com.barberia.modules.notifications.service.NotificacionService;
import com.barberia.modules.products.entity.Producto;
import com.barberia.modules.products.repository.ProductoRepository;
import com.barberia.modules.sales.dto.VentaItemsUpdateRequest;
import com.barberia.modules.sales.dto.VentaRequest;
import com.barberia.modules.sales.dto.VentaResponse;
import com.barberia.modules.sales.dto.VentaUpdateRequest;
import com.barberia.modules.sales.entity.*;
import com.barberia.modules.sales.mapper.VentaMapper;
import com.barberia.modules.sales.repository.VentaRepository;
import com.barberia.modules.services.entity.Paquete;
import com.barberia.modules.services.entity.Servicio;
import com.barberia.modules.services.entity.ServicioCortesia;
import com.barberia.modules.services.repository.PaqueteRepository;
import com.barberia.modules.services.repository.ServicioCortesiaRepository;
import com.barberia.modules.services.repository.ServicioRepository;
import com.barberia.modules.users.entity.Rol;
import com.barberia.modules.workers.repository.BarberoRepository;
import com.barberia.security.CustomUserDetails;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
    private final ServicioCortesiaRepository servicioCortesiaRepository;
    private final PaqueteRepository paqueteRepository;
    private final CitaRepository citaRepository;
    private final FinanzaRepository finanzaRepository;
    private final FidelizacionServiceImpl fidelizacionService;
    private final InventarioServiceImpl inventarioService;
    private final VentaMapper ventaMapper;
    private final AuditPublisher auditPublisher;
    private final NotificacionService notificacionService;
    private final EntityManager em;

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

    // "Mis Ventas" (BARBER): a diferencia del resto de los listados, aca
    // cada venta trae calculada la comision que le corresponde a ESE
    // barbero -- el barbero no necesita ver el total de la venta (esa plata
    // no es toda suya), solo cuanto le toca a el.
    @Transactional(readOnly = true)
    public PageResponse<VentaResponse> listarPorBarbero(Integer barberoId, LocalDateTime desde,
                                                          LocalDateTime hasta, Pageable pageable) {
        Page<Venta> page = ventaRepository.findByBarberoIdWithFilters(barberoId, desde, hasta, pageable);
        Map<Integer, BigDecimal> comisiones = calcularComisionesPorVenta(
                page.getContent().stream().map(Venta::getId).toList());
        return PageResponse.from(page.map(v -> ventaMapper.toResponse(v).toBuilder()
                .comisionCalculada(v.getEstado() == EstadoVenta.COMPLETADA
                        ? comisiones.getOrDefault(v.getId(), BigDecimal.ZERO)
                        : null)
                .build()));
    }

    // Comision real que generó cada venta, pesada por la tasa de CADA item
    // vendido (igual criterio que ComisionServiceImpl.calcularBasesYComisiones,
    // pero por venta puntual en vez de agregado por rango de fechas).
    @SuppressWarnings("unchecked")
    private Map<Integer, BigDecimal> calcularComisionesPorVenta(List<Integer> ventaIds) {
        if (ventaIds.isEmpty()) return Map.of();
        List<Object[]> filas = em.createNativeQuery(
                "SELECT vi.venta_id, " +
                "COALESCE(SUM(CASE WHEN vi.tipo = 'SERVICIO' THEN vi.subtotal * COALESCE(s.porcentaje_comision, 0) / 100 " +
                "                   WHEN vi.tipo = 'PAQUETE'  THEN vi.subtotal * COALESCE(pk.porcentaje_comision, 0) / 100 " +
                "                   WHEN vi.tipo = 'PRODUCTO' THEN vi.subtotal * COALESCE(p.porcentaje_comision, 0) / 100 " +
                "                   ELSE 0 END), 0) " +
                "FROM venta_items vi " +
                "LEFT JOIN servicios s ON s.id = vi.item_id AND vi.tipo = 'SERVICIO' " +
                "LEFT JOIN paquetes pk ON pk.id = vi.item_id AND vi.tipo = 'PAQUETE' " +
                "LEFT JOIN productos p ON p.id = vi.item_id AND vi.tipo = 'PRODUCTO' " +
                "WHERE vi.venta_id IN (:ids) GROUP BY vi.venta_id")
                .setParameter("ids", ventaIds)
                .getResultList();

        Map<Integer, BigDecimal> resultado = new HashMap<>();
        for (Object[] fila : filas) {
            BigDecimal comision = fila[1] != null ? ((BigDecimal) fila[1]).setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
            resultado.put(((Number) fila[0]).intValue(), comision);
        }
        return resultado;
    }

    @Transactional(readOnly = true)
    public PageResponse<VentaResponse> listarPorCliente(Integer clienteId, Pageable pageable) {
        return PageResponse.from(
                ventaRepository.findByClienteId(clienteId, pageable).map(ventaMapper::toResponse));
    }

    @Transactional
    public VentaResponse crear(VentaRequest request) {
        // Sin barbero a proposito: no toda venta la atiende uno (ej. alguien
        // compra o consume algo sin que sea por un servicio de barberia).
        var barbero = request.getBarberoId() != null
                ? barberoRepository.findById(request.getBarberoId())
                        .orElseThrow(() -> new ResourceNotFoundException("Barbero", request.getBarberoId()))
                : null;
        if (barbero != null && !Boolean.TRUE.equals(barbero.getEstado())) {
            throw new BusinessException("El barbero seleccionado no está activo");
        }

        CustomUserDetails userDetails = getCurrentUser();
        boolean esSolicitudDeBarbero = userDetails != null && "BARBER".equals(userDetails.getRol());

        // El barbero solo elige items -- cliente y pagos los asigna
        // recepcion al aceptar y cobrar la solicitud (ver confirmar()). Para
        // cualquier otro rol (venta directa, ya COMPLETADA), el cliente es
        // opcional (venta a alguien no registrado), pero los pagos siguen
        // siendo obligatorios -- se cobro plata de verdad, hay que saber como
        // (puede ser mas de un metodo, ej. mitad efectivo mitad Yape).
        boolean hayPagos = request.getPagos() != null && !request.getPagos().isEmpty();
        if (!esSolicitudDeBarbero && !hayPagos) {
            throw new BusinessException("El método de pago es obligatorio");
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

        EstadoVenta estadoInicial = esSolicitudDeBarbero ? EstadoVenta.PENDIENTE : EstadoVenta.COMPLETADA;
        MetodoPago metodoPagoResumen = hayPagos ? validarYResumirPagos(request.getPagos(), total) : null;

        Venta venta = Venta.builder()
                .clienteId(request.getClienteId())
                .clienteNombre(clienteNombre)
                .barberoId(request.getBarberoId())
                .barberoNombre(barbero != null ? barbero.getNombreCompleto() : null)
                .usuarioRegistroId(userDetails != null ? userDetails.getId() : null)
                .usuarioRegistroNombre(userDetails != null ? userDetails.getNombreCompleto() : "Sistema")
                .tipoVenta(tipoVenta)
                .metodoPago(metodoPagoResumen)
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

        if (hayPagos) {
            for (VentaRequest.PagoItemRequest pagoReq : request.getPagos()) {
                venta.getPagos().add(VentaPago.builder()
                        .venta(venta)
                        .metodoPago(pagoReq.getMetodoPago())
                        .monto(pagoReq.getMonto())
                        .build());
            }
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
            procesarCortesias(saved);
            int unidadesFidelizacion = contarUnidadesFidelizacion(saved.getItems());
            if (saved.getClienteId() != null && unidadesFidelizacion > 0) {
                AgregarSelloRequest selloRequest = new AgregarSelloRequest();
                selloRequest.setVentaId(saved.getId());
                selloRequest.setCantidad(unidadesFidelizacion);
                selloRequest.setMotivo("Servicio atendido - Venta #" + saved.getId());
                fidelizacionService.agregarSello(saved.getClienteId(), selloRequest);
            }
            if (cliente != null) {
                cliente.setUltimaVisita(saved.getFecha().toLocalDate());
                clienteRepository.save(cliente);
            }
        }

        String nombreBarberoLog = barbero != null ? barbero.getNombreCompleto() : "sin barbero";
        String actor = userDetails != null ? userDetails.getUsername() : "SISTEMA";
        auditPublisher.publish(AuditAction.SALE_CREATE, actor,
                "VENTA", saved.getId().toString(),
                (esSolicitudDeBarbero ? "Solicitud de venta creada (pendiente de cobro) total=" : "Venta creada total=")
                        + total + " barbero=" + nombreBarberoLog);
        log.info("Venta {} ID={} barbero={} total={}", esSolicitudDeBarbero ? "solicitada" : "registrada",
                saved.getId(), nombreBarberoLog, total);

        if (esSolicitudDeBarbero) {
            notificacionService.notificarRoles(List.of(Rol.ADMIN, Rol.CASHIER, Rol.RECEPTION),
                    TipoNotificacion.VENTA_PENDIENTE,
                    "Solicitud de cobro de " + barbero.getNombreCompleto() + " por S/ " + total,
                    "VENTA", saved.getId());
        }

        return ventaMapper.toResponse(saved);
    }

    @Transactional
    public VentaResponse confirmar(Integer id, List<VentaRequest.PagoItemRequest> pagos, Integer clienteId) {
        Venta venta = findOrThrow(id);
        if (venta.getEstado() != EstadoVenta.PENDIENTE) {
            throw new BusinessException("Esta venta no está pendiente de cobro");
        }

        // Las solicitudes que crea un barbero llegan sin cliente -- recepcion
        // lo puede asignar aca, al aceptar y cobrar. Sigue siendo opcional
        // (no todo el que compra o consume algo esta registrado).
        if (clienteId != null) {
            Cliente nuevoCliente = clienteRepository.findById(clienteId)
                    .orElseThrow(() -> new ResourceNotFoundException("Cliente", clienteId));
            venta.setClienteId(clienteId);
            venta.setClienteNombre(nuevoCliente.getNombreCompleto());
        }

        if (pagos == null || pagos.isEmpty()) {
            throw new BusinessException("No se puede cobrar una venta sin método de pago");
        }
        venta.setMetodoPago(validarYResumirPagos(pagos, venta.getTotal()));
        venta.getPagos().clear();
        for (VentaRequest.PagoItemRequest pagoReq : pagos) {
            venta.getPagos().add(VentaPago.builder()
                    .venta(venta)
                    .metodoPago(pagoReq.getMetodoPago())
                    .monto(pagoReq.getMonto())
                    .build());
        }

        venta.setEstado(EstadoVenta.COMPLETADA);
        Venta saved = ventaRepository.save(venta);

        if (saved.getCitaId() != null) {
            citaRepository.findById(saved.getCitaId()).ifPresent(cita -> {
                cita.setEstado(EstadoCita.ATENDIDA);
                citaRepository.save(cita);
            });
        }

        registrarIngresoAutomatico(saved);
        procesarCortesias(saved);

        int unidadesFidelizacion = contarUnidadesFidelizacion(saved.getItems());
        if (saved.getClienteId() != null && unidadesFidelizacion > 0) {
            AgregarSelloRequest selloRequest = new AgregarSelloRequest();
            selloRequest.setVentaId(saved.getId());
            selloRequest.setCantidad(unidadesFidelizacion);
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

    // Edicion de una venta ya registrada (solo ADMIN, ver @PreAuthorize en el
    // controller). Reemplaza tanto los datos generales como los items -- el
    // subtotal/total/tipo de venta se recalculan igual que en crear(). No
    // toca stock (la venta nunca lo descuenta directamente, ver crear(): solo
    // valida disponibilidad -- el descuento real es manual via Inventario).
    @Transactional
    public VentaResponse actualizar(Integer id, VentaUpdateRequest request) {
        Venta venta = findOrThrow(id);
        if (venta.getEstado() == EstadoVenta.ANULADA) {
            throw new BusinessException("No se puede editar una venta anulada");
        }

        // Ninguno de los dos es obligatorio (ver VentaUpdateRequest) -- no
        // toda venta tiene un cliente registrado ni un barbero asignado.
        Cliente nuevoCliente = request.getClienteId() != null
                ? clienteRepository.findById(request.getClienteId())
                        .orElseThrow(() -> new ResourceNotFoundException("Cliente", request.getClienteId()))
                : null;
        var nuevoBarbero = request.getBarberoId() != null
                ? barberoRepository.findById(request.getBarberoId())
                        .orElseThrow(() -> new ResourceNotFoundException("Barbero", request.getBarberoId()))
                : null;

        // Si la venta no tenia cliente y se le agrega uno ahora, hay que darle
        // TODOS los sellos que corresponden a los items actuales (no tenia
        // ninguno antes). Si ya tenia cliente y lo que cambia son los items,
        // solo se otorga la DIFERENCIA a favor (ej. tenia 1 unidad que sumaba
        // sello y ahora tiene 3 -> se le deben 2 mas) -- si la cantidad de
        // unidades que suman sello baja o queda igual, no se le quita nada:
        // no existe today un mecanismo seguro para deshacer sellos ya
        // otorgados (ver FidelizacionServiceImpl), asi que queda como esta.
        boolean clienteAgregado = venta.getClienteId() == null;
        int unidadesFidelizacionAntes = contarUnidadesFidelizacion(new ArrayList<>(venta.getItems()));

        boolean hayServicios = false;
        boolean hayProductos = false;
        BigDecimal subtotal = BigDecimal.ZERO;
        List<VentaItem> nuevosItems = new ArrayList<>();
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
                if (producto.getStockActual() < item.getCantidad()) {
                    throw new BusinessException("Stock insuficiente para '" + producto.getNombre() +
                            "'. Disponible: " + producto.getStockActual() + ", solicitado: " + item.getCantidad());
                }
                hayProductos = true;
            }
            BigDecimal itemSubtotal = item.getPrecio().multiply(BigDecimal.valueOf(item.getCantidad()));
            subtotal = subtotal.add(itemSubtotal);
            nuevosItems.add(VentaItem.builder()
                    .venta(venta)
                    .tipo(item.getTipo())
                    .itemId(item.getItemId())
                    .nombre(item.getNombre())
                    .precio(item.getPrecio())
                    .cantidad(item.getCantidad())
                    .subtotal(itemSubtotal)
                    .build());
        }

        venta.getItems().clear();
        venta.getItems().addAll(nuevosItems);
        venta.setTipoVenta(determinarTipo(hayServicios, hayProductos));
        venta.setSubtotal(subtotal);

        venta.setClienteId(request.getClienteId());
        venta.setClienteNombre(nuevoCliente != null ? nuevoCliente.getNombreCompleto() : null);
        venta.setBarberoId(request.getBarberoId());
        venta.setBarberoNombre(nuevoBarbero != null ? nuevoBarbero.getNombreCompleto() : null);
        venta.setNotas(request.getNotas());

        BigDecimal descuento = request.getDescuento() != null ? request.getDescuento() : BigDecimal.ZERO;
        BigDecimal nuevoTotal = subtotal.subtract(descuento);
        if (nuevoTotal.compareTo(BigDecimal.ZERO) < 0) nuevoTotal = BigDecimal.ZERO;
        venta.setDescuento(descuento);
        venta.setTotal(nuevoTotal);

        // @NotEmpty en VentaUpdateRequest.pagos ya garantiza que no venga vacio.
        venta.setMetodoPago(validarYResumirPagos(request.getPagos(), nuevoTotal));
        venta.getPagos().clear();
        for (VentaRequest.PagoItemRequest pagoReq : request.getPagos()) {
            venta.getPagos().add(VentaPago.builder()
                    .venta(venta)
                    .metodoPago(pagoReq.getMetodoPago())
                    .monto(pagoReq.getMonto())
                    .build());
        }

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

            int unidadesFidelizacionAhora = contarUnidadesFidelizacion(saved.getItems());
            int unidadesAOtorgar = clienteAgregado ? unidadesFidelizacionAhora
                    : Math.max(0, unidadesFidelizacionAhora - unidadesFidelizacionAntes);
            if (saved.getClienteId() != null && unidadesAOtorgar > 0) {
                AgregarSelloRequest selloRequest = new AgregarSelloRequest();
                selloRequest.setVentaId(saved.getId());
                selloRequest.setCantidad(unidadesAOtorgar);
                selloRequest.setMotivo("Servicio atendido - Venta #" + saved.getId() + " (agregado al editar)");
                fidelizacionService.agregarSello(saved.getClienteId(), selloRequest);
            }
            if (saved.getClienteId() != null) {
                clienteRepository.findById(saved.getClienteId()).ifPresent(cliente -> {
                    cliente.setUltimaVisita(saved.getFecha().toLocalDate());
                    clienteRepository.save(cliente);
                });
            }
        }

        CustomUserDetails userDetails = getCurrentUser();
        String actor = userDetails != null ? userDetails.getUsername() : "SISTEMA";
        auditPublisher.publish(AuditAction.SALE_UPDATE, actor,
                "VENTA", saved.getId().toString(), "Venta editada por administrador");
        log.info("Venta ID={} editada por {}", saved.getId(), actor);
        return ventaMapper.toResponse(saved);
    }

    // El barbero puede agregar/quitar items de SU PROPIA solicitud mientras
    // siga PENDIENTE (ej. el cliente pide un servicio mas a mitad de la
    // atencion) -- no puede tocar cliente, metodo de pago ni barbero (para
    // eso existe actualizar(), solo ADMIN), ni editar una solicitud ajena.
    // ADMIN/CASHIER/RECEPTION pueden editar los items de CUALQUIER solicitud
    // pendiente (ej. al aceptar y cobrar, si hay que sumar o sacar algo antes
    // de cerrar el cobro) -- para ellos no aplica el chequeo de dueño, ya
    // tienen la misma potestad sobre confirmar()/anular() de cualquier
    // solicitud (ver @PreAuthorize en el controller para el rol).
    @Transactional
    public VentaResponse actualizarItemsPropios(Integer id, VentaItemsUpdateRequest request) {
        Venta venta = findOrThrow(id);

        CustomUserDetails userDetails = getCurrentUser();
        boolean esBarbero = userDetails != null && "BARBER".equals(userDetails.getRol());
        if (esBarbero) {
            Integer miBarberoId = userDetails.getUsuario().getBarberoId();
            if (miBarberoId == null || !miBarberoId.equals(venta.getBarberoId())) {
                throw new BusinessException("No podés editar la solicitud de otro barbero");
            }
        }
        if (venta.getEstado() != EstadoVenta.PENDIENTE) {
            throw new BusinessException("Solo se puede editar mientras está pendiente de cobro");
        }

        boolean hayServicios = false;
        boolean hayProductos = false;
        BigDecimal subtotal = BigDecimal.ZERO;
        List<VentaItem> nuevosItems = new ArrayList<>();
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
                if (producto.getStockActual() < item.getCantidad()) {
                    throw new BusinessException("Stock insuficiente para '" + producto.getNombre() +
                            "'. Disponible: " + producto.getStockActual() + ", solicitado: " + item.getCantidad());
                }
                hayProductos = true;
            }
            BigDecimal itemSubtotal = item.getPrecio().multiply(BigDecimal.valueOf(item.getCantidad()));
            subtotal = subtotal.add(itemSubtotal);
            nuevosItems.add(VentaItem.builder()
                    .venta(venta)
                    .tipo(item.getTipo())
                    .itemId(item.getItemId())
                    .nombre(item.getNombre())
                    .precio(item.getPrecio())
                    .cantidad(item.getCantidad())
                    .subtotal(itemSubtotal)
                    .build());
        }

        venta.getItems().clear();
        venta.getItems().addAll(nuevosItems);
        venta.setTipoVenta(determinarTipo(hayServicios, hayProductos));
        venta.setSubtotal(subtotal);

        BigDecimal nuevoTotal = subtotal.subtract(venta.getDescuento());
        if (nuevoTotal.compareTo(BigDecimal.ZERO) < 0) nuevoTotal = BigDecimal.ZERO;
        venta.setTotal(nuevoTotal);

        Venta saved = ventaRepository.save(venta);

        String actor = userDetails.getUsername();
        auditPublisher.publish(AuditAction.SALE_UPDATE, actor,
                "VENTA", saved.getId().toString(), "Barbero editó los ítems de su solicitud pendiente");
        log.info("Venta ID={} - items editados por barbero {}", saved.getId(), actor);
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

    // Borrado definitivo (solo ADMIN, ver @PreAuthorize en el controller) --
    // a diferencia de anular(), esto saca la fila entera de la base (audit
    // log aparte). Igual que anular(), no revierte el sello de fidelizacion
    // ya otorgado (no existe mecanismo seguro para eso todavia). Si la venta
    // venia de una cita, desvincula esa cita (y la vuelve a PENDIENTE si
    // habia quedado ATENDIDA por esta misma venta) para no dejar una
    // referencia colgada a una venta que ya no existe.
    @Transactional
    public void eliminar(Integer id) {
        Venta venta = findOrThrow(id);
        finanzaRepository.deleteByVentaId(id);

        if (venta.getCitaId() != null) {
            citaRepository.findById(venta.getCitaId()).ifPresent(cita -> {
                if (cita.getVentaId() != null && cita.getVentaId().equals(venta.getId())) {
                    cita.setVentaId(null);
                    if (cita.getEstado() == EstadoCita.ATENDIDA) {
                        cita.setEstado(EstadoCita.PENDIENTE);
                    }
                    citaRepository.save(cita);
                }
            });
        }

        CustomUserDetails userDetails = getCurrentUser();
        String actor = userDetails != null ? userDetails.getUsername() : "SISTEMA";
        auditPublisher.publish(AuditAction.SALE_DELETE, actor,
                "VENTA", id.toString(), "Venta eliminada definitivamente total=" + venta.getTotal());
        log.info("Venta ID={} eliminada definitivamente por {}", id, actor);
        ventaRepository.delete(venta);
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

    // Valida que la suma de los pagos coincida exactamente con lo que se
    // esta cobrando y devuelve el resumen que va en Venta.metodoPago: el
    // metodo unico si hay uno solo, o MIXTO si se combinaron dos o mas (ej.
    // mitad efectivo, mitad Yape). No toca la coleccion Venta.pagos -- eso
    // lo arma cada llamador, porque en crear() la Venta recien se esta
    // construyendo y todavia no existe para referenciarla.
    private MetodoPago validarYResumirPagos(List<VentaRequest.PagoItemRequest> pagos, BigDecimal totalEsperado) {
        BigDecimal suma = pagos.stream()
                .map(VentaRequest.PagoItemRequest::getMonto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (suma.compareTo(totalEsperado) != 0) {
            throw new BusinessException("La suma de los métodos de pago (S/ " + suma
                    + ") no coincide con el total a cobrar (S/ " + totalEsperado + ")");
        }
        return pagos.size() == 1 ? pagos.get(0).getMetodoPago() : MetodoPago.MIXTO;
    }

    private TipoVenta determinarTipo(boolean hayServicios, boolean hayProductos) {
        if (hayServicios && hayProductos) return TipoVenta.MIXTA;
        if (hayProductos) return TipoVenta.PRODUCTO;
        return TipoVenta.SERVICIO;
    }

    // Cuantos sellos corresponden por los items de esta venta: no es "1 por
    // venta" sino la suma de la CANTIDAD de cada item cuya regla de
    // fidelizacion esta activada (ver Servicio/Producto/Paquete#aplicaFidelizacion,
    // editable desde Fidelizacion -> Que aplica sello) -- ej. un corte y un
    // alisado (ambos activados) en la misma venta son 2, no 1.
    // Por cada servicio vendido que tenga cortesia configurada (ver
    // ServicioCortesia / pantalla Servicios -> "Cortesía incluida"),
    // descuenta el stock del producto de cortesia automaticamente -- no
    // cobra nada ni afecta la comision del barbero, es un costo del
    // negocio. Si a algun producto de cortesia le falta stock (o esta
    // inactivo/eliminado), NO bloquea la venta (no tiene sentido no poder
    // vender un corte por quedarse sin la bebida de regalo) -- se valida
    // ACA, antes de llamar a inventarioService, a proposito: si dejaramos
    // que registrarMovimiento tire la excepcion y la agarraramos con un
    // try/catch aca, Spring igual marca la transaccion de esta venta como
    // rollback-only (es otro bean @Transactional, no importa que la
    // excepcion se atrape afuera) y la venta entera fallaria igual.
    private void procesarCortesias(Venta venta) {
        for (VentaItem item : venta.getItems()) {
            if (item.getTipo() != TipoItem.SERVICIO) continue;
            List<ServicioCortesia> cortesias = servicioCortesiaRepository.findByServicioId(item.getItemId());
            for (ServicioCortesia cortesia : cortesias) {
                int cantidadNecesaria = cortesia.getCantidad() * item.getCantidad();
                Producto producto = productoRepository.findById(cortesia.getProductoId()).orElse(null);
                if (producto == null || !Boolean.TRUE.equals(producto.getEstado())
                        || producto.getStockActual() < cantidadNecesaria) {
                    log.warn("No se pudo descontar la cortesía (productoId={}) de la venta ID={}: " +
                            "producto inactivo/eliminado o stock insuficiente", cortesia.getProductoId(), venta.getId());
                    continue;
                }
                MovimientoRequest movRequest = new MovimientoRequest();
                movRequest.setProductoId(cortesia.getProductoId());
                movRequest.setTipoMovimiento(TipoMovimiento.CORTESIA);
                movRequest.setCantidad(cantidadNecesaria);
                movRequest.setMotivo("Cortesía por " + item.getNombre() + " - Venta #" + venta.getId());
                movRequest.setVentaId(venta.getId());
                inventarioService.registrarMovimiento(movRequest);
            }
        }
    }

    private int contarUnidadesFidelizacion(List<VentaItem> items) {
        int total = 0;
        for (VentaItem item : items) {
            boolean aplica = switch (item.getTipo()) {
                case SERVICIO -> servicioRepository.findById(item.getItemId())
                        .map(Servicio::getAplicaFidelizacion).orElse(false);
                case PAQUETE -> paqueteRepository.findById(item.getItemId())
                        .map(Paquete::getAplicaFidelizacion).orElse(false);
                case PRODUCTO -> productoRepository.findById(item.getItemId())
                        .map(Producto::getAplicaFidelizacion).orElse(false);
            };
            if (Boolean.TRUE.equals(aplica)) total += item.getCantidad();
        }
        return total;
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
