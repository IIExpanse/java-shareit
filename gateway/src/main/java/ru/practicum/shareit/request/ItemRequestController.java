package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

@Validated
@Controller
@RequestMapping(path = "/requests")
@Slf4j
@RequiredArgsConstructor
public class ItemRequestController {

    private final RequestClient requestClient;

    @PostMapping
    public ResponseEntity<Object> addItemRequest(@RequestHeader(name = "X-Sharer-User-Id") long requesterId,
                                                 @RequestBody @Valid ItemRequestDto requestDto) {

        log.info("Creating itemRequest {}, requesterId={}", requestDto, requesterId);
        return requestClient.addItemRequest(requesterId, requestDto);
    }

    @GetMapping(path = "/{requestId}")
    public ResponseEntity<Object> getItemRequest(@RequestHeader(name = "X-Sharer-User-Id") long requesterId,
                                                 @PathVariable long requestId) {

        log.info("Get itemRequest, requesterId={}, requestId={}", requesterId, requestId);
        return requestClient.getItemRequest(requesterId, requestId);
    }

    @GetMapping
    public ResponseEntity<Object> getOwnItemRequests(
            @RequestHeader(name = "X-Sharer-User-Id") long requesterId) {

        log.info("Get user's itemRequests, requesterId={}", requesterId);
        return requestClient.getOwnItemRequests(requesterId);
    }

    @GetMapping(path = "/all")
    public ResponseEntity<Object> getOtherUsersRequests(
            @RequestHeader(name = "X-Sharer-User-Id") long requesterId,
            @RequestParam(defaultValue = "0") @PositiveOrZero int from,
            @RequestParam(defaultValue = "10") @Positive int size) {

        log.info("Get other users' itemRequests, requesterId={}, from={}, size={}", requesterId, from, size);
        return requestClient.getOtherUsersRequests(requesterId, from, size);
    }
}
