package ru.practicum.shareit.item.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDtoShort;
import ru.practicum.shareit.booking.exception.CommenterDontHaveBookingException;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.item.comment.dto.CommentDto;
import ru.practicum.shareit.item.comment.mapper.CommentMapper;
import ru.practicum.shareit.item.comment.model.Comment;
import ru.practicum.shareit.item.comment.repository.CommentRepository;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.exception.EmptyItemPatchRequestException;
import ru.practicum.shareit.item.exception.ItemNotFoundException;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.service.ActualItemBooking;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.item.service.UpdatedItemFields;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import static ru.practicum.shareit.item.service.ActualItemBooking.LAST;
import static ru.practicum.shareit.item.service.ActualItemBooking.NEXT;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final CommentRepository commentRepository;
    private final BookingService bookingService;
    private final UserService userService;
    private final ItemRequestService requestService;
    private final ItemMapper itemMapper;
    private final CommentMapper commentMapper;

    @Override
    public ItemDto addItem(ItemDto itemDto, long ownerId) {
        Item item;
        ItemRequest request;
        Long requestId = itemDto.getRequestId();

        if (requestId != null) {
            request = requestService.getRequest(requestId);
        } else {
            request = null;
        }

        item = itemMapper.mapToModel(itemDto, userService.getUser(ownerId), request);
        item.setId(null);
        item = itemRepository.save(item);

        log.debug("Добавлена новая вещь: {}", item);
        return itemMapper.mapToDto(item, null, null);
    }

    @Override
    @Transactional
    public ItemDto getItemDto(long id, long requesterId) {
        Item item = this.getItem(id);
        Map<ActualItemBooking, BookingDtoShort> lastAndNextBooking =
                bookingService.getLastAndNextBookingByItem(item, requesterId);

        return itemMapper.mapToDto(
                item,
                lastAndNextBooking.get(LAST),
                lastAndNextBooking.get(NEXT));
    }

    @Override
    public Item getItem(long itemId) {
        Optional<Item> itemOptional = itemRepository.findById(itemId);

        if (itemOptional.isEmpty()) {
            throw new ItemNotFoundException(String.format("Ошибка получения: вещь с id=%d не найдена.", itemId));
        }

        return itemOptional.get();
    }

    @Override
    public Collection<ItemDto> getOwnerItems(long ownerId,  int startingIndex, int collectionSize) {
        return itemRepository.findAllByOwnerId(ownerId).stream()
                .sorted(Comparator.comparing(Item::getId))
                .skip(startingIndex)
                .limit(collectionSize)
                .map(item -> {
                    Map<ActualItemBooking, BookingDtoShort> itemDtoBookingsMap =
                            bookingService.getLastAndNextBookingByItem(item, ownerId);
                    return itemMapper.mapToDto(item, itemDtoBookingsMap.get(LAST), itemDtoBookingsMap.get(NEXT));
                })
                .collect(Collectors.toList());
    }

    @Override
    public Collection<ItemDto> searchAvailableItems(long userId, String text,  int startingIndex, int collectionSize) {
        if (!text.isEmpty()) {
            return itemRepository.searchAvailableItemsByNameAndDescription(text).stream()
                    .sorted(Comparator.comparing(Item::getId))
                    .skip(startingIndex)
                    .limit(collectionSize)
                    .map(item -> {
                        Map<ActualItemBooking, BookingDtoShort> itemDtoBookingsMap = bookingService
                                .getLastAndNextBookingByItem(item, userId);
                        return itemMapper.mapToDto(item, itemDtoBookingsMap.get(LAST), itemDtoBookingsMap.get(NEXT));
                    })
                    .collect(Collectors.toList());
        } else return List.of();
    }

    @Override
    public ItemDto updateItem(ItemDto itemDto, long itemId, long ownerId) {
        Map<UpdatedItemFields, Boolean> targetFields = new HashMap<>();
        boolean empty = true;
        Item item;
        Map<ActualItemBooking, BookingDtoShort> itemDtoBookingsMap;

        if (itemDto.getName() != null) {
            targetFields.put(UpdatedItemFields.NAME, true);
            empty = false;
        } else {
            targetFields.put(UpdatedItemFields.NAME, false);
        }

        if (itemDto.getDescription() != null) {
            targetFields.put(UpdatedItemFields.DESCRIPTION, true);
            empty = false;
        } else {
            targetFields.put(UpdatedItemFields.DESCRIPTION, false);
        }

        if (itemDto.getAvailable() != null) {
            targetFields.put(UpdatedItemFields.AVAILABLE, true);
            empty = false;
        } else {
            targetFields.put(UpdatedItemFields.AVAILABLE, false);
        }

        if (empty) {
            throw new EmptyItemPatchRequestException("Ошибка обновления вещи: в запросе все поля равны null.");
        }

        item = itemMapper.mapToModel(itemDto, userService.getUser(ownerId), null);
        item.setId(itemId);
        item = itemRepository.updateItem(item, targetFields);
        itemDtoBookingsMap = bookingService.getLastAndNextBookingByItem(item, ownerId);

        log.debug("Обновлена вещь: {}", item);
        return itemMapper.mapToDto(item, itemDtoBookingsMap.get(LAST), itemDtoBookingsMap.get(NEXT));
    }

    @Override
    public CommentDto addComment(CommentDto commentDto, long authorId, long itemId) {
        if (bookingService.neverMadeBookings(authorId, itemId)) {
            throw new CommenterDontHaveBookingException(String.format("Ошибка при добавлении комментария: " +
                    "пользователь с id=%d не оформлял бронирований вещи с id=%d.", authorId, itemId));
        }

        Comment comment = commentMapper.mapToModel(
                commentDto, userService.getUser(authorId), this.getItem(itemId));
        comment.setCreated(LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS));
        comment = commentRepository.save(comment);

        log.debug("Добавлен комментарий: {}", comment);
        return commentMapper.mapToDto(comment);
    }
}
