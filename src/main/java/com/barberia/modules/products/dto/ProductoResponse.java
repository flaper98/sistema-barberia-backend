package com.barberia.modules.products.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class ProductoResponse {

    private final Integer id;
    private final String nombre;
    private final String categoria;
    private final BigDecimal precioCompra;
    private final BigDecimal precioVenta;
    private final Integer stockActual;
    private final Integer stockMinimo;
    private final Integer unidadesFaltantes;
    private final Boolean stockBajo;
    private final String descripcion;
    private final String codigoBarras;
    private final Boolean estado;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
}
