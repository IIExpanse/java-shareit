package ru.practicum.shareit.item.service;

import lombok.AllArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoRequest;
import ru.practicum.shareit.booking.exception.CommenterDontHaveBookingException;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.item.comment.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.exception.ItemNotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.exception.UserNotFoundException;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
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
    private BookingService bookingService;

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

    @Test
    public void addCommentTest() {
        UserDto user = userService.addUser(makeDefaultUser());
        UserDto booker = userService.addUser(UserDto.builder().name("Sam").email("new@mail.ru").build());
        long userId = user.getId();
        long bookerId = booker.getId();

        ItemDto item = itemService.addItem(makeDefaultItem(), userId);
        long itemId = item.getId();

        BookingDtoRequest request = makeDefaultBookingDtoRequest(itemId);

        BookingDto bookingDto = bookingService.addBooking(request, bookerId);
        bookingService.setApproval(bookingDto.getId(), true, userId);

        CommentDto comment = makeDefaultComment();
        comment = itemService.addComment(comment, bookerId, itemId);

        assertEquals(List.of(comment), itemService.getItemDto(itemId, userId).getComments());
    }

    @Test
    public void shouldThrowExceptionForCommenterWithoutBooking() {
        UserDto user = userService.addUser(makeDefaultUser());
        UserDto booker = userService.addUser(UserDto.builder().name("Sam").email("new@mail.ru").build());
        long userId = user.getId();
        long bookerId = booker.getId();

        ItemDto item = itemService.addItem(makeDefaultItem(), userId);
        long itemId = item.getId();

        CommentDto comment = makeDefaultComment();

        assertThrows(CommenterDontHaveBookingException.class,
                () -> itemService.addComment(comment, bookerId, itemId));
    }

    @Test
    public void getOwnerItemsTest() {
        UserDto userDto1 = makeDefaultUser();
        userDto1 = userService.addUser(userDto1);

        UserDto userDto2 = makeDefaultUser();
        userDto2.setEmail("new@mail.ru");
        userDto2 = userService.addUser(userDto2);

        ItemDto item1 = itemService.addItem(makeDefaultItem(), userDto1.getId());
        item1.setComments(List.of());
        ItemDto item2 = itemService.addItem(makeDefaultItem(), userDto1.getId());
        item2.setComments(List.of());

        assertEquals(List.of(item1, item2),
                itemService.getOwnerItems(userDto1.getId(), 0, Integer.MAX_VALUE));
        assertEquals(List.of(),
                itemService.getOwnerItems(userDto2.getId(), 0, Integer.MAX_VALUE));
    }

    private BookingDtoRequest makeDefaultBookingDtoRequest(long itemId) {
        return BookingDtoRequest.builder()
                .itemId(itemId)
                .start(LocalDateTime.now())
                .end(LocalDateTime.now().plusMinutes(1).plusDays(1).truncatedTo(ChronoUnit.SECONDS))
                .build();
    }

    private CommentDto makeDefaultComment() {
        return CommentDto.builder()
                .text("some smart thoughts")
                .build();
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
