package com.barberia.modules.inventory.entity;

// CONSUMO: el barbero consume un producto del stock (ej. una gaseosa) --
// descuenta el inventario igual que SALIDA, pero ademas queda ligado a un
// DescuentoConsumo que se la resta de su comision (ver ComisionServiceImpl).
// CORTESIA: un producto que se entrega gratis al cliente como parte de un
// servicio (ej. una cerveza con un corte premium) -- descuenta el
// inventario igual que SALIDA/CONSUMO, pero no afecta la comision de nadie
// ni cobra nada (ver Servicio.cortesias / VentaServiceImpl.procesarCortesias).
public enum TipoMovimiento {
    ENTRADA, SALIDA, AJUSTE, CONSUMO, CORTESIA
}
