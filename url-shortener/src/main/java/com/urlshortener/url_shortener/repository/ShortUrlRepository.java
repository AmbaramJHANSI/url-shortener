package com.urlshortener.url_shortener.repository;

import com.urlshortener.url_shortener.entity.ShortUrl;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShortUrlRepository extends JpaRepository<ShortUrl, Long> {

    Optional<ShortUrl> findByShortCode(String shortCode);

    Optional<ShortUrl> findByOriginalUrl(String originalUrl);

    List<ShortUrl> findAllByOrderByCreatedAtDesc();

    List<ShortUrl> findTop5ByOrderByClickCountDesc();

    List<ShortUrl> findByShortCodeContainingIgnoreCase(String shortCode);

    List<ShortUrl> findByOriginalUrlContainingIgnoreCase(String originalUrl);

    List<ShortUrl> findAllByOrderByClickCountDesc();

    List<ShortUrl> findAllByOrderByClickCountAsc();

    

}