package ru.practicum.shareit.item.repository;

import lombok.AllArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AllArgsConstructor(onConstructor_ = @Autowired)
@AutoConfigureTestDatabase
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Sql(scripts = "classpath:schema.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class ItemRepositoryTest {

    private UserRepository userRepository;
    private ItemRepository itemRepository;

    @Test
    public void existsItemByIdAndAvailableIsTrueTest() {
        User user = userRepository.save(makeDefaultUser());
        Item item1 = itemRepository.save(makeDefaultItem(user));
        assertTrue(itemRepository.existsItemByIdAndAvailableIsTrue(item1.getId()));

        Item item2 = makeDefaultItem(user);
        item2.setAvailable(false);
        item2 = itemRepository.save(item2);
        assertFalse(itemRepository.existsItemByIdAndAvailableIsTrue(item2.getId()));

        assertFalse(itemRepository.existsItemByIdAndAvailableIsTrue(3));
    }

    @Test
    public void findAllByOwnerIdTest() {
        User user1 = userRepository.save(makeDefaultUser());
        Item item1 = itemRepository.save(makeDefaultItem(user1));
        assertTrue(itemRepository.existsItemByIdAndAvailableIsTrue(item1.getId()));
        assertEquals(List.of(item1), itemRepository.findAllByOwnerId(user1.getId(), Pageable.unpaged()).getContent());

        User user2 = makeDefaultUser();
        user2.setEmail("new@mail.ru");
        user2 = userRepository.save(user2);
        Item item2 = makeDefaultItem(user2);
        item2 = itemRepository.save(item2);
        assertEquals(List.of(item2), itemRepository.findAllByOwnerId(user2.getId(), Pageable.unpaged()).getContent());
    }

    @Test
    public void searchAvailableItemsByNameAndDescriptionTest() {
        User user = userRepository.save(makeDefaultUser());

        Item item1 = makeDefaultItem(user);
        item1.setName("DEBUGGER");
        item1.setDescription("Some item descr");
        item1.setAvailable(true);
        itemRepository.save(item1);

        Item item2 = makeDefaultItem(user);
        item2.setName("Some item");
        item2.setDescription("DeBuGgER again");
        item2.setAvailable(true);
        itemRepository.save(item2);

        Item item3 = makeDefaultItem(user);
        item3.setName("Not to be found");
        item3.setDescription("Not debugger");
        item3.setAvailable(false);
        itemRepository.save(item3);

        assertEquals(List.of(item1, item2), itemRepository.searchAvailableItemsByNameAndDescription(
                "debUgger", Pageable.unpaged()).getContent());
    }

    @Test
    void deleteAllByOwnerIdTest() {
        User user = userRepository.save(makeDefaultUser());
        long userId = user.getId();
        Item item1 = itemRepository.save(makeDefaultItem(user));
        Item item2 = itemRepository.save(makeDefaultItem(user));
        assertEquals(List.of(item1, item2), itemRepository.findAllByOwnerId(userId, Pageable.unpaged()).getContent());

        itemRepository.deleteAllByOwner(user);
        assertEquals(List.of(), itemRepository.findAllByOwnerId(userId, Pageable.unpaged()).getContent());
    }

    private Item makeDefaultItem(User owner) {
        return Item.builder()
                .owner(owner)
                .name("DEBUGGER 9000")
                .description("Launch and debug!")
                .available(true)
                .build();
    }

    private User makeDefaultUser() {
        return User.builder()
                .name("Tom")
                .email("tomsmail@mail.ru")
                .build();
    }
}
