package ru.practicum.shareit.request;

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
import ru.practicum.shareit.request.dto.ItemRequestDto;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@Import(ItemRequestController.class)
class ItemRequestControllerTest {

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
    private ItemRequestClient itemRequestClient;

    private ItemRequestDto ItemRequestDto;
    private static final String SHARER_USER_ID = "X-Sharer-User-Id";

    @BeforeEach
    void setUp() {
        ItemRequestDto = new ItemRequestDto();
        ItemRequestDto.setDescription("Нужна дрель для ремонта");
    }

    @Test
    @DisplayName("Создание запроса на предмет с валидными данными -> возвращает 200 OK")
    void create_whenValidItemRequest_thenReturnOk() throws Exception {
        when(itemRequestClient.create(any(ItemRequestDto.class), anyLong()))
                .thenReturn(new ResponseEntity<>("{}", HttpStatus.OK));

        mockMvc.perform(post("/requests")
                        .header(SHARER_USER_ID, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ItemRequestDto)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Создание запроса на предмет с невалидными данными -> возвращает 400 Bad Request")
    void create_whenInvalidItemRequest_thenReturnBadRequest() throws Exception {
        ItemRequestDto invalidRequest = new ItemRequestDto();
        invalidRequest.setDescription("");

        mockMvc.perform(post("/requests")
                        .header(SHARER_USER_ID, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Создание запроса на предмет без заголовка X-Sharer-User-Id -> возвращает 400 Bad Request")
    void create_whenMissingUserIdHeader_thenReturnBadRequest() throws Exception {
        mockMvc.perform(post("/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ItemRequestDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Получение собственных запросов на предметы -> возвращает 200 OK")
    void getOwn_whenValidRequest_thenReturnOk() throws Exception {
        when(itemRequestClient.getByRequestor(anyLong()))
                .thenReturn(new ResponseEntity<>("[]", HttpStatus.OK));

        mockMvc.perform(get("/requests")
                        .header(SHARER_USER_ID, 1L))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Получение собственных запросов без заголовка X-Sharer-User-Id -> возвращает 400 Bad Request")
    void getOwn_whenMissingUserIdHeader_thenReturnBadRequest() throws Exception {
        mockMvc.perform(get("/requests"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Получение всех запросов на предметы -> возвращает 200 OK")
    void getAll_whenValidRequest_thenReturnOk() throws Exception {
        when(itemRequestClient.getAll(anyLong(), anyInt(), anyInt()))
                .thenReturn(new ResponseEntity<>("[]", HttpStatus.OK));

        mockMvc.perform(get("/requests/all")
                        .header(SHARER_USER_ID, 1L)
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Получение всех запросов с отрицательным параметром 'from' -> возвращает 400 Bad Request")
    void getAll_whenInvalidFromParameter_thenReturnBadRequest() throws Exception {
        mockMvc.perform(get("/requests/all")
                        .header(SHARER_USER_ID, 1L)
                        .param("from", "-1")
                        .param("size", "10"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Получение всех запросов с параметром 'size' меньше 1 -> возвращает 400 Bad Request")
    void getAll_whenInvalidSizeParameter_thenReturnBadRequest() throws Exception {
        mockMvc.perform(get("/requests/all")
                        .header(SHARER_USER_ID, 1L)
                        .param("from", "0")
                        .param("size", "0"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Получение всех запросов без параметров пагинации -> возвращает 200 OK")
    void getAll_whenMissingPaginationParameters_thenReturnOk() throws Exception {
        when(itemRequestClient.getAll(anyLong(), anyInt(), anyInt()))
                .thenReturn(new ResponseEntity<>("[]", HttpStatus.OK));

        mockMvc.perform(get("/requests/all")
                        .header(SHARER_USER_ID, 1L))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Получение запроса на предмет по ID -> возвращает 200 OK")
    void getById_whenValidRequest_thenReturnOk() throws Exception {
        when(itemRequestClient.getById(anyLong(), anyLong()))
                .thenReturn(new ResponseEntity<>("{}", HttpStatus.OK));

        mockMvc.perform(get("/requests/1")
                        .header(SHARER_USER_ID, 1L))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Получение запроса на предмет по ID без заголовка X-Sharer-User-Id -> возвращает 400 Bad Request")
    void getById_whenMissingUserIdHeader_thenReturnBadRequest() throws Exception {
        mockMvc.perform(get("/requests/1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Получение несуществующего запроса на предмет по ID -> возвращает 404 Not Found")
    void getById_whenItemRequestNotFound_thenReturnNotFound() throws Exception {
        when(itemRequestClient.getById(anyLong(), anyLong()))
                .thenReturn(new ResponseEntity<>(HttpStatus.NOT_FOUND));

        mockMvc.perform(get("/requests/999")
                        .header(SHARER_USER_ID, 1L))
                .andExpect(status().isNotFound());
    }
}