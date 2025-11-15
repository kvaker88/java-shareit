package ru.practicum.shareit.booking.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.booking.model.BookingStatus;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class BookingResponseDtoJsonTest {

    private JacksonTester<BookingResponseDto> json;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper;
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        JacksonTester.initFields(this, objectMapper);
    }

    @Test
    void testSerialize() throws Exception {
        BookingResponseDto dto = getBookingResponseDto();

        JsonContent<BookingResponseDto> result = json.write(dto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.start").isEqualTo("2024-01-01T10:00:00");
        assertThat(result).extractingJsonPathStringValue("$.end").isEqualTo("2024-01-02T10:00:00");
        assertThat(result).extractingJsonPathStringValue("$.status").isEqualTo("WAITING");
        assertThat(result).extractingJsonPathNumberValue("$.booker.id").isEqualTo(2);
        assertThat(result).extractingJsonPathStringValue("$.booker.name").isEqualTo("Иван Иванов");
        assertThat(result).extractingJsonPathNumberValue("$.item.id").isEqualTo(3);
        assertThat(result).extractingJsonPathStringValue("$.item.name").isEqualTo("Дрель");
    }

    private static BookingResponseDto getBookingResponseDto() {
        BookingResponseDto dto = new BookingResponseDto();
        dto.setId(1L);
        dto.setStart(LocalDateTime.of(2024, 1, 1, 10, 0));
        dto.setEnd(LocalDateTime.of(2024, 1, 2, 10, 0));
        dto.setStatus(BookingStatus.WAITING);

        BookingResponseDto.Booker booker = new BookingResponseDto.Booker();
        booker.setId(2L);
        booker.setName("Иван Иванов");
        dto.setBooker(booker);

        BookingResponseDto.Item item = new BookingResponseDto.Item();
        item.setId(3L);
        item.setName("Дрель");
        dto.setItem(item);
        return dto;
    }

    @Test
    void testDeserialize() throws Exception {
        String content = "{" +
                "\"id\": 1," +
                "\"start\": \"2024-01-01T10:00:00\"," +
                "\"end\": \"2024-01-02T10:00:00\"," +
                "\"status\": \"WAITING\"," +
                "\"booker\": {" +
                "  \"id\": 2," +
                "  \"name\": \"Иван Иванов\"" +
                "}," +
                "\"item\": {" +
                "  \"id\": 3," +
                "  \"name\": \"Дрель\"" +
                "}" +
                "}";

        BookingResponseDto result = json.parseObject(content);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getStart()).isEqualTo(LocalDateTime.of(2024, 1, 1, 10, 0));
        assertThat(result.getEnd()).isEqualTo(LocalDateTime.of(2024, 1, 2, 10, 0));
        assertThat(result.getStatus()).isEqualTo(BookingStatus.WAITING);
        assertThat(result.getBooker().getId()).isEqualTo(2L);
        assertThat(result.getBooker().getName()).isEqualTo("Иван Иванов");
        assertThat(result.getItem().getId()).isEqualTo(3L);
        assertThat(result.getItem().getName()).isEqualTo("Дрель");
    }
}