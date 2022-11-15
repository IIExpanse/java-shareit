package ru.practicum.shareit.item.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.item.comment.model.Comment;
import ru.practicum.shareit.item.comment.repository.CommentRepository;
import ru.practicum.shareit.item.exception.ItemNotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.item.service.UpdatedItemFields;
import ru.practicum.shareit.user.exception.UserNotFoundException;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;

    @Override
    public Item addItem(Item item) {
        long ownerId = item.getOwner().getId();
        Optional<User> userOptional = userRepository.findById(ownerId);

        if (userOptional.isEmpty()) {
            throw new UserNotFoundException(
                    String.format("Ошибка добавления вещи: владельца с id=%d не существует.", ownerId));
        }

        return itemRepository.save(item);
    }

    @Override
    public Item getItem(long id) {
        Optional<Item> itemOptional = itemRepository.findById(id);

        if (itemOptional.isEmpty()) {
            throw new ItemNotFoundException(String.format("Ошибка получения: вещь с id=%d не найдена.", id));
        }

        return itemOptional.get();
    }

    @Override
    public boolean isItemAvailable(long itemId) {
        return itemRepository.existsItemByIdAndAvailableIsTrue(itemId);
    }

    @Override
    public Collection<Item> getOwnerItems(long ownerId) {
        return itemRepository.findAllByOwnerId(ownerId);
    }

    @Override
    public Collection<Item> searchAvailableItems(String query) {
        return itemRepository.searchAvailableItemsByNameAndDescription(query);
    }

    @Override
    public Item updateItem(Item item, Map<UpdatedItemFields, Boolean> targetFields) {
        return itemRepository.updateItem(item, targetFields);
    }

    @Override
    public Comment addComment(Comment comment) {
        comment.setCreated(LocalDateTime.now());
        return commentRepository.save(comment);
    }
}
