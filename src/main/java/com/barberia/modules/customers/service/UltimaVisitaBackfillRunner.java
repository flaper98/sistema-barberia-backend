package com.barberia.modules.customers.service;

import com.barberia.modules.customers.entity.Cliente;
import com.barberia.modules.customers.repository.ClienteRepository;
import com.barberia.modules.sales.entity.Venta;
import com.barberia.modules.sales.repository.VentaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * "ultima_visita" es una columna real que nunca se llegó a escribir desde ningún
 * flujo (ni al crear una venta). Los clientes creados antes de que
 * {@link com.barberia.modules.sales.service.impl.VentaServiceImpl} empezara a
 * actualizarla quedaron con el valor en null para siempre; esto corre una sola
 * vez por cliente (solo toca los que todavía están en null) para completarlo
 * a partir de su venta más reciente.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UltimaVisitaBackfillRunner implements ApplicationRunner {

    private final ClienteRepository clienteRepository;
    private final VentaRepository ventaRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        List<Cliente> pendientes = clienteRepository.findByUltimaVisitaIsNull();
        if (pendientes.isEmpty()) return;

        int actualizados = 0;
        for (Cliente cliente : pendientes) {
            List<Venta> ultima = ventaRepository
                    .findByClienteId(cliente.getId(), PageRequest.of(0, 1, Sort.by("fecha").descending()))
                    .getContent();
            if (!ultima.isEmpty()) {
                cliente.setUltimaVisita(ultima.get(0).getFecha().toLocalDate());
                clienteRepository.save(cliente);
                actualizados++;
            }
        }
        if (actualizados > 0) {
            log.info("Backfill de última visita: {} cliente(s) actualizados", actualizados);
        }
    }
}
