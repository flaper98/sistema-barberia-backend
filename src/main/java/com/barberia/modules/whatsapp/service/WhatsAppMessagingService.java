package com.barberia.modules.whatsapp.service;

import com.barberia.common.whatsapp.WhatsAppClient;
import com.barberia.common.whatsapp.WhatsAppSendException;
import com.barberia.modules.whatsapp.entity.WhatsAppConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * Punto de entrada para mandar el mensaje de bienvenida por WhatsApp desde
 * otros módulos (ej. al registrar un cliente). Corre async y nunca deja
 * escapar una excepción: un problema con la API de WhatsApp (token vencido,
 * plantilla rechazada, red) no debe romper el registro del cliente, que es
 * lo importante.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WhatsAppMessagingService {

    private final WhatsAppConfigService configService;
    private final WhatsAppClient whatsAppClient;

    @Async
    public void enviarBienvenidaAsync(Integer clienteId, String nombreCliente, String telefono) {
        if (!configService.envioActivo()) return;
        if (!StringUtils.hasText(telefono)) return;

        try {
            WhatsAppConfig config = configService.obtenerConfigParaEnvio();
            String accessToken = configService.obtenerAccessTokenDescifrado()
                    .orElseThrow(() -> new WhatsAppSendException("No hay access token configurado"));
            whatsAppClient.enviarPlantillaBienvenida(
                    config.getGraphApiVersion(), config.getPhoneNumberId(), accessToken,
                    config.getTemplateName(), config.getLanguageCode(),
                    telefono, nombreCliente);
        } catch (Exception e) {
            log.warn("No se pudo enviar el WhatsApp de bienvenida al cliente ID={}: {}", clienteId, e.getMessage());
        }
    }
}
