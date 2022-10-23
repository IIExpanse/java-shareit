package ru.practicum.shareit.item.controller;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class ItemControllerTest {

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
    public void addItemTest() {
        addDefaultUser();
        ItemDto itemDto = makeDefaultItemDto();
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Sharer-User-Id", "1");

        ResponseEntity<ItemDto> response = template.postForEntity(
                getDefaultUri(),
                new HttpEntity<>(itemDto, headers),
                ItemDto.class
        );

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(itemDto, response.getBody());
    }

    @Test
    public void shouldThrowExceptionForAddingItemWithAbsentOwner() {
        ItemDto itemDto = makeDefaultItemDto();
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Sharer-User-Id", "1");

        ResponseEntity<ItemDto> response = template.postForEntity(
                getDefaultUri(),
                new HttpEntity<>(itemDto, headers),
                ItemDto.class
        );

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void getItemTest() {
        addDefaultUser();
        ItemDto itemDto = makeDefaultItemDto();
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Sharer-User-Id", "1");

        template.postForEntity(
                getDefaultUri(),
                new HttpEntity<>(itemDto, headers),
                ItemDto.class
        );

        ResponseEntity<ItemDto> response = template.exchange(
                getDefaultUri() + "/1",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                ItemDto.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(itemDto, response.getBody());
    }

    @Test
    public void shouldThrowExceptionForNotFoundItem() {
        addDefaultUser();
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Sharer-User-Id", "1");

        ResponseEntity<ItemDto> response = template.exchange(
                getDefaultUri() + "/1",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                ItemDto.class
        );

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void searchAvailableItemsTest() {
        addDefaultUser();
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Sharer-User-Id", "1");

        ItemDto item1 = makeCustomItemDto(1, "DEBUGGER", "Some item descr", true);
        template.postForEntity(
                getDefaultUri(),
                new HttpEntity<>(item1, headers),
                ItemDto.class
        );

        ItemDto item2 = makeCustomItemDto(2, "Some item", "DeBuGgER again", true);
        template.postForEntity(
                getDefaultUri(),
                new HttpEntity<>(item2, headers),
                ItemDto.class
        );

        ItemDto item3 = makeCustomItemDto(3, "Not to be found", "Not debugger", false);
        template.postForEntity(
                getDefaultUri(),
                new HttpEntity<>(item3, headers),
                ItemDto.class
        );

        ResponseEntity<Collection<ItemDto>> response = template.exchange(
                getDefaultUri() + "/search?text={text}",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                new ParameterizedTypeReference<>() {},
                Map.of("text", "debUgger")
        );
        assertEquals(List.of(item1, item2), response.getBody());
    }

    @Test
    public void updateItemTest() {
        addDefaultUser();
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Sharer-User-Id", "1");

        ItemDto item1 = makeCustomItemDto(1, "New item", "Some item descr", false);
        template.postForEntity(
                getDefaultUri(),
                new HttpEntity<>(item1, headers),
                ItemDto.class
        );

        item1 = makeCustomItemDto(1, null, "New item descr", true);
        ResponseEntity<ItemDto> response = template.exchange(
                getDefaultUri() + "/1",
                HttpMethod.PATCH,
                new HttpEntity<>(item1, headers),
                ItemDto.class
        );

        ItemDto resultItem = makeCustomItemDto(1, "New item", "New item descr", true);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(resultItem, response.getBody());
    }

    @Test
    public void shouldThrowExceptionForPatchRequestWithNullOnlyValues() {
        addDefaultUser();
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Sharer-User-Id", "1");

        ItemDto item1 = makeCustomItemDto(1, "New item", "Some item descr", false);
        template.postForEntity(
                getDefaultUri(),
                new HttpEntity<>(item1, headers),
                ItemDto.class
        );

        item1 = makeCustomItemDto(1, null, null, null);
        ResponseEntity<ItemDto> response = template.exchange(
                getDefaultUri() + "/1",
                HttpMethod.PATCH,
                new HttpEntity<>(item1, headers),
                ItemDto.class
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void shouldThrowExceptionForWrongOwnerUpdatingItem() {
        addDefaultUser();
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Sharer-User-Id", "1");

        ItemDto item1 = makeCustomItemDto(1, "New item", "Some item descr", false);
        template.postForEntity(
                getDefaultUri(),
                new HttpEntity<>(item1, headers),
                ItemDto.class
        );

        item1 = makeCustomItemDto(1, null, "New item descr", true);
        headers.set("X-Sharer-User-Id", "2");
        ResponseEntity<ItemDto> response = template.exchange(
                getDefaultUri() + "/1",
                HttpMethod.PATCH,
                new HttpEntity<>(item1, headers),
                ItemDto.class
        );

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
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
                .build();
    }

    private ItemDto makeCustomItemDto(long id, String name, String description, Boolean available) {
        return ItemDto.builder()
                .id(id)
                .name(name)
                .description(description)
                .available(available)
                .build();
    }

    private void addDefaultUser() {
        template.postForObject(String.format("http://localhost:%d/users", port),
                UserDto.builder()
                        .name("Tom")
                        .email("tomsmail@mail.ru")
                        .build(),
                UserDto.class
        );
    }
}
