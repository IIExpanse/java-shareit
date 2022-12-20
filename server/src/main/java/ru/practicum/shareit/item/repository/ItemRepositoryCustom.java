package ru.practicum.shareit.item.repository;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.UpdatedItemFields;

import java.util.Map;

@Repository
public interface ItemRepositoryCustom {
    Item updateItem(Item item, Map<UpdatedItemFields, Boolean> targetFields);
}
