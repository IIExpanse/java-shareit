package ru.practicum.shareit.user.controller;

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