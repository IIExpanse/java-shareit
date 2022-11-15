package ru.practicum.shareit.user.repository.impl;

import org.springframework.context.annotation.Lazy;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.user.exception.UserNotFoundException;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.repository.UserRepositoryCustom;
import ru.practicum.shareit.user.service.UpdatedUserFields;

import java.util.Map;
import java.util.Optional;

@Transactional
public class UserRepositoryImpl implements UserRepositoryCustom {

    private final UserRepository userRepository;

    public UserRepositoryImpl(@Lazy UserRepository userRepository){
        this.userRepository = userRepository;
    }

    @Override
    public User updateUser(User user, Map<UpdatedUserFields, Boolean> targetFields) {
        long id = user.getId();
        Optional<User> userOptional = userRepository.findById(id);

        if (userOptional.isPresent()) {
            User existingUser = userOptional.get();
            String name = user.getName();
            String newEmail = user.getEmail();
            User newUser;

            if (!targetFields.get(UpdatedUserFields.NAME)) {
                name = existingUser.getName();
            }
            if (!targetFields.get(UpdatedUserFields.EMAIL)) {
                newEmail = existingUser.getEmail();
            }
            newUser = User.builder()
                    .id(id)
                    .name(name)
                    .email(newEmail)
                    .build();

            return userRepository.save(newUser);

        } else throw new UserNotFoundException(
                String.format("Ошибка обновления: пользователь с id=%d не найден.", id));
    }
}
