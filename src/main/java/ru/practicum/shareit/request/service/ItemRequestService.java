package ru.practicum.shareit.request.service;

import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;

import java.util.Collection;

public interface ItemRequestService {

    ItemRequestDto addRequest(ItemRequestDto requestDto, long requesterId);

    ItemRequestDto getRequestDto(long requestId, long requesterId);

    ItemRequest getRequest(long requestId);

    Collection<ItemRequestDto> getOwnItemRequests(long requesterId);

    Collection<ItemRequestDto> getOtherUsersRequests(long requesterId, int startingIndex, int collectionSize);
}
