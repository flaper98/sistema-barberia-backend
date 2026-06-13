package com.barberia.modules.appointments.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Getter
@Setter
public class CitaRequest {

    private Integer clienteId;

    @NotNull(message = "El ID del barbero es obligatorio")
    private Integer barberoId;

    @NotNull(message = "La fecha es obligatoria")
    @FutureOrPresent(message = "La fecha no puede ser en el pasado")
    private LocalDate fecha;

    @NotNull(message = "La hora es obligatoria")
    private LocalTime hora;

    @DecimalMin(value = "0.00", message = "El total estimado no puede ser negativo")
    private BigDecimal totalEstimado;

    @Size(max = 1000)
    private String notas;

    private List<CitaServicioItemRequest> servicios;

    @Getter
    @Setter
    public static class CitaServicioItemRequest {
        private Integer servicioId;

        @NotBlank(message = "El nombre del servicio es obligatorio")
        private String servicioNombre;

        @NotNull
        @DecimalMin(value = "0.00")
        private BigDecimal precio;

        @NotNull
        @Min(value = 1)
        private Integer duracionMinutos;
    }
}
