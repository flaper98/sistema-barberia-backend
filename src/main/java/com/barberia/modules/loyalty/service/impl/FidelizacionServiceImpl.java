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
import com.barberia.modules.loyalty.entity.FidelizacionCuenta;
import com.barberia.modules.loyalty.entity.FidelizacionMovimiento;
import com.barberia.modules.loyalty.entity.Recompensa;
import com.barberia.modules.loyalty.entity.TipoMovimientoFidelizacion;
import com.barberia.modules.loyalty.repository.FidelizacionCuentaRepository;
import com.barberia.modules.loyalty.repository.FidelizacionMovimientoRepository;
import com.barberia.modules.loyalty.repository.RecompensaRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.StoredProcedureQuery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FidelizacionServiceImpl {

    private final FidelizacionCuentaRepository cuentaRepository;
    private final FidelizacionMovimientoRepository movimientoRepository;
    private final RecompensaRepository recompensaRepository;
    private final ClienteRepository clienteRepository;
    private final EntityManager em;
    private final AuditPublisher auditPublisher;

    @Transactional(readOnly = true)
    public FidelizacionCuentaResponse obtenerCuenta(Integer clienteId) {
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", clienteId));

        FidelizacionCuenta cuenta = cuentaRepository.findByClienteId(clienteId)
                .orElseGet(() -> FidelizacionCuenta.builder()
                        .clienteId(clienteId)
                        .sellosActuales(0)
                        .sellosNecesarios(5)
                        .recompensasDisponibles(0)
                        .recompensasCanjeadas(0)
                        .totalSellosAcumulados(0)
                        .build());

        int faltantes = Math.max(0, cuenta.getSellosNecesarios() - cuenta.getSellosActuales());

        return FidelizacionCuentaResponse.builder()
                .id(cuenta.getId())
                .clienteId(clienteId)
                .clienteNombre(cliente.getNombreCompleto())
                .sellosActuales(cuenta.getSellosActuales())
                .sellosNecesarios(cuenta.getSellosNecesarios())
                .sellosParaProximaRecompensa(faltantes)
                .recompensasDisponibles(cuenta.getRecompensasDisponibles())
                .recompensasCanjeadas(cuenta.getRecompensasCanjeadas())
                .totalSellosAcumulados(cuenta.getTotalSellosAcumulados())
                .ultimoMovimiento(cuenta.getUltimoMovimiento())
                .build();
    }

    @Transactional
    public SelloResponse agregarSello(Integer clienteId, AgregarSelloRequest request) {
        clienteRepository.findById(clienteId)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", clienteId));

        StoredProcedureQuery sp = em.createStoredProcedureQuery("sp_agregar_sello");
        sp.registerStoredProcedureParameter("p_cliente_id",    Integer.class, ParameterMode.IN);
        sp.registerStoredProcedureParameter("p_venta_id",      Integer.class, ParameterMode.IN);
        sp.registerStoredProcedureParameter("p_motivo",        String.class,  ParameterMode.IN);
        sp.registerStoredProcedureParameter("p_nueva_recompensa", Byte.class, ParameterMode.OUT);
        sp.registerStoredProcedureParameter("p_error",         String.class,  ParameterMode.OUT);

        sp.setParameter("p_cliente_id", clienteId);
        sp.setParameter("p_venta_id", request.getVentaId());
        sp.setParameter("p_motivo", request.getMotivo());

        sp.execute();

        String error = (String) sp.getOutputParameterValue("p_error");
        if (error != null) {
            throw new BusinessException("Error al agregar sello: " + error);
        }

        Object rawRecompensa = sp.getOutputParameterValue("p_nueva_recompensa");
        boolean nuevaRecompensa = rawRecompensa != null && ((Number) rawRecompensa).intValue() == 1;

        FidelizacionCuenta cuenta = cuentaRepository.findByClienteId(clienteId)
                .orElseThrow();

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
        clienteRepository.findById(clienteId)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", clienteId));

        StoredProcedureQuery sp = em.createStoredProcedureQuery("sp_canjear_recompensa");
        sp.registerStoredProcedureParameter("p_cliente_id", Integer.class, ParameterMode.IN);
        sp.registerStoredProcedureParameter("p_motivo",     String.class,  ParameterMode.IN);
        sp.registerStoredProcedureParameter("p_error",      String.class,  ParameterMode.OUT);

        sp.setParameter("p_cliente_id", clienteId);
        sp.setParameter("p_motivo", motivo);
        sp.execute();

        String error = (String) sp.getOutputParameterValue("p_error");
        if (error != null) {
            throw new BusinessException(error);
        }

        FidelizacionCuenta cuenta = cuentaRepository.findByClienteId(clienteId).orElseThrow();

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
        return PageResponse.from(
                cuentaRepository.findByRecompensasDisponiblesGreaterThan(0, pageable)
                        .map(cuenta -> {
                            Cliente c = clienteRepository.findById(cuenta.getClienteId()).orElse(null);
                            int faltantes = Math.max(0, cuenta.getSellosNecesarios() - cuenta.getSellosActuales());
                            return FidelizacionCuentaResponse.builder()
                                    .id(cuenta.getId())
                                    .clienteId(cuenta.getClienteId())
                                    .clienteNombre(c != null ? c.getNombreCompleto() : "Cliente")
                                    .sellosActuales(cuenta.getSellosActuales())
                                    .sellosNecesarios(cuenta.getSellosNecesarios())
                                    .sellosParaProximaRecompensa(faltantes)
                                    .recompensasDisponibles(cuenta.getRecompensasDisponibles())
                                    .recompensasCanjeadas(cuenta.getRecompensasCanjeadas())
                                    .totalSellosAcumulados(cuenta.getTotalSellosAcumulados())
                                    .ultimoMovimiento(cuenta.getUltimoMovimiento())
                                    .build();
                        }));
    }

    @Transactional(readOnly = true)
    public PageResponse<?> historialMovimientos(Integer clienteId, Pageable pageable) {
        return PageResponse.from(
                movimientoRepository.findByClienteIdOrderByFechaDesc(clienteId, pageable));
    }
}
