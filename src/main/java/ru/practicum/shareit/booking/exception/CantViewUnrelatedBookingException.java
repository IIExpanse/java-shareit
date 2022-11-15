package ru.practicum.shareit.booking.exception;

public class CantViewUnrelatedBookingException extends RuntimeException {
    public CantViewUnrelatedBookingException(String message) {
        super(message);
    }
}
