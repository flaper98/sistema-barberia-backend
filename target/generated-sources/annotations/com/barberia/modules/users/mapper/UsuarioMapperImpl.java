package com.barberia.modules.users.mapper;

import com.barberia.modules.users.dto.UsuarioResponse;
import com.barberia.modules.users.entity.Usuario;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-06-12T18:25:08-0500",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.10 (Microsoft)"
)
@Component
public class UsuarioMapperImpl implements UsuarioMapper {

    @Override
    public UsuarioResponse toResponse(Usuario usuario) {
        if ( usuario == null ) {
            return null;
        }

        UsuarioResponse.UsuarioResponseBuilder usuarioResponse = UsuarioResponse.builder();

        usuarioResponse.id( usuario.getId() );
        usuarioResponse.nombre( usuario.getNombre() );
        usuarioResponse.apellido( usuario.getApellido() );
        usuarioResponse.email( usuario.getEmail() );
        usuarioResponse.estado( usuario.getEstado() );
        usuarioResponse.avatar( usuario.getAvatar() );
        usuarioResponse.ultimoAcceso( usuario.getUltimoAcceso() );
        usuarioResponse.createdAt( usuario.getCreatedAt() );
        usuarioResponse.updatedAt( usuario.getUpdatedAt() );

        usuarioResponse.nombreCompleto( usuario.getNombreCompleto() );
        usuarioResponse.rol( usuario.getRol().name() );

        return usuarioResponse.build();
    }
}
