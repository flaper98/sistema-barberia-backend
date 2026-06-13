package com.barberia.modules.finance.mapper;

import com.barberia.modules.finance.dto.FinanzaResponse;
import com.barberia.modules.finance.entity.Finanza;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface FinanzaMapper {

    @Mapping(target = "tipo", expression = "java(f.getTipo().name())")
    @Mapping(target = "categoria", expression = "java(f.getCategoria().name())")
    FinanzaResponse toResponse(Finanza f);
}
