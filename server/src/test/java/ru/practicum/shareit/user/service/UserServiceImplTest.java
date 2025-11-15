package ru.practicum.shareit.user.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.exception.EmailAlreadyExistsException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.CreateUserRequestDto;
import ru.practicum.shareit.user.dto.UpdateUserRequestDto;
import ru.practicum.shareit.user.dto.UserResponseDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import(UserServiceImpl.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class UserServiceImplTest {

    @Autowired
    private UserServiceImpl userService;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setName("Тестовый пользователь");
        testUser.setEmail("test@yandex.ru");
        testUser = userRepository.save(testUser);
    }

    @Test
    @DisplayName("Создание пользователя с валидными данными -> пользователь создан")
    void createUser_whenValidData_thenUserCreated() {
        CreateUserRequestDto requestDto = new CreateUserRequestDto();
        requestDto.setName("Новый пользователь");
        requestDto.setEmail("newuser@yandex.ru");

        UserResponseDto result = userService.createUser(requestDto);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getName()).isEqualTo("Новый пользователь");
        assertThat(result.getEmail()).isEqualTo("newuser@yandex.ru");

        assertTrue(userRepository.findById(result.getId()).isPresent());
    }

    @Test
    @DisplayName("Создание пользователя с существующим email -> исключение")
    void createUser_whenEmailExists_thenThrowException() {
        CreateUserRequestDto requestDto = new CreateUserRequestDto();
        requestDto.setName("Другой пользователь");
        requestDto.setEmail("test@yandex.ru");

        assertThrows(EmailAlreadyExistsException.class,
                () -> userService.createUser(requestDto));
    }

    @Test
    @DisplayName("Получение пользователя по существующему ID -> пользователь найден")
    void getUserById_whenUserExists_thenReturnUser() {
        UserResponseDto result = userService.getUserById(testUser.getId());

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(testUser.getId());
        assertThat(result.getName()).isEqualTo("Тестовый пользователь");
        assertThat(result.getEmail()).isEqualTo("test@yandex.ru");
    }

    @Test
    @DisplayName("Получение пользователя по несуществующему ID -> исключение")
    void getUserById_whenUserNotExists_thenThrowException() {
        assertThrows(NotFoundException.class,
                () -> userService.getUserById(999L));
    }

    @Test
    @DisplayName("Получение всех пользователей -> возвращает список пользователей")
    void getAllUsers_whenUsersExist_thenReturnAllUsers() {
        User secondUser = new User();
        secondUser.setName("Второй пользователь");
        secondUser.setEmail("second@yandex.ru");
        userRepository.save(secondUser);

        List<UserResponseDto> result = userService.getAllUsers();

        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(UserResponseDto::getEmail)
                .containsExactlyInAnyOrder("test@yandex.ru", "second@yandex.ru");
    }

    @Test
    @DisplayName("Получение всех пользователей когда нет пользователей -> возвращает пустой список")
    void getAllUsers_whenNoUsers_thenReturnEmptyList() {
        userRepository.deleteAll();

        List<UserResponseDto> result = userService.getAllUsers();

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Обновление имени пользователя -> имя обновлено")
    void updateUser_whenUpdateName_thenNameUpdated() {
        UpdateUserRequestDto updateDto = new UpdateUserRequestDto();
        updateDto.setName("Обновленное имя");

        UserResponseDto result = userService.updateUser(testUser.getId(), updateDto);

        assertThat(result.getName()).isEqualTo("Обновленное имя");
        assertThat(result.getEmail()).isEqualTo("test@yandex.ru");

        User updatedUser = userRepository.findById(testUser.getId()).orElseThrow();
        assertThat(updatedUser.getName()).isEqualTo("Обновленное имя");
    }

    @Test
    @DisplayName("Обновление email пользователя -> email обновлен")
    void updateUser_whenUpdateEmail_thenEmailUpdated() {
        UpdateUserRequestDto updateDto = new UpdateUserRequestDto();
        updateDto.setEmail("newemail@yandex.ru");

        UserResponseDto result = userService.updateUser(testUser.getId(), updateDto);

        assertThat(result.getEmail()).isEqualTo("newemail@yandex.ru");
        assertThat(result.getName()).isEqualTo("Тестовый пользователь");
    }

    @Test
    @DisplayName("Обновление пользователя с существующим email -> исключение")
    void updateUser_whenEmailExists_thenThrowException() {
        User secondUser = new User();
        secondUser.setName("Второй пользователь");
        secondUser.setEmail("second@yandex.ru");
        userRepository.save(secondUser);

        UpdateUserRequestDto updateDto = new UpdateUserRequestDto();
        updateDto.setEmail("second@yandex.ru");

        Executable executable = () -> userService.updateUser(testUser.getId(), updateDto);
        assertThrows(EmailAlreadyExistsException.class, executable);
    }

    @Test
    @DisplayName("Обновление несуществующего пользователя -> исключение")
    void updateUser_whenUserNotExists_thenThrowException() {
        UpdateUserRequestDto updateDto = new UpdateUserRequestDto();
        updateDto.setName("Новое имя");

        assertThrows(NotFoundException.class,
                () -> userService.updateUser(999L, updateDto));
    }

    @Test
    @DisplayName("Удаление существующего пользователя -> пользователь удален")
    void deleteUser_whenUserExists_thenUserDeleted() {
        userService.deleteUser(testUser.getId());

        assertFalse(userRepository.existsById(testUser.getId()));
    }

    @Test
    @DisplayName("Удаление несуществующего пользователя -> исключение")
    void deleteUser_whenUserNotExists_thenThrowException() {
        assertThrows(NotFoundException.class,
                () -> userService.deleteUser(999L));
    }
}