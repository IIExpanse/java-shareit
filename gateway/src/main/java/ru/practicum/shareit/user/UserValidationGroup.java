package ru.practicum.shareit.user;

import lombok.Generated;

@Generated
public class UserValidationGroup {

    /**
     * Маркер для валидации объекта пользователя при создании нового.
     */
    public interface FullValidation {
    }

    /**
     * Маркер для валидации объекта пользователя при обновлении существующего.
     */
    public interface PatchValidation {
    }
}