package com.barberia.modules.products.service.impl;

import com.barberia.common.PageResponse;
import com.barberia.common.search.TextSearchSpecification;
import com.barberia.exception.ConflictException;
import com.barberia.exception.ResourceNotFoundException;
import com.barberia.modules.products.dto.ProductoRequest;
import com.barberia.modules.products.dto.ProductoResponse;
import com.barberia.modules.products.entity.Producto;
import com.barberia.modules.products.mapper.ProductoMapper;
import com.barberia.modules.products.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductoServiceImpl {

    private final ProductoRepository productoRepository;
    private final ProductoMapper productoMapper;

    @Transactional(readOnly = true)
    public PageResponse<ProductoResponse> listar(String search, Pageable pageable) {
        Specification<Producto> spec = Specification.<Producto>where((root, query, cb) -> cb.isTrue(root.get("estado")))
                .and(TextSearchSpecification.matches(search, "nombre"));
        Page<Producto> page = productoRepository.findAll(spec, pageable);
        return PageResponse.from(page.map(productoMapper::toResponse));
    }

    @Transactional(readOnly = true)
    public ProductoResponse obtenerPorId(Integer id) {
        return productoMapper.toResponse(findOrThrow(id));
    }

    @Transactional(readOnly = true)
    public PageResponse<ProductoResponse> listarConStockBajo(Pageable pageable) {
        return PageResponse.from(
                productoRepository.findProductosConStockBajo(pageable).map(productoMapper::toResponse));
    }

    @Transactional
    public ProductoResponse crear(ProductoRequest request) {
        if (StringUtils.hasText(request.getCodigoBarras())
                && productoRepository.existsByCodigoBarras(request.getCodigoBarras())) {
            throw new ConflictException("Ya existe un producto con el código de barras: "
                    + request.getCodigoBarras());
        }

        Producto producto = Producto.builder()
                .nombre(request.getNombre())
                .categoria(request.getCategoria())
                .precioCompra(request.getPrecioCompra())
                .precioVenta(request.getPrecioVenta())
                .stockActual(request.getStockActual())
                .stockMinimo(request.getStockMinimo())
                .descripcion(request.getDescripcion())
                .codigoBarras(request.getCodigoBarras())
                .estado(true)
                .build();

        producto = productoRepository.save(producto);
        log.info("Producto creado ID={}", producto.getId());
        return productoMapper.toResponse(producto);
    }

    @Transactional
    public ProductoResponse actualizar(Integer id, ProductoRequest request) {
        Producto producto = findOrThrow(id);

        if (StringUtils.hasText(request.getCodigoBarras())
                && productoRepository.existsByCodigoBarrasAndIdNot(request.getCodigoBarras(), id)) {
            throw new ConflictException("Ya existe otro producto con el código de barras: "
                    + request.getCodigoBarras());
        }

        producto.setNombre(request.getNombre());
        producto.setCategoria(request.getCategoria());
        producto.setPrecioCompra(request.getPrecioCompra());
        producto.setPrecioVenta(request.getPrecioVenta());
        producto.setStockActual(request.getStockActual());
        producto.setStockMinimo(request.getStockMinimo());
        producto.setDescripcion(request.getDescripcion());
        producto.setCodigoBarras(request.getCodigoBarras());

        producto = productoRepository.save(producto);
        log.info("Producto actualizado ID={}", producto.getId());
        return productoMapper.toResponse(producto);
    }

    @Transactional
    public void cambiarEstado(Integer id, Boolean estado) {
        findOrThrow(id);
        productoRepository.updateEstado(id, estado);
        log.info("Estado de producto ID={} cambiado a {}", id, estado);
    }

    public Producto findOrThrow(Integer id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto", id));
    }
}
