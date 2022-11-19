package ru.practicum.shareit.booking.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoRequest;
import ru.practicum.shareit.booking.dto.BookingDtoShort;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.dto.BookingStatus;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

@Mapper(componentModel = "spring")
public interface BookingMapper {
    @Mapping(source = "bookingDtoRequest.start", target = "startTime")
    @Mapping(source = "bookingDtoRequest.end", target = "endTime")
    @Mapping(source = "booker", target = "booker")
    @Mapping(source = "item", target = "item")
    @Mapping(target = "approved", ignore = true)
    @Mapping(target = "id", ignore = true)
    Booking mapToModel(BookingDtoRequest bookingDtoRequest, User booker, Item item);

    @Mapping(source = "booking.startTime", target = "start")
    @Mapping(source = "booking.endTime", target = "end")
    BookingDto mapToDto(Booking booking, BookingStatus status);

    @Mapping(source = "booking.booker.id", target = "bookerId")
    BookingDtoShort mapToShortDto(Booking booking);
}
