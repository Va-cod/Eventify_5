package com.eventify_4.exception;

public class VenueNotFoundException extends ResourceNotFoundException {
    public VenueNotFoundException(Long id) {
        super("Lugar no encontrado con id: " + id);
    }

    public VenueNotFoundException(String message) {
        super(message);
    }
}
