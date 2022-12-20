package ru.practicum.shareit.booking.exception;

public class CommenterDontHaveBookingException extends RuntimeException {
    public CommenterDontHaveBookingException(String message) {
        super(message);
    }
}
