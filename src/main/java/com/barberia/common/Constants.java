package com.barberia.common;

public final class Constants {

    private Constants() {}

    public static final String API_BASE = "/api";
    public static final String AUTH_BASE = API_BASE + "/auth";

    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final int MAX_PAGE_SIZE = 100;

    public static final String ROLE_ADMIN     = "ADMIN";
    public static final String ROLE_BARBER    = "BARBER";
    public static final String ROLE_CASHIER   = "CASHIER";
    public static final String ROLE_RECEPTION = "RECEPTION";

    public static final String HAS_ROLE_ADMIN     = "hasRole('ADMIN')";
    public static final String HAS_ROLE_BARBER    = "hasRole('BARBER')";
    public static final String HAS_ROLE_CASHIER   = "hasRole('CASHIER')";
    public static final String HAS_ROLE_RECEPTION = "hasRole('RECEPTION')";
    public static final String HAS_ANY_ROLE_ADMIN_CASHIER =
            "hasAnyRole('ADMIN','CASHIER')";
    public static final String HAS_ANY_ROLE_ADMIN_RECEPTION =
            "hasAnyRole('ADMIN','RECEPTION')";

    public static final String BEARER_PREFIX = "Bearer ";
    public static final String AUTH_HEADER   = "Authorization";

    public static final int STAMP_REQUIRED_DEFAULT = 5;
}
