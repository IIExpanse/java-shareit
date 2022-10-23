package ru.practicum.shareit.item.repository;

import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.UpdatedItemFields;

import java.util.Collection;
import java.util.Map;

public interface ItemRepository {

    Item addItem(Item item);

    Item getItem(long id);

    Collection<Item> getOwnerItems(long ownerId);

    Item updateItem(Item item, Map<UpdatedItemFields, Boolean> targetFields);

    Collection<Item> searchAvailableItems(String query);

    void deleteUserItems(long id);
}
