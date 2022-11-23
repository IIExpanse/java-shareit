package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoRequest;
import ru.practicum.shareit.booking.dto.BookingDtoShort;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ActualItemBooking;

import java.util.Collection;
import java.util.Map;

public interface BookingService {

    boolean neverMadeBookings(long bookerId, long itemId);

    BookingDto addBooking(BookingDtoRequest bookingDtoRequest, long bookerId);

    BookingDto getBookingDto(long bookingId, long requesterId);

    Collection<BookingDto> getBookingsByUserAndState(
            Long bookerId, Long ownerId, String state, int startingIndex, Integer collectionSize);

    BookingDto setApproval(long bookingId, boolean approved, long requesterId);

    Map<ActualItemBooking, BookingDtoShort> getLastAndNextBookingByItem(Item item, long requesterId);
}
