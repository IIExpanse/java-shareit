package ru.practicum.shareit.item.service;

import lombok.AllArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;
import ru.practicum.shareit.item.exception.ItemNotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.exception.UserNotFoundException;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@AllArgsConstructor(onConstructor_ = @Autowired)
@AutoConfigureTestDatabase
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Sql(scripts = "classpath:schema.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class ItemServiceTest {

    private UserService userService;
    private ItemService itemService;

    @Test
    public void addItemTest() {
        User user = userService.addUser(makeDefaultUser());
        Item item = itemService.addItem(makeDefaultItem(user));

        assertEquals(item, itemService.getItem(item.getId()));
    }

    @Test
    public void shouldThrowExceptionForAddingItemWithAbsentOwner() {
        User user = makeDefaultUser();
        user.setId(1L);
        Item item = makeDefaultItem(user);

        assertThrows(UserNotFoundException.class, () -> itemService.addItem(item));
    }

    @Test
    public void shouldThrowExceptionForAddingItemWithBlankNameOrDescription() {
        User user = userService.addUser(makeDefaultUser());
        Item item = makeDefaultItem(user);
        item.setName("");
        assertThrows(DataIntegrityViolationException.class, () -> itemService.addItem(item));

        item.setName("name");
        item.setDescription("");
        assertThrows(DataIntegrityViolationException.class, () -> itemService.addItem(item));

        item.setName("");
        assertThrows(DataIntegrityViolationException.class, () -> itemService.addItem(item));
    }

    @Test
    public void shouldThrowExceptionForGettingNotFoundItem() {
        assertThrows(ItemNotFoundException.class, () -> itemService.getItem(1L));
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
