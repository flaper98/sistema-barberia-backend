package com.barberia.modules.customers.service;

import com.barberia.common.PageResponse;
import com.barberia.modules.customers.dto.ClienteRequest;
import com.barberia.modules.customers.dto.ClienteResponse;
import org.springframework.data.domain.Pageable;

public interface ClienteService {

    PageResponse<ClienteResponse> listarClientes(String search, Pageable pageable);

    ClienteResponse obtenerPorId(Integer id);

    ClienteResponse crear(ClienteRequest request);

    ClienteResponse actualizar(Integer id, ClienteRequest request);

    void eliminar(Integer id);

    PageResponse<ClienteResponse> listarConRecompensas(Pageable pageable);
}
