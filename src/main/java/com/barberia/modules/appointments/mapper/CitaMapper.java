package com.barberia.modules.appointments.mapper;

import com.barberia.modules.appointments.dto.CitaResponse;
import com.barberia.modules.appointments.entity.Cita;
import com.barberia.modules.appointments.entity.CitaServicio;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CitaMapper {

    @Mapping(target = "estado", expression = "java(cita.getEstado().name())")
    @Mapping(target = "servicios", source = "servicios")
    CitaResponse toResponse(Cita cita);

    CitaResponse.CitaServicioItemResponse toServicioResponse(CitaServicio cs);
}
