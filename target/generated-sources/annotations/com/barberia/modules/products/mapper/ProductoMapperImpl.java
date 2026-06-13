package com.barberia.modules.products.mapper;

import com.barberia.modules.products.dto.ProductoResponse;
import com.barberia.modules.products.entity.Producto;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-06-12T18:25:09-0500",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.10 (Microsoft)"
)
@Component
public class ProductoMapperImpl implements ProductoMapper {

    @Override
    public ProductoResponse toResponse(Producto producto) {
        if ( producto == null ) {
            return null;
        }

        ProductoResponse.ProductoResponseBuilder productoResponse = ProductoResponse.builder();

        productoResponse.id( producto.getId() );
        productoResponse.nombre( producto.getNombre() );
        productoResponse.precioCompra( producto.getPrecioCompra() );
        productoResponse.precioVenta( producto.getPrecioVenta() );
        productoResponse.stockActual( producto.getStockActual() );
        productoResponse.stockMinimo( producto.getStockMinimo() );
        productoResponse.descripcion( producto.getDescripcion() );
        productoResponse.codigoBarras( producto.getCodigoBarras() );
        productoResponse.estado( producto.getEstado() );
        productoResponse.createdAt( producto.getCreatedAt() );
        productoResponse.updatedAt( producto.getUpdatedAt() );

        productoResponse.categoria( producto.getCategoria().name() );
        productoResponse.stockBajo( producto.isStockBajo() );
        productoResponse.unidadesFaltantes( Math.max(0, producto.getStockMinimo() - producto.getStockActual()) );

        return productoResponse.build();
    }
}
