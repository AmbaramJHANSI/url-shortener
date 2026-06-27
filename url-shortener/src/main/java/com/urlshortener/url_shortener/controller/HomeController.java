package com.urlshortener.url_shortener.controller;

import com.urlshortener.url_shortener.dto.ShortUrlRequest;
import com.urlshortener.url_shortener.dto.ShortenUrlResponse;
import com.urlshortener.url_shortener.entity.ShortUrl;
import com.urlshortener.url_shortener.service.UrlShortenerService;
import com.urlshortener.url_shortener.service.QRCodeService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Controller
public class HomeController {

    private final UrlShortenerService urlShortenerService;
    private final QRCodeService qrCodeService;

    public HomeController(
            UrlShortenerService urlShortenerService,
            QRCodeService qrCodeService) {
        this.urlShortenerService = urlShortenerService;
        this.qrCodeService = qrCodeService;
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
    
    @GetMapping("/dashboard")
    public String dashboard(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String sort,
            Model model) {

        List<ShortUrl> urls;

        if (search != null && !search.isBlank()) {

            urls = urlShortenerService.searchByShortCode(search);

            if (urls.isEmpty()) {
                urls = urlShortenerService.searchByOriginalUrl(search);
            }

        } else if ("clicks".equals(sort)) {

            urls = urlShortenerService.sortByClicksDesc();

        } else if ("least".equals(sort)) {

            urls = urlShortenerService.sortByClicksAsc();

        } else {

            urls = urlShortenerService.getAllUrls();
        }

        model.addAttribute("urls", urls);

        model.addAttribute("totalUrls",
                urlShortenerService.getTotalUrls());

        model.addAttribute("totalClicks",
                urlShortenerService.getTotalClicks());

        model.addAttribute("topUrls",
                urlShortenerService.getTopUrls());

        return "dashboard";
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
                ShortUrl shortUrl = urlShortenerService.createShortUrl(
                    shortUrlRequest.getUrl(),
                    shortUrlRequest.getCustomAlias(),
                    shortUrlRequest.getExpiryHours()
                );

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
                response.setQrCode(
                    qrCodeService.generateQRCode(fullShortUrl)
                );
            response.setOriginalUrl(shortUrl.getOriginalUrl());
            response.setCreatedAt(shortUrl.getCreatedAt());
            response.setExpiresAt(shortUrl.getExpiresAt());
            response.setClickCount(shortUrl.getClickCount());
            response.setLastAccessed(shortUrl.getLastAccessed());

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
    public String redirectToOriginalUrl(
            @PathVariable String shortCode,
            Model model) {
        try {
            ShortUrl shortUrl = urlShortenerService.getOriginalUrl(shortCode);
            return "redirect:" + shortUrl.getOriginalUrl();
        } catch (Exception e) {
            model.addAttribute("message", e.getMessage());
            return "expired";
        }
    }
}
