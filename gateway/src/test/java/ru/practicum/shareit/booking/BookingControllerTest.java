package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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
import ru.practicum.shareit.booking.dto.BookItemRequestDto;
import ru.practicum.shareit.exception.ErrorHandler;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@Import({BookingController.class, ErrorHandler.class})
class BookingControllerTest {

    @Configuration
    static class TestConfig {
        @Bean
        public ObjectMapper objectMapper() {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            return mapper;
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookingClient bookingClient;

    private BookItemRequestDto bookItemRequestDto;
    private static final String SHARER_USER_ID = "X-Sharer-User-Id";

    @BeforeEach
    void setUp() {
        bookItemRequestDto = new BookItemRequestDto();
        bookItemRequestDto.setItemId(1L);
        bookItemRequestDto.setStart(LocalDateTime.now().plusDays(1));
        bookItemRequestDto.setEnd(LocalDateTime.now().plusDays(2));
    }

    @Test
    @DisplayName("Получение бронирований пользователя с валидными параметрами -> возвращает 200 OK")
    void getBookings_whenValidRequest_thenReturnOk() throws Exception {
        when(bookingClient.getBookings(anyLong(), any(BookingState.class), anyInt(), anyInt()))
                .thenReturn(new ResponseEntity<>("[]", HttpStatus.OK));

        mockMvc.perform(get("/bookings")
                        .header(SHARER_USER_ID, 1L)
                        .param("state", "ALL")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Получение бронирований пользователя без заголовка X-Sharer-User-Id -> возвращает 400 Bad Request")
    void getBookings_whenMissingUserIdHeader_thenReturnBadRequest() throws Exception {
        mockMvc.perform(get("/bookings")
                        .param("state", "ALL")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Получение бронирований пользователя с отрицательным параметром 'from' -> возвращает 400 Bad Request")
    void getBookings_whenInvalidFromParameter_thenReturnBadRequest() throws Exception {
        mockMvc.perform(get("/bookings")
                        .header(SHARER_USER_ID, 1L)
                        .param("state", "ALL")
                        .param("from", "-1")
                        .param("size", "10"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Получение бронирований пользователя с параметром 'size' меньше 1 -> возвращает 400 Bad Request")
    void getBookings_whenInvalidSizeParameter_thenReturnBadRequest() throws Exception {
        mockMvc.perform(get("/bookings")
                        .header(SHARER_USER_ID, 1L)
                        .param("state", "ALL")
                        .param("from", "0")
                        .param("size", "0"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Получение бронирований пользователя без параметров пагинации -> возвращает 200 OK")
    void getBookings_whenMissingPaginationParameters_thenReturnOk() throws Exception {
        when(bookingClient.getBookings(anyLong(), any(BookingState.class), anyInt(), anyInt()))
                .thenReturn(new ResponseEntity<>("[]", HttpStatus.OK));

        mockMvc.perform(get("/bookings")
                        .header(SHARER_USER_ID, 1L)
                        .param("state", "ALL"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Получение бронирований пользователя с неизвестным состоянием -> возвращает 400 Bad Request")
    void getBookings_whenUnknownState_thenReturnBadRequest() throws Exception {
        mockMvc.perform(get("/bookings")
                        .header(SHARER_USER_ID, 1L)
                        .param("state", "UNKNOWN_STATE")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Получение бронирований владельца с валидными параметрами -> возвращает 200 OK")
    void getByOwnerId_whenValidRequest_thenReturnOk() throws Exception {
        when(bookingClient.getBookingsByOwner(anyLong(), any(BookingState.class), anyInt(), anyInt()))
                .thenReturn(new ResponseEntity<>("[]", HttpStatus.OK));

        mockMvc.perform(get("/bookings/owner")
                        .header(SHARER_USER_ID, 1L)
                        .param("state", "ALL")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Получение бронирований владельца без заголовка X-Sharer-User-Id -> возвращает 400 Bad Request")
    void getByOwnerId_whenMissingUserIdHeader_thenReturnBadRequest() throws Exception {
        mockMvc.perform(get("/bookings/owner")
                        .param("state", "ALL")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Получение бронирований владельца с отрицательным параметром 'from' -> возвращает 400 Bad Request")
    void getByOwnerId_whenInvalidFromParameter_thenReturnBadRequest() throws Exception {
        mockMvc.perform(get("/bookings/owner")
                        .header(SHARER_USER_ID, 1L)
                        .param("state", "ALL")
                        .param("from", "-1")
                        .param("size", "10"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Получение бронирований владельца с параметром 'size' меньше 1 -> возвращает 400 Bad Request")
    void getByOwnerId_whenInvalidSizeParameter_thenReturnBadRequest() throws Exception {
        mockMvc.perform(get("/bookings/owner")
                        .header(SHARER_USER_ID, 1L)
                        .param("state", "ALL")
                        .param("from", "0")
                        .param("size", "0"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Получение бронирований владельца с неизвестным состоянием -> возвращает 400 Bad Request")
    void getByOwnerId_whenUnknownState_thenReturnBadRequest() throws Exception {
        mockMvc.perform(get("/bookings/owner")
                        .header(SHARER_USER_ID, 1L)
                        .param("state", "UNKNOWN_STATE")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Создание бронирования с валидными данными -> возвращает 200 OK")
    void bookItem_whenValidRequest_thenReturnOk() throws Exception {
        when(bookingClient.bookItem(anyLong(), any(BookItemRequestDto.class)))
                .thenReturn(new ResponseEntity<>("{}", HttpStatus.OK));

        mockMvc.perform(post("/bookings")
                        .header(SHARER_USER_ID, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookItemRequestDto)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Создание бронирования с невалидными данными -> возвращает 400 Bad Request")
    void bookItem_whenInvalidRequest_thenReturnBadRequest() throws Exception {
        BookItemRequestDto invalidRequest = new BookItemRequestDto();

        mockMvc.perform(post("/bookings")
                        .header(SHARER_USER_ID, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Создание бронирования без заголовка X-Sharer-User-Id -> возвращает 400 Bad Request")
    void bookItem_whenMissingUserIdHeader_thenReturnBadRequest() throws Exception {
        mockMvc.perform(post("/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookItemRequestDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Получение бронирования по ID -> возвращает 200 OK")
    void getBooking_whenValidRequest_thenReturnOk() throws Exception {
        when(bookingClient.getBooking(anyLong(), anyLong()))
                .thenReturn(new ResponseEntity<>("{}", HttpStatus.OK));

        mockMvc.perform(get("/bookings/1")
                        .header(SHARER_USER_ID, 1L))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Получение бронирования по ID без заголовка X-Sharer-User-Id -> возвращает 400 Bad Request")
    void getBooking_whenMissingUserIdHeader_thenReturnBadRequest() throws Exception {
        mockMvc.perform(get("/bookings/1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Получение несуществующего бронирования по ID -> возвращает 404 Not Found")
    void getBooking_whenBookingNotFound_thenReturnNotFound() throws Exception {
        when(bookingClient.getBooking(anyLong(), anyLong()))
                .thenReturn(new ResponseEntity<>(HttpStatus.NOT_FOUND));

        mockMvc.perform(get("/bookings/999")
                        .header(SHARER_USER_ID, 1L))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Подтверждение бронирования -> возвращает 200 OK")
    void approveBooking_whenApproved_thenReturnOk() throws Exception {
        when(bookingClient.approveBooking(anyLong(), anyLong(), anyBoolean()))
                .thenReturn(new ResponseEntity<>("{}", HttpStatus.OK));

        mockMvc.perform(patch("/bookings/1")
                        .header(SHARER_USER_ID, 1L)
                        .param("approved", "true"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Отклонение бронирования -> возвращает 200 OK")
    void approveBooking_whenRejected_thenReturnOk() throws Exception {
        when(bookingClient.approveBooking(anyLong(), anyLong(), anyBoolean()))
                .thenReturn(new ResponseEntity<>("{}", HttpStatus.OK));

        mockMvc.perform(patch("/bookings/1")
                        .header(SHARER_USER_ID, 1L)
                        .param("approved", "false"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Подтверждение бронирования без заголовка X-Sharer-User-Id -> возвращает 400 Bad Request")
    void approveBooking_whenMissingUserIdHeader_thenReturnBadRequest() throws Exception {
        mockMvc.perform(patch("/bookings/1")
                        .param("approved", "true"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Подтверждение бронирования без параметра 'approved' -> возвращает 400 Bad Request")
    void approveBooking_whenMissingApprovedParameter_thenReturnBadRequest() throws Exception {
        mockMvc.perform(patch("/bookings/1")
                        .header(SHARER_USER_ID, 1L))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Подтверждение несуществующего бронирования -> возвращает 404 Not Found")
    void approveBooking_whenBookingNotFound_thenReturnNotFound() throws Exception {
        when(bookingClient.approveBooking(anyLong(), anyLong(), anyBoolean()))
                .thenReturn(new ResponseEntity<>(HttpStatus.NOT_FOUND));

        mockMvc.perform(patch("/bookings/999")
                        .header(SHARER_USER_ID, 1L)
                        .param("approved", "true"))
                .andExpect(status().isNotFound());
    }
}