package ru.practicum.shareit.item.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDtoShort;
import ru.practicum.shareit.booking.exception.CommenterDontHaveBookingException;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.item.comment.dto.CommentDto;
import ru.practicum.shareit.item.comment.mapper.CommentMapper;
import ru.practicum.shareit.item.comment.model.Comment;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.exception.EmptyItemPatchRequestException;
import ru.practicum.shareit.item.exception.ItemNotFoundException;
import ru.practicum.shareit.item.exception.WrongOwnerUpdatingItemException;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ActualItemBooking;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.item.service.UpdatedItemFields;
import ru.practicum.shareit.user.exception.UserNotFoundException;
import ru.practicum.shareit.user.service.UserService;

import javax.validation.Valid;
import java.util.*;
import java.util.stream.Collectors;

import static ru.practicum.shareit.item.service.ActualItemBooking.LAST;
import static ru.practicum.shareit.item.service.ActualItemBooking.NEXT;

@Validated
@RestController
@RequestMapping(path = "/items")
@Slf4j
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;
    private final UserService userService;
    private final BookingService bookingService;
    private final ItemMapper itemMapper;
    private final BookingMapper bookingMapper;
    private final CommentMapper commentMapper;

    /**
     * Добавление новой вещи.
     *
     * @param ownerId - идентификатор владельца вещи.
     * @param itemDto - DTO вещи. Все поля (кроме id) проходят валидацию.
     * @return DTO добавленной вещи с присвоенным id.
     * @throws UserNotFoundException - если владельца с указанным ownerId не существует.
     */
    @PostMapping
    public ResponseEntity<ItemDto> addItem(@RequestHeader(name = "X-Sharer-User-Id") long ownerId,
                                           @RequestBody @Valid ItemDto itemDto) {
        Item item = itemMapper.mapToModel(itemDto, userService.getUser(ownerId));
        ResponseEntity<ItemDto> response = new ResponseEntity<>(
                itemMapper.mapToDto(itemService.addItem(item), null, null), HttpStatus.CREATED);

        log.debug("Добавлена новая вещь: {}", response.getBody());
        return response;
    }

    @PostMapping(path = "/{itemId}/comment")
    public ResponseEntity<CommentDto> addComment(@RequestHeader(name = "X-Sharer-User-Id") long authorId,
                                                 @PathVariable long itemId,
                                                 @RequestBody @Valid CommentDto commentDto) {
        if (!bookingService.isCommentMadeAfterBooking(authorId, itemId)) {
            throw new CommenterDontHaveBookingException(String.format("Ошибка при добавлении комментария: " +
                    "пользователь с id=%d не оформлял бронирований вещи с id=%d.", authorId, itemId));
        }

        Comment comment = commentMapper.mapToModel(
                commentDto, userService.getUser(authorId), itemService.getItem(itemId));
        comment = itemService.addComment(comment);

        log.debug("Добавлен комментарий: {}", comment);
        return ResponseEntity.ok(commentMapper.mapToDto(comment));
    }

    /**
     * Получение существующей вещи.
     *
     * @param id     - идентификатор существующей вещи.
     * @param userId - идентификатор пользователя.
     * @return DTO существующей вещи.
     * @throws ItemNotFoundException - если вещь с указанным id не найдена.
     */
    @GetMapping(path = "/{id}")
    public ResponseEntity<ItemDto> getItem(@RequestHeader(name = "X-Sharer-User-Id") long userId,
                                           @PathVariable long id) {
        Item item = itemService.getItem(id);
        Map<ActualItemBooking, BookingDtoShort> itemDtoBookingsMap = getLastAndNextBooking(item, userId);


        return ResponseEntity.ok(itemMapper.mapToDto(item, itemDtoBookingsMap.get(LAST), itemDtoBookingsMap.get(NEXT)));
    }

    /**
     * Получение списка DTO всех вещей конкретного владельца.
     *
     * @param ownerId - идентификатор владельца.
     * @return Список вещей, которыми владеет пользователь с указанным ownerId. Может быть пустым.
     */
    @GetMapping
    public ResponseEntity<Collection<ItemDto>> getOwnerItems(@RequestHeader(name = "X-Sharer-User-Id") long ownerId) {
        Collection<ItemDto> collection = itemService.getOwnerItems(ownerId).stream()
                .map(item -> {
                    Map<ActualItemBooking, BookingDtoShort> itemDtoBookingsMap = getLastAndNextBooking(item, ownerId);
                    return itemMapper.mapToDto(item, itemDtoBookingsMap.get(LAST), itemDtoBookingsMap.get(NEXT));
                })
                .sorted(Comparator.comparing(ItemDto::getId))
                .collect(Collectors.toList());

        return ResponseEntity.ok(collection);
    }

    /**
     * Поиск всех доступных для бронирования вещей,
     * в названии или описании которых присутствует текст поискового запроса (регистр игнорируется).
     *
     * @param userId - идентификатор пользователя.
     * @param text   - текст поискового запроса. Не может быть пустым либо содержать только пробелы.
     * @return Список найденных вещей. При пустом запросе либо отсутствии результатов возвращается пустой список.
     */
    @GetMapping(path = "/search")
    public ResponseEntity<Collection<ItemDto>> searchAvailableItems(
            @RequestHeader(name = "X-Sharer-User-Id") long userId,
            @RequestParam String text) {
        if (!text.isEmpty()) {
            return ResponseEntity.ok(itemService.searchAvailableItems(text).stream()
                    .map(item -> {
                        Map<ActualItemBooking, BookingDtoShort> itemDtoBookingsMap = getLastAndNextBooking(item, userId);
                        return itemMapper.mapToDto(item, itemDtoBookingsMap.get(LAST), itemDtoBookingsMap.get(NEXT));
                    })
                    .sorted(Comparator.comparing(ItemDto::getId))
                    .collect(Collectors.toList()));

        } else return ResponseEntity.ok(List.of());
    }

    /**
     * Обновление существующей вещи. Как минимум одно поле в DTO (кроме id) не должно быть равно null.
     * На уровень сервиса и репозитория передается targetFields - таблица с указанием полей,
     * которые необходимо обновить
     *
     * @param ownerId - идентификатор пользователя, запрашивающего обновление данных вещи.
     *                Должен соответствовать идентификатору владельца.
     * @param itemId  - идентификатор обновляемой вещи.
     * @param itemDto - DTO вещи с не равными null полями, которые необходимо обновить.
     * @return DTO вещи после обновления данных.
     * @throws EmptyItemPatchRequestException  - если все поля DTO вещи в запросе (кроме id) равны null.
     * @throws ItemNotFoundException           - если обновляемая вещь не найдена.
     * @throws WrongOwnerUpdatingItemException - если идентификатор пользователя, запрашивающего обновление,
     *                                         не соответствует владельцу.
     */
    @PatchMapping(path = "/{itemId}")
    public ResponseEntity<ItemDto> updateItem(@RequestHeader(name = "X-Sharer-User-Id") long ownerId,
                                              @PathVariable long itemId,
                                              @RequestBody ItemDto itemDto) {
        Map<UpdatedItemFields, Boolean> targetFields = new HashMap<>();
        boolean empty = true;
        ResponseEntity<ItemDto> response;
        Item item;
        Map<ActualItemBooking, BookingDtoShort> itemDtoBookingsMap;

        if (itemDto.getName() != null) {
            targetFields.put(UpdatedItemFields.NAME, true);
            empty = false;
        } else {
            targetFields.put(UpdatedItemFields.NAME, false);
        }

        if (itemDto.getDescription() != null) {
            targetFields.put(UpdatedItemFields.DESCRIPTION, true);
            empty = false;
        } else {
            targetFields.put(UpdatedItemFields.DESCRIPTION, false);
        }

        if (itemDto.getAvailable() != null) {
            targetFields.put(UpdatedItemFields.AVAILABLE, true);
            empty = false;
        } else {
            targetFields.put(UpdatedItemFields.AVAILABLE, false);
        }

        if (empty) {
            throw new EmptyItemPatchRequestException("Ошибка обновления вещи: в запросе все поля равны null.");
        }

        item = itemMapper.mapToModel(itemDto, userService.getUser(ownerId));
        item.setId(itemId);
        item = itemService.updateItem(item, targetFields);
        itemDtoBookingsMap = getLastAndNextBooking(item, ownerId);

        response = ResponseEntity.ok(
                itemMapper.mapToDto(item, itemDtoBookingsMap.get(LAST), itemDtoBookingsMap.get(NEXT)));

        log.debug("Обновлена вещь: {}", response.getBody());
        return response;
    }

    private Map<ActualItemBooking, BookingDtoShort> getLastAndNextBooking(Item item, long requesterId) {
        Map<ActualItemBooking, BookingDtoShort> bookingsDtoMap = new HashMap<>();
        Map<ActualItemBooking, Booking> bookingsMap;

        if (item.getOwner().getId() == requesterId) {
            bookingsMap = new HashMap<>(bookingService.getLastAndNextBookingByItem(item));

        } else {
            bookingsMap = new HashMap<>();
            bookingsMap.put(LAST, null);
            bookingsMap.put(NEXT, null);
        }

        for (Map.Entry<ActualItemBooking, Booking> bookingEntry : bookingsMap.entrySet()) {
            Booking booking = bookingEntry.getValue();
            if (booking != null) {
                bookingsDtoMap.put(bookingEntry.getKey(), bookingMapper.mapToShortDto(booking));
            } else {
                bookingsDtoMap.put(bookingEntry.getKey(), null);
            }
        }

        return bookingsDtoMap;
    }
}
