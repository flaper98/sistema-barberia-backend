package com.barberia.modules.products.service.impl;

import com.barberia.exception.ConflictException;
import com.barberia.exception.ResourceNotFoundException;
import com.barberia.modules.products.dto.CategoriaRequest;
import com.barberia.modules.products.dto.CategoriaResponse;
import com.barberia.modules.products.entity.Categoria;
import com.barberia.modules.products.repository.CategoriaRepository;
import com.barberia.modules.products.repository.ProductoRepository;
import com.barberia.modules.products.service.CategoriaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoriaServiceImpl implements CategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final ProductoRepository productoRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CategoriaResponse> listarActivas() {
        return categoriaRepository.findByEstadoTrueOrderByNombre().stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoriaResponse> listarTodas() {
        return categoriaRepository.findAll().stream()
                .sorted((a, b) -> a.getNombre().compareToIgnoreCase(b.getNombre()))
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public CategoriaResponse crear(CategoriaRequest request) {
        String nombre = request.getNombre().trim();
        if (categoriaRepository.existsByNombreIgnoreCase(nombre)) {
            throw new ConflictException("Ya existe una categoría llamada '" + nombre + "'");
        }
        Categoria categoria = Categoria.builder().nombre(nombre).estado(true).build();
        categoria = categoriaRepository.save(categoria);
        log.info("Categoría de producto creada ID={} nombre={}", categoria.getId(), nombre);
        return toResponse(categoria);
    }

    @Override
    @Transactional
    public CategoriaResponse actualizar(Integer id, CategoriaRequest request) {
        Categoria categoria = findOrThrow(id);
        String nuevoNombre = request.getNombre().trim();

        if (categoriaRepository.existsByNombreIgnoreCaseAndIdNot(nuevoNombre, id)) {
            throw new ConflictException("Ya existe otra categoría llamada '" + nuevoNombre + "'");
        }

        String nombreAnterior = categoria.getNombre();
        categoria.setNombre(nuevoNombre);
        categoria = categoriaRepository.save(categoria);

        if (!nombreAnterior.equalsIgnoreCase(nuevoNombre)) {
            productoRepository.renombrarCategoriaEnProductos(nombreAnterior, nuevoNombre);
            log.info("Categoría renombrada de '{}' a '{}', productos actualizados", nombreAnterior, nuevoNombre);
        }
        return toResponse(categoria);
    }

    @Override
    @Transactional
    public void cambiarEstado(Integer id, Boolean estado) {
        findOrThrow(id);
        categoriaRepository.updateEstado(id, estado);
        log.info("Estado de categoría ID={} cambiado a {}", id, estado);
    }

    private Categoria findOrThrow(Integer id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría", id));
    }

    private CategoriaResponse toResponse(Categoria categoria) {
        return CategoriaResponse.builder()
                .id(categoria.getId())
                .nombre(categoria.getNombre())
                .estado(categoria.getEstado())
                .createdAt(categoria.getCreatedAt())
                .updatedAt(categoria.getUpdatedAt())
                .build();
    }
}
