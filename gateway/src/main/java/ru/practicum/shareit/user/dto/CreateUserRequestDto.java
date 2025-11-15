package ru.practicum.shareit.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateUserRequestDto {

    @NotBlank(message ="Имя пользователя должно быть заполнено")
    private String name;

    @NotBlank(message = "Email пользователя должен быть заполнен")
    @Email(message = "Некорректный формат email")
    private String email;
}