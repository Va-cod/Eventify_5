package com.eventify_4.exception;

public class VenueAlreadyBookedException extends RuntimeException {
    public VenueAlreadyBookedException(String message) {
        super(message);
    }
}
