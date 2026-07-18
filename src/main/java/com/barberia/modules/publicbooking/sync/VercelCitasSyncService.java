package com.barberia.modules.publicbooking.sync;

import com.barberia.modules.appointments.entity.Cita;
import com.barberia.modules.appointments.entity.EstadoCita;
import com.barberia.modules.appointments.repository.CitaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.util.List;
import java.util.Map;

/**
 * Trae las reservas registradas en el formulario público de barberia-arabes-web
 * (que escriben directo a una tabla `citas` en un Postgres separado, hoy en
 * Vercel) y las crea como {@link Cita} reales en la base local -- así aparecen
 * en el panel admin sin que el sitio público ni Vercel necesiten cambios.
 *
 * Ese formulario no pide barbero ni servicio (es un "dejanos tus datos y te
 * contactamos"), así que las citas importadas quedan con
 * {@code barberoId = null}, {@code barberoNombre = "Sin asignar"} y
 * {@code estado = PENDIENTE} -- el staff las completa con el flujo normal de
 * edición de citas una vez que contacta al cliente.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VercelCitasSyncService {

    private final CitaRepository citaRepository;
    private final JdbcTemplate vercelCitasJdbcTemplate;

    @Value("${app.sync.vercel-database-url:}")
    private String vercelDatabaseUrl;

    @Scheduled(fixedDelayString = "${app.sync.fixed-delay-ms:120000}")
    public void sincronizarProgramado() {
        sincronizar();
    }

    @Transactional
    public int sincronizar() {
        if (!StringUtils.hasText(vercelDatabaseUrl)) {
            log.debug("Sync de citas públicas inactivo: VERCEL_CITAS_DATABASE_URL no configurada");
            return 0;
        }

        List<Map<String, Object>> pendientes;
        try {
            pendientes = vercelCitasJdbcTemplate.queryForList(
                    "SELECT id, cliente_nombre, barbero_nombre, telefono, email, fecha, hora, " +
                    "notas, appointment_code, appointment_source " +
                    "FROM citas WHERE synced_at IS NULL AND appointment_source = 'PUBLIC_WEB' ORDER BY id");
        } catch (Exception e) {
            Throwable causaRaiz = org.springframework.core.NestedExceptionUtils.getMostSpecificCause(e);
            log.warn("No se pudo consultar el Postgres de citas públicas: {}", causaRaiz.getMessage());
            return 0;
        }

        if (pendientes.isEmpty()) {
            return 0;
        }

        int importadas = 0;
        for (Map<String, Object> fila : pendientes) {
            try {
                importarFila(fila);
                importadas++;
            } catch (Exception e) {
                log.error("Error importando cita pública id={}: {}", fila.get("id"), e.getMessage(), e);
            }
        }

        log.info("Sync de citas públicas: {} de {} importadas", importadas, pendientes.size());
        return importadas;
    }

    private void importarFila(Map<String, Object> fila) {
        Long postgresId = ((Number) fila.get("id")).longValue();
        String clienteNombre = (String) fila.get("cliente_nombre");
        String barberoNombre = (String) fila.get("barbero_nombre");
        String telefono = (String) fila.get("telefono");
        String email = (String) fila.get("email");
        String notasOriginales = (String) fila.get("notas");
        String appointmentCode = (String) fila.get("appointment_code");
        String appointmentSource = (String) fila.get("appointment_source");

        java.time.LocalDate fecha = ((Date) fila.get("fecha")).toLocalDate();
        java.time.LocalTime hora = ((Time) fila.get("hora")).toLocalTime();

        String notas = String.join("\n",
                java.util.stream.Stream.of(
                        StringUtils.hasText(telefono) ? "Tel: " + telefono : null,
                        StringUtils.hasText(email) ? "Email: " + email : null,
                        notasOriginales)
                .filter(StringUtils::hasText)
                .toList());

        Cita cita = Cita.builder()
                .clienteNombre(clienteNombre)
                .barberoId(null)
                .barberoNombre(StringUtils.hasText(barberoNombre) ? barberoNombre : "Sin asignar")
                .fecha(fecha)
                .hora(hora)
                .estado(EstadoCita.PENDIENTE)
                .totalEstimado(BigDecimal.ZERO)
                .notas(notas.isBlank() ? null : notas)
                .appointmentCode(appointmentCode)
                .appointmentSource(StringUtils.hasText(appointmentSource) ? appointmentSource : "PUBLIC_WEB")
                .build();

        citaRepository.save(cita);

        vercelCitasJdbcTemplate.update("UPDATE citas SET synced_at = NOW() WHERE id = ?", postgresId);
        log.info("Cita pública importada: postgresId={} -> citaLocalId={}", postgresId, cita.getId());
    }
}
