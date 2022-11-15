package ru.practicum.shareit.booking.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoRequest;
import ru.practicum.shareit.booking.exception.ApprovalAlreadySetException;
import ru.practicum.shareit.booking.exception.CantViewUnrelatedBookingException;
import ru.practicum.shareit.booking.exception.EndBeforeOrEqualsStartException;
import ru.practicum.shareit.booking.exception.WrongUserUpdatingBooking;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.booking.service.BookingStatus;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.exception.UserNotFoundException;
import ru.practicum.shareit.user.service.UserService;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

import static ru.practicum.shareit.booking.service.BookingStatus.*;

@Validated
@RestController
@RequestMapping(path = "/bookings")
@Slf4j
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;
    private final UserService userService;
    private final ItemService itemService;
    private final BookingMapper mapper;

    @PostMapping
    public ResponseEntity<BookingDto> addBooking(@RequestHeader(name = "X-Sharer-User-Id") long bookerId,
                                                 @RequestBody @Valid BookingDtoRequest bookingDto) {
        LocalDateTime start = bookingDto.getStart();
        LocalDateTime end = bookingDto.getEnd();
        Booking booking;
        BookingDto bookingDtoResponse;

        if (end.isBefore(start) || end.equals(start)) {
            throw new EndBeforeOrEqualsStartException(String.format(
                    "Ошибка при добавлении бронирования для вещи с id=%d от пользователя с id=%d: " +
                            "дата окончания бронирования раньше или равна дате начала.",
                    bookingDto.getItemId(),
                    bookerId
            ));
        }
        booking = mapper.mapToModel(
                bookingDto,
                userService.getUser(bookerId),
                itemService.getItem(bookingDto.getItemId()));
        booking = bookingService.addBooking(booking);

        bookingDtoResponse = mapper.mapToDto(booking, bookingService.determineStatus(booking));

        log.debug("Добавлено новое бронирование: {}", booking);
        return new ResponseEntity<>(bookingDtoResponse, HttpStatus.CREATED);
    }

    @GetMapping(path = "/{bookingId}")
    public ResponseEntity<BookingDto> getBooking(@RequestHeader(name = "X-Sharer-User-Id") long requesterId,
                                                 @PathVariable long bookingId) {
        Booking booking = bookingService.getBooking(bookingId);
        BookingDto bookingDtoResponse;

        if (requesterId == booking.getBooker().getId() || requesterId == booking.getItem().getOwner().getId()) {
            bookingDtoResponse = mapper.mapToDto(booking, bookingService.determineStatus(booking));

            return ResponseEntity.ok(bookingDtoResponse);

        } else throw new CantViewUnrelatedBookingException(
                String.format("Ошибка: попытка получения информации о бронировании с id=%d пользователем с id=%d, " +
                        "не являющимся автором бронирования или владельцем вещи.", bookingId, requesterId
                ));
    }

    @GetMapping
    public ResponseEntity<Collection<BookingDto>> getBookingsByBookerAndStatus(
            @RequestHeader(name = "X-Sharer-User-Id") long bookerId,
            @RequestParam(required = false) String state) {
        if (!userService.userExists(bookerId)) {
            throw new UserNotFoundException(
                    String.format("Ошибка при получении бронирований по автору: " +
                            "пользователя с id=%d не существует.", bookerId));
        }

        BookingStatus status = parseStatus(state);

        return ResponseEntity.ok(bookingService.getBookingsByBookerIdOrOwnerIdAndStatusSortedByDateDesc(
                        bookerId, null, status).stream()
                .map(booking -> mapper.mapToDto(booking, bookingService.determineStatus(booking)))
                .collect(Collectors.toCollection(ArrayList::new))
        );
    }

    @GetMapping(path = "/owner")
    public ResponseEntity<Collection<BookingDto>> getBookingsByOwnerAndStatus(
            @RequestHeader(name = "X-Sharer-User-Id") long ownerId,
            @RequestParam(required = false) String state) {
        if (!userService.userExists(ownerId)) {
            throw new UserNotFoundException(
                    String.format("Ошибка при получении бронирований по владельцу вещи: " +
                            "пользователя с id=%d не существует.", ownerId));
        }

        BookingStatus status = parseStatus(state);

        return ResponseEntity.ok(bookingService.getBookingsByBookerIdOrOwnerIdAndStatusSortedByDateDesc(
                        null, ownerId, status).stream()
                .map(booking -> mapper.mapToDto(booking, bookingService.determineStatus(booking)))
                .collect(Collectors.toCollection(ArrayList::new))
        );
    }

    @PatchMapping("/{bookingId}")
    public ResponseEntity<BookingDto> setBookingApproval(@RequestHeader(name = "X-Sharer-User-Id") long requesterId,
                                                         @PathVariable long bookingId,
                                                         @RequestParam boolean approved) {
        Booking booking = bookingService.getBooking(bookingId);
        if (booking.getItem().getOwner().getId() != requesterId) {
            throw new WrongUserUpdatingBooking(String.format("Ошибка: попытка изменить статус одобрения бронирования " +
                    "со стороны пользователя с id=%d, не являющегося владельцем бронируемой вещи.", requesterId));

        } else if (booking.getApproved() != null) {
            throw new ApprovalAlreadySetException(String.format(
                    "Ошибка: статус одобрения бронирования с id=%d уже был изменен ранее.", bookingId));
        }
        booking = bookingService.setApproval(bookingId, approved);

        log.debug("Одобрение бронирования с id={} изменено на {}", bookingId, approved);
        return ResponseEntity.ok(mapper.mapToDto(booking, bookingService.determineStatus(booking)));
    }

    private BookingStatus parseStatus(String state) {
        BookingStatus status;

        if (state != null) {
            try {
                status = BookingStatus.valueOf(state.toUpperCase());

            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Unknown state: UNSUPPORTED_STATUS");
            }
        } else {
            status = ALL;
        }
        return status;
    }
}
