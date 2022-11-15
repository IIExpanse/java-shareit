package ru.practicum.shareit.booking.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.exception.BookingNotFoundException;
import ru.practicum.shareit.booking.exception.CantBookOwnedItemException;
import ru.practicum.shareit.booking.exception.ItemNotAvailableForBookingException;
import ru.practicum.shareit.booking.exception.TimeWindowOccupiedException;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.booking.service.BookingStatus;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ActualItemBooking;
import ru.practicum.shareit.item.service.ItemService;

import java.time.LocalDateTime;
import java.util.*;

import static ru.practicum.shareit.booking.service.BookingStatus.*;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final ItemService itemService;

    @Override
    public boolean isCommentMadeAfterBooking(long bookerId, long itemId) {
        return !bookingRepository.getApprovedBookingsByBookerIdAndItemIdNotInFuture(bookerId, itemId).isEmpty();
    }

    @Override
    public Booking addBooking(Booking booking) {
        Item item = booking.getItem();
        long itemId = item.getId();
        long itemOwnerId = item.getOwner().getId();
        long bookerId = booking.getBooker().getId();

        if (itemOwnerId != bookerId && itemService.isItemAvailable(itemId)) {
            if (isTimeWindowFree(booking)) {
                return bookingRepository.save(booking);

            } else throw new TimeWindowOccupiedException(
                    String.format("Ошибка при добавлении бронирования с %s по %s: " +
                                    "временной промежуток полностью или частично занят.",
                            booking.getStartTime(), booking.getEndTime()));

        } else if (itemOwnerId == bookerId) {
            throw new CantBookOwnedItemException(String.format(
                    "Ошибка добавления бронирования: " +
                            "попытка пользователя с id=%d забронировать собственную вещь.", bookerId
            ));

        } else throw new ItemNotAvailableForBookingException(
                String.format("Ошибка добавления бронирования: " +
                        "вещь с id=%d недоступна для бронирования.", itemId));
    }

    @Override
    public Booking getBooking(long bookingId) {
        Optional<Booking> bookingOptional = bookingRepository.findById(bookingId);

        if (bookingOptional.isPresent()) {
            return bookingOptional.get();
        } else throw new BookingNotFoundException(String.format(
                "Ошибка при получении бронирования: объект с id=%d не найден.", bookingId));
    }

    @Override
    public Collection<Booking> getBookingsByBookerIdOrOwnerIdAndStatusSortedByDateDesc(
            Long bookerId, Long ownerId, BookingStatus status) {

        if (status == ALL) {
            return bookingRepository.getAllByBookerIdOrItemOwnerIdOrderByStartTimeDesc(bookerId, ownerId);

        } else if (status == WAITING) {
            return bookingRepository.getAllByBookerIdOrItemOwnerIdAndApprovedIsOrderByStartTimeDesc(
                    bookerId, ownerId, null);

        } else if (status == REJECTED) {
            return bookingRepository.getAllByBookerIdOrItemOwnerIdAndApprovedIsOrderByStartTimeDesc(
                    bookerId, ownerId, false);

        } else if (status == PAST) {
            return bookingRepository.getPastBookingsByBookerIdOrOwnerId(bookerId, ownerId);

        } else if (status == FUTURE) {
            return bookingRepository.getFutureBookingsByBookerIdOrOwnerId(bookerId, ownerId);

        } else return bookingRepository.getCurrentBookingsByBookerIdOrOwnerId(bookerId, ownerId);

    }

    @Override
    public Booking setApproval(long bookingId, boolean approved) {
        Booking booking = getBooking(bookingId);
        booking.setApproved(approved);

        return bookingRepository.save(booking);
    }

    private boolean isTimeWindowFree(Booking booking) {
        List<Booking> activeBookings = new ArrayList<>(
                bookingRepository.getActiveBookingsByItemIdOrderByStartTimeAsc(booking.getItem().getId()));
        final LocalDateTime startTime = booking.getStartTime();
        final LocalDateTime endTime = booking.getEndTime();
        LocalDateTime leftBorder = LocalDateTime.now();
        LocalDateTime rightBorder;

        if (activeBookings.size() > 0) {
            for (Booking activeBooking : activeBookings) {
                rightBorder = activeBooking.getStartTime();

                if (leftBorder.isBefore(startTime) && rightBorder.isAfter(endTime)) {
                    return true;

                } else if (leftBorder.isAfter(endTime)) {
                    return false;
                }
                leftBorder = rightBorder;
            }
            return leftBorder.isBefore(startTime);

        } else return true;
    }

    @Override
    public Map<ActualItemBooking, Booking> getLastAndNextBookingByItem(Item item) {
        List<Booking> currentAndFutureBookings = new ArrayList<>(
                bookingRepository.getActiveBookingsByItemIdOrderByStartTimeAsc(item.getId()));
        Map<ActualItemBooking, Booking> lastAndNextBooking = new HashMap<>();
        Booking lastBooking = null;
        Booking nextBooking = null;

        if (currentAndFutureBookings.size() > 1) {
            if (currentAndFutureBookings.get(0).getStartTime().isBefore(LocalDateTime.now())) {
                lastBooking = currentAndFutureBookings.get(0);
                nextBooking = currentAndFutureBookings.get(1);

            } else {
                nextBooking = currentAndFutureBookings.get(0);
                lastBooking = getLastBooking(item);
            }

        } else if (currentAndFutureBookings.size() == 1) {
            if (currentAndFutureBookings.get(0).getStartTime().isBefore(LocalDateTime.now())) {
                lastBooking = currentAndFutureBookings.get(0);

            } else {
                nextBooking = currentAndFutureBookings.get(0);
                lastBooking = getLastBooking(item);
            }
        }
        lastAndNextBooking.put(ActualItemBooking.LAST, lastBooking);
        lastAndNextBooking.put(ActualItemBooking.NEXT, nextBooking);

        return lastAndNextBooking;
    }

    @Override
    public BookingStatus determineStatus(Booking booking) {
        Boolean approved = booking.getApproved();

        if (approved == null) {
            return WAITING;

        } else if (!approved) {
            return REJECTED;

        } else return APPROVED;
    }

    private Booking getLastBooking(Item item) {
        List<Booking> pastBookings = new ArrayList<>(bookingRepository.getPastBookingsByItemId(item.getId()));
        Booking lastBooking = null;

        if (!pastBookings.isEmpty()) {
            lastBooking = pastBookings.get(0);

        }
        return lastBooking;
    }
}
