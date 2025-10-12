package ru.practicum.shareit.user;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

@RestController
@RequestMapping(path = "/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {
    private final UserService userService;

    @PostMapping
    public ResponseEntity<UserDto> createUser(
            @Valid @RequestBody UserDto userDto
    ) {
        log.info("Запрос на создание пользователя: {}", userDto);
        UserDto user = userService.createUser(userDto);
        return ResponseEntity.ok(user);
    }

    @PatchMapping("/{userId}")
    public ResponseEntity<UserDto> updateUser(
            @PathVariable Long userId,
            @RequestBody UserDto userDto
    ) {
        log.info("Запрос на обновление пользователя {}: {}", userId, userDto);
        UserDto user = userService.updateUser(userId, userDto);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserDto> getUserById(
            @PathVariable Long userId
    ) {
        log.info("Запрос на получение пользователя по ID {}", userId);
        UserDto user = userService.getUserById(userId);
        return ResponseEntity.ok(user);
    }

    @GetMapping
    public ResponseEntity<List<UserDto>> getAllUsers() {
        log.info("Запрос на получение всех пользователей");
        List<UserDto> users = userService.getAllUsers();
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