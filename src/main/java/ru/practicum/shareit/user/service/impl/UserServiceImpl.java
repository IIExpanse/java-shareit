package ru.practicum.shareit.user.service.impl;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.service.UpdatedUserFields;
import ru.practicum.shareit.user.service.UserService;

import java.util.Collection;
import java.util.Map;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    public User addUser(User user) {
        return userRepository.addUser(user);
    }

    @Override
    public User getUser(long id) {
        return userRepository.getUser(id);
    }

    @Override
    public Collection<User> getUsers() {
        return userRepository.getUsers();
    }

    @Override
    public User updateUser(User user, Map<UpdatedUserFields, Boolean> targetFields) {
        return userRepository.updateUser(user, targetFields);
    }

    /**
     * При удалении пользователя также вызывается метод удаления всех вещей, которыми он владеет.
     */
    @Override
    public void deleteUser(long id) {
        userRepository.deleteUser(id);
        itemRepository.deleteUserItems(id);
    }
}
