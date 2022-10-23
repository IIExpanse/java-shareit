package ru.practicum.shareit.item.exception;

public class EmptyItemPatchRequestException extends RuntimeException {
    public EmptyItemPatchRequestException(String message) {
        super(message);
    }
}
