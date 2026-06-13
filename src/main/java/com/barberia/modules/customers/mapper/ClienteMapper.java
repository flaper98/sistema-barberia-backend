package com.barberia.modules.customers.mapper;

import com.barberia.modules.customers.dto.ClienteResponse;
import com.barberia.modules.customers.entity.Cliente;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ClienteMapper {

    @Mapping(target = "nombreCompleto", expression = "java(cliente.getNombreCompleto())")
    ClienteResponse toResponse(Cliente cliente);
}
