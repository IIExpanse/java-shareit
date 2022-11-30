package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookItemRequestDto;
import ru.practicum.shareit.booking.dto.BookingState;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

@Controller
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
@Slf4j
@Validated
public class BookingController {
    private final BookingClient bookingClient;

    @PostMapping
    public ResponseEntity<Object> addBooking(@RequestHeader("X-Sharer-User-Id") long userId,
                                             @RequestBody @Valid BookItemRequestDto requestDto) {

        log.info("Creating booking {}, userId={}", requestDto, userId);
        return bookingClient.addBooking(userId, requestDto);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<Object> getBooking(@RequestHeader("X-Sharer-User-Id") long userId,
                                             @PathVariable long bookingId) {

        log.info("Get booking {}, userId={}", bookingId, userId);
        return bookingClient.getBooking(userId, bookingId);
    }

    @GetMapping
    public ResponseEntity<Object> getBookingsByBookerAndStatus(
            @RequestHeader("X-Sharer-User-Id") long userId,
            @RequestParam(name = "state", defaultValue = "all") String stateParam,
            @PositiveOrZero @RequestParam(name = "from", defaultValue = "0") int from,
            @Positive @RequestParam(name = "size", defaultValue = "10") int size) {

        BookingState state = BookingState.from(stateParam)
                .orElseThrow(() -> new IllegalArgumentException("Unknown state: " + stateParam));
        log.info("Get booking by booker with state {}, userId={}, from={}, size={}", stateParam, userId, from, size);
        return bookingClient.getBookingsByBookerAndStatus(userId, state, from, size);
    }

    @GetMapping(path = "/owner")
    public ResponseEntity<Object> getBookingsByOwnerAndStatus(
            @RequestHeader("X-Sharer-User-Id") long userId,
            @RequestParam(name = "state", defaultValue = "all") String stateParam,
            @PositiveOrZero @RequestParam(name = "from", defaultValue = "0") int from,
            @Positive @RequestParam(name = "size", defaultValue = "10") int size) {

        BookingState state = BookingState.from(stateParam)
                .orElseThrow(() -> new IllegalArgumentException("Unknown state: " + stateParam));
        log.info("Get booking by owner with state {}, userId={}, from={}, size={}", stateParam, userId, from, size);
        return bookingClient.getBookingsByOwnerAndStatus(userId, state, from, size);
    }

    @PatchMapping("/{bookingId}")
    public ResponseEntity<Object> setBookingApproval(@RequestHeader(name = "X-Sharer-User-Id") long requesterId,
                                                     @PathVariable long bookingId,
                                                     @RequestParam boolean approved) {

        log.info("Set booking approval with ownerId={}, bookingId={}, approved={}", requesterId, bookingId, approved);
        return bookingClient.setApproval(requesterId, bookingId, approved);
    }
}
