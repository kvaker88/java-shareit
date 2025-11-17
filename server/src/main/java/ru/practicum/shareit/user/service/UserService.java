package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.dto.CreateUserRequestDto;
import ru.practicum.shareit.user.dto.UpdateUserRequestDto;
import ru.practicum.shareit.user.dto.UserResponseDto;

import java.util.List;

public interface UserService {

    UserResponseDto createUser(CreateUserRequestDto createUserRequestDto);

    UserResponseDto updateUser(Long userId, UpdateUserRequestDto updateUserRequestDto);

    UserResponseDto getUserById(Long userId);

    List<UserResponseDto> getAllUsers();

    void deleteUser(Long userId);
}