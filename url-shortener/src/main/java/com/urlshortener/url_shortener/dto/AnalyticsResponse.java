package com.urlshortener.url_shortener.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AnalyticsResponse {

    private String shortCode;

    private String originalUrl;

    private Long clickCount;

    private LocalDateTime createdAt;

    private LocalDateTime lastAccessed;
}