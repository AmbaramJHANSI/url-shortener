package com.urlshortener.url_shortener.service;

import com.urlshortener.url_shortener.entity.ShortUrl;
import com.urlshortener.url_shortener.exception.ResourceNotFoundException;
import com.urlshortener.url_shortener.repository.ShortUrlRepository;
import com.urlshortener.url_shortener.util.ShortCodeGenerator;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Optional;
import java.util.List;

@Service
@Transactional
public class UrlShortenerService {

    private static final int MAX_RETRIES = 10;

    private final ShortUrlRepository shortUrlRepository;
    private final ShortCodeGenerator shortCodeGenerator;
    private final RedisTemplate<String, String> redisTemplate;

    public UrlShortenerService(
            ShortUrlRepository shortUrlRepository,
            ShortCodeGenerator shortCodeGenerator,
            RedisTemplate<String, String> redisTemplate) {

        this.shortUrlRepository = shortUrlRepository;
        this.shortCodeGenerator = shortCodeGenerator;
        this.redisTemplate = redisTemplate;
    }

    /**
     * Creates a short URL for the given original URL.
     * If the URL already exists, return the existing one.
     * Otherwise generate a unique short code, save it to MySQL,
     * and cache it in Redis.
     */
        public ShortUrl createShortUrl(
            String originalUrl,
            String customAlias,
            Integer expiryHours) {

        // Check if the URL already exists
        Optional<ShortUrl> existingUrl = shortUrlRepository.findByOriginalUrl(originalUrl);

        if (existingUrl.isPresent()) {

            // Load existing URL into Redis
            redisTemplate.opsForValue().set(
                    existingUrl.get().getShortCode(),
                    existingUrl.get().getOriginalUrl(),
                    Duration.ofHours(1)
            );

            System.out.println("Existing URL loaded into Redis.");

            return existingUrl.get();
        }

        String shortCode;
        // If user entered a custom alias
        if (customAlias != null && !customAlias.trim().isEmpty()) {
            Optional<ShortUrl> existingAlias =
                    shortUrlRepository.findByShortCode(customAlias);
            if (existingAlias.isPresent()) {
                throw new IllegalArgumentException(
                        "Custom alias already exists. Please choose another."
                );
            }
            shortCode = customAlias.trim();
        } else {
            shortCode = generateUniqueShortCode();
        }

        // Create new entity
        ShortUrl shortUrl = new ShortUrl();
        shortUrl.setShortCode(shortCode);
        shortUrl.setOriginalUrl(originalUrl);
        // Set expiration time (optional)
        if (expiryHours != null && expiryHours > 0) {
            shortUrl.setExpiresAt(
                java.time.LocalDateTime.now()
                    .plusHours(expiryHours)
            );
        }

        // Save into MySQL
        ShortUrl savedUrl = shortUrlRepository.save(shortUrl);

        // Store into Redis
        redisTemplate.opsForValue().set(
                savedUrl.getShortCode(),
                savedUrl.getOriginalUrl(), 
                Duration.ofHours(1)
        );

        System.out.println("Stored in Redis: " + savedUrl.getShortCode());

        return savedUrl;
    }

    /**
     * Generates a unique short code.
     */
    private String generateUniqueShortCode() {

        for (int attempt = 0; attempt < MAX_RETRIES; attempt++) {
            String shortCode = shortCodeGenerator.generateShortCode();
            Optional<ShortUrl> existing =
                    shortUrlRepository.findByShortCode(shortCode);

            if (existing.isEmpty()) {
                return shortCode;
            }
        }

        throw new IllegalArgumentException(
                "Unable to generate a unique short code after "
                        + MAX_RETRIES + " attempts");
    }

    /**
     * Retrieves the original URL.
     * First checks Redis cache.
     * If not found, retrieves from MySQL and caches it.
     */
    
    @Transactional
    public ShortUrl getOriginalUrl(String shortCode) {

        // ===========================
        // Step 1 : Check Redis Cache
        // ===========================
        String cachedUrl = redisTemplate.opsForValue().get(shortCode);

        if (cachedUrl != null) {

            System.out.println("========== CACHE HIT ==========");

            // Load entity from MySQL for analytics
            ShortUrl shortUrl = shortUrlRepository.findByShortCode(shortCode)
                    .orElseThrow(() ->
                            new ResourceNotFoundException("Short URL not found."));

            // Check expiration
            if (shortUrl.getExpiresAt() != null &&
                shortUrl.getExpiresAt().isBefore(java.time.LocalDateTime.now())) {
            throw new ResourceNotFoundException(
                "This short URL has expired."
            );
            }

            // Update analytics
            shortUrl.setClickCount(shortUrl.getClickCount() + 1);
            shortUrl.setLastAccessed(java.time.LocalDateTime.now());

            shortUrlRepository.save(shortUrl);

            return shortUrl;
        }

        // ===========================
        // Step 2 : Cache Miss
        // ===========================
        System.out.println("========== CACHE MISS ==========");

        ShortUrl shortUrl = shortUrlRepository.findByShortCode(shortCode)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Short URL not found."));

        // Check expiration
        if (shortUrl.getExpiresAt() != null &&
            shortUrl.getExpiresAt().isBefore(java.time.LocalDateTime.now())) {
            throw new ResourceNotFoundException(
                "This short URL has expired."
            );
        }

        // Update analytics
        shortUrl.setClickCount(shortUrl.getClickCount() + 1);
        shortUrl.setLastAccessed(java.time.LocalDateTime.now());

        shortUrlRepository.save(shortUrl);

        // Store into Redis
        redisTemplate.opsForValue().set(
                shortCode,
                shortUrl.getOriginalUrl(),
                Duration.ofHours(1)
        );
        System.out.println("Saved to Redis: " + shortCode);
        return shortUrl;
    }

    @Transactional(readOnly = true)
    public ShortUrl getAnalytics(String shortCode) {

        return shortUrlRepository.findByShortCode(shortCode)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Short URL not found."));
    }

    @Transactional(readOnly = true)
    public List<ShortUrl> getAllUrls() {
        return shortUrlRepository.findAllByOrderByCreatedAtDesc();
    }

    public long getTotalUrls() {

        return shortUrlRepository.count();
    }

    public long getTotalClicks() {

        return shortUrlRepository.findAll()

                .stream()

                .mapToLong(url -> url.getClickCount())

                .sum();
    }

    public List<ShortUrl> getTopUrls() {

        return shortUrlRepository.findTop5ByOrderByClickCountDesc();
    }

    @Transactional(readOnly = true)
    public List<ShortUrl> searchByShortCode(String keyword) {

        return shortUrlRepository
                .findByShortCodeContainingIgnoreCase(keyword);
    }

    @Transactional(readOnly = true)
    public List<ShortUrl> searchByOriginalUrl(String keyword) {

        return shortUrlRepository
                .findByOriginalUrlContainingIgnoreCase(keyword);
    }

    @Transactional(readOnly = true)
    public List<ShortUrl> sortByClicksDesc() {

        return shortUrlRepository.findAllByOrderByClickCountDesc();
    }

    @Transactional(readOnly = true)
    public List<ShortUrl> sortByClicksAsc() {

        return shortUrlRepository.findAllByOrderByClickCountAsc();
    }
}