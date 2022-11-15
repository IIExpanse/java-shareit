package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ActualItemBooking;

import java.util.Collection;
import java.util.Map;

public interface BookingService {

    boolean isCommentMadeAfterBooking(long bookerId, long itemId);

    Booking addBooking(Booking booking);

    Booking getBooking(long bookingId);

    Collection<Booking> getBookingsByBookerIdOrOwnerIdAndStatusSortedByDateDesc(
            Long bookerId, Long ownerId, BookingStatus status);

    Booking setApproval(long bookingId, boolean approved);

    Map<ActualItemBooking, Booking> getLastAndNextBookingByItem(Item item);

    BookingStatus determineStatus(Booking booking);
}
