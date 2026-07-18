package com.barberia.modules.services.service;

import com.barberia.modules.services.dto.PaqueteRequest;
import com.barberia.modules.services.dto.PaqueteResponse;

import java.util.List;

public interface PaqueteService {

    List<PaqueteResponse> listarActivos();

    List<PaqueteResponse> listarTodos();

    PaqueteResponse crear(PaqueteRequest request);

    PaqueteResponse actualizar(Integer id, PaqueteRequest request);

    void cambiarEstado(Integer id, Boolean estado);
}
