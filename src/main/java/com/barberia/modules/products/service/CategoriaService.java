package com.barberia.modules.products.service;

import com.barberia.modules.products.dto.CategoriaRequest;
import com.barberia.modules.products.dto.CategoriaResponse;

import java.util.List;

public interface CategoriaService {

    List<CategoriaResponse> listarActivas();

    List<CategoriaResponse> listarTodas();

    CategoriaResponse crear(CategoriaRequest request);

    CategoriaResponse actualizar(Integer id, CategoriaRequest request);

    void cambiarEstado(Integer id, Boolean estado);
}
