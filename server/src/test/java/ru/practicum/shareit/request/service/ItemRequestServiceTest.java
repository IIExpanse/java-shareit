package ru.practicum.shareit.request.service;

import lombok.AllArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.exception.RequestNotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.exception.UserNotFoundException;
import ru.practicum.shareit.user.service.UserService;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@AllArgsConstructor(onConstructor_ = @Autowired)
@AutoConfigureTestDatabase
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Sql(scripts = "classpath:schema.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class ItemRequestServiceTest {

    private UserService userService;
    private ItemRequestService requestService;

    @Test
    public void addRequestTest() {
        UserDto userDto = makeDefaultUser();
        userDto = userService.addUser(userDto);

        ItemRequestDto requestDto = makeDefaultRequest();
        requestDto = requestService.addRequest(requestDto, userDto.getId());
        requestDto.setCreated(requestDto.getCreated().truncatedTo(ChronoUnit.MICROS));
        requestDto.setItems(Set.of());


        assertEquals(requestDto, requestService.getRequestDto(requestDto.getId(), userDto.getId()));
    }

    @Test
    public void shouldThrowExceptionForAddingRequestFromNotFoundUser() {
        ItemRequestDto requestDto = makeDefaultRequest();
        assertThrows(UserNotFoundException.class, () -> requestService.addRequest(requestDto, 0));
    }

    @Test
    public void shouldThrowExceptionForGettingRequestFromNotFoundUser() {
        assertThrows(UserNotFoundException.class, () -> requestService.getRequestDto(0, 0));
    }

    @Test
    public void shouldThrowExceptionForGettingNotFoundRequest() {
        assertThrows(RequestNotFoundException.class, () -> requestService.getRequest(0));
    }

    @Test
    public void getOwnItemRequestsTest() {
        UserDto userDto = makeDefaultUser();
        userDto = userService.addUser(userDto);

        UserDto userDto2 = makeDefaultUser();
        userDto2.setEmail("new@mail.ru");
        userDto2 = userService.addUser(userDto2);

        ItemRequestDto requestDto = makeDefaultRequest();
        requestDto = requestService.addRequest(requestDto, userDto.getId());
        requestDto.setItems(Set.of());
        requestDto.setCreated(requestDto.getCreated().truncatedTo(ChronoUnit.MICROS));

        ItemRequestDto requestDto2 = makeDefaultRequest();
        requestService.addRequest(requestDto2, userDto2.getId());

        assertEquals(List.of(requestDto), requestService.getOwnItemRequests(userDto.getId()));
    }

    @Test
    public void shouldThrowExceptionForGettingOwnItemRequestsFromNotFoundUser() {
        assertThrows(UserNotFoundException.class, () -> requestService.getOwnItemRequests(0));
    }

    @Test
    public void getOtherUsersRequestsTest() {
        UserDto userDto = makeDefaultUser();
        userDto = userService.addUser(userDto);

        UserDto userDto2 = makeDefaultUser();
        userDto2.setEmail("new@mail.ru");
        userDto2 = userService.addUser(userDto2);

        ItemRequestDto requestDto = makeDefaultRequest();
        requestDto = requestService.addRequest(requestDto, userDto.getId());
        requestDto.setItems(Set.of());
        requestDto.setCreated(requestDto.getCreated().truncatedTo(ChronoUnit.MICROS));

        ItemRequestDto requestDto2 = makeDefaultRequest();
        requestService.addRequest(requestDto2, userDto2.getId());

        assertEquals(List.of(requestDto),
                requestService.getOtherUsersRequests(userDto2.getId(), 0, Integer.MAX_VALUE));
    }

    @Test
    public void shouldThrowExceptionForGettingOtherUsersRequestsFromNotFoundUser() {
        assertThrows(UserNotFoundException.class,
                () -> requestService.getOtherUsersRequests(0, 0, Integer.MAX_VALUE));
    }

    private ItemRequestDto makeDefaultRequest() {
        return ItemRequestDto.builder()
                .description("Need some item")
                .build();
    }

    private UserDto makeDefaultUser() {
        return UserDto.builder()
                .name("Tom")
                .email("tomsmail@mail.ru")
                .build();
    }
}
