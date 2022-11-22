package ru.practicum.shareit.request.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.exception.RequestNotFoundException;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.exception.UserNotFoundException;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ItemRequestServiceImpl implements ItemRequestService {

    private final UserService userService;
    private final ItemRequestRepository repository;
    private final ItemRequestMapper mapper;

    @Override
    public ItemRequestDto addRequest(ItemRequestDto requestDto, long requesterId) {
        ItemRequest request = mapper.mapToModel(requestDto, userService.getUser(requesterId),
                LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS));
        request.setId(null);
        request = repository.save(request);

        log.debug("Добавлен новый запрос на добавление вещи: {}", request);
        return mapper.mapToDto(request);
    }

    @Override
    public ItemRequestDto getRequestDto(long requestId, long requesterId) {
        if (userService.userNotFound(requesterId)) {
            throw new UserNotFoundException(
                    String.format("Ошибка при получении запроса на добавление вещи: " +
                            "пользователь с id=%d не найден", requesterId));
        }

        return mapper.mapToDto(this.getRequest(requestId));
    }

    @Override
    public ItemRequest getRequest(long requestId) {
        Optional<ItemRequest> requestOptional = repository.findById(requestId);

        if (requestOptional.isEmpty()) {
            throw new RequestNotFoundException(
                    String.format("Запрос с id=%d на добавление вещи не найден.", requestId));
        }

        return requestOptional.get();
    }

    @Override
    public Collection<ItemRequestDto> getOwnItemRequests(long requesterId) {
        if (userService.userNotFound(requesterId)) {
            throw new UserNotFoundException(
                    String.format("Ошибка при получении собственных запросов на добавление вещи: " +
                            "пользователь с id=%d не найден", requesterId));
        }

        return repository.findAllByRequesterIdOrderByCreatedDesc(requesterId).stream()
                .map(mapper::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public Collection<ItemRequestDto> getOtherUsersRequests(long requesterId, int startingIndex, int collectionSize) {
        if (userService.userNotFound(requesterId)) {
            throw new UserNotFoundException(
                    String.format("Ошибка при получении запросов других пользователей на добавление вещи: " +
                            "пользователь с id=%d не найден", requesterId));
        }

        return repository.findAllByRequesterIdNotOrderByCreatedDesc(requesterId).stream()
                .skip(startingIndex)
                .limit(collectionSize)
                .map(mapper::mapToDto)
                .collect(Collectors.toList());
    }
}
