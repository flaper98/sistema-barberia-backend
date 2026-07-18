package com.barberia.common.carddav;

/** Cualquier fallo hablando con un servidor CardDAV (red, credenciales, protocolo). Nunca debe interrumpir el flujo que la disparó. */
public class CardDavSyncException extends RuntimeException {
    public CardDavSyncException(String message) {
        super(message);
    }

    public CardDavSyncException(String message, Throwable cause) {
        super(message, cause);
    }
}
