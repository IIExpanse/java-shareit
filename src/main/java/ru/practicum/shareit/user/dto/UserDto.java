package ru.practicum.shareit.user.dto;

import lombok.*;
import ru.practicum.shareit.user.controller.UserValidationGroup;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {
    private Long id;
    @NotNull(message = "E-mail не может быть null.", groups = UserValidationGroup.FullValidation.class)
    @Email(message = "Некорректный E-mail.", groups = {
            UserValidationGroup.FullValidation.class,
            UserValidationGroup.PatchValidation.class})
    private String email;
    @NotBlank(message = "Имя пользователя не может быть пустым.", groups = UserValidationGroup.FullValidation.class)
    private String name;
}
