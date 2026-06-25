package com.urlshortener.url_shortener.service;

import com.urlshortener.url_shortener.entity.ShortUrl;
import com.urlshortener.url_shortener.exception.ResourceNotFoundException;
import com.urlshortener.url_shortener.repository.ShortUrlRepository;
import com.urlshortener.url_shortener.util.ShortCodeGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

@Service
@Transactional
public class UrlShortenerService {

    private static final int MAX_RETRIES = 10;
    private final ShortUrlRepository shortUrlRepository;
    private final ShortCodeGenerator shortCodeGenerator;

    @Autowired
    public UrlShortenerService(ShortUrlRepository shortUrlRepository, ShortCodeGenerator shortCodeGenerator) {
        this.shortUrlRepository = shortUrlRepository;
        this.shortCodeGenerator = shortCodeGenerator;
    }

    /**
     * Creates a short URL for the given original URL.
     * If the URL already exists in the database, returns the existing short URL.
     * Otherwise, generates a unique short code, handles collisions, and saves to the database.
     *
     * @param originalUrl the original URL to shorten
     * @return the ShortUrl entity with the generated short code
     * @throws IllegalArgumentException if unable to generate a unique code after max retries
     */
    public ShortUrl createShortUrl(String originalUrl) {
        // Check if the URL already exists
        Optional<ShortUrl> existingUrl = shortUrlRepository.findByOriginalUrl(originalUrl);
        if (existingUrl.isPresent()) {
            return existingUrl.get();
        }

        // Generate a unique short code with collision handling
        String shortCode = generateUniqueShortCode();
        
        // Create and save the new short URL
        ShortUrl shortUrl = new ShortUrl();
        shortUrl.setShortCode(shortCode);
        shortUrl.setOriginalUrl(originalUrl);

        return shortUrlRepository.save(shortUrl);
    }

    /**
     * Generates a unique short code, handling collisions by retrying if a code already exists.
     *
     * @return a unique short code
     * @throws IllegalArgumentException if unable to generate a unique code after max retries
     */
    private String generateUniqueShortCode() {
        for (int attempt = 0; attempt < MAX_RETRIES; attempt++) {
            String shortCode = shortCodeGenerator.generateShortCode();
            
            // Check if the code already exists
            Optional<ShortUrl> existing = shortUrlRepository.findByShortCode(shortCode);
            if (existing.isEmpty()) {
                return shortCode;
            }
        }
        
        throw new IllegalArgumentException("Unable to generate a unique short code after " + MAX_RETRIES + " attempts");
    }

    /**
     * Retrieves the original URL for the given short code.
     *
     * @param shortCode the short code to look up
     * @return an Optional containing the ShortUrl if found, or empty if not found
     */
    @Transactional(readOnly = true)
    public ShortUrl getOriginalUrl(String shortCode) {

        return shortUrlRepository.findByShortCode(shortCode)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Short URL not found."));
    }
}
