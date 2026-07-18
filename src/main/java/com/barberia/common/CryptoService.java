package com.barberia.common;

import com.barberia.exception.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Cifrado simétrico (AES-256-GCM) para secretos de terceros que se guardan en la
 * base de datos (ej. la contraseña de aplicación de iCloud) para que no queden
 * en texto plano en la tabla. La clave vive SOLO en una variable de entorno
 * (APP_ENCRYPTION_KEY), nunca en la base de datos ni en el repo — si la clave
 * no está configurada, el cifrado simplemente no está disponible (falla con un
 * mensaje claro) en vez de usar una clave débil por defecto.
 */
@Service
public class CryptoService {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH_BITS = 128;
    private static final int IV_LENGTH_BYTES = 12;

    private final String claveBase64;

    public CryptoService(@Value("${app.security.encryption-key:}") String claveBase64) {
        this.claveBase64 = claveBase64;
    }

    public boolean claveConfigurada() {
        return claveBase64 != null && !claveBase64.isBlank();
    }

    public String encriptar(String textoPlano) {
        requireClave();
        try {
            byte[] iv = new byte[IV_LENGTH_BYTES];
            new SecureRandom().nextBytes(iv);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, claveSecreta(), new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv));
            byte[] cifrado = cipher.doFinal(textoPlano.getBytes(StandardCharsets.UTF_8));

            byte[] resultado = new byte[iv.length + cifrado.length];
            System.arraycopy(iv, 0, resultado, 0, iv.length);
            System.arraycopy(cifrado, 0, resultado, iv.length, cifrado.length);
            return Base64.getEncoder().encodeToString(resultado);
        } catch (Exception e) {
            throw new BusinessException("No se pudo cifrar el dato: " + e.getMessage());
        }
    }

    public String desencriptar(String textoCifradoBase64) {
        requireClave();
        try {
            byte[] datos = Base64.getDecoder().decode(textoCifradoBase64);
            byte[] iv = new byte[IV_LENGTH_BYTES];
            System.arraycopy(datos, 0, iv, 0, iv.length);
            byte[] cifrado = new byte[datos.length - iv.length];
            System.arraycopy(datos, iv.length, cifrado, 0, cifrado.length);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, claveSecreta(), new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv));
            return new String(cipher.doFinal(cifrado), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new BusinessException("No se pudo descifrar el dato: " + e.getMessage());
        }
    }

    private void requireClave() {
        if (!claveConfigurada()) {
            throw new BusinessException(
                    "Falta configurar APP_ENCRYPTION_KEY en el servidor antes de guardar datos sensibles. "
                            + "Generá una clave de 32 bytes en base64 (ej. openssl rand -base64 32) y reiniciá el backend.");
        }
    }

    private SecretKeySpec claveSecreta() {
        byte[] key = Base64.getDecoder().decode(claveBase64);
        return new SecretKeySpec(key, "AES");
    }
}
