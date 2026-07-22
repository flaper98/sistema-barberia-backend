package com.barberia.modules.sales.entity;

public enum MetodoPago {
    EFECTIVO, TARJETA, YAPE, PLIN, TRANSFERENCIA,
    // Valor calculado, no elegible directamente -- se asigna solo cuando una
    // venta se paga con mas de un metodo a la vez (ver venta_pagos / Venta.pagos).
    MIXTO
}
