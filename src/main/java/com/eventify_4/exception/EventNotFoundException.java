package com.eventify_4.exception;

public class EventNotFoundException extends ResourceNotFoundException {
    public EventNotFoundException(Long id) {
        super("Evento no encontrado con id: " + id);
    }

    public EventNotFoundException(String message) {
        super(message);
    }
}
