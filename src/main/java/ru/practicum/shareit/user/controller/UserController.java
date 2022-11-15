package ru.practicum.shareit.user.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.exception.DuplicateEmailException;
import ru.practicum.shareit.user.exception.EmptyUserPatchRequestException;
import ru.practicum.shareit.user.exception.UserNotFoundException;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UpdatedUserFields;
import ru.practicum.shareit.user.service.UserService;

import javax.validation.Valid;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Класс для обработки запросов, работающих с объектами пользователей.
 */
@RestController
@RequestMapping(path = "/users")
@Slf4j
@RequiredArgsConstructor
@Validated
public class UserController {

    private final UserService service;
    private final UserMapper mapper;

    /**
     * Добавление нового пользователя.
     *
     * @param userDto - DTO пользователя, все поля (кроме ID) проходят валидацию.
     * @return Созданный DTO пользователя с присвоенным ему ID.
     * @throws DuplicateEmailException - если email пользователя уже существует в базе.
     */
    @PostMapping
    @Validated(value = UserValidationGroup.FullValidation.class)
    ResponseEntity<UserDto> addUser(@RequestBody @Valid UserDto userDto) {
        User user = mapper.mapToModel(userDto);
        user.setId(null);
        ResponseEntity<UserDto> response = new ResponseEntity<>(
                mapper.mapToDto(service.addUser(user)), HttpStatus.CREATED);

        log.debug("Добавлен новый пользователь: {}", response.getBody());
        return response;
    }

    /**
     * Получение существующего пользователя.
     *
     * @param id - идентификатор существующего пользователя.
     * @return DTO существующего пользователя.
     * @throws UserNotFoundException - если пользователя с указанным id не существует в базе.
     */
    @GetMapping(path = "/{id}")
    ResponseEntity<UserDto> getUser(@PathVariable long id) {
        return ResponseEntity.ok(mapper.mapToDto(service.getUser(id)));
    }

    /**
     * Получение списка всех существующих пользователей.
     *
     * @return Список с DTO пользователей.
     */
    @GetMapping
    ResponseEntity<Collection<UserDto>> getUsers() {
        return ResponseEntity.ok(service.getUsers().stream()
                .map(mapper::mapToDto)
                .collect(Collectors.toList()));
    }

    /**
     * Обновление данных существующего пользователя.
     * Для передачи на уровень сервиса и репозитория формируется targetFields - таблица с указанием полей,
     * которые нужно обновить.
     *
     * @param userDto - DTO с полями пользователей, которые необходимо обновить.
     *                Как минимум одно поле (кроме id) не должно быть null.
     * @param id      - идентификатор пользователя, чьи данные необходимо обновить.
     * @return DTO пользователя после обновления его данных.
     * @throws EmptyUserPatchRequestException - если в полученном объекте все поля (кроме id) равны null.
     * @throws DuplicateEmailException        - при попытке заменить email на уже существующий в базе.
     */
    @PatchMapping(path = "/{id}")
    @Validated(value = UserValidationGroup.PatchValidation.class)
    ResponseEntity<UserDto> updateUser(@RequestBody @Valid UserDto userDto, @PathVariable long id) {
        Map<UpdatedUserFields, Boolean> targetFields = new HashMap<>();
        boolean empty = true;
        ResponseEntity<UserDto> response;
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

        user = mapper.mapToModel(userDto);
        user.setId(id);
        response = ResponseEntity.ok(
                mapper.mapToDto(service.updateUser(user, targetFields)));

        log.debug("Обновлен пользователь: {}", response.getBody());
        return response;
    }

    /**
     * Удаление существующего пользователя. Также происходит удаление всех вещей, у которых он является владельцем.
     *
     * @param id - идентификатор удаляемого пользователя.
     * @throws UserNotFoundException - при попытке удалить несуществующего пользователя.
     */
    @DeleteMapping(path = "/{id}")
    @ResponseStatus(code = HttpStatus.OK)
    void deleteUser(@PathVariable long id) {
        service.deleteUser(id);
    }
}
