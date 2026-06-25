package com.urlshortener.url_shortener.util;

import org.springframework.stereotype.Component;
import java.security.SecureRandom;

@Component
public class ShortCodeGenerator {

    private static final String BASE62_ALPHABET = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final int BASE62_LENGTH = BASE62_ALPHABET.length();
    private static final int CODE_LENGTH = 6;
    private final SecureRandom secureRandom;

    public ShortCodeGenerator() {
        this.secureRandom = new SecureRandom();
    }

    /**
     * Generates a random 6-character short code using Base62 encoding.
     * Characters used: 0-9, a-z, A-Z
     *
     * @return a random 6-character short code
     */
    public String generateShortCode() {
        StringBuilder shortCode = new StringBuilder(CODE_LENGTH);

        for (int i = 0; i < CODE_LENGTH; i++) {
            int randomIndex = secureRandom.nextInt(BASE62_LENGTH);
            shortCode.append(BASE62_ALPHABET.charAt(randomIndex));
        }

        return shortCode.toString();
    }
}
