package com.barberia.modules.sales.mapper;

import com.barberia.modules.sales.dto.VentaResponse;
import com.barberia.modules.sales.entity.Venta;
import com.barberia.modules.sales.entity.VentaItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface VentaMapper {

    @Mapping(target = "tipoVenta", expression = "java(venta.getTipoVenta().name())")
    @Mapping(target = "metodoPago", expression = "java(venta.getMetodoPago().name())")
    @Mapping(target = "estado", expression = "java(venta.getEstado().name())")
    @Mapping(target = "items", source = "items")
    VentaResponse toResponse(Venta venta);

    @Mapping(target = "tipo", expression = "java(item.getTipo().name())")
    VentaResponse.VentaItemResponse toItemResponse(VentaItem item);
}
