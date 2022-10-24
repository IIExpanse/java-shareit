package ru.practicum.shareit.item.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.exception.EmptyItemPatchRequestException;
import ru.practicum.shareit.item.exception.ItemNotFoundException;
import ru.practicum.shareit.item.exception.WrongOwnerUpdatingItemException;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.item.service.UpdatedItemFields;
import ru.practicum.shareit.user.exception.UserNotFoundException;

import javax.validation.Valid;
import java.util.*;
import java.util.stream.Collectors;

@Validated
@RestController
@RequestMapping(path = "/items")
@Slf4j
@RequiredArgsConstructor
public class ItemController {

    private final ItemService service;
    private final ItemMapper mapper;

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
        Item item = mapper.mapToModel(itemDto);
        item.setOwnerId(ownerId);
        ResponseEntity<ItemDto> response = new ResponseEntity<>(
                mapper.mapToDto(service.addItem(item)), HttpStatus.CREATED);

        log.debug("Добавлена новая вещь: {}", response.getBody());
        return response;
    }

    /**
     * Получение существующей вещи.
     *
     * @param id - идентификатор существующей вещи.
     * @return DTO существующей вещи.
     * @throws ItemNotFoundException - если вещь с указанным id не найдена.
     */
    @GetMapping(path = "/{id}")
    public ResponseEntity<ItemDto> getItem(@PathVariable long id) {
        return ResponseEntity.ok(mapper.mapToDto(service.getItem(id)));
    }

    /**
     * Получение списка DTO всех вещей конкретного владельца.
     *
     * @param ownerId - идентификатор владельца.
     * @return Список вещей, которыми владеет пользователь с указанным ownerId. Может быть пустым.
     */
    @GetMapping
    public ResponseEntity<Collection<ItemDto>> getOwnerItems(@RequestHeader(name = "X-Sharer-User-Id") long ownerId) {
        Collection<ItemDto> collection = service.getOwnerItems(ownerId).stream()
                .map(mapper::mapToDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(collection);
    }

    /**
     * Поиск всех доступных для бронирования вещей,
     * в названии или описании которых присутствует текст поискового запроса (регистр игнорируется).
     *
     * @param text - текст поискового запроса. Не может быть пустым либо содержать только пробелы.
     * @return Список найденных вещей. При пустом запросе либо отсутствии результатов возвращается пустой список.
     */
    @GetMapping(path = "/search")
    public ResponseEntity<Collection<ItemDto>> searchAvailableItems(@RequestParam String text) {
        if (text.trim().length() > 0) {
            return ResponseEntity.ok(service.searchAvailableItems(text).stream()
                    .map(mapper::mapToDto)
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

        item = mapper.mapToModel(itemDto);
        item.setItemId(itemId);
        item.setOwnerId(ownerId);
        response = ResponseEntity.ok(
                mapper.mapToDto(service.updateItem(item, targetFields)));

        log.debug("Обновлена вещь: {}", response.getBody());
        return response;
    }
}
