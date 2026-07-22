package com.barberia.modules.finance.entity;

public enum CategoriaFinanza {
    // COMISION: egreso automatico al marcar un corte de comision como
    // pagado (ver ComisionServiceImpl.marcarComoPagado) -- reemplaza el uso
    // manual de SALARIO para pagos a barberos, ahora que cobran por comision.
    VENTA, ALQUILER, SALARIO, SUMINISTROS, SERVICIOS, MANTENIMIENTO, COMISION, OTRO
}
