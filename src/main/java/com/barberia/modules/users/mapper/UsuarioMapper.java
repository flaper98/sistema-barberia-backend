package com.barberia.modules.users.mapper;

import com.barberia.modules.users.dto.UsuarioResponse;
import com.barberia.modules.users.entity.Usuario;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UsuarioMapper {

    @Mapping(target = "nombreCompleto", expression = "java(usuario.getNombreCompleto())")
    @Mapping(target = "rol", expression = "java(usuario.getRol().name())")
    UsuarioResponse toResponse(Usuario usuario);
}
