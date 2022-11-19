package ru.practicum.shareit.booking.exception;

public class ApprovalAlreadySetException extends RuntimeException {
    public ApprovalAlreadySetException(String message) {
        super(message);
    }
}
