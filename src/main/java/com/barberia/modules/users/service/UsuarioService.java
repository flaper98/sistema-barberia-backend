package com.barberia.modules.users.service;

import com.barberia.common.PageResponse;
import com.barberia.modules.users.dto.UsuarioRequest;
import com.barberia.modules.users.dto.UsuarioResponse;
import org.springframework.data.domain.Pageable;

public interface UsuarioService {

    PageResponse<UsuarioResponse> listarUsuarios(String search, Pageable pageable);

    UsuarioResponse obtenerPorId(Integer id);

    UsuarioResponse crear(UsuarioRequest request);

    UsuarioResponse actualizar(Integer id, UsuarioRequest request);

    void cambiarEstado(Integer id, Boolean estado);

    void eliminar(Integer id);
}
