package ru.practicum.shareit.user.service;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.EmailAlreadyExistsException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dao.UserRepository;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private static final String USER_NOT_FOUND = "Не удалось найти пользователя с ID ";

    private final UserRepository userRepository;

    public UserDto createUser(@Valid UserDto createUserDto) {
        checkEmailUniqueness(createUserDto.getEmail());

        User user = UserMapper.toUser(createUserDto);
        User savedUser = userRepository.save(user);

        return UserMapper.toUserDto(savedUser);
    }

    public UserDto getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND + userId));
        return UserMapper.toUserDto(user);
    }

    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(UserMapper::toUserDto)
                .toList();
    }

    public UserDto updateUser(Long userId, UserDto updateUserDto) {
        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND + userId));

        if (updateUserDto.getName() != null && !updateUserDto.getName().isBlank()) {
            existingUser.setName(updateUserDto.getName());
        }

        if (updateUserDto.getEmail() != null && !updateUserDto.getEmail().isBlank()) {
            checkEmailUniqueness(updateUserDto.getEmail());
            existingUser.setEmail(updateUserDto.getEmail());
        }

        User updatedUser = userRepository.update(userId, existingUser);
        return UserMapper.toUserDto(updatedUser);
    }

    public void deleteUser(Long userId) {
        if (userRepository.nonExistsById(userId)) {
            throw new NotFoundException(USER_NOT_FOUND + userId);
        }
        userRepository.delete(userId);
    }

    private void checkEmailUniqueness(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            throw new EmailAlreadyExistsException("Пользователь с email " + email + " уже существует");
        });
    }
}