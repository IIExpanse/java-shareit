package ru.practicum.shareit.request.controller;

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
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Sql(scripts = "classpath:schema.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@AutoConfigureMockMvc
public class ItemRequestControllerTest {

    @LocalServerPort
    private int port;
    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper mapper;

    @Test
    public void addRequestTest() throws Exception {
        UserDto userDto = addDefaultUser("some@mail.com");

        ItemRequestDto requestDto = makeDefaultRequest();
        ItemRequestDto storedRequestDto;

        MockHttpServletResponse postResponse = mvc.perform(
                        post(getDefaultUri())
                                .content(mapper.writeValueAsString(requestDto))
                                .contentType(MediaType.APPLICATION_JSON)
                                .headers(getDefaultHeader(userDto.getId())))
                .andReturn().getResponse();
        assertEquals(HttpStatus.OK.value(), postResponse.getStatus());
        requestDto = mapper.readValue(postResponse.getContentAsString(), ItemRequestDto.class);
        requestDto.setItems(Set.of());

        MockHttpServletResponse getResponse = mvc.perform(
                        get(getDefaultUri() + "/" + requestDto.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .headers(getDefaultHeader(userDto.getId())))
                .andReturn().getResponse();
        assertEquals(HttpStatus.OK.value(), getResponse.getStatus());
        storedRequestDto = mapper.readValue(getResponse.getContentAsString(), ItemRequestDto.class);

        assertEquals(requestDto, storedRequestDto);
    }

    @Test
    public void shouldThrowExceptionForAddingRequestFromNotFoundUser() throws Exception {
        ItemRequestDto requestDto = makeDefaultRequest();
        MockHttpServletResponse postResponse = mvc.perform(
                        post(getDefaultUri())
                                .content(mapper.writeValueAsString(requestDto))
                                .contentType(MediaType.APPLICATION_JSON)
                                .headers(getDefaultHeader(0L)))
                .andReturn().getResponse();
        assertEquals(HttpStatus.NOT_FOUND.value(), postResponse.getStatus());
    }

    @Test
    public void shouldThrowExceptionForGettingRequestFromNotFoundUser() throws Exception {
        MockHttpServletResponse getResponse = mvc.perform(
                        get(getDefaultUri() + "/" + 0)
                                .contentType(MediaType.APPLICATION_JSON)
                                .headers(getDefaultHeader(0L)))
                .andReturn().getResponse();
        assertEquals(HttpStatus.NOT_FOUND.value(), getResponse.getStatus());

    }

    @Test
    public void shouldThrowExceptionForGettingNotFoundRequest() throws Exception {
        UserDto userDto = addDefaultUser("some@mail.com");

        MockHttpServletResponse getResponse = mvc.perform(
                        get(getDefaultUri() + "/" + 0)
                                .contentType(MediaType.APPLICATION_JSON)
                                .headers(getDefaultHeader(userDto.getId())))
                .andReturn().getResponse();
        assertEquals(HttpStatus.NOT_FOUND.value(), getResponse.getStatus());
    }

    @Test
    public void getOwnItemRequestsTest() throws Exception {
        Collection<ItemRequestDto> collection;

        UserDto userDto = addDefaultUser("some@mail.ru");
        UserDto userDto2 = addDefaultUser("new@mail.ru");

        ItemRequestDto requestDto = addRequest(makeDefaultRequest(), userDto.getId());
        requestDto.setCreated(requestDto.getCreated().truncatedTo(ChronoUnit.MICROS));

        addRequest(makeDefaultRequest(), userDto2.getId());
        requestDto.setCreated(requestDto.getCreated().truncatedTo(ChronoUnit.MICROS));

        MockHttpServletResponse getResponse = mvc.perform(
                        get(getDefaultUri())
                                .contentType(MediaType.APPLICATION_JSON)
                                .headers(getDefaultHeader(userDto.getId())))
                .andReturn().getResponse();
        assertEquals(HttpStatus.OK.value(), getResponse.getStatus());
        collection = mapper.readValue(getResponse.getContentAsString(), new TypeReference<>() {});

        assertEquals(List.of(requestDto), collection);
    }

    @Test
    public void shouldThrowExceptionForGettingOwnItemRequestsFromNotFoundUser() throws Exception {
        MockHttpServletResponse getResponse = mvc.perform(
                        get(getDefaultUri())
                                .contentType(MediaType.APPLICATION_JSON)
                                .headers(getDefaultHeader(0L)))
                .andReturn().getResponse();
        assertEquals(HttpStatus.NOT_FOUND.value(), getResponse.getStatus());
    }

    @Test
    public void getOtherUsersRequestsTest() throws Exception {
        Collection<ItemRequestDto> collection;

        UserDto userDto = addDefaultUser("some@mail.ru");
        UserDto userDto2 = addDefaultUser("new@mail.ru");

        ItemRequestDto requestDto = addRequest(makeDefaultRequest(), userDto.getId());
        requestDto.setCreated(requestDto.getCreated().truncatedTo(ChronoUnit.MICROS));

        addRequest(makeDefaultRequest(), userDto2.getId());
        requestDto.setCreated(requestDto.getCreated().truncatedTo(ChronoUnit.MICROS));

        MockHttpServletResponse getResponse = mvc.perform(
                        get(getDefaultUri() + "/all")
                                .contentType(MediaType.APPLICATION_JSON)
                                .headers(getDefaultHeader(userDto2.getId()))
                                .param("from", "0")
                                .param("size", "10"))
                .andReturn().getResponse();
        assertEquals(HttpStatus.OK.value(), getResponse.getStatus());
        collection = mapper.readValue(getResponse.getContentAsString(), new TypeReference<>() {});

        assertEquals(List.of(requestDto), collection);
    }

    @Test
    public void shouldThrowExceptionForGettingOtherUsersRequestsFromNotFoundUser() throws Exception {
        MockHttpServletResponse getResponse = mvc.perform(
                        get(getDefaultUri() + "/all")
                                .contentType(MediaType.APPLICATION_JSON)
                                .headers(getDefaultHeader(0L))
                                .param("from", "0")
                                .param("size", "10"))
                .andReturn().getResponse();
        assertEquals(HttpStatus.NOT_FOUND.value(), getResponse.getStatus());
    }

    private String getDefaultUri() {
        return String.format("http://localhost:%d/requests", port);
    }

    private HttpHeaders getDefaultHeader(Long userId) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Sharer-User-Id", userId.toString());
        return headers;
    }

    private ItemRequestDto addRequest(ItemRequestDto requestDto, long userId) throws Exception {
        MockHttpServletResponse postResponse = mvc.perform(
                        post(getDefaultUri())
                                .content(mapper.writeValueAsString(requestDto))
                                .contentType(MediaType.APPLICATION_JSON)
                                .headers(getDefaultHeader(userId)))
                .andReturn().getResponse();
        requestDto = mapper.readValue(postResponse.getContentAsString(), ItemRequestDto.class);
        requestDto.setItems(Set.of());

        return requestDto;
    }

    private ItemRequestDto makeDefaultRequest() {
        return ItemRequestDto.builder()
                .description("Need some item")
                .build();
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
