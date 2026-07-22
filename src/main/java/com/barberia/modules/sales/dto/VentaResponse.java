package com.barberia.modules.sales.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder(toBuilder = true)
public class VentaResponse {

    private final Integer id;
    private final Integer clienteId;
    private final String clienteNombre;
    private final Integer barberoId;
    private final String barberoNombre;
    private final String usuarioRegistroNombre;
    private final String tipoVenta;
    private final String metodoPago;
    private final BigDecimal subtotal;
    private final BigDecimal descuento;
    private final BigDecimal total;
    private final String estado;
    private final Integer citaId;
    private final String notas;
    private final LocalDateTime fecha;
    private final List<VentaItemResponse> items;
    private final List<VentaPagoResponse> pagos;

    // Solo se completa en "Mis Ventas" (GET /sales/by-worker/{id}): cuanto
    // le corresponde a ESE barbero de ESA venta, pesado por la tasa de cada
    // item vendido (ver ComisionServiceImpl). Null en el resto de los
    // listados -- ahi no aplica (no es la vista de un barbero puntual) --
    // y tambien null si la venta no esta COMPLETADA (todavia no genera
    // comision real).
    private final BigDecimal comisionCalculada;

    @Getter
    @Builder
    public static class VentaItemResponse {
        private final Integer id;
        private final String tipo;
        private final Integer itemId;
        private final String nombre;
        private final BigDecimal precio;
        private final Integer cantidad;
        private final BigDecimal subtotal;
    }

    @Getter
    @Builder
    public static class VentaPagoResponse {
        private final Integer id;
        private final String metodoPago;
        private final BigDecimal monto;
    }
}
