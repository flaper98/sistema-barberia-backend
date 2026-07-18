package com.barberia.modules.google.service;

import com.barberia.common.carddav.CardDavClient;
import com.barberia.common.carddav.CardDavContactData;
import com.barberia.common.carddav.CardDavSyncException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Punto de entrada para sincronizar un cliente con Google Contacts desde otros
 * módulos (ej. al registrarlo). Corre async y nunca deja escapar una excepción: un
 * problema con Google (credenciales vencidas, red, rate limit) no debe romper
 * el registro del cliente, que es lo importante.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GoogleSyncService {

    private static final String ENTRY_URL = "https://www.google.com/.well-known/carddav";

    private final GoogleConfigService configService;
    private final CardDavClient cardDavClient;

    @Async
    public void sincronizarClienteAsync(CardDavContactData datos) {
        if (!configService.sincronizacionActiva()) return;

        try {
            String username = configService.obtenerUsername();
            String appPassword = configService.obtenerAppPasswordDescifrada()
                    .orElseThrow(() -> new CardDavSyncException("No hay contraseña de aplicación configurada"));
            cardDavClient.sincronizarContacto(ENTRY_URL, username, appPassword,
                    configService::obtenerAddressbookHomeCacheado, configService::guardarAddressbookHomeCacheado,
                    datos);
        } catch (Exception e) {
            log.warn("No se pudo sincronizar el cliente ID={} con Google Contacts: {}", datos.clienteId(), e.getMessage());
        }
    }
}
