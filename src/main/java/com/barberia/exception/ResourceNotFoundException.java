package com.barberia.exception;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String resource, Long id) {
        super(resource + " con ID " + id + " no encontrado");
    }

    public ResourceNotFoundException(String resource, Integer id) {
        super(resource + " con ID " + id + " no encontrado");
    }

    public ResourceNotFoundException(String resource, String field, String value) {
        super(resource + " con " + field + " '" + value + "' no encontrado");
    }
}
