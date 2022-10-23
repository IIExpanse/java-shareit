package ru.practicum.shareit.item.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Item {
    private Long itemId;
    private Long ownerId;
    private final String name;
    private final String description;
    private final Boolean available;
}
