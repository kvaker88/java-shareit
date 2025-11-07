package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;

import java.util.List;

public interface BookingService {

    BookingResponseDto createBooking(BookingRequestDto bookingRequestDto, Long bookerId);

    BookingResponseDto updateBooking(Long bookingId, Long ownerId, Boolean approved);

    BookingResponseDto getBookingById(Long bookingId, Long userId);

    List<BookingResponseDto> getBookingByBookerId(Long bookerId, String state);

    List<BookingResponseDto> getBookingByOwnerId(Long ownerId, String state);

}