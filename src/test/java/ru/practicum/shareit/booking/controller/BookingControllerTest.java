package ru.practicum.shareit.booking.controller;

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
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

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

    private String getDefaultUri() {
        return String.format("http://localhost:%d/bookings", port);
    }

    private HttpHeaders getDefaultHeader(Long userId) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Sharer-User-Id", userId.toString());
        return headers;
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
