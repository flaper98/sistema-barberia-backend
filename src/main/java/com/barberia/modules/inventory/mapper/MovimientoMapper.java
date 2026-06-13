package com.barberia.modules.inventory.mapper;

import com.barberia.modules.inventory.dto.MovimientoResponse;
import com.barberia.modules.inventory.entity.MovimientoStock;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MovimientoMapper {

    @Mapping(target = "tipoMovimiento", expression = "java(m.getTipoMovimiento().name())")
    MovimientoResponse toResponse(MovimientoStock m);
}
