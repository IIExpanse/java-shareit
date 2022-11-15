package ru.practicum.shareit.item.controller;

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
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Sql(scripts = "classpath:schema.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@AutoConfigureMockMvc
public class ItemControllerTest {

    @LocalServerPort
    private int port;
    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper mapper;

    @Test
    public void addItemTest() throws Exception {
        addDefaultUser();
        ItemDto itemDto = makeDefaultItemDto();
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Sharer-User-Id", "1");

        MockHttpServletResponse response = mvc.perform(
                        post(getDefaultUri())
                                .headers(headers)
                                .content(mapper.writeValueAsString(itemDto))
                                .contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        assertEquals(HttpStatus.CREATED.value(), response.getStatus());
        itemDto.setComments(null);
        assertEquals(itemDto, mapper.readValue(response.getContentAsString(), ItemDto.class));
    }

    @Test
    public void shouldThrowExceptionForAddingItemWithAbsentOwner() throws Exception {
        ItemDto itemDto = makeDefaultItemDto();
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Sharer-User-Id", "1");

        MockHttpServletResponse response = mvc.perform(
                        post(getDefaultUri())
                                .headers(headers)
                                .content(mapper.writeValueAsString(itemDto))
                                .contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatus());
    }

    @Test
    public void getItemTest() throws Exception {
        addDefaultUser();
        ItemDto itemDto = makeDefaultItemDto();
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Sharer-User-Id", "1");

        mvc.perform(
                post(getDefaultUri())
                        .headers(headers)
                        .content(mapper.writeValueAsString(itemDto))
                        .contentType(MediaType.APPLICATION_JSON));

        MockHttpServletResponse response = mvc.perform(
                        get(getDefaultUri() + "/1")
                                .headers(headers)
                                .contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals(itemDto, mapper.readValue(response.getContentAsString(), ItemDto.class));
    }

    @Test
    public void shouldThrowExceptionForNotFoundItem() throws Exception {
        addDefaultUser();
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Sharer-User-Id", "1");

        MockHttpServletResponse response = mvc.perform(
                        get(getDefaultUri() + "/1")
                                .headers(headers)
                                .contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatus());
    }

    @Test
    public void searchAvailableItemsTest() throws Exception {
        addDefaultUser();
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Sharer-User-Id", "1");

        ItemDto item1 = makeDefaultItemDto();
        item1.setName("DEBUGGER");
        item1.setDescription("Some item descr");
        item1.setAvailable(true);

        mvc.perform(
                post(getDefaultUri())
                        .headers(headers)
                        .content(mapper.writeValueAsString(item1))
                        .contentType(MediaType.APPLICATION_JSON));

        ItemDto item2 = makeDefaultItemDto();
        item2.setId(2L);
        item2.setName("Some item");
        item2.setDescription("DeBuGgER again");
        item2.setAvailable(true);

        mvc.perform(
                post(getDefaultUri())
                        .headers(headers)
                        .content(mapper.writeValueAsString(item2))
                        .contentType(MediaType.APPLICATION_JSON));

        ItemDto item3 = makeDefaultItemDto();
        item3.setId(3L);
        item3.setName("Not to be found");
        item3.setDescription("Not debugger");
        item3.setAvailable(false);

        mvc.perform(
                post(getDefaultUri())
                        .headers(headers)
                        .content(mapper.writeValueAsString(item3))
                        .contentType(MediaType.APPLICATION_JSON));

        MockHttpServletResponse response = mvc.perform(
                        get(getDefaultUri() + "/search")
                                .headers(headers)
                                .contentType(MediaType.APPLICATION_JSON)
                                .param("text", "debUgger"))
                .andReturn().getResponse();

        assertEquals(List.of(item1, item2),
                mapper.readValue(response.getContentAsString(), new TypeReference<List<ItemDto>>() {
                }));
    }

    @Test
    public void updateItemTest() throws Exception {
        addDefaultUser();
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Sharer-User-Id", "1");

        ItemDto item1 = makeDefaultItemDto();
        item1.setName("New item");
        item1.setDescription("Some item descr");
        item1.setAvailable(false);

        mvc.perform(
                post(getDefaultUri())
                        .headers(headers)
                        .content(mapper.writeValueAsString(item1))
                        .contentType(MediaType.APPLICATION_JSON));

        item1.setName(null);
        item1.setDescription("New item descr");
        item1.setAvailable(true);

        MockHttpServletResponse response = mvc.perform(
                        patch(getDefaultUri() + "/1")
                                .headers(headers)
                                .content(mapper.writeValueAsString(item1))
                                .contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        item1.setName("New item");

        assertEquals(HttpStatus.OK.value(), response.getStatus());
        item1.setComments(null);
        assertEquals(item1, mapper.readValue(response.getContentAsString(), ItemDto.class));
    }

    @Test
    public void shouldThrowExceptionForPatchRequestWithNullOnlyValues() throws Exception {
        addDefaultUser();
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Sharer-User-Id", "1");

        ItemDto item1 = makeDefaultItemDto();

        mvc.perform(
                post(getDefaultUri())
                        .headers(headers)
                        .content(mapper.writeValueAsString(item1))
                        .contentType(MediaType.APPLICATION_JSON));

        item1.setName(null);
        item1.setDescription(null);
        item1.setAvailable(null);

        MockHttpServletResponse response = mvc.perform(
                        patch(getDefaultUri() + "/1")
                                .headers(headers)
                                .content(mapper.writeValueAsString(item1))
                                .contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatus());
    }

    @Test
    public void shouldThrowExceptionForWrongOwnerUpdatingItem() throws Exception {
        UserDto userDto = addDefaultUser();
        userDto.setEmail("new@mail.ru");
        mvc.perform(
                post(String.format("http://localhost:%d/users", port))
                        .content(mapper.writeValueAsString(userDto))
                        .contentType(MediaType.APPLICATION_JSON));

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Sharer-User-Id", "1");

        ItemDto item1 = makeDefaultItemDto();
        item1.setName("New item");
        item1.setDescription("Some item descr");
        item1.setAvailable(false);

        mvc.perform(
                post(getDefaultUri())
                        .headers(headers)
                        .content(mapper.writeValueAsString(item1))
                        .contentType(MediaType.APPLICATION_JSON));

        item1.setName(null);
        item1.setDescription("New item descr");
        item1.setAvailable(true);

        headers.set("X-Sharer-User-Id", "2");

        MockHttpServletResponse response = mvc.perform(
                        patch(getDefaultUri() + "/1")
                                .headers(headers)
                                .content(mapper.writeValueAsString(item1))
                                .contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        assertEquals(HttpStatus.FORBIDDEN.value(), response.getStatus());
    }

    private String getDefaultUri() {
        return String.format("http://localhost:%d/items", port);
    }

    private ItemDto makeDefaultItemDto() {
        return ItemDto.builder()
                .id(1L)
                .name("DEBUGGER 9000")
                .description("Launch and debug!")
                .available(true)
                .comments(List.of())
                .build();
    }

    private UserDto addDefaultUser() throws Exception {
        UserDto userDto = UserDto.builder()
                .name("Tom")
                .email("tomsmail@mail.ru")
                .build();

        mvc.perform(
                post(String.format("http://localhost:%d/users", port))
                        .content(mapper.writeValueAsString(userDto))
                        .contentType(MediaType.APPLICATION_JSON));

        return userDto;
    }
}
