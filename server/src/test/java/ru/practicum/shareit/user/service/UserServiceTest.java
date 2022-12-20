package ru.practicum.shareit.user.service;

import lombok.AllArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.exception.DuplicateEmailException;
import ru.practicum.shareit.user.exception.UserNotFoundException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AllArgsConstructor(onConstructor_ = @Autowired)
@AutoConfigureTestDatabase
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Sql(scripts = "classpath:schema.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class UserServiceTest {

    private UserService service;

    @Test
    public void shouldThrowExceptionForDuplicateEmail() {
        service.addUser(makeDefaultUser());

        assertThrows(DuplicateEmailException.class, () -> service.addUser(makeDefaultUser()));
    }

    @Test
    public void shouldThrowExceptionForNotFoundUser() {
        assertThrows(UserNotFoundException.class, () -> service.getUserDto(1L));
    }

    @Test
    public void shouldThrowExceptionForUpdatingUserWithDuplicateEmail() {
        service.addUser(makeDefaultUser());
        UserDto user = makeDefaultUser();
        user.setEmail("new@mail.ru");
        user = service.addUser(user);

        UserDto updatedUser = makeDefaultUser();
        updatedUser.setId(user.getId());

        UserDto finalUser = user;
        assertThrows(DuplicateEmailException.class, () -> service.updateUser(updatedUser, finalUser.getId()));
    }

    @Test
    public void deleteUserTest() {
        UserDto user = service.addUser(makeDefaultUser());
        long userId = user.getId();
        assertEquals(user, service.getUserDto(userId));

        service.deleteUser(userId);
        assertTrue(service.getUsers().isEmpty());
    }

    @Test
    public void shouldThrowExceptionForDeletingAbsentUser() {
        assertThrows(UserNotFoundException.class, () -> service.deleteUser(1L));
    }

    private UserDto makeDefaultUser() {
        return UserDto.builder()
                .name("Tom")
                .email("tomsmail@mail.ru")
                .build();
    }
}
