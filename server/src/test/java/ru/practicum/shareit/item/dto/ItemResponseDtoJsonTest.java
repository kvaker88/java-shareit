package ru.practicum.shareit.item.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.item.comment.dto.CommentResponseDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ItemResponseDtoJsonTest {

    private JacksonTester<ItemResponseDto> json;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        JacksonTester.initFields(this, objectMapper);
    }

    private ItemResponseDto createTestDto() {
        ItemResponseDto dto = new ItemResponseDto();
        dto.setId(1L);
        dto.setName("Дрель");
        dto.setDescription("Мощная дрель для ремонта");
        dto.setAvailable(true);
        dto.setOwnerId(2L);
        dto.setRequestId(3L);

        ItemResponseDto.BookingInfo lastBooking = new ItemResponseDto.BookingInfo();
        lastBooking.setId(4L);
        lastBooking.setBookerId(5L);
        dto.setLastBooking(lastBooking);

        ItemResponseDto.BookingInfo nextBooking = new ItemResponseDto.BookingInfo();
        nextBooking.setId(6L);
        nextBooking.setBookerId(7L);
        dto.setNextBooking(nextBooking);

        CommentResponseDto comment = new CommentResponseDto();
        comment.setId(8L);
        comment.setText("Отличная дрель!");
        comment.setAuthorName("Петр");
        comment.setCreated(LocalDateTime.of(2024, 1, 1, 10, 0));
        dto.setComments(List.of(comment));

        return dto;
    }

    @Test
    void testSerializeBasicFields() throws Exception {
        ItemResponseDto dto = createTestDto();

        JsonContent<ItemResponseDto> result = json.write(dto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo("Дрель");
        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo("Мощная дрель для ремонта");
        assertThat(result).extractingJsonPathBooleanValue("$.available").isTrue();
        assertThat(result).extractingJsonPathNumberValue("$.ownerId").isEqualTo(2);
        assertThat(result).extractingJsonPathNumberValue("$.requestId").isEqualTo(3);
    }

    @Test
    void testSerializeBookingInfo() throws Exception {
        ItemResponseDto dto = createTestDto();

        JsonContent<ItemResponseDto> result = json.write(dto);

        assertThat(result).extractingJsonPathNumberValue("$.lastBooking.id").isEqualTo(4);
        assertThat(result).extractingJsonPathNumberValue("$.lastBooking.bookerId").isEqualTo(5);
        assertThat(result).extractingJsonPathNumberValue("$.nextBooking.id").isEqualTo(6);
        assertThat(result).extractingJsonPathNumberValue("$.nextBooking.bookerId").isEqualTo(7);
    }

    @Test
    void testSerializeComments() throws Exception {
        ItemResponseDto dto = createTestDto();

        JsonContent<ItemResponseDto> result = json.write(dto);

        assertThat(result).extractingJsonPathNumberValue("$.comments[0].id").isEqualTo(8);
        assertThat(result).extractingJsonPathStringValue("$.comments[0].text").isEqualTo("Отличная дрель!");
        assertThat(result).extractingJsonPathStringValue("$.comments[0].authorName").isEqualTo("Петр");
        assertThat(result).extractingJsonPathStringValue("$.comments[0].created").isEqualTo("2024-01-01T10:00:00");
    }

    @Test
    void testDeserializeBasicFields() throws Exception {
        String content = "{" +
                "\"id\": 1," +
                "\"name\": \"Дрель\"," +
                "\"description\": \"Мощная дрель для ремонта\"," +
                "\"available\": true," +
                "\"ownerId\": 2," +
                "\"requestId\": 3" +
                "}";

        ItemResponseDto result = json.parseObject(content);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Дрель");
        assertThat(result.getDescription()).isEqualTo("Мощная дрель для ремонта");
        assertThat(result.getAvailable()).isTrue();
        assertThat(result.getOwnerId()).isEqualTo(2L);
        assertThat(result.getRequestId()).isEqualTo(3L);
    }

    @Test
    void testDeserializeWithBookings() throws Exception {
        String content = "{" +
                "\"id\": 1," +
                "\"name\": \"Дрель\"," +
                "\"lastBooking\": {" +
                "  \"id\": 4," +
                "  \"bookerId\": 5" +
                "}," +
                "\"nextBooking\": {" +
                "  \"id\": 6," +
                "  \"bookerId\": 7" +
                "}" +
                "}";

        ItemResponseDto result = json.parseObject(content);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Дрель");
        assertThat(result.getLastBooking().getId()).isEqualTo(4L);
        assertThat(result.getLastBooking().getBookerId()).isEqualTo(5L);
        assertThat(result.getNextBooking().getId()).isEqualTo(6L);
        assertThat(result.getNextBooking().getBookerId()).isEqualTo(7L);
    }

    @Test
    void testDeserializeWithComments() throws Exception {
        String content = "{" +
                "\"id\": 1," +
                "\"comments\": [" +
                "  {" +
                "    \"id\": 8," +
                "    \"text\": \"Отличная дрель!\"," +
                "    \"authorName\": \"Петр\"," +
                "    \"created\": \"2024-01-01T10:00:00\"" +
                "  }" +
                "]" +
                "}";

        ItemResponseDto result = json.parseObject(content);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getComments()).hasSize(1);
        assertThat(result.getComments().getFirst().getId()).isEqualTo(8L);
        assertThat(result.getComments().getFirst().getText()).isEqualTo("Отличная дрель!");
        assertThat(result.getComments().getFirst().getAuthorName()).isEqualTo("Петр");
        assertThat(result.getComments().getFirst().getCreated()).isEqualTo(LocalDateTime.of(2024, 1, 1, 10, 0));
    }

    @Test
    void testDeserializeFullObject() throws Exception {
        String content = "{" +
                "\"id\": 1," +
                "\"name\": \"Дрель\"," +
                "\"description\": \"Мощная дрель для ремонта\"," +
                "\"available\": true," +
                "\"ownerId\": 2," +
                "\"requestId\": 3," +
                "\"lastBooking\": {" +
                "  \"id\": 4," +
                "  \"bookerId\": 5" +
                "}," +
                "\"nextBooking\": {" +
                "  \"id\": 6," +
                "  \"bookerId\": 7" +
                "}," +
                "\"comments\": [" +
                "  {" +
                "    \"id\": 8," +
                "    \"text\": \"Отличная дрель!\"," +
                "    \"authorName\": \"Петр\"," +
                "    \"created\": \"2024-01-01T10:00:00\"" +
                "  }" +
                "]" +
                "}";

        ItemResponseDto result = json.parseObject(content);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Дрель");
        assertThat(result.getDescription()).isEqualTo("Мощная дрель для ремонта");
        assertThat(result.getAvailable()).isTrue();
        assertThat(result.getOwnerId()).isEqualTo(2L);
        assertThat(result.getRequestId()).isEqualTo(3L);
        assertThat(result.getLastBooking().getId()).isEqualTo(4L);
        assertThat(result.getLastBooking().getBookerId()).isEqualTo(5L);
        assertThat(result.getNextBooking().getId()).isEqualTo(6L);
        assertThat(result.getNextBooking().getBookerId()).isEqualTo(7L);
        assertThat(result.getComments()).hasSize(1);
        assertThat(result.getComments().getFirst().getId()).isEqualTo(8L);
        assertThat(result.getComments().getFirst().getText()).isEqualTo("Отличная дрель!");
        assertThat(result.getComments().getFirst().getAuthorName()).isEqualTo("Петр");
        assertThat(result.getComments().getFirst().getCreated()).isEqualTo(LocalDateTime.of(2024, 1, 1, 10, 0));
    }
}