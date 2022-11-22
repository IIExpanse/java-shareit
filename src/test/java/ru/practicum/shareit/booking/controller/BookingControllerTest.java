package ru.practicum.shareit.booking.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoRequest;
import ru.practicum.shareit.booking.dto.BookingStatus;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Sql(scripts = "classpath:schema.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@AutoConfigureMockMvc
public class BookingControllerTest {

    @LocalServerPort
    private int port;
    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper mapper;

    @Test
    public void addBookingTest() throws Exception {
        UserDto owner = addDefaultUser("tomsmail@mail.ru");
        UserDto booker = addDefaultUser("new@mail.ru");

        ItemDto itemDto = addDefaultItem(owner.getId());
        BookingDtoRequest bookingDtoRequest = makeDefaultBookingDtoRequest(itemDto.getId());

        MockHttpServletResponse response = mvc.perform(
                        post(getDefaultUri())
                                .content(mapper.writeValueAsString(bookingDtoRequest))
                                .contentType(MediaType.APPLICATION_JSON)
                                .headers(getDefaultHeader(booker.getId())))
                .andReturn().getResponse();
        assertEquals(response.getStatus(), HttpStatus.CREATED.value());
        BookingDto bookingDto = mapper.readValue(response.getContentAsString(), BookingDto.class);

        response = mvc.perform(
                        get(getDefaultUri() + "/" + bookingDto.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .headers(getDefaultHeader(owner.getId())))
                .andReturn().getResponse();

        assertEquals(response.getStatus(), HttpStatus.OK.value());
        assertEquals(bookingDto, mapper.readValue(response.getContentAsString(), BookingDto.class));
    }

    @Test
    public void shouldThrowExceptionForAddingBookingWithEndBeforeStart() throws Exception {
        UserDto user = addDefaultUser("mail@mail.ru");
        ItemDto item = addDefaultItem(user.getId());

        UserDto booker = addDefaultUser("new@mail.ru");

        BookingDtoRequest request = makeDefaultBookingDtoRequest(item.getId());
        request.setEnd(request.getStart().minusDays(1));

        MockHttpServletResponse response = mvc.perform(
                        post(getDefaultUri())
                                .content(mapper.writeValueAsString(request))
                                .contentType(MediaType.APPLICATION_JSON)
                                .headers(getDefaultHeader(booker.getId())))
                .andReturn().getResponse();

        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatus());
    }

    @Test
    public void shouldThrowExceptionForAddingBookingIntoOccupiedTimeWindow() throws Exception {
        LocalDateTime timePoint1 = LocalDateTime.now().plusMinutes(1);
        LocalDateTime timePoint2 = timePoint1.plusDays(1);
        LocalDateTime timePoint3 = timePoint2.plusDays(1);
        LocalDateTime timePoint4 = timePoint3.plusDays(1);
        LocalDateTime timePoint5 = timePoint4.plusDays(1);
        LocalDateTime timePoint6 = timePoint5.plusDays(1);

        UserDto user = addDefaultUser("mail@mail.ru");
        ItemDto item = addDefaultItem(user.getId());
        long itemId = item.getId();

        UserDto booker1 = addDefaultUser("new@mail.ru");
        UserDto booker2 = addDefaultUser("another@mail.com");
        UserDto booker3 = addDefaultUser("some@mail.ru");

        BookingDtoRequest booking1 = makeDefaultBookingDtoRequest(itemId);
        booking1.setStart(timePoint1);
        booking1.setEnd(timePoint3);
        addBooking(booking1, booker1.getId());

        BookingDtoRequest booking2 = makeDefaultBookingDtoRequest(itemId);
        booking2.setStart(timePoint4);
        booking2.setEnd(timePoint6);
        addBooking(booking2, booker2.getId());

        BookingDtoRequest booking3 = makeDefaultBookingDtoRequest(itemId);
        booking3.setStart(timePoint2);
        booking3.setEnd(timePoint5);
        MockHttpServletResponse response = mvc.perform(
                        post(getDefaultUri())
                                .content(mapper.writeValueAsString(booking3))
                                .contentType(MediaType.APPLICATION_JSON)
                                .headers(getDefaultHeader(booker3.getId())))
                .andReturn().getResponse();

        assertEquals(HttpStatus.CONFLICT.value(), response.getStatus());
    }

    @Test
    public void shouldThrowExceptionForBookingOwnedItem() throws Exception {
        UserDto user = addDefaultUser("some@mail.ru");
        ItemDto item = addDefaultItem(user.getId());

        BookingDtoRequest booking = makeDefaultBookingDtoRequest(item.getId());
        MockHttpServletResponse response = mvc.perform(
                        post(getDefaultUri())
                                .content(mapper.writeValueAsString(booking))
                                .contentType(MediaType.APPLICATION_JSON)
                                .headers(getDefaultHeader(user.getId())))
                .andReturn().getResponse();

        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatus());
    }

    @Test
    public void shouldThrowExceptionForBookingUnavailableItem() throws Exception {
        UserDto user = addDefaultUser("some@mail.ru");
        UserDto booker = addDefaultUser("another@mail.ru");

        ItemDto itemDto = ItemDto.builder()
                .name("DEBUGGER 9000")
                .description("Launch and debug!")
                .available(false)
                .build();
        MockHttpServletResponse response = mvc.perform(post(String.format("http://localhost:%d/items", port))
                        .content(mapper.writeValueAsString(itemDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .headers(getDefaultHeader(user.getId())))
                .andReturn().getResponse();
        itemDto = mapper.readValue(response.getContentAsString(), ItemDto.class);

        BookingDtoRequest booking = makeDefaultBookingDtoRequest(itemDto.getId());
        response = mvc.perform(
                        post(getDefaultUri())
                                .content(mapper.writeValueAsString(booking))
                                .contentType(MediaType.APPLICATION_JSON)
                                .headers(getDefaultHeader(booker.getId())))
                .andReturn().getResponse();

        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatus());
    }

    @Test
    public void shouldThrowExceptionForGettingNotFoundBooking() throws Exception {
        UserDto userDto = addDefaultUser("some@mail.ru");

        MockHttpServletResponse response = mvc.perform(
                        get(getDefaultUri() + "/0")
                                .contentType(MediaType.APPLICATION_JSON)
                                .headers(getDefaultHeader(userDto.getId())))
                .andReturn().getResponse();

        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatus());
    }

    @Test
    public void getBookingsByBookerOrOwnerAndStatusTest() throws Exception {
        UserDto user = addDefaultUser("some@mail.ru");
        long userId = user.getId();
        ItemDto item1 = addDefaultItem(userId);
        ItemDto item2 = addDefaultItem(userId);
        ItemDto item3 = addDefaultItem(userId);

        UserDto booker = addDefaultUser("new@mail.ru");
        long bookerId = booker.getId();

        BookingDtoRequest waitingBookingRequest = makeDefaultBookingDtoRequest(item1.getId());
        waitingBookingRequest.setStart(waitingBookingRequest.getStart().plusMinutes(1));
        waitingBookingRequest.setEnd(waitingBookingRequest.getEnd().plusMinutes(1));
        BookingDto waitingBooking = addBooking(waitingBookingRequest, bookerId);
        assertEquals(List.of(waitingBooking), getBookingsByBookerAndStatus(bookerId, BookingStatus.WAITING.toString()));
        assertEquals(List.of(waitingBooking), getBookingsByOwnerAndStatus(userId, BookingStatus.WAITING.toString()));

        BookingDtoRequest rejectedBookingRequest = makeDefaultBookingDtoRequest(item2.getId());
        BookingDto rejectedBooking = addBooking(rejectedBookingRequest, bookerId);
        rejectedBooking = setApproved(userId, rejectedBooking.getId(), false);
        assertEquals(List.of(rejectedBooking), getBookingsByBookerAndStatus(bookerId, BookingStatus.REJECTED.toString()));
        assertEquals(List.of(rejectedBooking), getBookingsByOwnerAndStatus(userId, BookingStatus.REJECTED.toString()));

        BookingDtoRequest futureBookingRequest = makeDefaultBookingDtoRequest(item3.getId());
        futureBookingRequest.setStart(futureBookingRequest.getStart().plusDays(1));
        futureBookingRequest.setEnd(futureBookingRequest.getEnd().plusDays(1));
        BookingDto futureBooking = addBooking(futureBookingRequest, bookerId);
        futureBooking = setApproved(userId, futureBooking.getId(), true);
        assertEquals(List.of(futureBooking, waitingBooking, rejectedBooking),
                getBookingsByBookerAndStatus(bookerId, BookingStatus.FUTURE.toString()));
        assertEquals(List.of(futureBooking, waitingBooking, rejectedBooking),
                getBookingsByOwnerAndStatus(userId, BookingStatus.FUTURE.toString()));

        assertEquals(List.of(futureBooking, waitingBooking, rejectedBooking),
                getBookingsByOwnerAndStatus(
                        userId, BookingStatus.ALL.toString()));
        assertEquals(List.of(futureBooking, waitingBooking, rejectedBooking),
                getBookingsByOwnerAndStatus(
                        userId, BookingStatus.ALL.toString()));
    }

    @Test
    public void setApprovalTest() throws Exception {
        UserDto user = addDefaultUser("some@mail.ru");
        ItemDto item = addDefaultItem(user.getId());
        UserDto booker = addDefaultUser("new@mail.ru");

        BookingDtoRequest booking = makeDefaultBookingDtoRequest(item.getId());
        BookingDto bookingDto = addBooking(booking, booker.getId());
        setApproved(user.getId(), bookingDto.getId(), true);

        MockHttpServletResponse response = mvc.perform(
                        get(getDefaultUri() + "/" + bookingDto.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .headers(getDefaultHeader(booker.getId())))
                .andReturn().getResponse();

        assertEquals(BookingStatus.APPROVED,
                mapper.readValue(response.getContentAsString(), BookingDto.class).getStatus());
    }

    private String getDefaultUri() {
        return String.format("http://localhost:%d/bookings", port);
    }

    private HttpHeaders getDefaultHeader(Long userId) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Sharer-User-Id", userId.toString());
        return headers;
    }

    private Collection<BookingDto> getBookingsByBookerAndStatus(
            Long booker, String state) throws Exception {
        MockHttpServletResponse response = mvc.perform(
                        get(getDefaultUri())
                                .contentType(MediaType.APPLICATION_JSON)
                                .headers(getDefaultHeader(booker))
                                .param("state", state)
                                .param("from", ((Integer) 0).toString())
                                .param("size", ((Integer) Integer.MAX_VALUE).toString()))
                .andReturn().getResponse();
        return mapper.readValue(response.getContentAsString(), new TypeReference<>() {});
    }

    private Collection<BookingDto> getBookingsByOwnerAndStatus(
            Long ownerId, String state) throws Exception {
        MockHttpServletResponse response = mvc.perform(
                        get(getDefaultUri() + "/owner")
                                .contentType(MediaType.APPLICATION_JSON)
                                .headers(getDefaultHeader(ownerId))
                                .param("state", state)
                                .param("from", ((Integer) 0).toString())
                                .param("size", ((Integer) Integer.MAX_VALUE).toString()))
                .andReturn().getResponse();

        return mapper.readValue(response.getContentAsString(), new TypeReference<>() {});
    }

    private BookingDto setApproved(long requesterId, long bookingId, Boolean approved) throws Exception {
        MockHttpServletResponse response = mvc.perform(
                        patch(getDefaultUri() + "/" + bookingId)
                                .headers(getDefaultHeader(requesterId))
                                .param("approved", approved.toString()))
                .andReturn().getResponse();
        return mapper.readValue(response.getContentAsString(), BookingDto.class);
    }

    private BookingDto addBooking(BookingDtoRequest bookingDtoRequest, long bookerId) throws Exception {
        MockHttpServletResponse response = mvc.perform(
                        post(getDefaultUri())
                                .content(mapper.writeValueAsString(bookingDtoRequest))
                                .contentType(MediaType.APPLICATION_JSON)
                                .headers(getDefaultHeader(bookerId)))
                .andReturn().getResponse();
        return mapper.readValue(response.getContentAsString(), BookingDto.class);
    }

    private BookingDtoRequest makeDefaultBookingDtoRequest(long itemId) {
        return BookingDtoRequest.builder()
                .start(LocalDateTime.now().plusMinutes(1).truncatedTo(ChronoUnit.SECONDS))
                .end(LocalDateTime.now().plusDays(1).truncatedTo(ChronoUnit.SECONDS))
                .itemId(itemId)
                .build();
    }

    private ItemDto addDefaultItem(long ownerId) throws Exception {
        ItemDto itemDto = ItemDto.builder()
                .name("DEBUGGER 9000")
                .description("Launch and debug!")
                .available(true)
                .build();

        MockHttpServletResponse response = mvc.perform(
                        post(String.format("http://localhost:%d/items", port))
                                .content(mapper.writeValueAsString(itemDto))
                                .contentType(MediaType.APPLICATION_JSON)
                                .headers(getDefaultHeader(ownerId)))
                .andReturn().getResponse();

        return mapper.readValue(response.getContentAsString(), ItemDto.class);
    }

    private UserDto addDefaultUser(String email) throws Exception {
        UserDto userDto = UserDto.builder()
                .name("Tom")
                .email(email)
                .build();

        MockHttpServletResponse response = mvc.perform(
                        post(String.format("http://localhost:%d/users", port))
                                .content(mapper.writeValueAsString(userDto))
                                .contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        return mapper.readValue(response.getContentAsString(), UserDto.class);
    }
}
