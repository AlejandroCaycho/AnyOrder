package com.anyorder.pos.anyorder.util;

import java.security.SecureRandom;
import java.util.Base64;

/**
 * Utilidad profesional para la generación de tokens seguros.
 * Utiliza SecureRandom para garantizar la impredecibilidad de los tokens generados.
 */
public class TokenGenerator {

    private static final SecureRandom secureRandom = new SecureRandom();
    private static final Base64.Encoder base64Encoder = Base64.getUrlEncoder().withoutPadding();

    /**
     * Genera un token aleatorio seguro de 32 bytes (codificado en Base64).
     * @return Token seguro único.
     */
    public static String generateToken() {
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);
        return base64Encoder.encodeToString(randomBytes);
    }

    /**
     * Genera un identificador corto seguro de 12 bytes.
     * Útil para referencias rápidas o tokens menos críticos.
     */
    public static String generateShortToken() {
        byte[] randomBytes = new byte[12];
        secureRandom.nextBytes(randomBytes);
        return base64Encoder.encodeToString(randomBytes);
    }
}
