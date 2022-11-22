package ru.practicum.shareit.booking.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoRequest;
import ru.practicum.shareit.booking.service.BookingService;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.Collection;

@Validated
@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public ResponseEntity<BookingDto> addBooking(@RequestHeader(name = "X-Sharer-User-Id") long bookerId,
                                                 @RequestBody @Valid BookingDtoRequest bookingDtoRequest) {

        return new ResponseEntity<>(bookingService.addBooking(bookingDtoRequest, bookerId), HttpStatus.CREATED);
    }

    @GetMapping(path = "/{bookingId}")
    public ResponseEntity<BookingDto> getBooking(@RequestHeader(name = "X-Sharer-User-Id") long requesterId,
                                                 @PathVariable long bookingId) {
        return ResponseEntity.ok(bookingService.getBookingDto(bookingId, requesterId));
    }

    @GetMapping
    public ResponseEntity<Collection<BookingDto>> getBookingsByBookerAndStatus(
            @RequestHeader(name = "X-Sharer-User-Id") long bookerId,
            @RequestParam(required = false) String state,
            @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
            @RequestParam(required = false) @Positive Integer size) {

        if (size == null) {
            size = Integer.MAX_VALUE;
        }
        return ResponseEntity.ok(bookingService.getBookingsByUserAndState(
                bookerId, null, state, from, size));
    }

    @GetMapping(path = "/owner")
    public ResponseEntity<Collection<BookingDto>> getBookingsByOwnerAndStatus(
            @RequestHeader(name = "X-Sharer-User-Id") long ownerId,
            @RequestParam(required = false) String state,
            @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
            @RequestParam(required = false) @Positive Integer size) {

        if (size == null) {
            size = Integer.MAX_VALUE;
        }
        return ResponseEntity.ok(bookingService.getBookingsByUserAndState(
                null, ownerId, state, from, size));
    }

    @PatchMapping("/{bookingId}")
    public ResponseEntity<BookingDto> setBookingApproval(@RequestHeader(name = "X-Sharer-User-Id") long requesterId,
                                                         @PathVariable long bookingId,
                                                         @RequestParam boolean approved) {

        return ResponseEntity.ok(bookingService.setApproval(bookingId, approved, requesterId));
    }
}
