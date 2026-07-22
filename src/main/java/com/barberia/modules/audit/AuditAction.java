package com.barberia.modules.audit;

public enum AuditAction {
    // Auth
    LOGIN_OK,
    LOGIN_FAIL,
    LOGOUT,

    // Usuarios
    USER_CREATE,
    USER_UPDATE,
    USER_STATUS_CHANGE,
    USER_DELETE,
    ROLE_CHANGE,

    // Clientes
    CLIENT_CREATE,
    CLIENT_UPDATE,
    CLIENT_DELETE,

    // Ventas
    SALE_CREATE,
    SALE_UPDATE,
    SALE_CANCEL,
    SALE_DELETE,

    // Inventario
    STOCK_MOVEMENT,

    // Fidelización
    STAMP_ADD,
    STAMP_REMOVE,
    REWARD_REDEEM,

    // Configuración
    CONFIG_UPDATE,
    LOYALTY_CONFIG_UPDATE,

    // Comisiones de barberos
    TIP_ADD,
    CONSUMPTION_DISCOUNT_ADD,
    COMMISION_CUT_CREATE,
    COMMISION_CUT_PAY
}
