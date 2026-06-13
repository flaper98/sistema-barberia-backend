package com.barberia.modules.services.mapper;

import com.barberia.modules.services.dto.ServicioResponse;
import com.barberia.modules.services.entity.Servicio;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ServicioMapper {

    @Mapping(target = "categoria", expression = "java(servicio.getCategoria().name())")
    ServicioResponse toResponse(Servicio servicio);
}
