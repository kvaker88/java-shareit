package ru.practicum.shareit.booking.dto;

import lombok.Data;
import ru.practicum.shareit.booking.model.BookingStatus;

import java.time.LocalDateTime;

@Data
public class BookingResponseDto {
    private Long id;
    private LocalDateTime start;
    private LocalDateTime end;
    private BookingStatus status;
    private Booker booker;
    private Item item;

    @Data
    public static class Booker {
        private Long id;
        private String name;
    }

    @Data
    public static class Item {
        private Long id;
        private String name;
    }
}