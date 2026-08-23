package com.barberia.modules.services.service.impl;

import com.barberia.common.PageResponse;
import com.barberia.common.search.TextSearchSpecification;
import com.barberia.exception.BusinessException;
import com.barberia.exception.ResourceNotFoundException;
import com.barberia.modules.products.entity.Producto;
import com.barberia.modules.products.repository.ProductoRepository;
import com.barberia.modules.services.dto.ServicioCortesiaDto;
import com.barberia.modules.services.dto.ServicioRequest;
import com.barberia.modules.services.dto.ServicioResponse;
import com.barberia.modules.services.entity.Servicio;
import com.barberia.modules.services.entity.ServicioCortesia;
import com.barberia.modules.services.mapper.ServicioMapper;
import com.barberia.modules.services.repository.ServicioCortesiaRepository;
import com.barberia.modules.services.repository.ServicioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ServicioServiceImpl {

    private final ServicioRepository servicioRepository;
    private final ServicioCortesiaRepository cortesiaRepository;
    private final ProductoRepository productoRepository;
    private final ServicioMapper servicioMapper;

    @Transactional(readOnly = true)
    public PageResponse<ServicioResponse> listar(String search, Pageable pageable) {
        Specification<Servicio> spec = Specification.<Servicio>where((root, query, cb) -> cb.isTrue(root.get("estado")))
                .and(TextSearchSpecification.matches(search, "nombre"));
        Page<Servicio> page = servicioRepository.findAll(spec, pageable);
        return PageResponse.from(page.map(s -> conCortesias(servicioMapper.toResponse(s), s.getId())));
    }

    @Transactional(readOnly = true)
    public ServicioResponse obtenerPorId(Integer id) {
        Servicio servicio = findOrThrow(id);
        return conCortesias(servicioMapper.toResponse(servicio), servicio.getId());
    }

    @Transactional
    public ServicioResponse crear(ServicioRequest request) {
        Servicio servicio = Servicio.builder()
                .nombre(request.getNombre())
                .categoria(request.getCategoria())
                .precio(request.getPrecio())
                .precioVariable(Boolean.TRUE.equals(request.getPrecioVariable()))
                .duracionMinutos(request.getDuracionMinutos())
                .descripcion(request.getDescripcion())
                .estado(true)
                .build();

        servicio = servicioRepository.save(servicio);
        List<ServicioCortesiaDto> cortesias = guardarCortesias(servicio.getId(), request.getCortesias());
        log.info("Servicio creado ID={}", servicio.getId());
        return conCortesias(servicioMapper.toResponse(servicio), cortesias);
    }

    @Transactional
    public ServicioResponse actualizar(Integer id, ServicioRequest request) {
        Servicio servicio = findOrThrow(id);

        servicio.setNombre(request.getNombre());
        servicio.setCategoria(request.getCategoria());
        servicio.setPrecio(request.getPrecio());
        servicio.setPrecioVariable(Boolean.TRUE.equals(request.getPrecioVariable()));
        servicio.setDuracionMinutos(request.getDuracionMinutos());
        servicio.setDescripcion(request.getDescripcion());

        servicio = servicioRepository.save(servicio);
        List<ServicioCortesiaDto> cortesias = guardarCortesias(servicio.getId(), request.getCortesias());
        log.info("Servicio actualizado ID={}", servicio.getId());
        return conCortesias(servicioMapper.toResponse(servicio), cortesias);
    }

    // Reemplaza por completo la config de cortesias de este servicio (mas
    // simple que diffear altas/bajas/cambios de cantidad, y esta pantalla no
    // tiene tanto volumen como para que importe el costo de borrar+reinsertar).
    private List<ServicioCortesiaDto> guardarCortesias(Integer servicioId, List<ServicioCortesiaDto> pedidas) {
        cortesiaRepository.deleteByServicioId(servicioId);
        if (pedidas == null || pedidas.isEmpty()) return List.of();

        return pedidas.stream().map(dto -> {
            Producto producto = productoRepository.findById(dto.getProductoId())
                    .orElseThrow(() -> new ResourceNotFoundException("Producto", dto.getProductoId()));
            if (!Boolean.TRUE.equals(producto.getEstado())) {
                throw new BusinessException("El producto de cortesía '" + producto.getNombre() + "' no está activo");
            }
            cortesiaRepository.save(ServicioCortesia.builder()
                    .servicioId(servicioId)
                    .productoId(producto.getId())
                    .cantidad(dto.getCantidad())
                    .build());
            return ServicioCortesiaDto.builder()
                    .productoId(producto.getId())
                    .productoNombre(producto.getNombre())
                    .cantidad(dto.getCantidad())
                    .build();
        }).toList();
    }

    private ServicioResponse conCortesias(ServicioResponse response, Integer servicioId) {
        List<ServicioCortesiaDto> cortesias = cortesiaRepository.findByServicioId(servicioId).stream()
                .map(c -> ServicioCortesiaDto.builder()
                        .productoId(c.getProductoId())
                        .productoNombre(productoRepository.findById(c.getProductoId())
                                .map(Producto::getNombre).orElse("(producto eliminado)"))
                        .cantidad(c.getCantidad())
                        .build())
                .toList();
        return conCortesias(response, cortesias);
    }

    private ServicioResponse conCortesias(ServicioResponse response, List<ServicioCortesiaDto> cortesias) {
        return response.toBuilder().cortesias(cortesias).build();
    }

    @Transactional
    public void cambiarEstado(Integer id, Boolean estado) {
        findOrThrow(id);
        servicioRepository.updateEstado(id, estado);
        log.info("Estado de servicio ID={} cambiado a {}", id, estado);
    }

    private Servicio findOrThrow(Integer id) {
        return servicioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Servicio", id));
    }
}
