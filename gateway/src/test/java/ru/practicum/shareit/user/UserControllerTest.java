package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.user.dto.CreateUserRequestDto;
import ru.practicum.shareit.user.dto.UpdateUserRequestDto;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@Import(UserController.class)
class UserControllerTest {

    @Configuration
    static class TestConfig {
        @Bean
        public ObjectMapper objectMapper() {
            return new ObjectMapper();
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserClient userClient;

    private CreateUserRequestDto createUserDto;
    private UpdateUserRequestDto updateUserDto;

    @BeforeEach
    void setUp() {
        createUserDto = new CreateUserRequestDto();
        createUserDto.setName("Имя");
        createUserDto.setEmail("mail@yandex.ru");

        updateUserDto = new UpdateUserRequestDto();
        updateUserDto.setName("Обновленное имя");
        updateUserDto.setEmail("updated@yandex.ru");
    }

    @Test
    @DisplayName("Создание пользователя с валидными данными -> возвращает 200 OK")
    void create_whenValidUser_thenReturnOk() throws Exception {
        when(userClient.create(any(CreateUserRequestDto.class)))
                .thenReturn(new ResponseEntity<>("{}", HttpStatus.OK));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createUserDto)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Создание пользователя с невалидными данными -> возвращает 400 Bad Request")
    void create_whenInvalidUser_thenReturnBadRequest() throws Exception {
        CreateUserRequestDto invalidUser = new CreateUserRequestDto();
        invalidUser.setName("");
        invalidUser.setEmail("invalid-email");

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidUser)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Обновление пользователя с валидными данными -> возвращает 200 OK")
    void update_whenValidRequest_thenReturnOk() throws Exception {
        when(userClient.update(anyLong(), any(UpdateUserRequestDto.class)))
                .thenReturn(new ResponseEntity<>("{}", HttpStatus.OK));

        mockMvc.perform(patch("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateUserDto)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Обновление пользователя с невалидными данными -> возвращает 400 Bad Request")
    void update_whenInvalidUser_thenReturnBadRequest() throws Exception {
        UpdateUserRequestDto invalidUser = new UpdateUserRequestDto();
        invalidUser.setName("");
        invalidUser.setEmail("invalid-email");

        mockMvc.perform(patch("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidUser)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Обновление несуществующего пользователя -> возвращает 404 Not Found")
    void update_whenUserNotFound_thenReturnNotFound() throws Exception {
        when(userClient.update(anyLong(), any(UpdateUserRequestDto.class)))
                .thenReturn(new ResponseEntity<>(HttpStatus.NOT_FOUND));

        mockMvc.perform(patch("/users/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateUserDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Получение пользователя по ID -> возвращает 200 OK")
    void getById_whenValidRequest_thenReturnOk() throws Exception {
        when(userClient.getById(anyLong()))
                .thenReturn(new ResponseEntity<>("{}", HttpStatus.OK));

        mockMvc.perform(get("/users/1"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Получение несуществующего пользователя по ID -> возвращает 404 Not Found")
    void getById_whenUserNotFound_thenReturnNotFound() throws Exception {
        when(userClient.getById(anyLong()))
                .thenReturn(new ResponseEntity<>(HttpStatus.NOT_FOUND));

        mockMvc.perform(get("/users/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Получение всех пользователей -> возвращает 200 OK")
    void getAll_whenValidRequest_thenReturnOk() throws Exception {
        when(userClient.getAll())
                .thenReturn(new ResponseEntity<>("[]", HttpStatus.OK));

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Удаление пользователя -> возвращает 200 OK")
    void delete_whenValidRequest_thenReturnOk() throws Exception {
        when(userClient.delete(anyLong()))
                .thenReturn(new ResponseEntity<>(HttpStatus.OK));

        mockMvc.perform(delete("/users/1"))
                .andExpect(status().isOk());
    }
}