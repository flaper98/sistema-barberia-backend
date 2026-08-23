package com.barberia.common.whatsapp;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

/**
 * Cliente mínimo para la API oficial de WhatsApp Business (Meta Cloud API) —
 * un único POST de "mensaje de plantilla" por contacto nuevo. A diferencia de
 * CardDAV, esta sí es una API oficial y documentada, pero con una restricción
 * importante: no se puede mandar texto libre a un número que nunca escribió
 * primero, solo plantillas pre-aprobadas por Meta (ver WhatsAppConfig
 * .templateName). El texto de la plantilla en sí NO se manda acá — ya está
 * aprobado del lado de Meta, solo se manda el nombre de la plantilla y los
 * valores de sus variables.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WhatsAppClient {

    private static final String CODIGO_PAIS_DEFECTO = "51";

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public void enviarPlantillaBienvenida(String graphApiVersion, String phoneNumberId, String accessToken,
                                           String templateName, String languageCode,
                                           String telefonoDestino, String nombreCliente) {
        String uri = "https://graph.facebook.com/" + graphApiVersion + "/" + phoneNumberId + "/messages";
        String body = construirBody(templateName, languageCode, normalizarTelefono(telefonoDestino), nombreCliente);

        HttpRequest request = HttpRequest.newBuilder(URI.create(uri))
                .timeout(Duration.ofSeconds(15))
                .header("Authorization", "Bearer " + accessToken)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response = enviar(request);
        if (response.statusCode() >= 300) {
            throw new WhatsAppSendException(
                    "La API de WhatsApp respondió " + response.statusCode() + ": " + truncar(response.body()));
        }
        log.info("Mensaje de bienvenida de WhatsApp enviado (http={})", response.statusCode());
    }

    private String construirBody(String templateName, String languageCode, String telefonoDestino, String nombreCliente) {
        ObjectNode root = objectMapper.createObjectNode();
        root.put("messaging_product", "whatsapp");
        root.put("to", telefonoDestino);
        root.put("type", "template");

        ObjectNode template = root.putObject("template");
        template.put("name", templateName);
        template.putObject("language").put("code", languageCode);

        ObjectNode component = objectMapper.createObjectNode();
        component.put("type", "body");
        ObjectNode parametro = objectMapper.createObjectNode();
        parametro.put("type", "text");
        parametro.put("text", nombreCliente);
        component.putArray("parameters").add(parametro);
        template.putArray("components").add(component);

        try {
            return objectMapper.writeValueAsString(root);
        } catch (Exception e) {
            throw new WhatsAppSendException("No se pudo armar el mensaje de WhatsApp: " + e.getMessage(), e);
        }
    }

    /** Mismo criterio que el link manual del frontend (whatsapp.util.ts): 9 dígitos -> se asume Perú y se antepone 51. */
    private String normalizarTelefono(String telefono) {
        String digitos = telefono == null ? "" : telefono.replaceAll("\\D", "");
        return digitos.length() == 9 ? CODIGO_PAIS_DEFECTO + digitos : digitos;
    }

    private HttpResponse<String> enviar(HttpRequest request) {
        try {
            return httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new WhatsAppSendException("Error de red al conectar con la API de WhatsApp: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new WhatsAppSendException("Envío de WhatsApp interrumpido", e);
        }
    }

    private String truncar(String texto) {
        if (texto == null) return "";
        return texto.length() > 300 ? texto.substring(0, 300) + "…" : texto;
    }
}
