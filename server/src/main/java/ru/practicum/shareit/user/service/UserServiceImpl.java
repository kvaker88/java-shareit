package ru.practicum.shareit.user.service;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import ru.practicum.shareit.exception.EmailAlreadyExistsException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.CreateUserRequestDto;
import ru.practicum.shareit.user.dto.UpdateUserRequestDto;
import ru.practicum.shareit.user.dto.UserResponseDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.utils.UserTools;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {
    private static final String USER_NOT_FOUND = "Не удалось найти пользователя с ID ";

    private final UserRepository userRepository;

    @Override
    @Transactional
    public UserResponseDto createUser(@Valid CreateUserRequestDto createUserRequestDto) {
        isEmailTaken(createUserRequestDto.getEmail());

        User user = UserMapper.toUser(createUserRequestDto);

        User savedUser = userRepository.save(user);

        return UserMapper.toUserDto(savedUser);
    }

    @Override
    public UserResponseDto getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND + userId));
        return UserMapper.toUserDto(user);
    }

    @Override
    public List<UserResponseDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(UserMapper::toUserDto)
                .toList();
    }

    @Override
    @Transactional
    public UserResponseDto updateUser(Long userId, UpdateUserRequestDto updateUpdateUserRequestDto) {
        UserTools.validateUserForUpdate(updateUpdateUserRequestDto);

        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND + userId));

        if (StringUtils.hasText(updateUpdateUserRequestDto.getName())) {
            existingUser.setName(updateUpdateUserRequestDto.getName());
        }

        if (StringUtils.hasText(updateUpdateUserRequestDto.getEmail())) {
            isEmailTaken(updateUpdateUserRequestDto.getEmail());
            existingUser.setEmail(updateUpdateUserRequestDto.getEmail());
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

    private void isEmailTaken(String email) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new EmailAlreadyExistsException(String.format("Пользователь с email %s уже существует", email));
        }
    }
}