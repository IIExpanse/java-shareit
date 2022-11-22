package ru.practicum.shareit.item.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.shareit.booking.dto.BookingDtoShort;
import ru.practicum.shareit.item.comment.mapper.CommentMapper;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

@Mapper(componentModel = "spring", uses = CommentMapper.class)
public interface ItemMapper {

    @Mapping(source = "itemDto.id", target = "id")
    @Mapping(source = "itemDto.name", target = "name")
    @Mapping(target = "bookings", ignore = true)
    @Mapping(target = "comments", ignore = true)
    @Mapping(source = "itemDto.description", target = "description")
    Item mapToModel(ItemDto itemDto, User owner, ItemRequest request);

    @Mapping(source = "item.id", target = "id")
    @Mapping(source = "item.request.id", target = "requestId")
    ItemDto mapToDto(Item item, BookingDtoShort lastBooking, BookingDtoShort nextBooking);

    @Mapping(source = "item.id", target = "id")
    @Mapping(source = "item.request.id", target = "requestId")
    @Mapping(target = "lastBooking", ignore = true)
    @Mapping(target = "nextBooking", ignore = true)
    @Mapping(target = "comments", ignore = true)
    ItemDto mapToShortDto(Item item);
}