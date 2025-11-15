package ru.practicum.shareit.item;

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
import ru.practicum.shareit.exception.ErrorHandler;
import ru.practicum.shareit.item.dto.CommentRequestDto;
import ru.practicum.shareit.item.dto.ItemRequestDto;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@Import({ItemController.class, ErrorHandler.class})
class ItemControllerTest {

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
    private ItemClient itemClient;

    private ItemRequestDto itemRequestDto;
    private CommentRequestDto commentRequestDto;
    private static final String SHARER_USER_ID = "X-Sharer-User-Id";

    @BeforeEach
    void setUp() {
        itemRequestDto = new ItemRequestDto();
        itemRequestDto.setName("Дрель");
        itemRequestDto.setDescription("Мощная дрель для ремонта");
        itemRequestDto.setAvailable(true);

        commentRequestDto = new CommentRequestDto();
        commentRequestDto.setText("Отличная дрель, всем рекомендую!");
    }

    @Test
    @DisplayName("Создание предмета с валидными данными -> возвращает 200 OK")
    void create_whenValidItem_thenReturnOk() throws Exception {
        when(itemClient.create(any(ItemRequestDto.class), anyLong()))
                .thenReturn(new ResponseEntity<>("{}", HttpStatus.OK));

        mockMvc.perform(post("/items")
                        .header(SHARER_USER_ID, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemRequestDto)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Создание предмета с невалидными данными -> возвращает 400 Bad Request")
    void create_whenInvalidItem_thenReturnBadRequest() throws Exception {
        ItemRequestDto invalidItem = new ItemRequestDto();
        invalidItem.setName("");
        invalidItem.setDescription("");
        invalidItem.setAvailable(null);

        mockMvc.perform(post("/items")
                        .header(SHARER_USER_ID, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidItem)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Создание предмета без заголовка X-Sharer-User-Id -> возвращает 400 Bad Request")
    void create_whenMissingUserIdHeader_thenReturnBadRequest() throws Exception {
        mockMvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemRequestDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Обновление предмета с валидными данными -> возвращает 200 OK")
    void update_whenValidRequest_thenReturnOk() throws Exception {
        when(itemClient.update(anyLong(), any(ItemRequestDto.class), anyLong()))
                .thenReturn(new ResponseEntity<>("{}", HttpStatus.OK));

        mockMvc.perform(patch("/items/1")
                        .header(SHARER_USER_ID, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemRequestDto)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Обновление предмета без заголовка X-Sharer-User-Id -> возвращает 400 Bad Request")
    void update_whenMissingUserIdHeader_thenReturnBadRequest() throws Exception {
        mockMvc.perform(patch("/items/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemRequestDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Обновление несуществующего предмета -> возвращает 404 Not Found")
    void update_whenItemNotFound_thenReturnNotFound() throws Exception {
        when(itemClient.update(anyLong(), any(ItemRequestDto.class), anyLong()))
                .thenReturn(new ResponseEntity<>(HttpStatus.NOT_FOUND));

        mockMvc.perform(patch("/items/999")
                        .header(SHARER_USER_ID, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemRequestDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Получение предмета по ID -> возвращает 200 OK")
    void getById_whenValidRequest_thenReturnOk() throws Exception {
        when(itemClient.getById(anyLong(), anyLong()))
                .thenReturn(new ResponseEntity<>("{}", HttpStatus.OK));

        mockMvc.perform(get("/items/1")
                        .header(SHARER_USER_ID, 1L))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Получение предмета по ID без заголовка X-Sharer-User-Id -> возвращает 400 Bad Request")
    void getById_whenMissingUserIdHeader_thenReturnBadRequest() throws Exception {
        mockMvc.perform(get("/items/1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Получение несуществующего предмета по ID -> возвращает 404 Not Found")
    void getById_whenItemNotFound_thenReturnNotFound() throws Exception {
        when(itemClient.getById(anyLong(), anyLong()))
                .thenReturn(new ResponseEntity<>(HttpStatus.NOT_FOUND));

        mockMvc.perform(get("/items/999")
                        .header(SHARER_USER_ID, 1L))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Получение предметов по владельцу -> возвращает 200 OK")
    void getByOwnerId_whenValidRequest_thenReturnOk() throws Exception {
        when(itemClient.getByOwnerId(anyLong()))
                .thenReturn(new ResponseEntity<>("[]", HttpStatus.OK));

        mockMvc.perform(get("/items")
                        .header(SHARER_USER_ID, 1L))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Получение предметов по владельцу без заголовка X-Sharer-User-Id -> возвращает 400 Bad Request")
    void getByOwnerId_whenMissingUserIdHeader_thenReturnBadRequest() throws Exception {
        mockMvc.perform(get("/items"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Поиск предметов по тексту -> возвращает 200 OK")
    void search_whenValidRequest_thenReturnOk() throws Exception {
        when(itemClient.search(anyString(), anyInt(), anyInt()))
                .thenReturn(new ResponseEntity<>("[]", HttpStatus.OK));

        mockMvc.perform(get("/items/search")
                        .param("text", "дрель")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Поиск предметов с отрицательным параметром 'from' -> возвращает 400 Bad Request")
    void search_whenInvalidFromParameter_thenReturnBadRequest() throws Exception {
        mockMvc.perform(get("/items/search")
                        .param("text", "дрель")
                        .param("from", "-1")
                        .param("size", "10"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Поиск предметов с параметром 'size' меньше 1 -> возвращает 400 Bad Request")
    void search_whenInvalidSizeParameter_thenReturnBadRequest() throws Exception {
        mockMvc.perform(get("/items/search")
                        .param("text", "дрель")
                        .param("from", "0")
                        .param("size", "0"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Поиск предметов без параметра 'text' -> возвращает 400 Bad Request")
    void search_whenMissingTextParameter_thenReturnBadRequest() throws Exception {
        mockMvc.perform(get("/items/search")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Поиск предметов с пустым параметром 'text' -> возвращает 200 OK")
    void search_whenEmptyTextParameter_thenReturnOk() throws Exception {
        when(itemClient.search(anyString(), anyInt(), anyInt()))
                .thenReturn(new ResponseEntity<>("[]", HttpStatus.OK));

        mockMvc.perform(get("/items/search")
                        .param("text", "")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Поиск предметов без параметров пагинации -> возвращает 200 OK")
    void search_whenMissingPaginationParameters_thenReturnOk() throws Exception {
        when(itemClient.search(anyString(), anyInt(), anyInt()))
                .thenReturn(new ResponseEntity<>("[]", HttpStatus.OK));

        mockMvc.perform(get("/items/search")
                        .param("text", "дрель"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Добавление комментария к предмету -> возвращает 200 OK")
    void addComment_whenValidRequest_thenReturnOk() throws Exception {
        when(itemClient.addComment(anyLong(), any(CommentRequestDto.class), anyLong()))
                .thenReturn(new ResponseEntity<>("{}", HttpStatus.OK));

        mockMvc.perform(post("/items/1/comment")
                        .header(SHARER_USER_ID, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentRequestDto)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Добавление комментария без заголовка X-Sharer-User-Id -> возвращает 400 Bad Request")
    void addComment_whenMissingUserIdHeader_thenReturnBadRequest() throws Exception {
        mockMvc.perform(post("/items/1/comment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentRequestDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Добавление комментария с пустым текстом -> возвращает 400 Bad Request")
    void addComment_whenEmptyText_thenReturnBadRequest() throws Exception {
        CommentRequestDto invalidComment = new CommentRequestDto();
        invalidComment.setText("");

        mockMvc.perform(post("/items/1/comment")
                        .header(SHARER_USER_ID, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidComment)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Добавление комментария к несуществующему предмету -> возвращает 404 Not Found")
    void addComment_whenItemNotFound_thenReturnNotFound() throws Exception {
        when(itemClient.addComment(anyLong(), any(CommentRequestDto.class), anyLong()))
                .thenReturn(new ResponseEntity<>(HttpStatus.NOT_FOUND));

        mockMvc.perform(post("/items/999/comment")
                        .header(SHARER_USER_ID, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentRequestDto)))
                .andExpect(status().isNotFound());
    }
}