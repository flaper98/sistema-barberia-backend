package com.barberia.modules.customers.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * Versión mínima de un cliente para selectores (ej. adjuntar uno a una
 * solicitud de venta) -- a propósito NO incluye teléfono, correo ni sellos
 * de fidelidad, para que cualquier rol autenticado pueda pedirla sin
 * exponer datos de contacto/fidelización que no le corresponda ver (ver
 * ClienteController#listarParaSelector, mismo criterio que
 * BarberoSelectorResponse).
 */
@Getter
@Builder
public class ClienteSelectorResponse {
    private final Integer id;
    private final String nombre;
    private final String apellido;
}
