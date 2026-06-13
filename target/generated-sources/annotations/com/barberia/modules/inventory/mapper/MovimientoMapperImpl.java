package com.barberia.modules.inventory.mapper;

import com.barberia.modules.inventory.dto.MovimientoResponse;
import com.barberia.modules.inventory.entity.MovimientoStock;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-06-12T18:25:08-0500",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.10 (Microsoft)"
)
@Component
public class MovimientoMapperImpl implements MovimientoMapper {

    @Override
    public MovimientoResponse toResponse(MovimientoStock m) {
        if ( m == null ) {
            return null;
        }

        MovimientoResponse.MovimientoResponseBuilder movimientoResponse = MovimientoResponse.builder();

        movimientoResponse.id( m.getId() );
        movimientoResponse.productoId( m.getProductoId() );
        movimientoResponse.productoNombre( m.getProductoNombre() );
        movimientoResponse.cantidad( m.getCantidad() );
        movimientoResponse.stockAnterior( m.getStockAnterior() );
        movimientoResponse.stockNuevo( m.getStockNuevo() );
        movimientoResponse.motivo( m.getMotivo() );
        movimientoResponse.usuarioNombre( m.getUsuarioNombre() );
        movimientoResponse.ventaId( m.getVentaId() );
        movimientoResponse.createdAt( m.getCreatedAt() );

        movimientoResponse.tipoMovimiento( m.getTipoMovimiento().name() );

        return movimientoResponse.build();
    }
}
