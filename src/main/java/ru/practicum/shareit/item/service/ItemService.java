package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.comment.model.Comment;
import ru.practicum.shareit.item.model.Item;

import java.util.Collection;
import java.util.Map;

public interface ItemService {

    Item addItem(Item item);

    Item getItem(long id);

    boolean isItemAvailable(long itemId);

    Collection<Item> getOwnerItems(long ownerId);

    Collection<Item> searchAvailableItems(String query);

    Item updateItem(Item item, Map<UpdatedItemFields, Boolean> targetFields);

    Comment addComment(Comment comment);
}
