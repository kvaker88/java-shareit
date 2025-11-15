package ru.practicum.shareit.user.dto;

import lombok.Data;

@Data
public class CreateUserRequestDto {
    private String name;
    private String email;
}