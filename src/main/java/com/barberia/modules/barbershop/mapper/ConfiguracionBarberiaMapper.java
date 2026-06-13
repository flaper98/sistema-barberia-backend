package com.barberia.modules.barbershop.mapper;

import com.barberia.modules.barbershop.dto.ConfiguracionBarberiaResponse;
import com.barberia.modules.barbershop.entity.ConfiguracionBarberia;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ConfiguracionBarberiaMapper {

    ConfiguracionBarberiaResponse toResponse(ConfiguracionBarberia config);
}
