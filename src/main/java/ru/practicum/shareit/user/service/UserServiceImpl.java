package ru.practicum.shareit.user.service;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.EmailAlreadyExistsException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {
    private static final String USER_NOT_FOUND = "Не удалось найти пользователя с ID ";

    private final UserRepository userRepository;

    @Override
    @Transactional
    public UserDto createUser(@Valid UserDto createUserDto) {
        checkEmailUniqueness(createUserDto.getEmail());

        User user = UserMapper.toUser(createUserDto);
        User savedUser = userRepository.save(user);

        return UserMapper.toUserDto(savedUser);
    }

    @Override
    public UserDto getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND + userId));
        return UserMapper.toUserDto(user);
    }

    @Override
    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(UserMapper::toUserDto)
                .toList();
    }

    @Override
    @Transactional
    public UserDto updateUser(Long userId, UserDto updateUserDto) {
        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND + userId));

        if (updateUserDto.getName() != null && !updateUserDto.getName().isBlank()) {
            existingUser.setName(updateUserDto.getName());
        }

        if (updateUserDto.getEmail() != null && !updateUserDto.getEmail().isBlank()) {
            userRepository.findByEmailAndIdNot(updateUserDto.getEmail(), userId).ifPresent(user -> {
                throw new EmailAlreadyExistsException("Пользователь с email " + updateUserDto.getEmail()
                        + " уже существует");
            });
            existingUser.setEmail(updateUserDto.getEmail());
        }

        User updatedUser = userRepository.save(existingUser);
        return UserMapper.toUserDto(updatedUser);
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException(USER_NOT_FOUND + userId);
        }
        userRepository.deleteById(userId);
    }

    private void checkEmailUniqueness(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            throw new EmailAlreadyExistsException("Пользователь с email " + email + " уже существует");
        });
    }
}