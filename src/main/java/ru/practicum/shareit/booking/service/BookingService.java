package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.model.BookingState;

import java.util.List;

public interface BookingService {

    BookingResponseDto createBooking(BookingRequestDto bookingRequestDto, Long bookerId);

    BookingResponseDto updateBooking(Long bookingId, Long ownerId, boolean approved);

    BookingResponseDto getBookingById(Long bookingId, Long userId);

    List<BookingResponseDto> getBookingByBookerId(Long bookerId, BookingState state);

    List<BookingResponseDto> getBookingByOwnerId(Long ownerId, BookingState state);

}