package ru.practicum.shareit.item.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.exception.CommenterDontHaveBookingException;
import ru.practicum.shareit.item.comment.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.exception.EmptyItemPatchRequestException;
import ru.practicum.shareit.item.exception.ItemNotFoundException;
import ru.practicum.shareit.item.exception.WrongOwnerUpdatingItemException;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.exception.UserNotFoundException;

import java.util.Collection;

@RestController
@RequestMapping(path = "/items")
@Slf4j
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    /**
     * Добавление новой вещи.
     *
     * @param ownerId - идентификатор владельца вещи.
     * @param itemDto - DTO вещи. Все поля (кроме id) проходят валидацию.
     * @return DTO добавленной вещи с присвоенным id.
     * @throws UserNotFoundException - если владельца с указанным ownerId не существует.
     */
    @PostMapping
    public ResponseEntity<ItemDto> addItem(@RequestHeader(name = "X-Sharer-User-Id") Long ownerId,
                                           @RequestBody ItemDto itemDto) {

        return new ResponseEntity<>(itemService.addItem(itemDto, ownerId), HttpStatus.CREATED);
    }

    /**
     * Добавление комментария к существующей вещи.
     *
     * @param authorId   - идентификатор автора комментария.
     * @param itemId     идентификатор комментируемой вещи.
     * @param commentDto - передаваемый объект комментария, должен содержать текст.
     * @return DTO комментария, внесенного в базу.
     * @throws CommenterDontHaveBookingException - если оставляющий комментарий пользователь ни разу не бронировал вещь.
     * @throws ItemNotFoundException             - если вещь с указанным id не найдена.
     * @throws UserNotFoundException             - если комментатора с указанным id не существует.
     */
    @PostMapping(path = "/{itemId}/comment")
    public ResponseEntity<CommentDto> addComment(@RequestHeader(name = "X-Sharer-User-Id") Long authorId,
                                                 @PathVariable Long itemId,
                                                 @RequestBody CommentDto commentDto) {

        return ResponseEntity.ok(itemService.addComment(commentDto, authorId, itemId));
    }

    /**
     * Получение существующей вещи.
     *
     * @param id          - идентификатор существующей вещи.
     * @param requesterId - идентификатор пользователя.
     * @return DTO существующей вещи.
     * @throws ItemNotFoundException - если вещь с указанным id не найдена.
     */
    @GetMapping(path = "/{id}")
    public ResponseEntity<ItemDto> getItem(@RequestHeader(name = "X-Sharer-User-Id") Long requesterId,
                                           @PathVariable Long id) {
        return ResponseEntity.ok(itemService.getItemDto(id, requesterId));
    }

    /**
     * Получение списка DTO всех вещей конкретного владельца.
     *
     * @param ownerId - идентификатор владельца.
     * @return Список вещей, которыми владеет пользователь с указанным ownerId. Может быть пустым.
     */
    @GetMapping
    public ResponseEntity<Collection<ItemDto>> getOwnerItems(
            @RequestHeader(name = "X-Sharer-User-Id") Long ownerId,
            @RequestParam Integer from,
            @RequestParam Integer size) {

        return ResponseEntity.ok(itemService.getOwnerItems(ownerId, from, size));
    }

    /**
     * Поиск всех доступных для бронирования вещей,
     * в названии или описании которых присутствует текст поискового запроса (регистр игнорируется).
     *
     * @param ownerId - идентификатор пользователя.
     * @param text    - текст поискового запроса. Не может быть пустым либо содержать только пробелы.
     * @return Список найденных вещей. При пустом запросе либо отсутствии результатов возвращается пустой список.
     */
    @GetMapping(path = "/search")
    public ResponseEntity<Collection<ItemDto>> searchAvailableItems(
            @RequestHeader(name = "X-Sharer-User-Id") Long ownerId,
            @RequestParam String text,
            @RequestParam Integer from,
            @RequestParam Integer size) {

        return ResponseEntity.ok(itemService.searchAvailableItems(ownerId, text, from, size));
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
    public ResponseEntity<ItemDto> updateItem(@RequestHeader(name = "X-Sharer-User-Id") Long ownerId,
                                              @PathVariable Long itemId,
                                              @RequestBody ItemDto itemDto) {

        return ResponseEntity.ok(itemService.updateItem(itemDto, itemId, ownerId));
    }
}
