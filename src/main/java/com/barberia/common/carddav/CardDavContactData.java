package com.barberia.common.carddav;

/** Datos mínimos de un cliente necesarios para armar su vCard — desacoplado de la entidad Cliente a propósito. */
public record CardDavContactData(
        Integer clienteId,
        String nombre,
        String apellido,
        String telefono,
        String correo
) {
}
