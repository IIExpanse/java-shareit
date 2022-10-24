package ru.practicum.shareit.user.repository.impl;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.user.exception.DuplicateEmailException;
import ru.practicum.shareit.user.exception.UserNotFoundException;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.service.UpdatedUserFields;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class InMemoryUserRepository implements UserRepository {

    private long idCounter = 1;
    private final Map<Long, User> userMap = new HashMap<>();

    @Override
    public User addUser(User user) {
        String newEmail = user.getEmail();

        if (getUsers().stream().map(User::getEmail).noneMatch(email -> email.equals(newEmail))) {
            user.setId(idCounter);
            userMap.put(idCounter, user);

            return userMap.get(idCounter++);

        } else {
            throw new DuplicateEmailException("Ошибка добавления пользователя: такой email уже существует.");
        }
    }

    @Override
    public User getUser(long id) {
        if (userMap.containsKey(id)) {
            return userMap.get(id);

        } else throw new UserNotFoundException(
                String.format("Ошибка получения: пользователь с id=%d не найден.", id));
    }

    @Override
    public Collection<User> getUsers() {
        return List.copyOf(userMap.values());
    }

    @Override
    public User updateUser(User user, Map<UpdatedUserFields, Boolean> targetFields) {
        long id = user.getId();

        if (userMap.containsKey(id)) {
            User existingUser = userMap.get(user.getId());
            String name = user.getName();
            String newEmail = user.getEmail();
            User newUser;

            if (!targetFields.get(UpdatedUserFields.NAME)) {
                name = existingUser.getName();
            }
            if (!targetFields.get(UpdatedUserFields.EMAIL)) {
                newEmail = existingUser.getEmail();
            } else {
                String finalNewEmail = newEmail;
                if (getUsers().stream().map(User::getEmail).anyMatch(email -> email.equals(finalNewEmail))) {
                    throw new DuplicateEmailException(
                            String.format(
                                    "Ошибка обновления пользователя с id=%d: такой email уже существует.", id));
                }
            }
            newUser = User.builder()
                    .id(id)
                    .name(name)
                    .email(newEmail)
                    .build();
            userMap.put(id, newUser);

            return newUser;

        } else throw new UserNotFoundException(
                String.format("Ошибка обновления: пользователь с id=%d не найден.", id));
    }

    @Override
    public void deleteUser(long id) {
        if (userMap.containsKey(id)) {
            userMap.remove(id);

        } else throw new UserNotFoundException(
                String.format("Ошибка удаления: пользователь с id=%d не найден.", id));
    }
}
