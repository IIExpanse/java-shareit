package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.model.User;

import java.util.Collection;
import java.util.Map;

public interface UserService {

    User addUser(User user);

    User getUser(long id);

    Collection<User> getUsers();

    User updateUser(User user, Map<UpdatedUserFields, Boolean> targetFields);

    void deleteUser(long id);
}
