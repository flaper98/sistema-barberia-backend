package com.barberia.modules.loyalty.service.impl;

import com.barberia.common.PageResponse;
import com.barberia.exception.BusinessException;
import com.barberia.exception.ResourceNotFoundException;
import com.barberia.modules.audit.AuditAction;
import com.barberia.modules.audit.AuditPublisher;
import com.barberia.modules.customers.entity.Cliente;
import com.barberia.modules.customers.repository.ClienteRepository;
import com.barberia.modules.customers.repository.ClienteSpecifications;
import com.barberia.modules.loyalty.dto.ActualizarReglaFidelizacionRequest;
import com.barberia.modules.loyalty.dto.AgregarSelloRequest;
import com.barberia.modules.loyalty.dto.FidelizacionCuentaResponse;
import com.barberia.modules.loyalty.dto.ReglaFidelizacionResponse;
import com.barberia.modules.loyalty.dto.SelloResponse;
import com.barberia.modules.loyalty.entity.FidelizacionConfig;
import com.barberia.modules.loyalty.entity.FidelizacionCuenta;
import com.barberia.modules.loyalty.entity.FidelizacionMovimiento;
import com.barberia.modules.loyalty.entity.Recompensa;
import com.barberia.modules.loyalty.entity.TipoMovimientoFidelizacion;
import com.barberia.modules.loyalty.repository.FidelizacionConfigRepository;
import com.barberia.modules.loyalty.repository.FidelizacionCuentaRepository;
import com.barberia.modules.loyalty.repository.FidelizacionMovimientoRepository;
import com.barberia.modules.loyalty.repository.RecompensaRepository;
import com.barberia.modules.products.entity.Producto;
import com.barberia.modules.products.repository.ProductoRepository;
import com.barberia.modules.services.entity.Paquete;
import com.barberia.modules.services.entity.Servicio;
import com.barberia.modules.services.repository.PaqueteRepository;
import com.barberia.modules.services.repository.ServicioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * La lógica de sellos/recompensas vivía originalmente en los stored procedures
 * {@code sp_agregar_sello} / {@code sp_canjear_recompensa}, que nunca llegaron
 * a existir en ninguna base (ni en migraciones del repo) -- por eso el botón
 * "agregar sello" siempre fallaba. Se reimplementa acá en JPA plano, y además
 * sincroniza los contadores denormalizados en {@link Cliente}
 * (cantidadSellos/recompensasDisponibles) que la lista de clientes muestra
 * directamente y que dependían de que esos mismos procedures los actualizaran.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FidelizacionServiceImpl {

    private final FidelizacionCuentaRepository cuentaRepository;
    private final FidelizacionMovimientoRepository movimientoRepository;
    private final RecompensaRepository recompensaRepository;
    private final ClienteRepository clienteRepository;
    private final FidelizacionConfigRepository configRepository;
    private final AuditPublisher auditPublisher;
    private final ServicioRepository servicioRepository;
    private final ProductoRepository productoRepository;
    private final PaqueteRepository paqueteRepository;

    @Transactional(readOnly = true)
    public FidelizacionCuentaResponse obtenerCuenta(Integer clienteId) {
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", clienteId));

        FidelizacionConfig config = obtenerConfig();
        FidelizacionCuenta cuenta = cuentaRepository.findByClienteId(clienteId)
                .orElseGet(() -> cuentaVacia(clienteId, config));

        return toResponse(cuenta, cliente.getNombreCompleto(), config.getDescripcionRecompensa());
    }

    @Transactional
    public FidelizacionCuentaResponse actualizarDescripcionRecompensa(Integer clienteId, String descripcion) {
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", clienteId));

        FidelizacionConfig config = obtenerConfig();
        FidelizacionCuenta cuenta = obtenerOCrearCuenta(clienteId, config);

        cuenta.setDescripcionRecompensa(
                descripcion != null && !descripcion.isBlank() ? descripcion.trim() : null);
        cuenta = cuentaRepository.save(cuenta);
        sincronizarCliente(cliente, cuenta);
        log.info("Descripción de recompensa actualizada para cliente ID={}", clienteId);

        return toResponse(cuenta, cliente.getNombreCompleto(), config.getDescripcionRecompensa());
    }

    @Transactional
    public SelloResponse agregarSello(Integer clienteId, AgregarSelloRequest request) {
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", clienteId));

        FidelizacionConfig config = obtenerConfig();
        FidelizacionCuenta cuenta = obtenerOCrearCuenta(clienteId, config);

        // sellosPorGanancia es cuanto vale CADA "ganancia" (config, default
        // 1); request.getCantidad() es CUANTAS ganancias hubo en esta venta
        // (ej. 2 servicios que suman sello en la misma venta = 2, no 1 --
        // ver VentaServiceImpl#contarUnidadesFidelizacion). El uso manual
        // (boton "Agregar sello" en Fidelizacion) no manda cantidad, así que
        // vale 1 -- se comporta exactamente como antes en ese caso.
        int porGanancia = config.getSellosPorGanancia() != null ? config.getSellosPorGanancia() : 1;
        int vecesQueGano = request.getCantidad() != null && request.getCantidad() > 0 ? request.getCantidad() : 1;
        int cantidad = porGanancia * vecesQueGano;
        cuenta.setSellosActuales(cuenta.getSellosActuales() + cantidad);
        cuenta.setTotalSellosAcumulados(cuenta.getTotalSellosAcumulados() + cantidad);

        boolean nuevaRecompensa = false;
        while (cuenta.getSellosActuales() >= cuenta.getSellosNecesarios()) {
            cuenta.setSellosActuales(cuenta.getSellosActuales() - cuenta.getSellosNecesarios());
            cuenta.setRecompensasDisponibles(cuenta.getRecompensasDisponibles() + 1);
            nuevaRecompensa = true;
        }
        cuenta.setUltimoMovimiento(LocalDateTime.now());
        cuenta = cuentaRepository.save(cuenta);

        sincronizarCliente(cliente, cuenta);

        movimientoRepository.save(FidelizacionMovimiento.builder()
                .cuentaId(cuenta.getId())
                .clienteId(clienteId)
                .clienteNombre(cliente.getNombreCompleto())
                .tipo(TipoMovimientoFidelizacion.GANADO)
                .cantidad(cantidad)
                .motivo(request.getMotivo())
                .ventaId(request.getVentaId())
                .build());

        auditPublisher.publish(AuditAction.STAMP_ADD, "SISTEMA",
                "CLIENTE", clienteId.toString(),
                "Sello agregado. Nueva recompensa: " + nuevaRecompensa);
        log.info("Sello agregado para cliente ID={} nueva_recompensa={}", clienteId, nuevaRecompensa);

        return SelloResponse.builder()
                .sellosActuales(cuenta.getSellosActuales())
                .sellosNecesarios(cuenta.getSellosNecesarios())
                .recompensasDisponibles(cuenta.getRecompensasDisponibles())
                .nuevaRecompensaObtenida(nuevaRecompensa)
                .mensaje(nuevaRecompensa
                        ? "¡Felicitaciones! El cliente obtuvo una nueva recompensa."
                        : "Sello agregado correctamente. Sellos actuales: " + cuenta.getSellosActuales())
                .build();
    }

    // Para corregir un sello agregado por error -- quita de a uno. Si los
    // sellos actuales ya estan en 0 pero el cliente tiene una recompensa
    // completa, deshace ese ciclo (le devuelve sellosNecesarios-1 y le saca
    // una recompensa) en vez de dejar sellosActuales en negativo.
    @Transactional
    public SelloResponse quitarSello(Integer clienteId, String motivo) {
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", clienteId));

        FidelizacionCuenta cuenta = cuentaRepository.findByClienteId(clienteId)
                .orElseThrow(() -> new BusinessException("El cliente no tiene sellos para quitar"));

        int cantidad = 1;
        if (cuenta.getSellosActuales() <= 0 && cuenta.getRecompensasDisponibles() <= 0) {
            throw new BusinessException("El cliente no tiene sellos para quitar");
        }

        if (cuenta.getSellosActuales() > 0) {
            cuenta.setSellosActuales(cuenta.getSellosActuales() - cantidad);
        } else {
            cuenta.setRecompensasDisponibles(cuenta.getRecompensasDisponibles() - 1);
            cuenta.setSellosActuales(cuenta.getSellosNecesarios() - cantidad);
        }
        cuenta.setTotalSellosAcumulados(Math.max(0, cuenta.getTotalSellosAcumulados() - cantidad));
        cuenta.setUltimoMovimiento(LocalDateTime.now());
        cuenta = cuentaRepository.save(cuenta);

        sincronizarCliente(cliente, cuenta);

        movimientoRepository.save(FidelizacionMovimiento.builder()
                .cuentaId(cuenta.getId())
                .clienteId(clienteId)
                .clienteNombre(cliente.getNombreCompleto())
                .tipo(TipoMovimientoFidelizacion.AJUSTE)
                .cantidad(-cantidad)
                .motivo(motivo)
                .build());

        auditPublisher.publish(AuditAction.STAMP_REMOVE, "SISTEMA",
                "CLIENTE", clienteId.toString(), "Sello quitado. Motivo: " + motivo);
        log.info("Sello quitado para cliente ID={}", clienteId);

        return SelloResponse.builder()
                .sellosActuales(cuenta.getSellosActuales())
                .sellosNecesarios(cuenta.getSellosNecesarios())
                .recompensasDisponibles(cuenta.getRecompensasDisponibles())
                .nuevaRecompensaObtenida(false)
                .mensaje("Sello quitado correctamente. Sellos actuales: " + cuenta.getSellosActuales())
                .build();
    }

    @Transactional
    public SelloResponse canjearRecompensa(Integer clienteId, String motivo) {
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", clienteId));

        FidelizacionCuenta cuenta = cuentaRepository.findByClienteId(clienteId)
                .orElseThrow(() -> new BusinessException("El cliente no tiene recompensas disponibles para canjear"));

        if (cuenta.getRecompensasDisponibles() <= 0) {
            throw new BusinessException("El cliente no tiene recompensas disponibles para canjear");
        }

        cuenta.setRecompensasDisponibles(cuenta.getRecompensasDisponibles() - 1);
        cuenta.setRecompensasCanjeadas(cuenta.getRecompensasCanjeadas() + 1);
        cuenta.setDescripcionRecompensa(null);
        cuenta.setUltimoMovimiento(LocalDateTime.now());
        cuenta = cuentaRepository.save(cuenta);

        sincronizarCliente(cliente, cuenta);

        movimientoRepository.save(FidelizacionMovimiento.builder()
                .cuentaId(cuenta.getId())
                .clienteId(clienteId)
                .clienteNombre(cliente.getNombreCompleto())
                .tipo(TipoMovimientoFidelizacion.CANJEADO)
                .cantidad(cuenta.getSellosNecesarios())
                .motivo(motivo)
                .build());

        auditPublisher.publish(AuditAction.REWARD_REDEEM, "SISTEMA",
                "CLIENTE", clienteId.toString(), "Recompensa canjeada. Motivo: " + motivo);
        log.info("Recompensa canjeada para cliente ID={}", clienteId);

        return SelloResponse.builder()
                .sellosActuales(cuenta.getSellosActuales())
                .sellosNecesarios(cuenta.getSellosNecesarios())
                .recompensasDisponibles(cuenta.getRecompensasDisponibles())
                .nuevaRecompensaObtenida(false)
                .mensaje("Recompensa canjeada exitosamente. Recompensas restantes: "
                        + cuenta.getRecompensasDisponibles())
                .build();
    }

    @Transactional(readOnly = true)
    public List<Recompensa> listarRecompensas() {
        return recompensaRepository.findByActivoTrue();
    }

    @Transactional(readOnly = true)
    public PageResponse<FidelizacionCuentaResponse> listarClientesConRecompensas(Pageable pageable) {
        FidelizacionConfig config = obtenerConfig();
        return PageResponse.from(
                cuentaRepository.findByRecompensasDisponiblesGreaterThan(0, pageable)
                        .map(cuenta -> {
                            Cliente c = clienteRepository.findById(cuenta.getClienteId()).orElse(null);
                            return toResponse(cuenta, c != null ? c.getNombreCompleto() : "Cliente",
                                    config.getDescripcionRecompensa());
                        }));
    }

    @Transactional(readOnly = true)
    public PageResponse<FidelizacionCuentaResponse> listarTodasLasCuentas(Pageable pageable, String search) {
        FidelizacionConfig config = obtenerConfig();
        // Antes se usaba findByEstadoTrue(pageable) sin busqueda -- con 1000+
        // clientes, la pantalla de Fidelizacion solo mostraba la primera
        // pagina ordenada alfabeticamente y no habia forma de buscar del lado
        // del servidor, asi que un cliente recien creado (con 0 sellos, sin
        // razon para aparecer en esa primera pagina) parecia "no estar" en
        // Fidelizacion aunque el backend siempre lo hubiera incluido tarde o
        // temprano. Se reusa la misma Specification de busqueda de Clientes.
        Specification<Cliente> spec = Specification.where(ClienteSpecifications.activo())
                .and(ClienteSpecifications.conTexto(search));
        return PageResponse.from(
                clienteRepository.findAll(spec, pageable)
                        .map(cliente -> {
                            FidelizacionCuenta cuenta = cuentaRepository.findByClienteId(cliente.getId())
                                    .orElseGet(() -> cuentaVacia(cliente.getId(), config));
                            return toResponse(cuenta, cliente.getNombreCompleto(), config.getDescripcionRecompensa());
                        }));
    }

    @Transactional(readOnly = true)
    public PageResponse<?> historialMovimientos(Integer clienteId, Pageable pageable) {
        return PageResponse.from(
                movimientoRepository.findByClienteIdOrderByFechaDesc(clienteId, pageable));
    }

    private FidelizacionCuenta obtenerOCrearCuenta(Integer clienteId, FidelizacionConfig config) {
        return cuentaRepository.findByClienteId(clienteId)
                .orElseGet(() -> cuentaRepository.save(FidelizacionCuenta.builder()
                        .clienteId(clienteId)
                        .sellosActuales(0)
                        .sellosNecesarios(config.getSellosNecesarios())
                        .recompensasDisponibles(0)
                        .recompensasCanjeadas(0)
                        .totalSellosAcumulados(0)
                        .build()));
    }

    private FidelizacionCuenta cuentaVacia(Integer clienteId, FidelizacionConfig config) {
        return FidelizacionCuenta.builder()
                .clienteId(clienteId)
                .sellosActuales(0)
                .sellosNecesarios(config.getSellosNecesarios())
                .recompensasDisponibles(0)
                .recompensasCanjeadas(0)
                .totalSellosAcumulados(0)
                .build();
    }

    private FidelizacionConfig obtenerConfig() {
        return configRepository.findById(1)
                .orElseThrow(() -> new ResourceNotFoundException("FidelizacionConfig", 1));
    }

    private void sincronizarCliente(Cliente cliente, FidelizacionCuenta cuenta) {
        cliente.setCantidadSellos(cuenta.getSellosActuales());
        cliente.setRecompensasDisponibles(cuenta.getRecompensasDisponibles());
        cliente.setDescripcionRecompensa(cuenta.getDescripcionRecompensa());
        clienteRepository.save(cliente);
    }

    private FidelizacionCuentaResponse toResponse(FidelizacionCuenta cuenta, String clienteNombre,
                                                    String descripcionRecompensaDefault) {
        int faltantes = Math.max(0, cuenta.getSellosNecesarios() - cuenta.getSellosActuales());
        return FidelizacionCuentaResponse.builder()
                .id(cuenta.getId())
                .clienteId(cuenta.getClienteId())
                .clienteNombre(clienteNombre)
                .sellosActuales(cuenta.getSellosActuales())
                .sellosNecesarios(cuenta.getSellosNecesarios())
                .sellosParaProximaRecompensa(faltantes)
                .recompensasDisponibles(cuenta.getRecompensasDisponibles())
                .recompensasCanjeadas(cuenta.getRecompensasCanjeadas())
                .totalSellosAcumulados(cuenta.getTotalSellosAcumulados())
                .descripcionRecompensa(cuenta.getDescripcionRecompensa() != null
                        ? cuenta.getDescripcionRecompensa() : descripcionRecompensaDefault)
                .ultimoMovimiento(cuenta.getUltimoMovimiento())
                .build();
    }

    // ─── Reglas de fidelización (por servicio / producto / paquete) ────────
    // Que un servicio/producto/paquete sume sello no depende de quien lo
    // vendio, sino de que se vendio -- ver VentaServiceImpl#calificaParaFidelizacion,
    // que consulta estas mismas reglas al completar una venta.

    @Transactional(readOnly = true)
    public List<ReglaFidelizacionResponse> listarReglasServicios() {
        return servicioRepository.findByEstadoTrue().stream()
                .sorted((a, b) -> a.getNombre().compareToIgnoreCase(b.getNombre()))
                .map(s -> ReglaFidelizacionResponse.builder()
                        .id(s.getId()).nombre(s.getNombre()).precio(s.getPrecio())
                        .aplicaFidelizacion(s.getAplicaFidelizacion())
                        .build())
                .toList();
    }

    @Transactional
    public ReglaFidelizacionResponse actualizarReglaServicio(Integer id, ActualizarReglaFidelizacionRequest request) {
        Servicio servicio = servicioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Servicio", id));
        servicio.setAplicaFidelizacion(request.getAplicaFidelizacion());
        servicio = servicioRepository.save(servicio);
        log.info("Regla de fidelización actualizada: servicio {} -> {}", servicio.getNombre(), request.getAplicaFidelizacion());
        return ReglaFidelizacionResponse.builder()
                .id(servicio.getId()).nombre(servicio.getNombre()).precio(servicio.getPrecio())
                .aplicaFidelizacion(servicio.getAplicaFidelizacion())
                .build();
    }

    @Transactional(readOnly = true)
    public List<ReglaFidelizacionResponse> listarReglasProductos() {
        return productoRepository.findByEstadoTrue(org.springframework.data.domain.Pageable.unpaged()).stream()
                .sorted((a, b) -> a.getNombre().compareToIgnoreCase(b.getNombre()))
                .map(p -> ReglaFidelizacionResponse.builder()
                        .id(p.getId()).nombre(p.getNombre()).precio(p.getPrecioVenta())
                        .aplicaFidelizacion(p.getAplicaFidelizacion())
                        .build())
                .toList();
    }

    @Transactional
    public ReglaFidelizacionResponse actualizarReglaProducto(Integer id, ActualizarReglaFidelizacionRequest request) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto", id));
        producto.setAplicaFidelizacion(request.getAplicaFidelizacion());
        producto = productoRepository.save(producto);
        log.info("Regla de fidelización actualizada: producto {} -> {}", producto.getNombre(), request.getAplicaFidelizacion());
        return ReglaFidelizacionResponse.builder()
                .id(producto.getId()).nombre(producto.getNombre()).precio(producto.getPrecioVenta())
                .aplicaFidelizacion(producto.getAplicaFidelizacion())
                .build();
    }

    @Transactional(readOnly = true)
    public List<ReglaFidelizacionResponse> listarReglasPaquetes() {
        return paqueteRepository.findByEstadoTrueOrderByPrecio().stream()
                .map(pk -> ReglaFidelizacionResponse.builder()
                        .id(pk.getId()).nombre(pk.getNombre()).precio(pk.getPrecio())
                        .aplicaFidelizacion(pk.getAplicaFidelizacion())
                        .build())
                .toList();
    }

    @Transactional
    public ReglaFidelizacionResponse actualizarReglaPaquete(Integer id, ActualizarReglaFidelizacionRequest request) {
        Paquete paquete = paqueteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Paquete", id));
        paquete.setAplicaFidelizacion(request.getAplicaFidelizacion());
        paquete = paqueteRepository.save(paquete);
        log.info("Regla de fidelización actualizada: paquete {} -> {}", paquete.getNombre(), request.getAplicaFidelizacion());
        return ReglaFidelizacionResponse.builder()
                .id(paquete.getId()).nombre(paquete.getNombre()).precio(paquete.getPrecio())
                .aplicaFidelizacion(paquete.getAplicaFidelizacion())
                .build();
    }
}
