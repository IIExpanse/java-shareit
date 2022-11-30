package ru.practicum.shareit.user.repository;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UpdatedUserFields;

import java.util.Map;

@Repository
public interface UserRepositoryCustom {
    User updateUser(User user, Map<UpdatedUserFields, Boolean> targetFields);
}
