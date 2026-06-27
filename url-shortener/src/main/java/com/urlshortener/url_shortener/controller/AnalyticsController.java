package com.urlshortener.url_shortener.controller;

import com.urlshortener.url_shortener.dto.AnalyticsResponse;
import com.urlshortener.url_shortener.entity.ShortUrl;
import com.urlshortener.url_shortener.service.UrlShortenerService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/analytics")
public class AnalyticsController {

    private final UrlShortenerService urlShortenerService;

    public AnalyticsController(UrlShortenerService urlShortenerService) {
        this.urlShortenerService = urlShortenerService;
    }

    @GetMapping("/{shortCode}")
    public AnalyticsResponse getAnalytics(@PathVariable String shortCode) {

        ShortUrl shortUrl = urlShortenerService.getAnalytics(shortCode);

        AnalyticsResponse response = new AnalyticsResponse();

        response.setShortCode(shortUrl.getShortCode());
        response.setOriginalUrl(shortUrl.getOriginalUrl());
        response.setClickCount(shortUrl.getClickCount());
        response.setCreatedAt(shortUrl.getCreatedAt());
        response.setLastAccessed(shortUrl.getLastAccessed());

        return response;
    }
}