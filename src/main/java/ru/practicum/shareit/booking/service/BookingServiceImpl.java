package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
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
    private static final String UNKNOWN_STATE = "Неизвестный статус ";

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final BookingMapper bookingMapper;

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

    @Override
    @Transactional
    public BookingResponseDto updateBooking(Long bookingId, Long ownerId, Boolean approved) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NoSuchElementException(BOOKING_NOT_FOUND));

        if (!booking.getItem().getOwner().equals(ownerId)) {
            throw new IllegalArgumentException("Только владелец может обновить статус бронирования");
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
                .orElseThrow(() -> new NoSuchElementException(BOOKING_NOT_FOUND));

        if (!booking.getBooker().getId().equals(userId) &&
                !booking.getItem().getOwner().equals(userId)) {
            throw new NoSuchElementException("Доступ запрещен");
        }

        return bookingMapper.toBookingResponseDto(booking);
    }

    @Override
    public List<BookingResponseDto> getBookingByBookerId(Long bookerId, String state) {
        userRepository.findById(bookerId).orElseThrow(() -> new NoSuchElementException(USER_NOT_FOUND));

        return switch (state.toUpperCase()) {
            case "ALL" -> bookingRepository.findByBookerIdOrderByStartDesc(bookerId).stream()
                    .map(bookingMapper::toBookingResponseDto)
                    .toList();
            case "CURRENT" -> {
                LocalDateTime now = LocalDateTime.now();
                yield bookingRepository.findCurrentBookingsByBooker(bookerId, now).stream()
                        .map(bookingMapper::toBookingResponseDto)
                        .toList();
            }
            case "PAST" -> bookingRepository.findByBookerIdAndEndBeforeOrderByStartDesc(
                            bookerId, LocalDateTime.now()).stream()
                    .map(bookingMapper::toBookingResponseDto)
                    .toList();
            case "FUTURE" -> bookingRepository.findByBookerIdAndStartAfterOrderByStartDesc(
                            bookerId, LocalDateTime.now()).stream()
                    .map(bookingMapper::toBookingResponseDto)
                    .toList();
            case "WAITING", "REJECTED" -> {
                BookingStatus status = BookingStatus.valueOf(state.toUpperCase());
                yield bookingRepository.findByBookerIdAndStatusOrderByStartDesc(bookerId, status).stream()
                        .map(bookingMapper::toBookingResponseDto)
                        .toList();
            }
            default -> throw new IllegalArgumentException(UNKNOWN_STATE + state);
        };
    }

    @Override
    public List<BookingResponseDto> getBookingByOwnerId(Long ownerId, String state) {
        userRepository.findById(ownerId).orElseThrow(() -> new NoSuchElementException(USER_NOT_FOUND));

        // Получаем все itemIds владельца
        List<Long> ownerItemIds = itemRepository.findByOwner(ownerId).stream()
                .map(Item::getId)
                .toList();

        if (ownerItemIds.isEmpty()) {
            return List.of();
        }

        return switch (state.toUpperCase()) {
            case "ALL" -> bookingRepository.findByItemIdInOrderByStartDesc(ownerItemIds).stream()
                    .map(bookingMapper::toBookingResponseDto)
                    .toList();
            case "CURRENT" -> bookingRepository.findCurrentBookingsByItems(ownerItemIds, LocalDateTime.now()).stream()
                    .map(bookingMapper::toBookingResponseDto)
                    .toList();
            case "PAST" ->
                    bookingRepository.findByItemIdInAndEndBeforeOrderByStartDesc(ownerItemIds, LocalDateTime.now()).stream()
                            .map(bookingMapper::toBookingResponseDto)
                            .toList();
            case "FUTURE" ->
                    bookingRepository.findByItemIdInAndStartAfterOrderByStartDesc(ownerItemIds, LocalDateTime.now()).stream()
                            .map(bookingMapper::toBookingResponseDto)
                            .toList();
            case "WAITING", "REJECTED" -> {
                BookingStatus status = BookingStatus.valueOf(state.toUpperCase());
                yield bookingRepository.findByItemIdInAndStatusOrderByStartDesc(ownerItemIds, status).stream()
                        .map(bookingMapper::toBookingResponseDto)
                        .toList();
            }
            default -> throw new IllegalArgumentException(UNKNOWN_STATE + state);
        };
    }
}