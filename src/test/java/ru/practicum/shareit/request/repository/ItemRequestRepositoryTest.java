package ru.practicum.shareit.request.repository;

import lombok.AllArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
@AllArgsConstructor(onConstructor_ = @Autowired)
@AutoConfigureTestDatabase
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Sql(scripts = "classpath:schema.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class ItemRequestRepositoryTest {

    private UserRepository userRepository;
    private ItemRequestRepository requestRepository;

    @Test
    public void findAllByRequesterIdOrderByCreatedDescTest() {
        User requester = makeDefaultUser();
        requester = userRepository.save(requester);

        ItemRequest request1 = requestRepository.save(makeDefaultRequest(requester));
        ItemRequest request2 = requestRepository.save(makeDefaultRequest(requester));

        assertEquals(List.of(request2, request1),
                requestRepository.findAllByRequesterIdOrderByCreatedDesc(requester.getId()));
    }

    @Test
    public void findAllByRequesterIdNotOrderByCreatedDesc() {
        User requester = makeDefaultUser();
        requester = userRepository.save(requester);

        User requester2 = makeDefaultUser();
        requester2.setEmail("new@mail.com");
        requester2 = userRepository.save(requester2);

        ItemRequest request1 = requestRepository.save(makeDefaultRequest(requester));
        requestRepository.save(makeDefaultRequest(requester2));

        assertEquals(List.of(request1),
                requestRepository.findAllByRequesterIdNotOrderByCreatedDesc(requester2.getId()));
    }

    private ItemRequest makeDefaultRequest(User requester) {
        return ItemRequest.builder()
                .requester(requester)
                .description("Need some item")
                .created(LocalDateTime.now().truncatedTo(ChronoUnit.MICROS))
                .build();
    }

    private User makeDefaultUser() {
        return User.builder()
                .name("Tom")
                .email("tomsmail@mail.ru")
                .build();
    }
}
