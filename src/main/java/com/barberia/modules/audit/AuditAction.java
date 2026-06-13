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
    SALE_CANCEL,

    // Inventario
    STOCK_MOVEMENT,

    // Fidelización
    STAMP_ADD,
    REWARD_REDEEM,

    // Configuración
    CONFIG_UPDATE,
    LOYALTY_CONFIG_UPDATE
}
