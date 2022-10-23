package ru.practicum.shareit.user.repository;

import lombok.AllArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.user.exception.DuplicateEmailException;
import ru.practicum.shareit.user.exception.UserNotFoundException;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UpdatedUserFields;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AllArgsConstructor(onConstructor_ = @Autowired)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class InMemoryUserRepositoryTest {

    UserRepository repository;

    @Test
    public void addAndGetUserTest() {
        User user = makeDefaultUser();
        user.setId(1L);
        repository.addUser(user);

        assertEquals(user, repository.getUser(1));
    }

    @Test
    public void shouldNotAddUserWithExistingEmail() {
        User user = makeDefaultUser();
        repository.addUser(user);

        assertThrows(DuplicateEmailException.class, () -> repository.addUser(user));
    }

    @Test
    public void shouldThrowExceptionForNotFoundUser() {
        assertThrows(UserNotFoundException.class, () -> repository.getUser(1));
    }

    @Test
    public void getUsersTest() {
        User user1 = makeDefaultUser();
        user1.setId(1L);
        repository.addUser(user1);

        User user2 = makeCustomUser("Sam", "samsmail@yandex.ru");
        user2.setId(2L);
        repository.addUser(user2);

        assertEquals(List.of(user1, user2), repository.getUsers());
    }

    @Test
    public void updateUserTest() {
        Map<UpdatedUserFields, Boolean> targetFields = new HashMap<>();

        User user = makeCustomUser("Sam", "samsmail@yandex.ru");
        user.setId(1L);
        repository.addUser(user);

        user = makeCustomUser(null, "newsamsmail@yandex.ru");
        user.setId(1L);
        targetFields.put(UpdatedUserFields.NAME, false);
        targetFields.put(UpdatedUserFields.EMAIL, true);
        repository.updateUser(user, targetFields);

        User resultUser = makeCustomUser("Sam", "newsamsmail@yandex.ru");
        resultUser.setId(1L);

        assertEquals(resultUser, repository.getUser(1));
    }

    @Test
    public void shouldNotUpdateUserWithDuplicateEmail() {
        Map<UpdatedUserFields, Boolean> targetFields = new HashMap<>();

        User user = makeCustomUser("Sam", "samsmail@yandex.ru");
        user.setId(1L);
        repository.addUser(user);

        user = makeCustomUser(null, "samsmail@yandex.ru");
        user.setId(1L);
        targetFields.put(UpdatedUserFields.NAME, false);
        targetFields.put(UpdatedUserFields.EMAIL, true);

        User finalUser = user;
        assertThrows(DuplicateEmailException.class, () -> repository.updateUser(finalUser, targetFields));
    }

    @Test
    public void deleteUserTest() {
        User user = makeDefaultUser();
        user.setId(1L);
        repository.addUser(user);
        repository.deleteUser(1);

        assertTrue(repository.getUsers().isEmpty());
    }

    @Test
    public void shouldThrowExceptionForDeletingAbsentUser() {
        assertThrows(UserNotFoundException.class, () -> repository.deleteUser(1));
    }

    private User makeDefaultUser() {
        return User.builder()
                        .name("Tom")
                        .email("tomsmail@mail.ru")
                        .build();
    }

    private User makeCustomUser(String name, String email) {
        return User.builder()
                .name(name)
                .email(email)
                .build();
    }
}
