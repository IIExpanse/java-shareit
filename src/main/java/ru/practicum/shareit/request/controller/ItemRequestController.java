package ru.practicum.shareit.request.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.Collection;

@Validated
@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
public class ItemRequestController {

    private final ItemRequestService service;

    @PostMapping
    public ResponseEntity<ItemRequestDto> addItemRequest(@RequestHeader(name = "X-Sharer-User-Id") long requesterId,
                                                         @RequestBody @Valid ItemRequestDto requestDto) {
        return ResponseEntity.ok(service.addRequest(requestDto, requesterId));
    }

    @GetMapping(path = "/{requestId}")
    public ResponseEntity<ItemRequestDto> getItemRequest(@RequestHeader(name = "X-Sharer-User-Id") long requesterId,
                                                         @PathVariable long requestId) {
        return ResponseEntity.ok(service.getRequestDto(requestId, requesterId));
    }

    @GetMapping
    public ResponseEntity<Collection<ItemRequestDto>> getOwnItemRequests(
            @RequestHeader(name = "X-Sharer-User-Id") long requesterId) {
        return ResponseEntity.ok(service.getOwnItemRequests(requesterId));
    }

    @GetMapping(path = "/all")
    public ResponseEntity<Collection<ItemRequestDto>> getOtherUsersRequests(
            @RequestHeader(name = "X-Sharer-User-Id") long requesterId,
            @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
            @RequestParam(required = false) @Positive Integer size) {
        if (size == null) {
            size = Integer.MAX_VALUE;
        }
        return ResponseEntity.ok(service.getOtherUsersRequests(requesterId, from, size));
    }
}
