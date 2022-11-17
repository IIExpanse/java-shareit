package ru.practicum.shareit.user.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.exception.DuplicateEmailException;
import ru.practicum.shareit.user.exception.EmptyUserPatchRequestException;
import ru.practicum.shareit.user.exception.UserNotFoundException;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.service.UpdatedUserFields;
import ru.practicum.shareit.user.service.UserService;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final UserMapper userMapper;

    @Override
    public UserDto addUser(UserDto userDto) {
        User user = userMapper.mapToModel(userDto);
        user.setId(null);

        try {
            user = userRepository.save(user);
            log.debug("Добавлен новый пользователь: {}", user);
            return userMapper.mapToDto(user);

        } catch (DataIntegrityViolationException e) {
            throw new DuplicateEmailException("Ошибка добавления пользователя: такой email уже существует.");
        }

    }

    @Override
    public UserDto getUserDto(long userId) {

        return userMapper.mapToDto(this.getUser(userId));
    }

    @Override
    public User getUser(long userId) {
        Optional<User> user = userRepository.findById(userId);

        if (user.isEmpty()) {
            throw new UserNotFoundException(
                    String.format("Ошибка получения: пользователь с id=%d не найден.", userId));
        }

        return user.get();
    }

    @Override
    public Collection<UserDto> getUsers() {

        return userRepository.findAll().stream()
                .map(userMapper::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public boolean userNotFound(long userId) {
        return !userRepository.existsById(userId);
    }

    @Override
    public UserDto updateUser(UserDto userDto, long userId) {
        Map<UpdatedUserFields, Boolean> targetFields = new HashMap<>();
        boolean empty = true;
        User user;

        if (userDto.getName() != null) {
            targetFields.put(UpdatedUserFields.NAME, true);
            empty = false;
        } else {
            targetFields.put(UpdatedUserFields.NAME, false);
        }

        if (userDto.getEmail() != null) {
            targetFields.put(UpdatedUserFields.EMAIL, true);
            empty = false;
        } else {
            targetFields.put(UpdatedUserFields.EMAIL, false);
        }

        if (empty) {
            throw new EmptyUserPatchRequestException("Ошибка обновления пользователя: в запросе все поля равны null.");
        }

        user = userMapper.mapToModel(userDto);
        user.setId(userId);

        try {
            user = userRepository.updateUser(user, targetFields);

            log.debug("Обновлен пользователь: {}", user);
            return userMapper.mapToDto(user);

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
