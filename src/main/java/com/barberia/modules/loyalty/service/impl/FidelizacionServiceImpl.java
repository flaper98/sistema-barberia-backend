package com.barberia.modules.loyalty.service.impl;

import com.barberia.common.PageResponse;
import com.barberia.exception.BusinessException;
import com.barberia.exception.ResourceNotFoundException;
import com.barberia.modules.audit.AuditAction;
import com.barberia.modules.audit.AuditPublisher;
import com.barberia.modules.customers.entity.Cliente;
import com.barberia.modules.customers.repository.ClienteRepository;
import com.barberia.modules.loyalty.dto.AgregarSelloRequest;
import com.barberia.modules.loyalty.dto.FidelizacionCuentaResponse;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
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

        int cantidad = config.getSellosPorGanancia() != null ? config.getSellosPorGanancia() : 1;
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
    public PageResponse<FidelizacionCuentaResponse> listarTodasLasCuentas(Pageable pageable) {
        FidelizacionConfig config = obtenerConfig();
        return PageResponse.from(
                clienteRepository.findByEstadoTrue(pageable)
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
}
