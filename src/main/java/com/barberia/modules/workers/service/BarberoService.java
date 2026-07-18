package com.barberia.modules.workers.service;

import com.barberia.common.PageResponse;
import com.barberia.modules.workers.dto.BarberoRequest;
import com.barberia.modules.workers.dto.BarberoResponse;
import com.barberia.modules.workers.dto.BarberoSelectorResponse;
import com.barberia.modules.workers.dto.BarberoSummaryResponse;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface BarberoService {

    PageResponse<BarberoResponse> listarBarberos(String search, Pageable pageable);

    List<BarberoSelectorResponse> listarParaSelector();

    BarberoResponse obtenerPorId(Integer id);

    BarberoResponse crear(BarberoRequest request);

    BarberoResponse actualizar(Integer id, BarberoRequest request);

    void cambiarEstado(Integer id, Boolean estado);

    void eliminar(Integer id);

    BarberoSummaryResponse obtenerResumen(Integer id, LocalDate desde, LocalDate hasta);
}
