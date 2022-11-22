package ru.practicum.shareit.item.mapper;

import lombok.AllArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Да здравствует покрытие ради покрытия..
 */
@SpringBootTest
@AllArgsConstructor(onConstructor_ = @Autowired)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class ItemMapperTest {

    private ItemMapper mapper;

    @Test
    public void mapToShortDtoTest() {
        Item item = Item.builder()
                .id(123L)
                .name("item")
                .description("desc")
                .request(null)
                .bookings(Set.of())
                .available(false)
                .comments(Set.of())
                .build();

        ItemDto shortDto = mapper.mapToShortDto(item);

        assertEquals(item.getId(), shortDto.getId());
        assertEquals(item.getName(), shortDto.getName());
        assertEquals(item.getAvailable(), shortDto.getAvailable());
        assertNull(shortDto.getLastBooking());
        assertNull(shortDto.getNextBooking());
        assertNull(shortDto.getComments());
        assertNull(shortDto.getRequestId());
    }
}
