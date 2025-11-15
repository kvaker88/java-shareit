package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.AccessDeniedException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingServiceImpl implements BookingService {
    private static final String BOOKING_NOT_FOUND = "Не удалось найти бронирование";
    private static final String USER_NOT_FOUND = "Не удалось найти пользователя";
    private static final String ITEM_NOT_FOUND = "Не удалось найти предмет";
    private static final String ITEM_NOT_AVAILABLE = "Предмет недоступен";

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final BookingMapper bookingMapper;

    private static final Sort NEWEST_FIRST = Sort.by(Sort.Direction.DESC, "start");

    @Override
    @Transactional
    public BookingResponseDto createBooking(BookingRequestDto bookingRequestDto, Long bookerId) {
        User booker = userRepository.findById(bookerId)
                .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND));

        Item item = itemRepository.findById(bookingRequestDto.getItemId())
                .orElseThrow(() -> new NotFoundException(ITEM_NOT_FOUND));

        if (!item.getAvailable()) {
            throw new IllegalArgumentException(ITEM_NOT_AVAILABLE);
        }

        if (item.getOwner().equals(bookerId)) {
            throw new NoSuchElementException("Владелец не может забронировать собственный товар");
        }

        if (bookingRepository.existsOverlappingBookings(item.getId(),
                bookingRequestDto.getStart(), bookingRequestDto.getEnd())) {
            throw new IllegalArgumentException("Товар уже забронирован на указанный период");
        }

        Booking booking = bookingMapper.toBooking(bookingRequestDto);
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(BookingStatus.WAITING);

        Booking savedBooking = bookingRepository.save(booking);
        return bookingMapper.toBookingResponseDto(savedBooking);
    }

    @Transactional
    public BookingResponseDto updateBooking(Long bookingId, Long ownerId, boolean approved) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException(BOOKING_NOT_FOUND));

        if (!booking.getItem().getOwner().equals(ownerId)) {
            throw new AccessDeniedException("Только владелец может обновить статус бронирования");
        }

        if (!userRepository.existsById(ownerId)) {
            throw new NotFoundException(USER_NOT_FOUND);
        }

        if (booking.getStatus() != BookingStatus.WAITING) {
            throw new IllegalArgumentException("Статус бронирования уже определен");
        }

        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        Booking updatedBooking = bookingRepository.save(booking);
        return bookingMapper.toBookingResponseDto(updatedBooking);
    }

    @Override
    public BookingResponseDto getBookingById(Long bookingId, Long userId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException(BOOKING_NOT_FOUND));

        if (!booking.getBooker().getId().equals(userId) &&
                !booking.getItem().getOwner().equals(userId)) {
            throw new AccessDeniedException("Доступ запрещен");
        }

        return bookingMapper.toBookingResponseDto(booking);
    }

    public List<BookingResponseDto> getBookingByBookerId(Long bookerId, String state) {
        userRepository.findById(bookerId).orElseThrow(() -> new NotFoundException(USER_NOT_FOUND));

        BookingState bookingState = parseBookingState(state);

        return switch (bookingState) {
            case ALL -> bookingRepository.findByBookerId(bookerId, NEWEST_FIRST).stream()
                    .map(bookingMapper::toBookingResponseDto)
                    .toList();
            case CURRENT -> {
                LocalDateTime now = LocalDateTime.now();
                yield bookingRepository.findCurrentBookingsByBooker(bookerId, now, NEWEST_FIRST).stream()
                        .map(bookingMapper::toBookingResponseDto)
                        .toList();
            }
            case PAST -> bookingRepository.findByBookerIdAndEndBefore(
                            bookerId, LocalDateTime.now(), NEWEST_FIRST).stream()
                    .map(bookingMapper::toBookingResponseDto)
                    .toList();
            case FUTURE -> bookingRepository.findByBookerIdAndStartAfter(
                            bookerId, LocalDateTime.now(), NEWEST_FIRST).stream()
                    .map(bookingMapper::toBookingResponseDto)
                    .toList();
            case WAITING, REJECTED -> {
                BookingStatus status = BookingStatus.valueOf(bookingState.name());
                yield bookingRepository.findByBookerIdAndStatus(bookerId, status, NEWEST_FIRST).stream()
                        .map(bookingMapper::toBookingResponseDto)
                        .toList();
            }
        };
    }

    @Override
    public List<BookingResponseDto> getBookingByOwnerId(Long ownerId, String state) {
        userRepository.findById(ownerId).orElseThrow(() -> new NotFoundException(USER_NOT_FOUND));

        BookingState bookingState = parseBookingState(state);

        List<Long> ownerItemIds = itemRepository.findByOwner(ownerId).stream()
                .map(Item::getId)
                .toList();

        if (ownerItemIds.isEmpty()) {
            return List.of();
        }

        return switch (bookingState) {
            case ALL -> bookingRepository.findByItemIdIn(ownerItemIds, NEWEST_FIRST).stream()
                    .map(bookingMapper::toBookingResponseDto)
                    .toList();
            case CURRENT -> bookingRepository.findCurrentBookingsByItems(
                            ownerItemIds, LocalDateTime.now(), NEWEST_FIRST).stream()
                    .map(bookingMapper::toBookingResponseDto)
                    .toList();
            case PAST -> bookingRepository.findByItemIdInAndEndBefore(
                            ownerItemIds, LocalDateTime.now(), NEWEST_FIRST).stream()
                    .map(bookingMapper::toBookingResponseDto)
                    .toList();
            case FUTURE -> bookingRepository.findByItemIdInAndStartAfter(
                            ownerItemIds, LocalDateTime.now(), NEWEST_FIRST).stream()
                    .map(bookingMapper::toBookingResponseDto)
                    .toList();
            case WAITING, REJECTED -> {
                BookingStatus status = BookingStatus.valueOf(bookingState.name());
                yield bookingRepository.findByItemIdInAndStatus(ownerItemIds, status, NEWEST_FIRST).stream()
                        .map(bookingMapper::toBookingResponseDto)
                        .toList();
            }
        };
    }

    private BookingState parseBookingState(String state) {
        try {
            return BookingState.valueOf(state.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Неизвестное состояние: " + state);
        }
    }
}