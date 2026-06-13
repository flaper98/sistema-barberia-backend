package com.barberia.modules.appointments.repository;

import com.barberia.modules.appointments.entity.Cita;
import com.barberia.modules.appointments.entity.EstadoCita;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface CitaRepository extends JpaRepository<Cita, Integer> {

    Page<Cita> findByFechaOrderByHoraAsc(LocalDate fecha, Pageable pageable);

    Page<Cita> findByBarberoIdOrderByFechaAscHoraAsc(Integer barberoId, Pageable pageable);

    Page<Cita> findByClienteIdOrderByFechaDescHoraDesc(Integer clienteId, Pageable pageable);

    @Query("SELECT c FROM Cita c WHERE c.fecha = :fecha AND c.barberoId = :barberoId " +
           "AND c.estado NOT IN ('CANCELADA', 'NO_ASISTIO') " +
           "AND c.hora = :hora")
    List<Cita> findCitasSolapadas(
            @Param("fecha") LocalDate fecha,
            @Param("barberoId") Integer barberoId,
            @Param("hora") LocalTime hora);

    @Query("SELECT c FROM Cita c WHERE c.fecha = :fecha AND c.barberoId = :barberoId " +
           "AND c.estado NOT IN ('CANCELADA', 'NO_ASISTIO') " +
           "AND c.hora = :hora AND c.id <> :excludeId")
    List<Cita> findCitasSolapadasExcluyendo(
            @Param("fecha") LocalDate fecha,
            @Param("barberoId") Integer barberoId,
            @Param("hora") LocalTime hora,
            @Param("excludeId") Integer excludeId);

    @Query("SELECT c FROM Cita c WHERE " +
           "(:fecha IS NULL OR c.fecha = :fecha) AND " +
           "(:estado IS NULL OR c.estado = :estado) " +
           "ORDER BY c.fecha ASC, c.hora ASC")
    Page<Cita> findWithFilters(
            @Param("fecha") LocalDate fecha,
            @Param("estado") EstadoCita estado,
            Pageable pageable);

    @Query("SELECT c FROM Cita c LEFT JOIN FETCH c.servicios " +
           "WHERE c.fecha = :fecha AND c.barberoId = :barberoId " +
           "AND c.estado NOT IN ('CANCELADA', 'NO_ASISTIO')")
    List<Cita> findActivasByBarberoIdAndFecha(
            @Param("barberoId") Integer barberoId,
            @Param("fecha") LocalDate fecha);

    java.util.Optional<Cita> findByAppointmentCode(String appointmentCode);
}
