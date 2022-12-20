package ru.practicum.shareit.booking.exception;

public class TimeWindowOccupiedException extends RuntimeException {
    public TimeWindowOccupiedException(String message) {
        super(message);
    }
}
