package com.barberia.common.search;

import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Estandar de busqueda de texto para toda la aplicacion: separa el texto
 * ingresado en palabras y exige que CADA palabra aparezca en ALGUNO de los
 * campos indicados, sin importar el orden.
 * <p>
 * Evita el bug clasico de comparar el texto completo contra un solo campo a
 * la vez -- por ejemplo, buscar "Flavio Talledo" no encontraba a un cliente
 * con nombre "Flavio" y apellido "Talledo Villacrez", porque el texto
 * completo nunca aparecia entero en NINGUN campo individual (estaba
 * repartido entre dos columnas). Separando por palabras, cada una se busca
 * de forma independiente en cualquiera de los campos.
 * <p>
 * Usar con cualquier repositorio que extienda JpaSpecificationExecutor:
 * <pre>
 *   Specification.where(TextSearchSpecification.matches(search, "nombre", "apellido"))
 * </pre>
 * Los nombres de campo son propiedades simples de la entidad (no soporta
 * rutas anidadas tipo "cliente.nombre").
 */
public final class TextSearchSpecification {

    private TextSearchSpecification() {}

    public static <T> Specification<T> matches(String search, String... campos) {
        return (root, query, cb) -> {
            if (!StringUtils.hasText(search) || campos.length == 0) return cb.conjunction();

            List<String> palabras = Arrays.stream(search.trim().split("\\s+"))
                    .filter(StringUtils::hasText)
                    .toList();
            if (palabras.isEmpty()) return cb.conjunction();

            Predicate[] porPalabra = palabras.stream().map(palabra -> {
                String comodin = "%" + palabra.toLowerCase() + "%";
                Predicate[] porCampo = Arrays.stream(campos)
                        .map(campo -> cb.like(cb.lower(root.<String>get(campo)), comodin))
                        .toArray(Predicate[]::new);
                return cb.or(porCampo);
            }).toArray(Predicate[]::new);

            // Cada palabra escrita tiene que matchear en ALGUN campo (AND
            // entre palabras, OR entre campos por palabra).
            return cb.and(porPalabra);
        };
    }

    // Ancho minimo del ID con ceros a la izquierda, igual que
    // formatId() en el frontend (id.toString().padStart(4, '0')). Usamos
    // GREATEST(4, largo real) como ancho del LPAD para que nunca trunque
    // IDs de 5+ digitos -- MySQL LPAD corta el string si ya es mas largo
    // que el ancho pedido (LPAD('12345', 4, '0') = '1234'), y un ancho fijo
    // grande (ej. 9) tampoco sirve: el monton de ceros de relleno en IDs
    // chicos hace que casi cualquier busqueda de puros ceros matchee de
    // pura casualidad (buscar "0004" encontraba tambien el id=40, 204, etc.
    // porque "000000040" contiene "0004" en algun punto de sus ceros).
    private static final int ANCHO_MIN_PAD_ID = 4;

    /**
     * Igual que {@link #matches}, pero ademas hace que cada palabra pueda
     * matchear contra el ID (con ceros a la izquierda, igual que se muestra
     * en pantalla como "código de cliente" -- ej. "0004") en vez de
     * matchear unicamente los campos de texto indicados.
     */
    public static <T> Specification<T> matchesConId(String search, String idField, String... camposTexto) {
        return (root, query, cb) -> {
            if (!StringUtils.hasText(search)) return cb.conjunction();

            List<String> palabras = Arrays.stream(search.trim().split("\\s+"))
                    .filter(StringUtils::hasText)
                    .toList();
            if (palabras.isEmpty()) return cb.conjunction();

            Expression<String> idComoTexto = cb.function("LPAD", String.class,
                    root.get(idField).as(String.class),
                    cb.function("GREATEST", Integer.class,
                            cb.literal(ANCHO_MIN_PAD_ID),
                            cb.function("CHAR_LENGTH", Integer.class, root.get(idField).as(String.class))),
                    cb.literal("0"));

            Predicate[] porPalabra = palabras.stream().map(palabra -> {
                String comodin = "%" + palabra.toLowerCase() + "%";
                List<Predicate> porCampo = new ArrayList<>();
                for (String campo : camposTexto) {
                    porCampo.add(cb.like(cb.lower(root.<String>get(campo)), comodin));
                }
                porCampo.add(cb.like(idComoTexto, comodin));
                return cb.or(porCampo.toArray(new Predicate[0]));
            }).toArray(Predicate[]::new);

            return cb.and(porPalabra);
        };
    }
}
