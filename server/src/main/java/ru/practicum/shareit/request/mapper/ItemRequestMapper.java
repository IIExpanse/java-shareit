package ru.practicum.shareit.request.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

@Mapper(componentModel = "spring", uses = ItemMapper.class)
public interface ItemRequestMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "items", ignore = true)
    @Mapping(target = "description", source = "requestDto.description")
    @Mapping(source = "created", target = "created")
    ItemRequest mapToModel(ItemRequestDto requestDto, User requester, LocalDateTime created);

    ItemRequestDto mapToDto(ItemRequest request);
}
