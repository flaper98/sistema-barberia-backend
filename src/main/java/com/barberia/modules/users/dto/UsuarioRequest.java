package com.barberia.modules.users.dto;

import com.barberia.modules.users.entity.Rol;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Datos para crear o actualizar un usuario del sistema")
public class UsuarioRequest {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre no puede superar 100 caracteres")
    @Schema(example = "Juan")
    private String nombre;

    @NotBlank(message = "El apellido es obligatorio")
    @Size(max = 100, message = "El apellido no puede superar 100 caracteres")
    @Schema(example = "Pérez")
    private String apellido;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email no tiene un formato válido")
    @Size(max = 150, message = "El email no puede superar 150 caracteres")
    @Schema(example = "juan.perez@barberia.com")
    private String email;

    @Size(min = 6, max = 100, message = "La contraseña debe tener entre 6 y 100 caracteres")
    @Schema(description = "Obligatorio al crear, opcional al actualizar", example = "secreto123")
    private String password;

    @NotNull(message = "El rol es obligatorio")
    @Schema(example = "CASHIER", allowableValues = {"ADMIN", "BARBER", "CASHIER", "RECEPTION"})
    private Rol rol;

    @Size(max = 500, message = "La URL del avatar no puede superar 500 caracteres")
    private String avatar;
}
