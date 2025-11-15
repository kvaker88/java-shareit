package ru.practicum.shareit.user.dto;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Email;
import lombok.Data;

@Data
public class UpdateUserRequestDto {

    @Nullable
    private String name;

    @Nullable
    @Email(message = "Некорректный формат email")
    private String email;
}