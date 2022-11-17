package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.comment.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;

import java.util.Collection;

public interface ItemService {

    ItemDto addItem(ItemDto itemDto, long ownerId);

    ItemDto getItemDto(long id, long requesterId);

    Item getItem(long itemId);

    Collection<ItemDto> getOwnerItems(long ownerId);

    Collection<ItemDto> searchAvailableItems(long ownerId, String text);

    ItemDto updateItem(ItemDto itemDto, long itemId, long ownerId);

    CommentDto addComment(CommentDto commentDto, long authorId, long itemId);
}
