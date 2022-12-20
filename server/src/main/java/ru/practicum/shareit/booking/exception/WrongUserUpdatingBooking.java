package ru.practicum.shareit.booking.exception;

public class WrongUserUpdatingBooking extends RuntimeException {
    public WrongUserUpdatingBooking(String message) {
        super(message);
    }
}
