package com.barberia.modules.customers.repository;

import com.barberia.common.search.TextSearchSpecification;
import com.barberia.modules.customers.entity.Cliente;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

/**
 * Specifications componibles para la busqueda de Clientes. Reemplaza al
 * viejo patron de @Query con muchos ":param IS NULL OR ..." (que ademas
 * tenia un bug real de busqueda -- ver {@link TextSearchSpecification}).
 */
public final class ClienteSpecifications {

    private ClienteSpecifications() {}

    public static Specification<Cliente> activo() {
        return (root, query, cb) -> cb.isTrue(root.get("estado"));
    }

    public static Specification<Cliente> conTexto(String search) {
        // Ademas de nombre/apellido/telefono/correo, matchea contra el ID
        // con ceros a la izquierda -- es el "código de cliente" que se
        // muestra en la columna ID de la tabla (ej. "0004").
        return TextSearchSpecification.matchesConId(search, "id", "nombre", "apellido", "telefono", "correo");
    }

    public static Specification<Cliente> sellosMin(Integer minimo) {
        return (root, query, cb) -> minimo == null
                ? cb.conjunction()
                : cb.greaterThanOrEqualTo(root.get("cantidadSellos"), minimo);
    }

    public static Specification<Cliente> conRecompensa(Boolean conRecompensa) {
        return (root, query, cb) -> {
            if (conRecompensa == null) return cb.conjunction();
            return conRecompensa
                    ? cb.greaterThan(root.get("recompensasDisponibles"), 0)
                    : cb.equal(root.get("recompensasDisponibles"), 0);
        };
    }

    public static Specification<Cliente> nuncaVisito(Boolean nuncaVisito) {
        return (root, query, cb) -> (nuncaVisito == null || !nuncaVisito)
                ? cb.conjunction()
                : cb.isNull(root.get("ultimaVisita"));
    }

    public static Specification<Cliente> inactivoDesde(LocalDate fecha) {
        return (root, query, cb) -> fecha == null
                ? cb.conjunction()
                : cb.and(cb.isNotNull(root.get("ultimaVisita")), cb.lessThanOrEqualTo(root.get("ultimaVisita"), fecha));
    }
}
