package com.barberia.modules.barbershop.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ConfiguracionBarberiaRequest {

    @NotBlank(message = "El nombre de la barbería es obligatorio")
    @Size(max = 150, message = "El nombre no puede superar 150 caracteres")
    private String nombre;

    @Size(max = 255)
    private String direccion;

    @Pattern(regexp = "^[0-9\\-\\+\\s()]{7,20}$", message = "El teléfono no tiene un formato válido")
    private String telefono;

    @Email(message = "El email no tiene un formato válido")
    @Size(max = 150)
    private String email;

    @Size(max = 20)
    private String ruc;

    @Size(max = 150)
    private String horario;

    @Size(max = 500)
    private String logoUrl;

    @Size(max = 500, message = "El mensaje no puede superar 500 caracteres")
    private String mensajeBienvenidaWhatsapp;

    @Size(max = 500, message = "El mensaje no puede superar 500 caracteres")
    private String mensajeRecordatorioWhatsapp;
}
