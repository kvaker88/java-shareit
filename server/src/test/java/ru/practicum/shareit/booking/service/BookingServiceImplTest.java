package ru.practicum.shareit.booking.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@DataJpaTest
@Import(BookingServiceImpl.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class BookingServiceImplTest {

    @Autowired
    private BookingServiceImpl bookingService;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    @MockBean
    private BookingMapper bookingMapper;

    private User owner;
    private User booker;
    private User anotherUser;
    private Item availableItem;
    private Item unavailableItem;
    private Booking existingBooking;

    @BeforeEach
    void setUp() {
        owner = new User();
        owner.setName("Владелец");
        owner.setEmail("owner@yandex.ru");
        owner = userRepository.save(owner);

        booker = new User();
        booker.setName("Арендатор");
        booker.setEmail("booker@yandex.ru");
        booker = userRepository.save(booker);

        anotherUser = new User();
        anotherUser.setName("Другой пользователь");
        anotherUser.setEmail("another@yandex.ru");
        anotherUser = userRepository.save(anotherUser);

        availableItem = new Item();
        availableItem.setName("Дрель");
        availableItem.setDescription("Мощная дрель");
        availableItem.setAvailable(true);
        availableItem.setOwner(owner.getId());
        availableItem = itemRepository.save(availableItem);

        unavailableItem = new Item();
        unavailableItem.setName("Сломанная дрель");
        unavailableItem.setDescription("Не работает");
        unavailableItem.setAvailable(false);
        unavailableItem.setOwner(owner.getId());
        unavailableItem = itemRepository.save(unavailableItem);

        existingBooking = new Booking();
        existingBooking.setStart(LocalDateTime.now().plusDays(1));
        existingBooking.setEnd(LocalDateTime.now().plusDays(2));
        existingBooking.setItem(availableItem);
        existingBooking.setBooker(booker);
        existingBooking.setStatus(BookingStatus.WAITING);
        existingBooking = bookingRepository.save(existingBooking);

        when(bookingMapper.toBooking(any(BookingRequestDto.class))).thenAnswer(invocation -> {
            BookingRequestDto dto = invocation.getArgument(0);
            Booking booking = new Booking();
            booking.setStart(dto.getStart());
            booking.setEnd(dto.getEnd());
            return booking;
        });

        when(bookingMapper.toBookingResponseDto(any(Booking.class))).thenAnswer(invocation -> {
            Booking booking = invocation.getArgument(0);
            BookingResponseDto dto = new BookingResponseDto();
            dto.setId(booking.getId());
            dto.setStart(booking.getStart());
            dto.setEnd(booking.getEnd());
            dto.setStatus(booking.getStatus());

            BookingResponseDto.Item itemDto = new BookingResponseDto.Item();
            itemDto.setId(booking.getItem().getId());
            itemDto.setName(booking.getItem().getName());
            dto.setItem(itemDto);

            BookingResponseDto.Booker bookerDto = new BookingResponseDto.Booker();
            bookerDto.setId(booking.getBooker().getId());
            bookerDto.setName(booking.getBooker().getName());
            dto.setBooker(bookerDto);

            return dto;
        });
    }

    @Test
    @DisplayName("Создание бронирования с валидными данными -> бронирование создано")
    void createBooking_whenValidData_thenBookingCreated() {
        BookingRequestDto requestDto = new BookingRequestDto();
        requestDto.setItemId(availableItem.getId());
        requestDto.setStart(LocalDateTime.now().plusDays(3));
        requestDto.setEnd(LocalDateTime.now().plusDays(4));

        BookingResponseDto result = bookingService.createBooking(requestDto, booker.getId());

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(BookingStatus.WAITING);
        assertThat(result.getItem().getId()).isEqualTo(availableItem.getId());
        assertThat(result.getBooker().getId()).isEqualTo(booker.getId());

        List<Booking> bookings = bookingRepository.findAll();
        assertThat(bookings).hasSize(2);
    }

    @Test
    @DisplayName("Создание бронирования недоступного предмета -> исключение")
    void createBooking_whenItemNotAvailable_thenThrowException() {
        BookingRequestDto requestDto = new BookingRequestDto();
        requestDto.setItemId(unavailableItem.getId());
        requestDto.setStart(LocalDateTime.now().plusDays(1));
        requestDto.setEnd(LocalDateTime.now().plusDays(2));

        Executable executable = () -> bookingService.createBooking(requestDto, booker.getId());
        assertThrows(IllegalArgumentException.class, executable);
    }

    @Test
    @DisplayName("Создание бронирования собственного предмета -> исключение")
    void createBooking_whenOwnItem_thenThrowException() {
        BookingRequestDto requestDto = new BookingRequestDto();
        requestDto.setItemId(availableItem.getId());
        requestDto.setStart(LocalDateTime.now().plusDays(1));
        requestDto.setEnd(LocalDateTime.now().plusDays(2));

        Executable executable = () -> bookingService.createBooking(requestDto, owner.getId());
        assertThrows(NoSuchElementException.class, executable);
    }

    @Test
    @DisplayName("Создание бронирования несуществующим пользователем -> исключение")
    void createBooking_whenUserNotExists_thenThrowException() {
        BookingRequestDto requestDto = new BookingRequestDto();
        requestDto.setItemId(availableItem.getId());
        requestDto.setStart(LocalDateTime.now().plusDays(1));
        requestDto.setEnd(LocalDateTime.now().plusDays(2));

        assertThrows(NotFoundException.class,
                () -> bookingService.createBooking(requestDto, 999L));
    }

    @Test
    @DisplayName("Создание бронирования несуществующего предмета -> исключение")
    void createBooking_whenItemNotExists_thenThrowException() {
        BookingRequestDto requestDto = new BookingRequestDto();
        requestDto.setItemId(999L);
        requestDto.setStart(LocalDateTime.now().plusDays(1));
        requestDto.setEnd(LocalDateTime.now().plusDays(2));

        Executable executable = () -> bookingService.createBooking(requestDto, booker.getId());
        assertThrows(NotFoundException.class, executable);
    }

    @Test
    @DisplayName("Подтверждение бронирования владельцем -> статус изменен на APPROVED")
    void updateBooking_whenApprovedByOwner_thenStatusApproved() {
        BookingResponseDto result = bookingService.updateBooking(existingBooking.getId(), owner.getId(), true);

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(BookingStatus.APPROVED);

        Booking updatedBooking = bookingRepository.findById(existingBooking.getId()).orElseThrow();
        assertThat(updatedBooking.getStatus()).isEqualTo(BookingStatus.APPROVED);
    }

    @Test
    @DisplayName("Отклонение бронирования владельцем -> статус изменен на REJECTED")
    void updateBooking_whenRejectedByOwner_thenStatusRejected() {
        BookingResponseDto result = bookingService.updateBooking(existingBooking.getId(), owner.getId(), false);

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(BookingStatus.REJECTED);

        Booking updatedBooking = bookingRepository.findById(existingBooking.getId()).orElseThrow();
        assertThat(updatedBooking.getStatus()).isEqualTo(BookingStatus.REJECTED);
    }

    @Test
    @DisplayName("Обновление бронирования не владельцем -> исключение")
    void updateBooking_whenNotOwner_thenThrowException() {
        Executable executable = () -> bookingService
                .updateBooking(existingBooking.getId(), booker.getId(), true);
        assertThrows(AccessDeniedException.class, executable);
    }

    @Test
    @DisplayName("Обновление уже обработанного бронирования -> исключение")
    void updateBooking_whenAlreadyProcessed_thenThrowException() {
        bookingService.updateBooking(existingBooking.getId(), owner.getId(), true);

        Executable executable = () -> bookingService.updateBooking(existingBooking.getId(), owner.getId(), false);
        assertThrows(IllegalArgumentException.class, executable);
    }

    @Test
    @DisplayName("Получение бронирования по ID владельцем -> возвращает бронирование")
    void getBookingById_whenOwner_thenReturnBooking() {
        BookingResponseDto result = bookingService.getBookingById(existingBooking.getId(), owner.getId());

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(existingBooking.getId());
        assertThat(result.getItem().getId()).isEqualTo(availableItem.getId());
        assertThat(result.getBooker().getId()).isEqualTo(booker.getId());
    }

    @Test
    @DisplayName("Получение бронирования по ID арендатором -> возвращает бронирование")
    void getBookingById_whenBooker_thenReturnBooking() {
        BookingResponseDto result = bookingService.getBookingById(existingBooking.getId(), booker.getId());

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(existingBooking.getId());
        assertThat(result.getItem().getId()).isEqualTo(availableItem.getId());
    }

    @Test
    @DisplayName("Получение бронирования по ID посторонним пользователем -> исключение")
    void getBookingById_whenOtherUser_thenThrowException() {
        Executable executable = () -> bookingService.getBookingById(existingBooking.getId(), anotherUser.getId());
        assertThrows(AccessDeniedException.class, executable);
    }

    @Test
    @DisplayName("Получение несуществующего бронирования -> исключение")
    void getBookingById_whenBookingNotExists_thenThrowException() {
        Executable executable = () -> bookingService.getBookingById(999L, owner.getId());
        assertThrows(NotFoundException.class, executable);
    }

    @Test
    @DisplayName("Получение бронирований арендатора со state=ALL -> возвращает все бронирования")
    void getBookingByBookerId_whenStateAll_thenReturnAllBookings() {
        List<BookingResponseDto> result = bookingService.getBookingByBookerId(booker.getId(), "ALL");

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getId()).isEqualTo(existingBooking.getId());
    }

    @Test
    @DisplayName("Получение бронирований арендатора со state=CURRENT -> возвращает текущие бронирования")
    void getBookingByBookerId_whenStateCurrent_thenReturnCurrentBookings() {
        Booking currentBooking = new Booking();
        currentBooking.setStart(LocalDateTime.now().minusDays(1));
        currentBooking.setEnd(LocalDateTime.now().plusDays(1));
        currentBooking.setItem(availableItem);
        currentBooking.setBooker(booker);
        currentBooking.setStatus(BookingStatus.APPROVED);
        bookingRepository.save(currentBooking);

        List<BookingResponseDto> result = bookingService.getBookingByBookerId(booker.getId(), "CURRENT");

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getId()).isEqualTo(currentBooking.getId());
    }

    @Test
    @DisplayName("Получение бронирований арендатора со state=PAST -> возвращает прошедшие бронирования")
    void getBookingByBookerId_whenStatePast_thenReturnPastBookings() {
        Booking pastBooking = new Booking();
        pastBooking.setStart(LocalDateTime.now().minusDays(3));
        pastBooking.setEnd(LocalDateTime.now().minusDays(2));
        pastBooking.setItem(availableItem);
        pastBooking.setBooker(booker);
        pastBooking.setStatus(BookingStatus.APPROVED);
        bookingRepository.save(pastBooking);

        List<BookingResponseDto> result = bookingService.getBookingByBookerId(booker.getId(), "PAST");

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getId()).isEqualTo(pastBooking.getId());
    }

    @Test
    @DisplayName("Получение бронирований арендатора со state=FUTURE -> возвращает будущие бронирования")
    void getBookingByBookerId_whenStateFuture_thenReturnFutureBookings() {
        List<BookingResponseDto> result = bookingService.getBookingByBookerId(booker.getId(), "FUTURE");

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getId()).isEqualTo(existingBooking.getId());
    }

    @Test
    @DisplayName("Получение бронирований арендатора со state=WAITING -> возвращает ожидающие бронирования")
    void getBookingByBookerId_whenStateWaiting_thenReturnWaitingBookings() {
        List<BookingResponseDto> result = bookingService.getBookingByBookerId(booker.getId(), "WAITING");

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getId()).isEqualTo(existingBooking.getId());
    }

    @Test
    @DisplayName("Получение бронирований владельца со state=ALL -> возвращает все бронирования")
    void getBookingByOwnerId_whenStateAll_thenReturnAllBookings() {
        List<BookingResponseDto> result = bookingService.getBookingByOwnerId(owner.getId(), "ALL");

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getId()).isEqualTo(existingBooking.getId());
    }

    @Test
    @DisplayName("Получение бронирований владельца без предметов -> возвращает пустой список")
    void getBookingByOwnerId_whenNoItems_thenReturnEmptyList() {
        List<BookingResponseDto> result = bookingService.getBookingByOwnerId(anotherUser.getId(), "ALL");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Получение бронирований с неверным state -> исключение")
    void getBookingByBookerId_whenInvalidState_thenThrowException() {
        Executable executable = () -> bookingService.getBookingByBookerId(booker.getId(), "INVALID_STATE");
        assertThrows(IllegalArgumentException.class, executable);
    }

    @Test
    @DisplayName("Создание бронирования с пересекающимися датами -> исключение")
    void createBooking_whenOverlappingDates_thenThrowException() {
        Booking overlappingBooking = new Booking();
        overlappingBooking.setStart(LocalDateTime.now().plusDays(1));
        overlappingBooking.setEnd(LocalDateTime.now().plusDays(2));
        overlappingBooking.setItem(availableItem);
        overlappingBooking.setBooker(anotherUser);
        overlappingBooking.setStatus(BookingStatus.APPROVED);
        bookingRepository.save(overlappingBooking);

        BookingRequestDto requestDto = new BookingRequestDto();
        requestDto.setItemId(availableItem.getId());
        requestDto.setStart(LocalDateTime.now().plusDays(1).plusHours(1));
        requestDto.setEnd(LocalDateTime.now().plusDays(2).minusHours(1));

        Executable executable = () -> bookingService.createBooking(requestDto, booker.getId());
        assertThrows(IllegalArgumentException.class, executable);
    }

    @Test
    @DisplayName("Получение бронирований несуществующим арендатором -> исключение")
    void getBookingByBookerId_whenUserNotExists_thenThrowException() {
        assertThrows(NotFoundException.class,
                () -> bookingService.getBookingByBookerId(999L, "ALL"));
    }
}