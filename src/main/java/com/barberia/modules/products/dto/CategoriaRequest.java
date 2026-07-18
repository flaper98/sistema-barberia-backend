package com.barberia.modules.products.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoriaRequest {

    @NotBlank(message = "El nombre de la categoría es obligatorio")
    @Size(max = 80, message = "El nombre no puede superar 80 caracteres")
    private String nombre;
}
