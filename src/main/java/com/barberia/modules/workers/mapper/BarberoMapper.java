package com.barberia.modules.workers.mapper;

import com.barberia.modules.workers.dto.BarberoResponse;
import com.barberia.modules.workers.entity.Barbero;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BarberoMapper {

    @Mapping(target = "nombreCompleto", expression = "java(barbero.getNombreCompleto())")
    BarberoResponse toResponse(Barbero barbero);
}
