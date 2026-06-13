package com.barberia.modules.finance.dto;

import com.barberia.modules.finance.entity.CategoriaFinanza;
import com.barberia.modules.finance.entity.TipoFinanza;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class FinanzaRequest {

    @NotNull(message = "El tipo es obligatorio (INGRESO o EGRESO)")
    private TipoFinanza tipo;

    @NotNull(message = "La categoría es obligatoria")
    private CategoriaFinanza categoria;

    @NotBlank(message = "La descripción es obligatoria")
    @Size(max = 255, message = "La descripción no puede superar 255 caracteres")
    private String descripcion;

    @NotNull(message = "El monto es obligatorio")
    @DecimalMin(value = "0.01", message = "El monto debe ser mayor a 0")
    private BigDecimal monto;

    @NotNull(message = "La fecha es obligatoria")
    private LocalDate fecha;

    @Size(max = 100)
    private String referencia;
}
