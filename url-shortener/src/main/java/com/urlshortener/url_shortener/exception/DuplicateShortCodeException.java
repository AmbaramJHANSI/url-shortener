package com.urlshortener.url_shortener.exception;

public class DuplicateShortCodeException extends RuntimeException {

    public DuplicateShortCodeException(String message) {
        super(message);
    }

}