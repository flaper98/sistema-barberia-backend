package com.barberia.modules.inventory.service.impl;

import com.barberia.common.PageResponse;
import com.barberia.exception.BusinessException;
import com.barberia.exception.ResourceNotFoundException;
import com.barberia.modules.audit.AuditAction;
import com.barberia.modules.audit.AuditPublisher;
import com.barberia.modules.inventory.dto.MovimientoRequest;
import com.barberia.modules.inventory.dto.MovimientoResponse;
import com.barberia.modules.inventory.entity.MovimientoStock;
import com.barberia.modules.inventory.entity.TipoMovimiento;
import com.barberia.modules.inventory.mapper.MovimientoMapper;
import com.barberia.modules.inventory.repository.MovimientoStockRepository;
import com.barberia.modules.products.entity.Producto;
import com.barberia.modules.products.repository.ProductoRepository;
import com.barberia.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventarioServiceImpl {

    private final MovimientoStockRepository movimientoRepository;
    private final ProductoRepository productoRepository;
    private final MovimientoMapper movimientoMapper;
    private final AuditPublisher auditPublisher;

    @Transactional(readOnly = true)
    public PageResponse<MovimientoResponse> listarMovimientos(Integer productoId, Pageable pageable) {
        if (productoId != null) {
            return PageResponse.from(
                    movimientoRepository.findByProductoId(productoId, pageable)
                            .map(movimientoMapper::toResponse));
        }
        return PageResponse.from(
                movimientoRepository.findAll(pageable).map(movimientoMapper::toResponse));
    }

    @Transactional
    public MovimientoResponse registrarMovimiento(MovimientoRequest request) {
        Producto producto = productoRepository.findById(request.getProductoId())
                .orElseThrow(() -> new ResourceNotFoundException("Producto", request.getProductoId()));

        if (!Boolean.TRUE.equals(producto.getEstado())) {
            throw new BusinessException("El producto está inactivo y no puede recibir movimientos");
        }

        int stockAnterior = producto.getStockActual();
        int stockNuevo = calcularStockNuevo(request.getTipoMovimiento(),
                stockAnterior, request.getCantidad());

        CustomUserDetails userDetails = getCurrentUser();

        MovimientoStock movimiento = MovimientoStock.builder()
                .productoId(producto.getId())
                .productoNombre(producto.getNombre())
                .tipoMovimiento(request.getTipoMovimiento())
                .cantidad(request.getCantidad())
                .stockAnterior(stockAnterior)
                .stockNuevo(stockNuevo)
                .motivo(request.getMotivo())
                .usuarioId(userDetails != null ? userDetails.getId() : null)
                .usuarioNombre(userDetails != null ? userDetails.getNombreCompleto() : "Sistema")
                .build();

        movimiento = movimientoRepository.save(movimiento);

        String actor = userDetails != null ? userDetails.getUsername() : "SISTEMA";
        auditPublisher.publish(AuditAction.STOCK_MOVEMENT, actor,
                "PRODUCTO", producto.getId().toString(),
                "Movimiento " + request.getTipoMovimiento() + " cant=" + request.getCantidad() +
                " stock: " + stockAnterior + "->" + stockNuevo);
        log.info("Movimiento de stock registrado: tipo={} producto={} cantidad={} stock: {}->{}",
                request.getTipoMovimiento(), producto.getNombre(),
                request.getCantidad(), stockAnterior, stockNuevo);

        return movimientoMapper.toResponse(movimiento);
    }

    private int calcularStockNuevo(TipoMovimiento tipo, int stockActual, int cantidad) {
        return switch (tipo) {
            case ENTRADA -> stockActual + cantidad;
            case SALIDA -> {
                if (stockActual < cantidad) {
                    throw new BusinessException(
                            "Stock insuficiente. Disponible: " + stockActual + ", solicitado: " + cantidad);
                }
                yield stockActual - cantidad;
            }
            case AJUSTE -> cantidad;
        };
    }

    private CustomUserDetails getCurrentUser() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails) {
            return (CustomUserDetails) auth.getPrincipal();
        }
        return null;
    }
}
