package com.urlshortener.url_shortener.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.URL;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShortUrlRequest {

    @NotBlank(message = "URL cannot be blank")
    @URL(message = "URL must be valid")
    private String url;

    /**
     * Optional custom alias.
     * Examples:
     * github
     * google
     * myportfolio
     */
    @Pattern(
            regexp = "^[a-zA-Z0-9_-]*$",
            message = "Alias can contain only letters, numbers, hyphens and underscores."
    )
    private String customAlias;

    /**
     * Optional expiry time in hours. Leave null for permanent URLs.
     */
    private Integer expiryHours;
}