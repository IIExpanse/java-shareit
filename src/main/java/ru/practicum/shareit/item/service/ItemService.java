package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.model.Item;

import java.util.Collection;
import java.util.Map;

public interface ItemService {

    Item addItem(Item item);

    Item getItem(long id);

    Collection<Item> getOwnerItems(long ownerId);

    Item updateItem(Item item, Map<UpdatedItemFields, Boolean> targetFields);

    Collection<Item> searchAvailableItems(String query);
}
