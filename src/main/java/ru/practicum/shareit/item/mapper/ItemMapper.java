package ru.practicum.shareit.item.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;

@Mapper(componentModel = "spring")
public interface ItemMapper {

    @Mapping(source = "id", target = "itemId")
    Item mapToModel(ItemDto itemDto);

    @Mapping(source = "itemId", target = "id")
    ItemDto mapToDto(Item item);
}
