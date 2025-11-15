package ru.practicum.shareit.user.dto;

import lombok.Data;

@Data
public class UpdateUserRequestDto {

    private String name;
    private String email;
}