package ru.practicum.shareit.booking.dto;

import java.time.LocalDateTime;

import javax.validation.constraints.Future;
import javax.validation.constraints.FutureOrPresent;
import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BookItemRequestDto {
    @NotNull(message = "Идентификатор бронируемой вещи не может быть пустым")
    private long itemId;
    @FutureOrPresent(message = "Дата начала бронирования должна быть в будущем.")
    @NotNull(message = "Дата начала бронирования не может быть пустой.")
    private LocalDateTime start;
    @Future(message = "Дата окончания бронирования должна быть в будущем.")
    @NotNull(message = "Дата окончания бронирования не может быть пустой.")
    private LocalDateTime end;
}
