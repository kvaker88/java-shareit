package ru.practicum.shareit.booking;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.service.BookingService;

import java.util.List;

@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;
    private static final String SHARER_USER_ID = "X-Sharer-User-Id";

    @PostMapping
    public BookingResponseDto createBooking(
            @Valid @RequestBody BookingRequestDto bookingRequestDto,
            @RequestHeader(SHARER_USER_ID) Long userId
    ) {
        return bookingService.createBooking(bookingRequestDto, userId);
    }

    @PatchMapping("/{bookingId}")
    public BookingResponseDto updateBooking(
            @PathVariable Long bookingId,
            @RequestParam Boolean approved,
            @RequestHeader(SHARER_USER_ID) Long userId
    ) {
        return bookingService.updateBooking(bookingId, userId, approved);
    }

    @GetMapping("/{bookingId}")
    public BookingResponseDto getBookingById(
            @PathVariable Long bookingId,
            @RequestHeader(SHARER_USER_ID) Long userId
    ) {
        return bookingService.getBookingById(bookingId, userId);
    }

    @GetMapping
    public List<BookingResponseDto> getBookingByBookerId(
            @RequestHeader(SHARER_USER_ID) Long userId,
            @RequestParam(defaultValue = "ALL") String state
    ) {
        return bookingService.getBookingByBookerId(userId, state);
    }

    @GetMapping("/owner")
    public List<BookingResponseDto> getBookingByOwnerId(
            @RequestHeader(SHARER_USER_ID) Long userId,
            @RequestParam(defaultValue = "ALL") String state
    ) {
        return bookingService.getBookingByOwnerId(userId, state);
    }
}