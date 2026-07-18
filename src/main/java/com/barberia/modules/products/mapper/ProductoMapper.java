package com.barberia.modules.products.mapper;

import com.barberia.modules.products.dto.ProductoResponse;
import com.barberia.modules.products.entity.Producto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProductoMapper {

    @Mapping(target = "stockBajo", expression = "java(producto.isStockBajo())")
    @Mapping(target = "unidadesFaltantes",
             expression = "java(Math.max(0, producto.getStockMinimo() - producto.getStockActual()))")
    ProductoResponse toResponse(Producto producto);
}
