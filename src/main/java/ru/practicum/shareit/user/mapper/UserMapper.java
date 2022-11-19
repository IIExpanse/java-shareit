package ru.practicum.shareit.user.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(target = "items", ignore = true)
    @Mapping(target = "comments", ignore = true)
    @Mapping(target = "bookings", ignore = true)
    User mapToModel(UserDto itemDto);

    UserDto mapToDto(User user);
}
