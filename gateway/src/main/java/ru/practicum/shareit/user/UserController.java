package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;

import javax.validation.Valid;

@Controller
@RequestMapping(path = "/users")
@Slf4j
@RequiredArgsConstructor
@Validated
public class UserController {

    private final UserClient userClient;

    @PostMapping
    @Validated(value = UserValidationGroup.FullValidation.class)
    public ResponseEntity<Object> addUser(@RequestBody @Valid UserDto userDto) {

        log.info("Creating user {}", userDto);
        return userClient.addUser(userDto);
    }

    @GetMapping(path = "/{id}")
    public ResponseEntity<Object> getUser(@PathVariable Long id) {

        log.info("Get user, userId={}", id);
        return userClient.getUser(id);
    }

    @GetMapping
    public ResponseEntity<Object> getUsers() {

        log.info("Get users");
        return userClient.getUsers();
    }

    @PatchMapping(path = "/{id}")
    @Validated(value = UserValidationGroup.PatchValidation.class)
    public ResponseEntity<Object> updateUser(@RequestBody @Valid UserDto userDto, @PathVariable Long id) {

        log.info("Updating user {}, id={}", userDto, id);
        return userClient.updateUser(userDto, id);
    }

    @DeleteMapping(path = "/{id}")
    @ResponseStatus(code = HttpStatus.OK)
    public void deleteUser(@PathVariable Long id) {

        log.info("Delete user, userId={}", id);
        userClient.deleteUser(id);
    }
}