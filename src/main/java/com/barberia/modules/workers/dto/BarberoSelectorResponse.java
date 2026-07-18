package com.barberia.modules.workers.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * Versión mínima de un barbero para selectores (ej. elegir quién atendió al
 * momento de vender) — a propósito NO incluye comisión ni total generado,
 * para que cualquier rol autenticado pueda pedirla sin exponer datos
 * financieros de otros barberos que no le corresponda ver.
 */
@Getter
@Builder
public class BarberoSelectorResponse {
    private final Integer id;
    private final String nombre;
    private final String apellido;
    private final String especialidad;
    private final String avatar;
}
