package com.barberia.modules.products.dto;

import com.barberia.modules.products.entity.CategoriaProducto;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ProductoRequest {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 150, message = "El nombre no puede superar 150 caracteres")
    private String nombre;

    @NotNull(message = "La categoría es obligatoria")
    private CategoriaProducto categoria;

    @NotNull(message = "El precio de compra es obligatorio")
    @DecimalMin(value = "0.00", message = "El precio de compra no puede ser negativo")
    private BigDecimal precioCompra;

    @NotNull(message = "El precio de venta es obligatorio")
    @DecimalMin(value = "0.00", message = "El precio de venta no puede ser negativo")
    private BigDecimal precioVenta;

    @NotNull(message = "El stock mínimo es obligatorio")
    @Min(value = 0, message = "El stock mínimo no puede ser negativo")
    private Integer stockMinimo;

    @Size(max = 2000)
    private String descripcion;

    @Size(max = 50, message = "El código de barras no puede superar 50 caracteres")
    private String codigoBarras;
}
