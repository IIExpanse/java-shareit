package ru.practicum.shareit.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Future;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BookingDtoRequest {
    @NotNull(message = "Идентификатор бронируемой вещи не может быть пустым")
    private Long itemId;
    @Future(message = "Дата начала бронирования должна быть в будущем.")
    @NotNull(message = "Дата начала бронирования не может быть пустой.")
    LocalDateTime start;
    @Future(message = "Дата окончания бронирования должна быть в будущем.")
    @NotNull(message = "Дата окончания бронирования не может быть пустой.")
    LocalDateTime end;
}
