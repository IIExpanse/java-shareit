package ru.practicum.shareit.user.exception;

public class EmptyUserPatchRequestException extends RuntimeException {

    public EmptyUserPatchRequestException(String message) {
        super(message);
    }
}
