package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.CreateUserRequestDto;
import ru.practicum.shareit.user.dto.UpdateUserRequestDto;
import ru.practicum.shareit.user.dto.UserResponseDto;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

@RestController
@RequestMapping(path = "/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {
    private final UserService userService;

    @PostMapping
    public ResponseEntity<UserResponseDto> createUser(
            @RequestBody CreateUserRequestDto createUserRequestDto
    ) {
        log.info("Запрос на создание пользователя: {}", createUserRequestDto);
        UserResponseDto user = userService.createUser(createUserRequestDto);
        return ResponseEntity.ok(user);
    }

    @PatchMapping("/{userId}")
    public ResponseEntity<UserResponseDto> updateUser(
            @PathVariable Long userId,
            @RequestBody UpdateUserRequestDto updateUserRequestDto
    ) {
        log.info("Запрос на обновление пользователя {}: {}", userId, updateUserRequestDto);
        UserResponseDto user = userService.updateUser(userId, updateUserRequestDto);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserResponseDto> getUserById(
            @PathVariable Long userId
    ) {
        log.info("Запрос на получение пользователя по ID {}", userId);
        UserResponseDto user = userService.getUserById(userId);
        return ResponseEntity.ok(user);
    }

    @GetMapping
    public ResponseEntity<List<UserResponseDto>> getAllUsers() {
        log.info("Запрос на получение всех пользователей");
        List<UserResponseDto> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(
            @PathVariable Long userId
    ) {
        log.info("Запрос на удаление пользователя {}", userId);
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }
}