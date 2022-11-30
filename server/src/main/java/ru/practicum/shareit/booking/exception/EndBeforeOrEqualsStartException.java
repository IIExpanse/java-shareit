package ru.practicum.shareit.booking.exception;

public class EndBeforeOrEqualsStartException extends RuntimeException {
    public EndBeforeOrEqualsStartException(String message) {
        super(message);
    }
}
