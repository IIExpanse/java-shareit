package ru.practicum.shareit.item.repository.impl;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.exception.ItemNotFoundException;
import ru.practicum.shareit.item.exception.WrongOwnerUpdatingItemException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.service.UpdatedItemFields;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
public class InMemoryItemRepository implements ItemRepository {

    private long idCounter = 1;
    private final Map<Long, Item> itemMap = new HashMap<>();

    @Override
    public Item addItem(Item item) {
        item.setItemId(idCounter);
        itemMap.put(idCounter, item);

        return getItem(idCounter++);
    }

    @Override
    public Item getItem(long id) {
        if (itemMap.containsKey(id)) {
            return itemMap.get(id);

        } else throw new ItemNotFoundException(String.format("Ошибка получения: вещь с id=%d не найдена.", id));
    }

    @Override
    public Collection<Item> getOwnerItems(long ownerId) {
        return List.copyOf(itemMap.values().stream()
                .filter(item -> item.getOwnerId() == ownerId)
                .collect(Collectors.toList()));
    }

    @Override
    public Item updateItem(Item item, Map<UpdatedItemFields, Boolean> targetFields) {
        long itemId = item.getItemId();
        long ownerId = item.getOwnerId();

        if (itemMap.containsKey(itemId)) {
            Item existingItem = getItem(itemId);
            if (existingItem.getOwnerId() != ownerId) {
                throw new WrongOwnerUpdatingItemException(String.format("Ошибка: запрос на обновление вещи с id=%d" +
                        " исходит от пользователя, не являющегося ее владельцем.", itemId));
            }

            String name = item.getName();
            String description = item.getDescription();
            Boolean available = item.getAvailable();
            Item newItem;

            if (!targetFields.get(UpdatedItemFields.NAME)) {
                name = existingItem.getName();
            }
            if (!targetFields.get(UpdatedItemFields.DESCRIPTION)) {
                description = existingItem.getDescription();
            }
            if (!targetFields.get(UpdatedItemFields.AVAILABLE)) {
                available = existingItem.getAvailable();
            }

            newItem = Item.builder()
                    .itemId(itemId)
                    .ownerId(existingItem.getOwnerId())
                    .name(name)
                    .description(description)
                    .available(available)
                    .build();
            itemMap.put(itemId, newItem);

            return newItem;

        } else {
            throw new ItemNotFoundException(String.format("Ошибка обновления: вещь с id=%d не найдена.", itemId));
        }
    }

    @Override
    public Collection<Item> searchAvailableItems(String query) {
        String lowerQuery = query.toLowerCase();
        return itemMap.values().stream()
                .filter(item -> (item.getName().toLowerCase().contains(lowerQuery)
                        || item.getDescription().toLowerCase().contains(lowerQuery)) && item.getAvailable())
                .collect(Collectors.toList());
    }

    @Override
    public void deleteUserItems(long id) {
        getOwnerItems(id).stream()
                .map(Item::getItemId)
                .forEach(itemMap::remove);
    }
}
