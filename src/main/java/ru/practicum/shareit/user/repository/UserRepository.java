package ru.practicum.shareit.user.repository;

import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UpdatedUserFields;

import java.util.Collection;
import java.util.Map;

public interface UserRepository {

    User addUser(User user);

    User getUser(long id);

    Collection<User> getUsers();

    User updateUser(User user, Map<UpdatedUserFields, Boolean> targetFields);

    void deleteUser(long id);
}
