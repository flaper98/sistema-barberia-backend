package com.barberia.modules.inventory.entity;

// CONSUMO: el barbero consume un producto del stock (ej. una gaseosa) --
// descuenta el inventario igual que SALIDA, pero ademas queda ligado a un
// DescuentoConsumo que se la resta de su comision (ver ComisionServiceImpl).
public enum TipoMovimiento {
    ENTRADA, SALIDA, AJUSTE, CONSUMO
}
