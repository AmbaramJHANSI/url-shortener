package com.urlshortener.url_shortener.controller;

import com.urlshortener.url_shortener.ShortUrl;
import com.urlshortener.url_shortener.UrlShortenerService;
import com.urlshortener.url_shortener.dto.ShortUrlRequest;
import com.urlshortener.url_shortener.dto.ShortenUrlResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;
import java.util.Optional;

@Controller
public class HomeController {

    private final UrlShortenerService urlShortenerService;

    @Autowired
    public HomeController(UrlShortenerService urlShortenerService) {
        this.urlShortenerService = urlShortenerService;
    }

    /**
     * Displays the URL shortener form page.
     *
     * @return the form page template
     */
    @GetMapping("/")
    public String showForm(Model model) {
        model.addAttribute("shortUrlRequest", new ShortUrlRequest());
        return "index";
    }

    /**
     * Processes the form submission to create a shortened URL.
     *
     * @param shortUrlRequest the request object containing the URL to shorten
     * @param model the Spring UI model
     * @param request the HTTP request to construct the short URL
     * @return the result page template
     */
    @PostMapping("/shorten")
    public String shortenUrl(@Valid @ModelAttribute ShortUrlRequest shortUrlRequest, Model model, HttpServletRequest request) {
        try {
            // Create the short URL
            ShortUrl shortUrl = urlShortenerService.createShortUrl(shortUrlRequest.getUrl());

            // Construct the full short URL
            String baseUrl = request.getScheme() + "://" + request.getServerName();
            if (request.getServerPort() != 80 && request.getServerPort() != 443) {
                baseUrl += ":" + request.getServerPort();
            }
            String fullShortUrl = baseUrl + "/" + shortUrl.getShortCode();

            // Create the response DTO
            ShortenUrlResponse response = new ShortenUrlResponse();
            response.setShortCode(shortUrl.getShortCode());
            response.setShortUrl(fullShortUrl);
            response.setOriginalUrl(shortUrl.getOriginalUrl());
            response.setCreatedAt(shortUrl.getCreatedAt());
            response.setExpiresAt(shortUrl.getExpiresAt());

            model.addAttribute("response", response);
            return "result";
        } catch (Exception e) {
            model.addAttribute("error", "Failed to shorten URL: " + e.getMessage());
            model.addAttribute("shortUrlRequest", shortUrlRequest);
            return "index";
        }
    }

    /**
     * Redirects to the original URL for the given short code.
     *
     * @param shortCode the short code of the URL
     * @return a RedirectView to the original URL or error page if not found
     */
    @GetMapping("/{shortCode}")
    public RedirectView redirectToOriginalUrl(@PathVariable String shortCode) {
        Optional<ShortUrl> shortUrl = urlShortenerService.getOriginalUrl(shortCode);

        if (shortUrl.isPresent()) {
            return new RedirectView(shortUrl.get().getOriginalUrl());
        } else {
            return new RedirectView("/");
        }
    }
}
