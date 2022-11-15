package ru.practicum.shareit.user.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.exception.DuplicateEmailException;
import ru.practicum.shareit.user.exception.UserNotFoundException;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.service.UpdatedUserFields;
import ru.practicum.shareit.user.service.UserService;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    public User addUser(User user) {
        try {
            return userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateEmailException("Ошибка добавления пользователя: такой email уже существует.");
        }

    }

    @Override
    public User getUser(long id) {
        Optional<User> user = userRepository.findById(id);

        if (user.isEmpty()) {
            throw new UserNotFoundException(
                    String.format("Ошибка получения: пользователь с id=%d не найден.", id));
        }

        return user.get();
    }

    @Override
    public Collection<User> getUsers() {
        return userRepository.findAll();
    }

    @Override
    public boolean userExists(long userId) {
        return userRepository.existsById(userId);
    }

    @Override
    public User updateUser(User user, Map<UpdatedUserFields, Boolean> targetFields) {
        try {
            return userRepository.updateUser(user, targetFields);

        } catch (DataIntegrityViolationException e) {
            throw new DuplicateEmailException("Ошибка обновления пользователя: такой email уже существует.");
        }
    }

    /**
     * При удалении пользователя также вызывается метод удаления всех вещей, которыми он владеет.
     */
    @Override
    public void deleteUser(long id) {
        if (userRepository.existsById(id)) {
            Optional<User> userOptional = userRepository.findById(id);
            if (userOptional.isPresent()) {
                itemRepository.deleteAllByOwner(userOptional.get());

            } else throw new RuntimeException();
            userRepository.deleteById(id);

        } else throw new UserNotFoundException(String.format("Ошибка удаления: пользователь с id=%d не найден.", id));
    }
}
