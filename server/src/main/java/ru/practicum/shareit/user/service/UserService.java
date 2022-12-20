package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.Collection;

public interface UserService {

    UserDto addUser(UserDto userDto);

    UserDto getUserDto(long id);

    User getUser(long userId);

    Collection<UserDto> getUsers();

    boolean userNotFound(long userId);

    UserDto updateUser(UserDto userDto, long userId);

    void deleteUser(long id);
}
