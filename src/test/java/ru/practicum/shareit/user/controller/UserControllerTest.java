package ru.practicum.shareit.user.controller;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class UserControllerTest {

    @LocalServerPort
    private int port;

    private static final TestRestTemplate template = new TestRestTemplate();

    /**
     * Стандартная JDK-библиотека не поддерживает PATCH-запросы.
     * Для работы соответствующих тестов требуется ее замена.
     */
    @BeforeAll
    public static void refreshAndSetPatchCompliantTemplate() {
        template.getRestTemplate().setRequestFactory(new HttpComponentsClientHttpRequestFactory());
    }

    @Test
    public void addUserTest() {
        UserDto userDto = makeDefaultUserDto();

        ResponseEntity<UserDto> response = template.postForEntity(
                getDefaultUri(),
                userDto,
                UserDto.class
        );

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(userDto, response.getBody());
    }

    @Test
    public void shouldNotAddUserWithDuplicateEmail() {
        template.postForEntity(
                getDefaultUri(),
                makeDefaultUserDto(),
                UserDto.class
        );

        ResponseEntity<UserDto> response = template.postForEntity(
                getDefaultUri(),
                makeDefaultUserDto(),
                UserDto.class
        );

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    @Test
    public void getUserTest() {
        UserDto userDto = makeDefaultUserDto();
        template.postForEntity(
                getDefaultUri(),
                userDto,
                UserDto.class
        );

        ResponseEntity<UserDto> response = template.getForEntity(
                getDefaultUri() + "/1",
                UserDto.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(userDto, response.getBody());
    }

    @Test
    public void shouldThrowExceptionForNotFoundUser() {
        ResponseEntity<UserDto> response = template.getForEntity(
                getDefaultUri() + "/1",
                UserDto.class
        );

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void getUsersTest() {
        UserDto userDto1 = makeDefaultUserDto();
        template.postForEntity(
                getDefaultUri(),
                userDto1,
                UserDto.class
        );

        UserDto userDto2 = makeCustomUserDto(2, "Sam", "samsmail@mail.ru");
        template.postForEntity(
                getDefaultUri(),
                userDto2,
                UserDto.class
        );

        ResponseEntity<Collection<UserDto>> response = template.exchange(
                getDefaultUri(),
                HttpMethod.GET,
                new HttpEntity<>(null),
                new ParameterizedTypeReference<>() {}
        );

        assertEquals(List.of(userDto1, userDto2), response.getBody());
    }

    @Test
    public void updateUserTest() {
        UserDto userDto = makeCustomUserDto(1, "Sam", "samsmail@yandex.ru");
        template.postForEntity(
                getDefaultUri(),
                userDto,
                UserDto.class
        );

        userDto = makeCustomUserDto(1, null, "newsamsmail@yandex.ru");
        ResponseEntity<UserDto> response = template.exchange(
                getDefaultUri() + "/1",
                HttpMethod.PATCH,
                new HttpEntity<>(userDto),
                UserDto.class
        );

        UserDto resultUserDto = makeCustomUserDto(1, "Sam", "newsamsmail@yandex.ru");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(resultUserDto, response.getBody());
    }

    @Test
    public void shouldThrowExceptionForEmptyPatchRequest() {
        UserDto userDto = makeCustomUserDto(1, "Sam", "samsmail@yandex.ru");
        template.postForEntity(
                getDefaultUri(),
                userDto,
                UserDto.class
        );

        userDto = makeCustomUserDto(1, null, null);
        ResponseEntity<UserDto> response = template.exchange(
                getDefaultUri() + "/1",
                HttpMethod.PATCH,
                new HttpEntity<>(userDto),
                UserDto.class
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void shouldThrowExceptionForPatchingWithDuplicateEmail() {
        UserDto userDto = makeCustomUserDto(1, "Sam", "samsmail@yandex.ru");
        template.postForEntity(
                getDefaultUri(),
                userDto,
                UserDto.class
        );

        userDto = makeCustomUserDto(1, null, "samsmail@yandex.ru");
        ResponseEntity<UserDto> response = template.exchange(
                getDefaultUri() + "/1",
                HttpMethod.PATCH,
                new HttpEntity<>(userDto),
                UserDto.class
        );

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    @Test
    public void deleteUserTest() {
        template.postForEntity(
                getDefaultUri(),
                makeDefaultUserDto(),
                UserDto.class
        );

        ResponseEntity<String> deleteResponse = template.exchange(
                getDefaultUri() + "/1",
                HttpMethod.DELETE,
                new HttpEntity<>(null),
                String.class
        );

        ResponseEntity<Collection<UserDto>> listResponse = template.exchange(
                getDefaultUri(),
                HttpMethod.GET,
                new HttpEntity<>(null),
                new ParameterizedTypeReference<>() {}
        );

        assertEquals(HttpStatus.OK, deleteResponse.getStatusCode());
        assertTrue(Objects.requireNonNull(listResponse.getBody()).isEmpty());
    }

    @Test
    public void shouldThrowExceptionForDeletingAbsentUser() {
        ResponseEntity<String> response = template.exchange(
                getDefaultUri() + "/1",
                HttpMethod.DELETE,
                new HttpEntity<>(null),
                String.class
        );

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
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

    private UserDto makeCustomUserDto(long id, String name, String email) {
        return UserDto.builder()
                .id(id)
                .name(name)
                .email(email)
                .build();
    }
}
