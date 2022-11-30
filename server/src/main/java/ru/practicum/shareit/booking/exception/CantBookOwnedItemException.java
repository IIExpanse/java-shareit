package ru.practicum.shareit.booking.exception;

public class CantBookOwnedItemException extends RuntimeException {
    public CantBookOwnedItemException(String message) {
        super(message);
    }
}
