package ru.practicum.shareit.item.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CommentDto {
    private Long id;
    @NotBlank(message = "Текст комментария не может пустым.")
    private String text;
    private String authorName;
    private LocalDateTime created;
}
