package ru.practicum.shareit.request.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.item.dto.ItemRequestDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ItemRequestResponseDtoJsonTest {

    private JacksonTester<ItemRequestResponseDto> json;

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
        ItemRequestResponseDto dto = new ItemRequestResponseDto();
        dto.setId(1L);
        dto.setDescription("Нужна дрель для ремонта");
        dto.setCreated(LocalDateTime.of(2024, 1, 1, 10, 0));

        ItemRequestDto item1 = new ItemRequestDto();
        item1.setDescription("Мощная дрель");

        ItemRequestDto item2 = new ItemRequestDto();
        item2.setDescription("Аккумуляторная отвертка");

        dto.setItems(List.of(item1, item2));

        JsonContent<ItemRequestResponseDto> result = json.write(dto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.description")
                .isEqualTo("Нужна дрель для ремонта");
        assertThat(result).extractingJsonPathStringValue("$.created")
                .isEqualTo("2024-01-01T10:00:00");
        assertThat(result).extractingJsonPathStringValue("$.items[0].description")
                .isEqualTo("Мощная дрель");
        assertThat(result).extractingJsonPathStringValue("$.items[1].description")
                .isEqualTo("Аккумуляторная отвертка");
    }

    @Test
    void testDeserialize() throws Exception {
        String content = "{" +
                "\"id\": 1," +
                "\"description\": \"Нужна дрель для ремонта\"," +
                "\"created\": \"2024-01-01T10:00:00\"," +
                "\"items\": [" +
                "  {\"description\": \"Мощная дрель\"}," +
                "  {\"description\": \"Аккумуляторная отвертка\"}" +
                "]" +
                "}";

        ItemRequestResponseDto result = json.parseObject(content);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getDescription()).isEqualTo("Нужна дрель для ремонта");
        assertThat(result.getCreated()).isEqualTo(LocalDateTime.of(2024, 1, 1, 10, 0));
        assertThat(result.getItems()).hasSize(2);
        assertThat(result.getItems().get(0).getDescription()).isEqualTo("Мощная дрель");
        assertThat(result.getItems().get(1).getDescription()).isEqualTo("Аккумуляторная отвертка");
    }
}