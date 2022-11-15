package ru.practicum.shareit.booking.service;

import lombok.AllArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;
import ru.practicum.shareit.booking.exception.BookingNotFoundException;
import ru.practicum.shareit.booking.exception.CantBookOwnedItemException;
import ru.practicum.shareit.booking.exception.ItemNotAvailableForBookingException;
import ru.practicum.shareit.booking.exception.TimeWindowOccupiedException;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
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
    private BookingService service;

    @Test
    public void addBookingTest() {
        User user = userService.addUser(makeDefaultUser());
        Item item = itemService.addItem(makeDefaultItem(user));

        User booker = makeDefaultUser();
        booker.setEmail("new@mail.ru");
        booker = userService.addUser(booker);

        Booking booking = service.addBooking(makeDefaultBooking(item, booker));

        assertEquals(booking, service.getBooking(booking.getId()));
    }

    @Test
    public void shouldThrowExceptionForAddingBookingIntoOccupiedTimeWindow() {
        LocalDateTime timePoint1 = LocalDateTime.now().plusMinutes(1);
        LocalDateTime timePoint2 = timePoint1.plusDays(1);
        LocalDateTime timePoint3 = timePoint2.plusDays(1);
        LocalDateTime timePoint4 = timePoint3.plusDays(1);
        LocalDateTime timePoint5 = timePoint4.plusDays(1);
        LocalDateTime timePoint6 = timePoint5.plusDays(1);

        User user = userService.addUser(makeDefaultUser());
        Item item = itemService.addItem(makeDefaultItem(user));

        User booker1 = makeDefaultUser();
        booker1.setEmail("new1@mail.ru");
        booker1 = userService.addUser(booker1);

        User booker2 = makeDefaultUser();
        booker2.setEmail("new2@mail.ru");
        booker2 = userService.addUser(booker2);

        User booker3 = makeDefaultUser();
        booker3.setEmail("new3@mail.ru");
        booker3 = userService.addUser(booker3);

        Booking booking1 = makeDefaultBooking(item, booker1);
        booking1.setStartTime(timePoint1);
        booking1.setEndTime(timePoint3);
        service.addBooking(booking1);

        Booking booking2 = makeDefaultBooking(item, booker2);
        booking2.setStartTime(timePoint4);
        booking2.setEndTime(timePoint6);
        service.addBooking(booking2);

        Booking booking3 = makeDefaultBooking(item, booker3);
        booking3.setStartTime(timePoint2);
        booking3.setEndTime(timePoint5);

        assertThrows(TimeWindowOccupiedException.class, () -> service.addBooking(booking3));
    }

    @Test
    public void shouldThrowExceptionForBookingOwnedItem() {
        User user = userService.addUser(makeDefaultUser());
        Item item = itemService.addItem(makeDefaultItem(user));

        assertThrows(CantBookOwnedItemException.class, () -> service.addBooking(makeDefaultBooking(item, user)));
    }

    @Test
    public void shouldThrowExceptionForBookingUnavailableItem() {
        User user = userService.addUser(makeDefaultUser());
        Item item = makeDefaultItem(user);
        item.setAvailable(false);
        item = itemService.addItem(item);

        User booker = makeDefaultUser();
        booker.setEmail("new@mail.ru");
        booker = userService.addUser(booker);

        Item finalItem = item;
        User finalBooker = booker;
        assertThrows(ItemNotAvailableForBookingException.class,
                () -> service.addBooking(makeDefaultBooking(finalItem, finalBooker)));
    }

    @Test
    public void shouldThrowExceptionForGettingNotFoundBooking() {
        assertThrows(BookingNotFoundException.class, () -> service.getBooking(1));
    }

    @Test
    public void getBookingsByBookerIdAndStatusSortedByDateDescTest() {
        User user = userService.addUser(makeDefaultUser());
        Item item1 = itemService.addItem(makeDefaultItem(user));
        Item item2 = itemService.addItem(makeDefaultItem(user));
        Item item3 = itemService.addItem(makeDefaultItem(user));
        Item item4 = itemService.addItem(makeDefaultItem(user));
        Item item5 = itemService.addItem(makeDefaultItem(user));

        User booker = makeDefaultUser();
        booker.setEmail("new1@mail.ru");
        booker = userService.addUser(booker);
        long bookerId = booker.getId();

        Booking waitingBooking = makeDefaultBooking(item1, booker);
        waitingBooking.setStartTime(waitingBooking.getStartTime().plusMinutes(1));
        waitingBooking.setEndTime(waitingBooking.getEndTime().plusMinutes(1));
        waitingBooking = service.addBooking(waitingBooking);
        assertEquals(List.of(waitingBooking), service.getBookingsByBookerIdOrOwnerIdAndStatusSortedByDateDesc(
                bookerId, null, BookingStatus.WAITING));

        Booking rejectedBooking = makeDefaultBooking(item2, booker);
        rejectedBooking.setApproved(false);
        rejectedBooking = service.addBooking(rejectedBooking);
        assertEquals(List.of(rejectedBooking), service.getBookingsByBookerIdOrOwnerIdAndStatusSortedByDateDesc(
                bookerId, null, BookingStatus.REJECTED));

        Booking pastBooking = makeDefaultBooking(item3, booker);
        pastBooking.setApproved(true);
        pastBooking.setStartTime(pastBooking.getStartTime().minusDays(3));
        pastBooking.setEndTime(pastBooking.getEndTime().minusDays(3));
        pastBooking = service.addBooking(pastBooking);
        assertEquals(List.of(pastBooking), service.getBookingsByBookerIdOrOwnerIdAndStatusSortedByDateDesc(
                bookerId, null, BookingStatus.PAST));

        Booking futureBooking = makeDefaultBooking(item4, booker);
        futureBooking.setApproved(true);
        futureBooking.setStartTime(futureBooking.getStartTime().plusDays(1));
        futureBooking.setEndTime(futureBooking.getEndTime().plusDays(1));
        futureBooking = service.addBooking(futureBooking);
        assertEquals(List.of(futureBooking, waitingBooking, rejectedBooking), service.getBookingsByBookerIdOrOwnerIdAndStatusSortedByDateDesc(
                bookerId, null, BookingStatus.FUTURE));

        Booking currentBooking = makeDefaultBooking(item5, booker);
        currentBooking.setApproved(true);
        currentBooking.setStartTime(currentBooking.getStartTime().minusHours(1));
        currentBooking.setEndTime(currentBooking.getEndTime().plusDays(1));
        currentBooking = service.addBooking(currentBooking);
        assertEquals(List.of(currentBooking), service.getBookingsByBookerIdOrOwnerIdAndStatusSortedByDateDesc(
                bookerId, null, BookingStatus.CURRENT));

        futureBooking = service.getBooking(futureBooking.getId());
        waitingBooking = service.getBooking(waitingBooking.getId());
        rejectedBooking = service.getBooking(rejectedBooking.getId());
        currentBooking = service.getBooking(currentBooking.getId());
        pastBooking = service.getBooking(pastBooking.getId());
        assertEquals(List.of(futureBooking, waitingBooking, rejectedBooking, currentBooking, pastBooking),
                service.getBookingsByBookerIdOrOwnerIdAndStatusSortedByDateDesc(
                bookerId, null, BookingStatus.ALL));
    }

    @Test
    public void setApprovalTest() {
        User user = userService.addUser(makeDefaultUser());
        Item item = itemService.addItem(makeDefaultItem(user));

        User booker = makeDefaultUser();
        booker.setEmail("new1@mail.ru");
        booker = userService.addUser(booker);
        Booking booking = service.addBooking(makeDefaultBooking(item, booker));

        assertTrue(service.setApproval(booking.getId(), true).getApproved());
        assertFalse(service.setApproval(booking.getId(), false).getApproved());
    }

    private Booking makeDefaultBooking(Item item, User booker) {
        return Booking.builder()
                .item(item)
                .booker(booker)
                .startTime(LocalDateTime.now().plusMinutes(1))
                .endTime(LocalDateTime.now().plusMinutes(1).plusDays(1))
                .build();
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
