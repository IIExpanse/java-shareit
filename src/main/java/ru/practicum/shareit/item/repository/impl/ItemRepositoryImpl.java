package ru.practicum.shareit.item.repository.impl;

import org.springframework.context.annotation.Lazy;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.item.exception.ItemNotFoundException;
import ru.practicum.shareit.item.exception.WrongOwnerUpdatingItemException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.repository.ItemRepositoryCustom;
import ru.practicum.shareit.item.service.UpdatedItemFields;

import java.util.Map;
import java.util.Optional;

@Transactional
public class ItemRepositoryImpl implements ItemRepositoryCustom {

    private final ItemRepository repository;

    public ItemRepositoryImpl(@Lazy ItemRepository repository) {
        this.repository = repository;
    }

    @Override
    public Item updateItem(Item item, Map<UpdatedItemFields, Boolean> targetFields) {
        long itemId = item.getId();
        long ownerId = item.getOwner().getId();
        Optional<Item> itemOptional = repository.findById(itemId);

        if (itemOptional.isPresent()) {
            Item existingItem = itemOptional.get();
            if (existingItem.getOwner().getId() != ownerId) {
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
                    .id(itemId)
                    .owner(existingItem.getOwner())
                    .name(name)
                    .description(description)
                    .available(available)
                    .build();

            return repository.save(newItem);

        } else throw new ItemNotFoundException(String.format("Ошибка обновления: вещь с id=%d не найдена.", itemId));
    }
}
