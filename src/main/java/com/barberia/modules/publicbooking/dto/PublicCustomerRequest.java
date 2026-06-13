package com.barberia.modules.publicbooking.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PublicCustomerRequest {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑüÜ ]+$", message = "El nombre solo puede contener letras y espacios")
    private String nombre;

    @NotBlank(message = "El apellido es obligatorio")
    @Size(min = 2, max = 100, message = "El apellido debe tener entre 2 y 100 caracteres")
    @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑüÜ ]+$", message = "El apellido solo puede contener letras y espacios")
    private String apellido;

    @NotBlank(message = "El teléfono es obligatorio")
    @Pattern(regexp = "^\\d{9,15}$", message = "El teléfono debe tener entre 9 y 15 dígitos")
    private String telefono;

    @Email(message = "El correo no es válido")
    @Size(max = 150)
    private String correo;
}
