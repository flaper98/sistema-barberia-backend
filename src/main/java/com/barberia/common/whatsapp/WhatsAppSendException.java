package com.barberia.common.whatsapp;

/** Cualquier fallo hablando con la API de WhatsApp Business (red, credenciales, plantilla). Nunca debe interrumpir el flujo que la disparó. */
public class WhatsAppSendException extends RuntimeException {
    public WhatsAppSendException(String message) {
        super(message);
    }

    public WhatsAppSendException(String message, Throwable cause) {
        super(message, cause);
    }
}
