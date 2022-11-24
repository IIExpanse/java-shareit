package ru.practicum.shareit.booking.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoRequest;
import ru.practicum.shareit.booking.exception.*;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.item.exception.ItemNotFoundException;
import ru.practicum.shareit.user.exception.UserNotFoundException;

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

    /**
     * Добавление нового бронирования.
     *
     * @param bookerId          - идентификатор пользователя.
     * @param bookingDtoRequest - сохраняемый объект бронирования.
     * @return DTO добавленного объекта бронирования.
     * @throws EndBeforeOrEqualsStartException     - если время начала бронирования находится после времени старта.
     * @throws ItemNotFoundException               - если бронируемая вещь не найдена.
     * @throws TimeWindowOccupiedException         - если бронируемый промежуток времени занят.
     * @throws CantBookOwnedItemException          - при попытке владельца забронировать собственную вещь.
     * @throws ItemNotAvailableForBookingException - если вещь не доступна для бронирования.
     * @throws UserNotFoundException               - если пользователя с указанным id не существует.
     */
    @PostMapping
    public ResponseEntity<BookingDto> addBooking(@RequestHeader(name = "X-Sharer-User-Id") long bookerId,
                                                 @RequestBody @Valid BookingDtoRequest bookingDtoRequest) {

        return new ResponseEntity<>(bookingService.addBooking(bookingDtoRequest, bookerId), HttpStatus.CREATED);
    }

    /**
     * Получение существующего бронирования.
     *
     * @param requesterId - идентификатор пользователя.
     * @param bookingId   - идентификатор бронирования.
     * @return DTO существующего объекта бронирования.
     * @throws CantViewUnrelatedBookingException - при попытке просмотреть бронирование сторонним пользователем
     *                                           (не владельцем и не заказчиком бронирования).
     * @throws BookingNotFoundException          - если запрашиваемый объект не найден.
     * @throws UserNotFoundException             - если пользователя с указанным id не существует.
     */
    @GetMapping(path = "/{bookingId}")
    public ResponseEntity<BookingDto> getBooking(@RequestHeader(name = "X-Sharer-User-Id") long requesterId,
                                                 @PathVariable long bookingId) {
        return ResponseEntity.ok(bookingService.getBookingDto(bookingId, requesterId));
    }

    /**
     * Получение списка бронирований по заказчику бронирования и статусу объектов с возможностью использования пагинации.
     *
     * @param bookerId - идентификатор пользователя.
     * @param state    - статус запрашиваемых объектов.
     * @param from     - индекс элемента, с которого должен начинаться список.
     * @param size     - размер списка.
     * @return список объектов, отсортированных по времени от самых последних.
     * @throws UserNotFoundException - если пользователя с указанным id не существует.
     */
    @GetMapping
    public ResponseEntity<Collection<BookingDto>> getBookingsByBookerAndStatus(
            @RequestHeader(name = "X-Sharer-User-Id") long bookerId,
            @RequestParam(required = false) String state,
            @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
            @RequestParam(required = false) @Positive Integer size) {

        return ResponseEntity.ok(bookingService.getBookingsByUserAndState(
                bookerId, null, state, from, size));
    }

    /**
     * Получение списка бронирований по владельцу и статусу объектов с возможностью использования пагинации.
     *
     * @param ownerId - идентификатор пользователя.
     * @param state   - статус запрашиваемых объектов.
     * @param from    - индекс элемента, с которого должен начинаться список.
     * @param size    - размер списка.
     * @return список объектов, отсортированных по времени от самых последних.
     * @throws UserNotFoundException - если пользователя с указанным id не существует.
     */
    @GetMapping(path = "/owner")
    public ResponseEntity<Collection<BookingDto>> getBookingsByOwnerAndStatus(
            @RequestHeader(name = "X-Sharer-User-Id") long ownerId,
            @RequestParam(required = false) String state,
            @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
            @RequestParam(required = false) @Positive Integer size) {

        return ResponseEntity.ok(bookingService.getBookingsByUserAndState(
                null, ownerId, state, from, size));
    }

    /**
     * Изменение статуса одобрения бронирования, находящегося в ожидании решения владельца бронируемой вещи.
     *
     * @param requesterId - идентификатор пользователя, меняющего статус одобрения.
     * @param bookingId   - идентификатор бронирования.
     * @param approved    - устанавливаемый статус (одобрение или отказ).
     * @return DTO объекта бронирования после изменения статуса.
     * @throws UserNotFoundException       - если пользователя с указанным id не существует.
     * @throws BookingNotFoundException    - если изменяемый объект не найден.
     * @throws ApprovalAlreadySetException - если статус объекта уже меняли ранее.
     * @throws WrongUserUpdatingBooking    - если попытка изменить статус исходит не от владельца.
     */
    @PatchMapping("/{bookingId}")
    public ResponseEntity<BookingDto> setBookingApproval(@RequestHeader(name = "X-Sharer-User-Id") long requesterId,
                                                         @PathVariable long bookingId,
                                                         @RequestParam boolean approved) {

        return ResponseEntity.ok(bookingService.setApproval(bookingId, approved, requesterId));
    }
}
