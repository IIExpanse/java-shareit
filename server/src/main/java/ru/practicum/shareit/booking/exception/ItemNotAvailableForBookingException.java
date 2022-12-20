package ru.practicum.shareit.booking.exception;

public class ItemNotAvailableForBookingException extends RuntimeException {
    public ItemNotAvailableForBookingException(String message) {
        super(message);
    }
}
