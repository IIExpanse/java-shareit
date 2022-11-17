package ru.practicum.shareit.item.service;

import lombok.AllArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.exception.ItemNotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.exception.UserNotFoundException;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

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
        UserDto user = userService.addUser(makeDefaultUser());
        ItemDto item = itemService.addItem(makeDefaultItem(), user.getId());
        item.setComments(List.of());

        assertEquals(item, itemService.getItemDto(item.getId(), user.getId()));
    }

    @Test
    public void shouldThrowExceptionForAddingItemWithAbsentOwner() {
        UserDto user = makeDefaultUser();
        user.setId(1L);
        ItemDto item = makeDefaultItem();

        assertThrows(UserNotFoundException.class, () -> itemService.addItem(item, user.getId()));
    }

    @Test
    public void shouldThrowExceptionForAddingItemWithBlankNameOrDescription() {
        UserDto user = userService.addUser(makeDefaultUser());
        ItemDto item = makeDefaultItem();
        item.setName("");
        assertThrows(DataIntegrityViolationException.class, () -> itemService.addItem(item, user.getId()));

        item.setName("name");
        item.setDescription("");
        assertThrows(DataIntegrityViolationException.class, () -> itemService.addItem(item, user.getId()));

        item.setName("");
        assertThrows(DataIntegrityViolationException.class, () -> itemService.addItem(item, user.getId()));
    }

    @Test
    public void shouldThrowExceptionForGettingNotFoundItem() {
        UserDto user = userService.addUser(makeDefaultUser());

        assertThrows(ItemNotFoundException.class, () -> itemService.getItemDto(1L, user.getId()));
    }

    private ItemDto makeDefaultItem() {
        return ItemDto.builder()
                .name("DEBUGGER 9000")
                .description("Launch and debug!")
                .available(true)
                .build();
    }

    private UserDto makeDefaultUser() {
        return UserDto.builder()
                .name("Tom")
                .email("tomsmail@mail.ru")
                .build();
    }
}
