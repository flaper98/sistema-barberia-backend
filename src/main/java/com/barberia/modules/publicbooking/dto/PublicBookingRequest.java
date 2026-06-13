package com.barberia.modules.publicbooking.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Getter
@Setter
public class PublicBookingRequest {

    @Valid
    @NotNull(message = "Los datos del cliente son obligatorios")
    private PublicCustomerRequest customer;

    @NotNull(message = "El ID del barbero es obligatorio")
    private Integer workerId;

    @NotEmpty(message = "Debes seleccionar al menos un servicio")
    @Size(max = 10, message = "No se pueden seleccionar más de 10 servicios")
    private List<Integer> serviceIds;

    @NotNull(message = "La fecha es obligatoria")
    @FutureOrPresent(message = "La fecha no puede ser en el pasado")
    private LocalDate appointmentDate;

    @NotNull(message = "La hora de inicio es obligatoria")
    private LocalTime startTime;

    @Size(max = 500, message = "Las notas no pueden superar los 500 caracteres")
    private String notes;
}
