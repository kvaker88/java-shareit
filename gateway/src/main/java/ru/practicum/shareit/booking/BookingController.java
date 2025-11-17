package ru.practicum.shareit.booking;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookItemRequestDto;

@Controller
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
@Slf4j
@Validated
public class BookingController {
    private static final String SHARER_USER_ID = "X-Sharer-User-Id";

    private final BookingClient bookingClient;

    @GetMapping
    public ResponseEntity<Object> getBookings(
            @NotNull @RequestHeader(SHARER_USER_ID) long userId,
            @RequestParam(name = "state", defaultValue = "all") String stateParam,
            @Min(value = 0, message = "Параметр 'from' не может быть отрицательным")
            @RequestParam(name = "from", defaultValue = "0") Integer from,
            @Min(value = 1, message = "Параметр 'size' должен быть не менее 1")
            @RequestParam(name = "size", defaultValue = "10") Integer size
    ) {
        BookingState state = BookingState.from(stateParam)
                .orElseThrow(() -> new IllegalArgumentException("Unknown state: " + stateParam));
        log.info("Получение бронирования {}, userId={}, from={}, size={}", stateParam, userId, from, size);
        return bookingClient.getBookings(userId, state, from, size);
    }

    @GetMapping("/owner")
    public ResponseEntity<Object> getByOwnerId(
            @NotNull @RequestHeader(SHARER_USER_ID) long userId,
            @RequestParam(name = "state", defaultValue = "all") String stateParam,
            @Min(value = 0, message = "Параметр 'from' не может быть отрицательным")
            @RequestParam(name = "from", defaultValue = "0") Integer from,
            @Min(value = 1, message = "Параметр 'size' должен быть не менее 1")
            @RequestParam(name = "size", defaultValue = "10") Integer size
    ) {
        BookingState state = BookingState.from(stateParam)
                .orElseThrow(() -> new IllegalArgumentException("Неизвестное состояние: " + stateParam));
        log.info("Получение бронирования по владельцу {}, userId={}, from={}, size={}", stateParam, userId, from, size);
        return bookingClient.getBookingsByOwner(userId, state, from, size);
    }

    @PostMapping
    public ResponseEntity<Object> bookItem(
            @NotNull @RequestHeader(SHARER_USER_ID) long userId,
            @RequestBody @Valid BookItemRequestDto requestDto
    ) {
        log.info("Создание бронирования {}, userId={}", requestDto, userId);
        return bookingClient.bookItem(userId, requestDto);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<Object> getBooking(
            @NotNull @RequestHeader(SHARER_USER_ID) long userId,
            @PathVariable Long bookingId
    ) {
        log.info("Получение бронирования по ID {}, userId={}", bookingId, userId);
        return bookingClient.getBooking(userId, bookingId);
    }

    @PatchMapping("/{bookingId}")
    public ResponseEntity<Object> approveBooking(
            @RequestHeader(SHARER_USER_ID) long userId,
            @PathVariable Long bookingId,
            @RequestParam boolean approved
    ) {
        log.info("Подтверждение бронирования {}, userId={}, approved={}", bookingId, userId, approved);
        return bookingClient.approveBooking(userId, bookingId, approved);
    }
}