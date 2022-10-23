package ru.practicum.shareit.item.service.impl;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.item.service.UpdatedItemFields;
import ru.practicum.shareit.user.exception.UserNotFoundException;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.Collection;
import java.util.Map;

@Service
@AllArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Override
    public Item addItem(Item item) {
        long ownerId = item.getOwnerId();

        try {
            userRepository.getUser(ownerId);
        } catch (UserNotFoundException e) {
            throw new UserNotFoundException(
                    String.format("Ошибка добавления вещи: владельца с id=%d не существует.", ownerId));
        }

        return itemRepository.addItem(item);
    }

    @Override
    public Item getItem(long id) {
        return itemRepository.getItem(id);
    }

    @Override
    public Collection<Item> getOwnerItems(long ownerId) {
        return itemRepository.getOwnerItems(ownerId);
    }

    @Override
    public Item updateItem(Item item, Map<UpdatedItemFields, Boolean> targetFields) {
        return itemRepository.updateItem(item, targetFields);
    }

    @Override
    public Collection<Item> searchAvailableItems(String query) {
        return itemRepository.searchAvailableItems(query);
    }
}
