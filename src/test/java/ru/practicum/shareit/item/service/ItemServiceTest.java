package ru.practicum.shareit.item.service;

import lombok.AllArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.exception.UserNotFoundException;

@SpringBootTest
@AllArgsConstructor(onConstructor_ = @Autowired)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class ItemServiceTest {

    private ItemService service;

    @Test
    public void shouldThrowExceptionForAddingItemWithAbsentOwner() {
        Item item = makeDefaultItem();
        item.setItemId(1L);
        item.setOwnerId(1L);

        Assertions.assertThrows(UserNotFoundException.class, () -> service.addItem(item));
    }

    private Item makeDefaultItem() {
        return Item.builder()
                .name("DEBUGGER 9000")
                .description("Launch and debug!")
                .available(true)
                .build();
    }
}
