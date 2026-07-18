package com.barberia.modules.products.service;

import com.barberia.modules.products.entity.Categoria;
import com.barberia.modules.products.repository.CategoriaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/** Siembra las categorías que antes eran un enum fijo, para que el ADMIN arranque con algo útil. */
@Slf4j
@Component
@RequiredArgsConstructor
public class CategoriaSeeder implements ApplicationRunner {

    private static final List<String> CATEGORIAS_POR_DEFECTO =
            List.of("Shampoo", "Pomada", "Aceite", "Navaja", "Accesorio", "Otro");

    private final CategoriaRepository repository;

    @Override
    public void run(ApplicationArguments args) {
        if (repository.count() > 0) return;
        log.info("Sembrando categorías de productos por defecto (primer arranque)");
        repository.saveAll(CATEGORIAS_POR_DEFECTO.stream()
                .map(nombre -> Categoria.builder().nombre(nombre).estado(true).build())
                .toList());
    }
}
