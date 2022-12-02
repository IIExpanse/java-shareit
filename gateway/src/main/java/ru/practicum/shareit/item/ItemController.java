package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

@Validated
@Controller
@RequestMapping(path = "/items")
@Slf4j
@RequiredArgsConstructor
public class ItemController {

    private final ItemClient itemClient;

    @PostMapping
    public ResponseEntity<Object> addItem(@RequestHeader(name = "X-Sharer-User-Id") Long ownerId,
                                          @RequestBody @Valid ItemDto itemDto) {

        log.info("Creating item {}, ownerId={}", itemDto, ownerId);
        return itemClient.addItem(ownerId, itemDto);
    }

    @PostMapping(path = "/{itemId}/comment")
    public ResponseEntity<Object> addComment(@RequestHeader(name = "X-Sharer-User-Id") Long authorId,
                                             @PathVariable Long itemId,
                                             @RequestBody @Valid CommentDto commentDto) {

        log.info("Creating comment {}, authorId={}, itemId={}", commentDto, authorId, itemId);
        return itemClient.addComment(authorId, itemId, commentDto);
    }

    @GetMapping(path = "/{id}")
    public ResponseEntity<Object> getItem(@RequestHeader(name = "X-Sharer-User-Id") Long requesterId,
                                          @PathVariable Long id) {

        log.info("Get item, requesterId={}, itemId={}", requesterId, id);
        return itemClient.getItem(requesterId, id);
    }

    @GetMapping
    public ResponseEntity<Object> getOwnerItems(
            @RequestHeader(name = "X-Sharer-User-Id") Long ownerId,
            @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
            @RequestParam(defaultValue = "10") @Positive Integer size) {

        log.info("Get owner items, ownerId={}, from={}, size={}", ownerId, from, size);
        return itemClient.getOwnerItems(ownerId, from, size);
    }

    @GetMapping(path = "/search")
    public ResponseEntity<Object> searchAvailableItems(
            @RequestHeader(name = "X-Sharer-User-Id") Long ownerId,
            @RequestParam String text,
            @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
            @RequestParam(defaultValue = "10") @Positive Integer size) {

        log.info("Search items, ownerId={}, text={}, from={}, size={}", ownerId, text, from, size);
        return itemClient.searchAvailableItems(ownerId, text, from, size);
    }

    @PatchMapping(path = "/{itemId}")
    public ResponseEntity<Object> updateItem(@RequestHeader(name = "X-Sharer-User-Id") Long ownerId,
                                             @PathVariable Long itemId,
                                             @RequestBody ItemDto itemDto) {

        log.info("Updating item {}, ownerId={}, itemId={}", itemDto, ownerId, itemId);
        return itemClient.updateItem(ownerId, itemId, itemDto);
    }
}
