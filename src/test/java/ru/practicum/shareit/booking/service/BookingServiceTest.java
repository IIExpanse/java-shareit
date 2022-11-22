package ru.practicum.shareit.booking.service;

import lombok.AllArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoRequest;
import ru.practicum.shareit.booking.dto.BookingStatus;
import ru.practicum.shareit.booking.exception.*;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AllArgsConstructor(onConstructor_ = @Autowired)
@AutoConfigureTestDatabase
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Sql(scripts = "classpath:schema.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class BookingServiceTest {

    private UserService userService;
    private ItemService itemService;
    private BookingService bookingService;

    @Test
    public void addBookingTest() {
        UserDto user = userService.addUser(makeDefaultUser());
        ItemDto item = itemService.addItem(makeDefaultItem(), user.getId());

        UserDto booker = makeDefaultUser();
        booker.setEmail("new@mail.ru");
        booker = userService.addUser(booker);

        BookingDto booking = bookingService.addBooking(makeDefaultBookingDtoRequest(item.getId()), booker.getId());

        assertEquals(booking, bookingService.getBookingDto(booking.getId(), booker.getId()));
    }

    @Test
    public void shouldThrowExceptionForAddingBookingWithEndBeforeStart() {
        UserDto user = userService.addUser(makeDefaultUser());
        ItemDto item = itemService.addItem(makeDefaultItem(), user.getId());

        UserDto booker = makeDefaultUser();
        booker.setEmail("new@mail.ru");
        booker = userService.addUser(booker);

        BookingDtoRequest request = makeDefaultBookingDtoRequest(item.getId());
        request.setEnd(request.getStart().minusDays(1));

        UserDto finalBooker = booker;
        assertThrows(EndBeforeOrEqualsStartException.class, () -> bookingService.addBooking(request, finalBooker.getId()));
    }

    @Test
    public void shouldThrowExceptionForAddingBookingIntoOccupiedTimeWindow() {
        LocalDateTime timePoint1 = LocalDateTime.now().plusMinutes(1);
        LocalDateTime timePoint2 = timePoint1.plusDays(1);
        LocalDateTime timePoint3 = timePoint2.plusDays(1);
        LocalDateTime timePoint4 = timePoint3.plusDays(1);
        LocalDateTime timePoint5 = timePoint4.plusDays(1);
        LocalDateTime timePoint6 = timePoint5.plusDays(1);

        UserDto user = userService.addUser(makeDefaultUser());
        ItemDto item = itemService.addItem(makeDefaultItem(), user.getId());
        long itemId = item.getId();

        UserDto booker1 = makeDefaultUser();
        booker1.setEmail("new1@mail.ru");
        booker1 = userService.addUser(booker1);

        UserDto booker2 = makeDefaultUser();
        booker2.setEmail("new2@mail.ru");
        booker2 = userService.addUser(booker2);

        UserDto booker3 = makeDefaultUser();
        booker3.setEmail("new3@mail.ru");
        booker3 = userService.addUser(booker3);

        BookingDtoRequest booking1 = makeDefaultBookingDtoRequest(itemId);
        booking1.setStart(timePoint1);
        booking1.setEnd(timePoint3);
        bookingService.addBooking(booking1, booker1.getId());

        BookingDtoRequest booking2 = makeDefaultBookingDtoRequest(itemId);
        booking2.setStart(timePoint4);
        booking2.setEnd(timePoint6);
        bookingService.addBooking(booking2, booker2.getId());

        BookingDtoRequest booking3 = makeDefaultBookingDtoRequest(itemId);
        booking3.setStart(timePoint2);
        booking3.setEnd(timePoint5);

        UserDto finalBooker = booker3;
        assertThrows(TimeWindowOccupiedException.class, () -> bookingService.addBooking(booking3, finalBooker.getId()));
    }

    @Test
    public void shouldThrowExceptionForBookingOwnedItem() {
        UserDto user = userService.addUser(makeDefaultUser());
        ItemDto item = itemService.addItem(makeDefaultItem(), user.getId());

        assertThrows(CantBookOwnedItemException.class,
                () -> bookingService.addBooking(makeDefaultBookingDtoRequest(item.getId()), user.getId()));
    }

    @Test
    public void shouldThrowExceptionForBookingUnavailableItem() {
        UserDto user = userService.addUser(makeDefaultUser());
        ItemDto item = makeDefaultItem();
        item.setAvailable(false);
        item = itemService.addItem(item, user.getId());

        UserDto booker = makeDefaultUser();
        booker.setEmail("new@mail.ru");
        booker = userService.addUser(booker);

        ItemDto finalItem = item;
        UserDto finalBooker = booker;
        assertThrows(ItemNotAvailableForBookingException.class,
                () -> bookingService.addBooking(makeDefaultBookingDtoRequest(finalItem.getId()), finalBooker.getId()));
    }

    @Test
    public void shouldThrowExceptionForGettingNotFoundBooking() {
        UserDto userDto = makeDefaultUser();
        userDto = userService.addUser(userDto);

        UserDto finalUserDto = userDto;
        assertThrows(BookingNotFoundException.class,
                () -> bookingService.getBookingDto(finalUserDto.getId(), 1));
    }

    @Test
    public void shouldThrowExceptionForGettingUnrelatedBooking() {
        UserDto owner = userService.addUser(makeDefaultUser());

        ItemDto item = makeDefaultItem();
        item.setAvailable(true);
        item = itemService.addItem(item, owner.getId());

        UserDto booker = makeDefaultUser();
        booker.setEmail("new@mail.ru");
        booker = userService.addUser(booker);

        UserDto otherUser = makeDefaultUser();
        otherUser.setEmail("another@mail.ru");
        otherUser = userService.addUser(otherUser);

        BookingDto booking = bookingService.addBooking(makeDefaultBookingDtoRequest(item.getId()), booker.getId());

        UserDto finalOtherUser = otherUser;
        assertThrows(CantViewUnrelatedBookingException.class,
                () -> bookingService.getBookingDto(booking.getId(), finalOtherUser.getId()));
    }

    @Test
    public void getBookingsByBookerOrOwnerAndStatusTest() {
        UserDto user = userService.addUser(makeDefaultUser());
        long userId = user.getId();
        ItemDto item1 = itemService.addItem(makeDefaultItem(), userId);
        ItemDto item2 = itemService.addItem(makeDefaultItem(), userId);
        ItemDto item3 = itemService.addItem(makeDefaultItem(), userId);
        ItemDto item4 = itemService.addItem(makeDefaultItem(), userId);
        ItemDto item5 = itemService.addItem(makeDefaultItem(), userId);

        UserDto booker = makeDefaultUser();
        booker.setEmail("new1@mail.ru");
        booker = userService.addUser(booker);
        long bookerId = booker.getId();

        BookingDtoRequest waitingBookingRequest = makeDefaultBookingDtoRequest(item1.getId());
        waitingBookingRequest.setStart(waitingBookingRequest.getStart().plusMinutes(1));
        waitingBookingRequest.setEnd(waitingBookingRequest.getEnd().plusMinutes(1));
        BookingDto waitingBooking = bookingService.addBooking(waitingBookingRequest, bookerId);
        assertEquals(List.of(waitingBooking), bookingService.getBookingsByUserAndState(
                bookerId, null, BookingStatus.WAITING.toString(), 0, Integer.MAX_VALUE));

        BookingDtoRequest rejectedBookingRequest = makeDefaultBookingDtoRequest(item2.getId());
        BookingDto rejectedBooking = bookingService.addBooking(rejectedBookingRequest, bookerId);
        rejectedBooking = bookingService.setApproval(rejectedBooking.getId(), false, userId);
        assertEquals(List.of(rejectedBooking), bookingService.getBookingsByUserAndState(
                bookerId, null, BookingStatus.REJECTED.toString(), 0, Integer.MAX_VALUE));

        BookingDtoRequest pastBookingRequest = makeDefaultBookingDtoRequest(item3.getId());
        pastBookingRequest.setStart(pastBookingRequest.getStart().minusDays(3));
        pastBookingRequest.setEnd(pastBookingRequest.getEnd().minusDays(3));
        BookingDto pastBooking = bookingService.addBooking(pastBookingRequest, bookerId);
        pastBooking = bookingService.setApproval(pastBooking.getId(), true, userId);
        assertEquals(List.of(pastBooking), bookingService.getBookingsByUserAndState(
                bookerId, null, BookingStatus.PAST.toString(), 0, Integer.MAX_VALUE));

        BookingDtoRequest futureBookingRequest = makeDefaultBookingDtoRequest(item4.getId());
        futureBookingRequest.setStart(futureBookingRequest.getStart().plusDays(1));
        futureBookingRequest.setEnd(futureBookingRequest.getEnd().plusDays(1));
        BookingDto futureBooking = bookingService.addBooking(futureBookingRequest, bookerId);
        futureBooking = bookingService.setApproval(futureBooking.getId(), true, userId);
        assertEquals(List.of(futureBooking, waitingBooking, rejectedBooking), bookingService.getBookingsByUserAndState(
                bookerId, null, BookingStatus.FUTURE.toString(), 0, Integer.MAX_VALUE));

        BookingDtoRequest currentBookingRequest = makeDefaultBookingDtoRequest(item5.getId());
        currentBookingRequest.setStart(currentBookingRequest.getStart().minusHours(1));
        currentBookingRequest.setEnd(currentBookingRequest.getEnd().plusDays(1));
        BookingDto currentBooking = bookingService.addBooking(currentBookingRequest, bookerId);
        currentBooking = bookingService.setApproval(currentBooking.getId(), true, userId);
        assertEquals(List.of(currentBooking), bookingService.getBookingsByUserAndState(
                bookerId, null, BookingStatus.CURRENT.toString(), 0, Integer.MAX_VALUE));

        assertEquals(List.of(futureBooking, waitingBooking, rejectedBooking, currentBooking, pastBooking),
                bookingService.getBookingsByUserAndState(
                bookerId, null, BookingStatus.ALL.toString(), 0, Integer.MAX_VALUE));
    }

    @Test
    public void setApprovalTest() {
        UserDto user = userService.addUser(makeDefaultUser());
        ItemDto item = itemService.addItem(makeDefaultItem(), user.getId());

        UserDto booker = makeDefaultUser();
        booker.setEmail("new1@mail.ru");
        booker = userService.addUser(booker);
        BookingDto booking = bookingService.addBooking(makeDefaultBookingDtoRequest(item.getId()), booker.getId());
        bookingService.setApproval(booking.getId(), true, user.getId());

        assertTrue(bookingService.getBookingsByUserAndState(
                booker.getId(),
                null,
                BookingStatus.WAITING.toString(),
                0,
                Integer.MAX_VALUE).isEmpty());
    }

    @Test
    public void shouldThrowExceptionForWrongUserSettingApproval() {
        UserDto user = userService.addUser(makeDefaultUser());
        ItemDto item = itemService.addItem(makeDefaultItem(), user.getId());

        UserDto booker = makeDefaultUser();
        booker.setEmail("new1@mail.ru");
        booker = userService.addUser(booker);
        BookingDto booking = bookingService.addBooking(makeDefaultBookingDtoRequest(item.getId()), booker.getId());

        UserDto finalBooker = booker;
        assertThrows(WrongUserUpdatingBooking.class,
                () -> bookingService.setApproval(booking.getId(), true, finalBooker.getId()));
    }

    @Test
    public void shouldThrowExceptionForAlreadySetApproval() {
        UserDto user = userService.addUser(makeDefaultUser());
        ItemDto item = itemService.addItem(makeDefaultItem(), user.getId());

        UserDto booker = makeDefaultUser();
        booker.setEmail("new1@mail.ru");
        booker = userService.addUser(booker);
        BookingDto booking = bookingService.addBooking(makeDefaultBookingDtoRequest(item.getId()), booker.getId());
        bookingService.setApproval(booking.getId(), true, user.getId());

        assertThrows(ApprovalAlreadySetException.class,
                () -> bookingService.setApproval(booking.getId(), false, user.getId()));
    }

    private BookingDtoRequest makeDefaultBookingDtoRequest(long itemId) {
        return BookingDtoRequest.builder()
                .itemId(itemId)
                .start(LocalDateTime.now().plusMinutes(1).truncatedTo(ChronoUnit.SECONDS))
                .end(LocalDateTime.now().plusMinutes(1).plusDays(1).truncatedTo(ChronoUnit.SECONDS))
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
