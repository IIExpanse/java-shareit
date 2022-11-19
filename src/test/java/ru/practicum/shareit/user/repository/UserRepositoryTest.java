package ru.practicum.shareit.user.repository;

import lombok.AllArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UpdatedUserFields;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@AllArgsConstructor(onConstructor_ = @Autowired)
@AutoConfigureTestDatabase
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Sql(scripts = "classpath:schema.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class UserRepositoryTest {

    private UserRepository repository;

    @Test
    public void updateUserTest() {
        Map<UpdatedUserFields, Boolean> targetFields = new HashMap<>();
        targetFields.put(UpdatedUserFields.EMAIL, false);
        targetFields.put(UpdatedUserFields.NAME, true);

        User user1 = makeDefaultUser();
        repository.save(user1);

        User user2 = makeDefaultUser();
        user2.setId(1L);
        user2.setEmail(null);
        user2.setName("Sam");
        repository.updateUser(user2, targetFields);

        User finishedUser = makeDefaultUser();
        finishedUser.setId(1L);
        finishedUser.setName("Sam");

        Optional<User> result = repository.findById(1L);
        assertTrue(result.isPresent());
        assertEquals(finishedUser, result.get());
    }

    private User makeDefaultUser() {
        return User.builder()
                .name("Tom")
                .email("tomsmail@mail.ru")
                .build();
    }
}
