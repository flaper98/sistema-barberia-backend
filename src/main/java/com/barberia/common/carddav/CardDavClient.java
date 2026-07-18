package com.barberia.common.carddav;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Cliente CardDAV mínimo, compartido por cualquier proveedor que hable el protocolo
 * (iCloud, Google Contacts). Este protocolo NO está documentado oficialmente por
 * ninguno de los dos para uso de terceros — es el mismo que usan las apps nativas de
 * contactos, reconstruido por la comunidad (sabre/dav, tsdav, DAVx5, vdirsyncer).
 * Puede cambiar sin aviso; si empieza a fallar, lo primero a revisar es si el
 * proveedor cambió algo en su flujo de descubrimiento.
 *
 * Flujo (3 pasos, cada uno un PROPFIND), a partir de una URL de entrada específica
 * de cada proveedor (ej. "https://contacts.icloud.com/" para iCloud,
 * "https://www.google.com/.well-known/carddav" para Google):
 *  1. PROPFIND en la URL de entrada -> current-user-principal
 *  2. PROPFIND ese principal -> addressbook-home-set
 *  3. PROPFIND (Depth 1) ese home -> la colección cuyo resourcetype incluye "addressbook"
 * El resultado del paso 3 se cachea vía los callbacks que recibe cada llamada, para
 * no repetir el descubrimiento en cada contacto.
 */
@Slf4j
@Service
public class CardDavClient {

    private static final String NS_DAV = "DAV:";
    private static final String NS_CARDDAV = "urn:ietf:params:xml:ns:carddav";

    private static final String BODY_CURRENT_USER_PRINCIPAL =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                    + "<d:propfind xmlns:d=\"DAV:\"><d:prop><d:current-user-principal/></d:prop></d:propfind>";

    private static final String BODY_ADDRESSBOOK_HOME_SET =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                    + "<d:propfind xmlns:d=\"DAV:\" xmlns:card=\"urn:ietf:params:xml:ns:carddav\">"
                    + "<d:prop><card:addressbook-home-set/></d:prop></d:propfind>";

    private static final String BODY_RESOURCETYPE =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                    + "<d:propfind xmlns:d=\"DAV:\"><d:prop><d:resourcetype/></d:prop></d:propfind>";

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    public void sincronizarContacto(String entryUrl, String username, String appPassword,
                                     Supplier<Optional<String>> obtenerCacheado, Consumer<String> guardarCacheado,
                                     CardDavContactData datos) {
        String coleccionUrl = obtenerColeccionAddressbook(entryUrl, username, appPassword, obtenerCacheado, guardarCacheado);
        String uid = uidDeterministico(datos.clienteId());
        String vcard = construirVCard(datos, uid);
        URI uri = URI.create(coleccionUrl + uid + ".vcf");

        HttpRequest request = HttpRequest.newBuilder(uri)
                .timeout(Duration.ofSeconds(15))
                .header("Authorization", basicAuth(username, appPassword))
                .header("Content-Type", "text/vcard; charset=utf-8")
                .PUT(HttpRequest.BodyPublishers.ofString(vcard, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response = enviar(request);
        if (response.statusCode() >= 300) {
            throw new CardDavSyncException(
                    "El servidor respondió " + response.statusCode() + " al crear el contacto: " + truncar(response.body()));
        }
        log.info("Cliente ID={} sincronizado vía CardDAV (uid={}, http={})", datos.clienteId(), uid, response.statusCode());
    }

    private String obtenerColeccionAddressbook(String entryUrl, String username, String appPassword,
                                                 Supplier<Optional<String>> obtenerCacheado, Consumer<String> guardarCacheado) {
        Optional<String> cacheado = obtenerCacheado.get();
        if (cacheado.isPresent()) return cacheado.get();

        String coleccion = descubrirColeccionAddressbook(entryUrl, username, appPassword);
        guardarCacheado.accept(coleccion);
        log.info("Colección de contactos descubierta y cacheada: {}", coleccion);
        return coleccion;
    }

    private String descubrirColeccionAddressbook(String entryUrl, String username, String appPassword) {
        Document principalDoc = parseXml(propfind(entryUrl, username, appPassword, BODY_CURRENT_USER_PRINCIPAL, 0));
        String principalHref = extraerHrefDentroDe(principalDoc, "current-user-principal", "current-user-principal");
        String principalUrl = URI.create(entryUrl).resolve(principalHref).toString();

        Document homeDoc = parseXml(propfind(principalUrl, username, appPassword, BODY_ADDRESSBOOK_HOME_SET, 0));
        String homeHref = extraerHrefDentroDe(homeDoc, "addressbook-home-set", "addressbook-home-set");
        String homeUrl = URI.create(principalUrl).resolve(homeHref).toString();
        if (!homeUrl.endsWith("/")) homeUrl += "/";

        Document listadoDoc = parseXml(propfind(homeUrl, username, appPassword, BODY_RESOURCETYPE, 1));
        String coleccionHref = encontrarColeccionAddressbook(listadoDoc, homeUrl);
        String coleccionUrl = URI.create(homeUrl).resolve(coleccionHref).toString();
        return coleccionUrl.endsWith("/") ? coleccionUrl : coleccionUrl + "/";
    }

    private String extraerHrefDentroDe(Document doc, String namespaceLocalName, String etiquetaParaError) {
        NodeList contenedores = doc.getElementsByTagNameNS(NS_DAV, namespaceLocalName);
        if (contenedores.getLength() == 0) {
            // Puede venir en el namespace carddav en vez de DAV (algunos props son CARD:)
            contenedores = doc.getElementsByTagNameNS(NS_CARDDAV, namespaceLocalName);
        }
        if (contenedores.getLength() == 0) {
            throw new CardDavSyncException("La respuesta del servidor no incluyó " + etiquetaParaError);
        }
        Element contenedor = (Element) contenedores.item(0);
        NodeList hrefs = contenedor.getElementsByTagNameNS(NS_DAV, "href");
        if (hrefs.getLength() == 0) {
            throw new CardDavSyncException("La respuesta del servidor no incluyó href dentro de " + etiquetaParaError);
        }
        return hrefs.item(0).getTextContent().trim();
    }

    private String encontrarColeccionAddressbook(Document doc, String baseUrl) {
        NodeList responses = doc.getElementsByTagNameNS(NS_DAV, "response");
        for (int i = 0; i < responses.getLength(); i++) {
            Element response = (Element) responses.item(i);
            NodeList resourcetypes = response.getElementsByTagNameNS(NS_DAV, "resourcetype");
            if (resourcetypes.getLength() == 0) continue;
            Element resourcetype = (Element) resourcetypes.item(0);
            if (resourcetype.getElementsByTagNameNS(NS_CARDDAV, "addressbook").getLength() > 0) {
                NodeList hrefs = response.getElementsByTagNameNS(NS_DAV, "href");
                if (hrefs.getLength() > 0) {
                    return hrefs.item(0).getTextContent().trim();
                }
            }
        }
        throw new CardDavSyncException("No se encontró una colección de tipo addressbook dentro de " + baseUrl);
    }

    private String propfind(String url, String username, String appPassword, String body, int depth) {
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(15))
                .header("Authorization", basicAuth(username, appPassword))
                .header("Content-Type", "application/xml; charset=utf-8")
                .header("Depth", String.valueOf(depth))
                .method("PROPFIND", HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                .build();
        HttpResponse<String> response = enviar(request);
        if (response.statusCode() != 207) {
            throw new CardDavSyncException(
                    "El servidor respondió " + response.statusCode() + " a PROPFIND " + url + ": " + truncar(response.body()));
        }
        return response.body();
    }

    private HttpResponse<String> enviar(HttpRequest request) {
        try {
            return httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new CardDavSyncException("Error de red al conectar: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new CardDavSyncException("Sincronización interrumpida", e);
        }
    }

    private Document parseXml(String body) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            return builder.parse(new InputSource(new StringReader(body)));
        } catch (Exception e) {
            throw new CardDavSyncException("No se pudo interpretar la respuesta XML del servidor: " + e.getMessage(), e);
        }
    }

    private String uidDeterministico(Integer clienteId) {
        return UUID.nameUUIDFromBytes(("barberia-cliente-" + clienteId).getBytes(StandardCharsets.UTF_8)).toString();
    }

    private String construirVCard(CardDavContactData datos, String uid) {
        StringBuilder sb = new StringBuilder();
        sb.append("BEGIN:VCARD\r\n");
        sb.append("VERSION:3.0\r\n");
        sb.append("N:").append(escapar(datos.apellido())).append(';').append(escapar(datos.nombre())).append(";;;\r\n");
        sb.append("FN:").append(escapar((datos.nombre() + " " + datos.apellido()).trim())).append("\r\n");
        if (StringUtils.hasText(datos.telefono())) {
            sb.append("TEL;TYPE=CELL:").append(datos.telefono()).append("\r\n");
        }
        if (StringUtils.hasText(datos.correo())) {
            sb.append("EMAIL;TYPE=INTERNET:").append(datos.correo()).append("\r\n");
        }
        sb.append("NOTE:Cliente de Árabes Barber Studio (sincronizado automáticamente)\r\n");
        sb.append("UID:").append(uid).append("\r\n");
        sb.append("END:VCARD\r\n");
        return sb.toString();
    }

    private String escapar(String valor) {
        if (valor == null) return "";
        return valor.replace("\\", "\\\\").replace(";", "\\;").replace(",", "\\,");
    }

    private String basicAuth(String username, String appPassword) {
        String credenciales = username + ":" + appPassword;
        return "Basic " + Base64.getEncoder().encodeToString(credenciales.getBytes(StandardCharsets.UTF_8));
    }

    private String truncar(String texto) {
        if (texto == null) return "";
        return texto.length() > 300 ? texto.substring(0, 300) + "…" : texto;
    }
}
