package com.urlshortener.url_shortener.dto;

import org.hibernate.validator.constraints.URL;
import jakarta.validation.constraints.NotBlank;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShortUrlRequest {

    @NotBlank(message = "URL cannot be blank")
    @URL(message = "URL must be a valid URL format")
    private String url;
}
