package ru.practicum.shareit.item.repository;

import lombok.AllArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.item.exception.ItemNotFoundException;
import ru.practicum.shareit.item.exception.WrongOwnerUpdatingItemException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.UpdatedItemFields;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AllArgsConstructor(onConstructor_ = @Autowired)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class InMemoryItemRepositoryTest {

    private ItemRepository itemRepository;
    private UserRepository userRepository;

    @Test
    public void addAndGetItemTest() {
        addDefaultUser();
        Item item = makeDefaultItem();
        item.setItemId(1L);
        item.setOwnerId(1L);
        itemRepository.addItem(item);


        assertEquals(item, itemRepository.getItem(1));
    }

    @Test
    public void shouldThrowExceptionForNotFoundItem() {
        assertThrows(ItemNotFoundException.class, () -> itemRepository.getItem(1));
    }

    @Test
    public void getOwnerItemsTest() {
        addDefaultUser();
        Item item1 = makeDefaultItem();
        item1.setItemId(1L);
        item1.setOwnerId(1L);
        itemRepository.addItem(item1);

        Item item2 = makeCustomItem("Second item", "Some item descr", false);
        item2.setItemId(2L);
        item2.setOwnerId(1L);
        itemRepository.addItem(item2);

        assertEquals(List.of(item1, item2), itemRepository.getOwnerItems(1));
    }

    @Test
    public void updateItemTest() {
        Map<UpdatedItemFields, Boolean> targetFields = new HashMap<>();

        addDefaultUser();
        Item item1 = makeCustomItem("New item", "Some item descr", false);
        item1.setOwnerId(1L);
        itemRepository.addItem(item1);

        item1 = makeCustomItem(null, "New item descr", true);
        item1.setItemId(1L);
        item1.setOwnerId(1L);
        targetFields.put(UpdatedItemFields.NAME, false);
        targetFields.put(UpdatedItemFields.DESCRIPTION, true);
        targetFields.put(UpdatedItemFields.AVAILABLE, true);
        itemRepository.updateItem(item1, targetFields);

        Item resultItem = makeCustomItem("New item", "New item descr", true);
        resultItem.setItemId(1L);
        resultItem.setOwnerId(1L);

        assertEquals(resultItem, itemRepository.getItem(1));
    }

    @Test
    public void shouldThrowExceptionForWrongOwnerOnUpdate() {
        Map<UpdatedItemFields, Boolean> targetFields = new HashMap<>();

        addDefaultUser();
        Item item1 = makeCustomItem("New item", "Some item descr", false);
        item1.setOwnerId(1L);
        itemRepository.addItem(item1);

        item1 = makeCustomItem(null, "New item descr", true);
        item1.setItemId(1L);
        item1.setOwnerId(2L);
        targetFields.put(UpdatedItemFields.NAME, false);
        targetFields.put(UpdatedItemFields.DESCRIPTION, true);
        targetFields.put(UpdatedItemFields.AVAILABLE, true);

        Item finalItem = item1;
        assertThrows(WrongOwnerUpdatingItemException.class, () -> itemRepository.updateItem(finalItem, targetFields));
    }

    @Test
    public void searchAvailableItemsTest() {
        Item item1 = makeCustomItem("DEBUGGER", "Some item descr", true);
        item1.setItemId(1L);
        item1.setOwnerId(1L);
        itemRepository.addItem(item1);

        Item item2 = makeCustomItem("Some item", "DeBuGgER again", true);
        item2.setItemId(2L);
        item2.setOwnerId(1L);
        itemRepository.addItem(item2);

        Item item3 = makeCustomItem("Not to be found", "Not debugger", false);
        item3.setItemId(3L);
        item3.setOwnerId(1L);
        itemRepository.addItem(item3);

        assertEquals(List.of(item1, item2), itemRepository.searchAvailableItems("debUgger"));
    }

    @Test
    public void deleteUserItemsTest() {
        addDefaultUser();
        Item item1 = makeDefaultItem();
        item1.setItemId(1L);
        item1.setOwnerId(1L);
        itemRepository.addItem(item1);

        Item item2 = makeDefaultItem();
        item2.setItemId(2L);
        item2.setOwnerId(1L);
        itemRepository.addItem(item2);

        itemRepository.deleteUserItems(1);
        assertTrue(itemRepository.getOwnerItems(1).isEmpty());
    }

    private Item makeDefaultItem() {
        return Item.builder()
                .name("DEBUGGER 9000")
                .description("Launch and debug!")
                .available(true)
                .build();
    }

    private Item makeCustomItem(String name, String description, boolean available) {
        return Item.builder()
                .name(name)
                .description(description)
                .available(available)
                .build();
    }

    private void addDefaultUser() {
        userRepository.addUser(
                User.builder()
                        .name("Tom")
                        .email("tomsmail@mail.ru")
                        .build()
        );
    }
}
