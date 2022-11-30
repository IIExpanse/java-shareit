package ru.practicum.shareit.user.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Sql(scripts = "classpath:schema.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@AutoConfigureMockMvc
public class UserControllerTest {

    @LocalServerPort
    private int port;
    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper mapper;

    @Test
    public void addUserTest() throws Exception {
        UserDto userDto = makeDefaultUserDto();

        MockHttpServletResponse response = mvc.perform(
                        post(getDefaultUri())
                                .content(mapper.writeValueAsString(userDto))
                                .contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        assertEquals(HttpStatus.CREATED.value(), response.getStatus());
        assertEquals(userDto, mapper.readValue(response.getContentAsString(), UserDto.class));
    }

    @Test
    public void shouldNotAddUserWithDuplicateEmail() throws Exception {
        UserDto userDto = makeDefaultUserDto();

        mvc.perform(post(getDefaultUri())
                .content(mapper.writeValueAsString(userDto))
                .contentType(MediaType.APPLICATION_JSON));

        userDto.setId(2L);

        MockHttpServletResponse response = mvc.perform(
                        post(getDefaultUri())
                                .content(mapper.writeValueAsString(userDto))
                                .contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        assertEquals(HttpStatus.CONFLICT.value(), response.getStatus());
    }

    @Test
    public void getUserTest() throws Exception {
        UserDto userDto = makeDefaultUserDto();

        mvc.perform(post(getDefaultUri())
                .content(mapper.writeValueAsString(userDto))
                .contentType(MediaType.APPLICATION_JSON));

        MockHttpServletResponse response = mvc.perform(
                        get(getDefaultUri() + "/1"))
                .andReturn().getResponse();

        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals(userDto, mapper.readValue(response.getContentAsString(), UserDto.class));
    }

    @Test
    public void shouldThrowExceptionForNotFoundUser() throws Exception {

        MockHttpServletResponse response = mvc.perform(
                        get(getDefaultUri() + "/1"))
                .andReturn().getResponse();

        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatus());
    }

    @Test
    public void getUsersTest() throws Exception {
        UserDto userDto1 = makeDefaultUserDto();

        mvc.perform(post(getDefaultUri())
                .content(mapper.writeValueAsString(userDto1))
                .contentType(MediaType.APPLICATION_JSON));

        UserDto userDto2 = makeDefaultUserDto();
        userDto2.setId(2L);
        userDto2.setName("Sam");
        userDto2.setEmail("samsmail@mail.ru");

        mvc.perform(post(getDefaultUri())
                .content(mapper.writeValueAsString(userDto2))
                .contentType(MediaType.APPLICATION_JSON));

        MockHttpServletResponse response = mvc.perform(
                        get(getDefaultUri())
                                .contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        assertEquals(List.of(userDto1, userDto2),
                mapper.readValue(response.getContentAsString(), new TypeReference<List<UserDto>>() {
                }));
    }

    @Test
    public void updateUserTest() throws Exception {
        UserDto userDto = makeDefaultUserDto();
        userDto.setName("Sam");
        userDto.setEmail("samsmail@yandex.ru");

        mvc.perform(post(getDefaultUri())
                .content(mapper.writeValueAsString(userDto))
                .contentType(MediaType.APPLICATION_JSON));

        userDto.setName(null);
        userDto.setEmail("newsamsmail@yandex.ru");

        MockHttpServletResponse response = mvc.perform(
                        patch(getDefaultUri() + "/1")
                                .content(mapper.writeValueAsString(userDto))
                                .contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        userDto.setName("Sam");

        assertEquals(HttpStatus.OK.value(), response.getStatus());
        assertEquals(userDto, mapper.readValue(response.getContentAsString(), UserDto.class));
    }

    @Test
    public void shouldThrowExceptionForEmptyPatchRequest() throws Exception {
        UserDto userDto = makeDefaultUserDto();

        mvc.perform(post(getDefaultUri())
                .content(mapper.writeValueAsString(userDto))
                .contentType(MediaType.APPLICATION_JSON));

        userDto.setName(null);
        userDto.setEmail(null);

        MockHttpServletResponse response = mvc.perform(
                        patch(getDefaultUri() + "/1")
                                .content(mapper.writeValueAsString(userDto))
                                .contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatus());
    }

    @Test
    public void shouldThrowExceptionForPatchingWithDuplicateEmail() throws Exception {
        UserDto userDto1 = makeDefaultUserDto();
        userDto1.setName("Sam");
        userDto1.setEmail("samsmail@yandex.ru");

        mvc.perform(post(getDefaultUri())
                .content(mapper.writeValueAsString(userDto1))
                .contentType(MediaType.APPLICATION_JSON));

        UserDto userDto2 = makeDefaultUserDto();
        userDto2.setId(2L);
        userDto2.setName("Tom");
        userDto2.setEmail("another@mail.com");

        mvc.perform(post(getDefaultUri())
                .content(mapper.writeValueAsString(userDto2))
                .contentType(MediaType.APPLICATION_JSON));

        userDto1.setName(null);

        MockHttpServletResponse response = mvc.perform(
                        patch(getDefaultUri() + "/2")
                                .content(mapper.writeValueAsString(userDto1))
                                .contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        assertEquals(HttpStatus.CONFLICT.value(), response.getStatus());
    }

    @Test
    public void deleteUserTest() throws Exception {

        mvc.perform(post(getDefaultUri())
                .content(mapper.writeValueAsString(makeDefaultUserDto()))
                .contentType(MediaType.APPLICATION_JSON));

        MockHttpServletResponse deleteResponse = mvc.perform(
                        delete(getDefaultUri() + "/1"))
                .andReturn().getResponse();

        MockHttpServletResponse listResponse = mvc.perform(
                        get(getDefaultUri()))
                .andReturn().getResponse();

        assertEquals(HttpStatus.OK.value(), deleteResponse.getStatus());
        assertTrue(mapper.readValue(listResponse.getContentAsString(),
                new TypeReference<List<UserDto>>() {
                }).isEmpty());
    }

    @Test
    public void shouldThrowExceptionForDeletingAbsentUser() throws Exception {

        MockHttpServletResponse response = mvc.perform(
                        delete(getDefaultUri() + "/1"))
                .andReturn().getResponse();

        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatus());
    }

    private String getDefaultUri() {
        return String.format("http://localhost:%d/users", port);
    }

    private UserDto makeDefaultUserDto() {
        return UserDto.builder()
                .id(1L)
                .name("Tom")
                .email("tomsmail@mail.ru")
                .build();
    }
}
