package ru.practicum.shareit.item.exception;

public class WrongOwnerUpdatingItemException extends RuntimeException {
    public WrongOwnerUpdatingItemException(String message) {
        super(message);
    }
}
