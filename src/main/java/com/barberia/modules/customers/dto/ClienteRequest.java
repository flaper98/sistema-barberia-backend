package com.barberia.modules.customers.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Schema(description = "Datos para crear o actualizar un cliente")
public class ClienteRequest {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre no puede superar 100 caracteres")
    @Schema(example = "Carlos")
    private String nombre;

    @NotBlank(message = "El apellido es obligatorio")
    @Size(max = 100, message = "El apellido no puede superar 100 caracteres")
    @Schema(example = "Mendoza")
    private String apellido;

    @Email(message = "El correo no tiene un formato válido")
    @Size(max = 150, message = "El correo no puede superar 150 caracteres")
    @Schema(example = "carlos.m@gmail.com")
    private String correo;

    @NotBlank(message = "El teléfono es obligatorio")
    @Pattern(regexp = "^[0-9\\-\\+\\s()]{7,20}$", message = "El teléfono no tiene un formato válido")
    @Schema(example = "999-111-001")
    private String telefono;

    @Past(message = "La fecha de nacimiento debe ser una fecha pasada")
    @Schema(example = "1990-03-15")
    private LocalDate fechaNacimiento;

    @Size(max = 1000, message = "Las notas no pueden superar 1000 caracteres")
    private String notas;
}
