package ru.practicum.shareit.user.service;

import lombok.AllArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;
import ru.practicum.shareit.user.exception.DuplicateEmailException;
import ru.practicum.shareit.user.exception.UserNotFoundException;
import ru.practicum.shareit.user.model.User;

import java.util.HashMap;
import java.util.Map;

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
        assertThrows(UserNotFoundException.class, () -> service.getUser(1L));
    }

    @Test
    public void shouldThrowExceptionForUpdatingUserWithDuplicateEmail() {
        service.addUser(makeDefaultUser());
        User user = makeDefaultUser();
        user.setEmail("new@mail.ru");
        service.addUser(user);

        Map<UpdatedUserFields, Boolean> targetFields = new HashMap<>();
        targetFields.put(UpdatedUserFields.EMAIL, true);
        targetFields.put(UpdatedUserFields.NAME, false);
        User updatedUser = makeDefaultUser();
        updatedUser.setId(user.getId());

        assertThrows(DuplicateEmailException.class, () -> service.updateUser(updatedUser, targetFields));
    }

    @Test
    public void deleteUserTest() {
        User user = service.addUser(makeDefaultUser());
        long userId = user.getId();
        assertEquals(user, service.getUser(userId));

        service.deleteUser(userId);
        assertTrue(service.getUsers().isEmpty());
    }

    @Test
    public void shouldThrowExceptionForDeletingAbsentUser() {
        assertThrows(UserNotFoundException.class, () -> service.deleteUser(1L));
    }

    private User makeDefaultUser() {
        return User.builder()
                .name("Tom")
                .email("tomsmail@mail.ru")
                .build();
    }
}
