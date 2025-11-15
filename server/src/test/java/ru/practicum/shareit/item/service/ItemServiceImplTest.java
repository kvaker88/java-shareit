package ru.practicum.shareit.item.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.AccessDeniedException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.comment.dto.CommentRequestDto;
import ru.practicum.shareit.item.comment.dto.CommentResponseDto;
import ru.practicum.shareit.item.comment.model.Comment;
import ru.practicum.shareit.item.comment.repository.CommentRepository;
import ru.practicum.shareit.item.dto.ItemRequestDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@DataJpaTest
@Import(ItemServiceImpl.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ItemServiceImplTest {

    @Autowired
    private ItemServiceImpl itemService;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private CommentRepository commentRepository;

    @MockBean
    private ItemMapper itemMapper;

    private User owner;
    private User booker;
    private User anotherUser;
    private Item existingItem;

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

        existingItem = new Item();
        existingItem.setName("Дрель");
        existingItem.setDescription("Мощная дрель");
        existingItem.setAvailable(true);
        existingItem.setOwner(owner.getId());
        existingItem = itemRepository.save(existingItem);

        when(itemMapper.toItem(any(ItemRequestDto.class))).thenAnswer(invocation -> {
            ItemRequestDto dto = invocation.getArgument(0);
            Item item = new Item();
            item.setName(dto.getName());
            item.setDescription(dto.getDescription());
            item.setAvailable(dto.getAvailable());
            return item;
        });

        when(itemMapper.toItemResponseDto(any(Item.class))).thenAnswer(invocation -> {
            Item item = invocation.getArgument(0);
            ItemResponseDto dto = new ItemResponseDto();
            dto.setId(item.getId());
            dto.setName(item.getName());
            dto.setDescription(item.getDescription());
            dto.setAvailable(item.getAvailable());
            dto.setComments(List.of());
            return dto;
        });
    }

    @Test
    @DisplayName("Создание предмета с валидными данными -> предмет создан")
    void createItem_whenValidData_thenItemCreated() {
        ItemRequestDto requestDto = new ItemRequestDto();
        requestDto.setName("Молоток");
        requestDto.setDescription("Простой молоток");
        requestDto.setAvailable(true);

        ItemResponseDto result = itemService.createItem(owner.getId(), requestDto);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Молоток");
        assertThat(result.getDescription()).isEqualTo("Простой молоток");
        assertThat(result.getAvailable()).isTrue();

        List<Item> items = itemRepository.findAll();
        assertThat(items).hasSize(2);
    }

    @Test
    @DisplayName("Создание предмета несуществующим пользователем -> исключение")
    void createItem_whenUserNotExists_thenThrowException() {
        ItemRequestDto requestDto = new ItemRequestDto();
        requestDto.setName("Молоток");
        requestDto.setDescription("Простой молоток");
        requestDto.setAvailable(true);

        assertThrows(NotFoundException.class,
                () -> itemService.createItem(999L, requestDto));
    }

    @Test
    @DisplayName("Получение предмета по ID владельцем -> возвращает с информацией о бронированиях")
    void getItemById_whenOwner_thenReturnWithBookingInfo() {
        when(itemMapper.toItemResponseDto(any(Item.class))).thenAnswer(invocation -> {
            Item item = invocation.getArgument(0);
            ItemResponseDto dto = new ItemResponseDto();
            dto.setId(item.getId());
            dto.setName(item.getName());
            dto.setDescription(item.getDescription());
            dto.setAvailable(item.getAvailable());
            dto.setComments(List.of());
            return dto;
        });

        Booking pastBooking = new Booking();
        pastBooking.setStart(LocalDateTime.now().minusDays(2));
        pastBooking.setEnd(LocalDateTime.now().minusDays(1));
        pastBooking.setItem(existingItem);
        pastBooking.setBooker(booker);
        pastBooking.setStatus(BookingStatus.APPROVED);
        bookingRepository.save(pastBooking);

        Booking futureBooking = new Booking();
        futureBooking.setStart(LocalDateTime.now().plusDays(1));
        futureBooking.setEnd(LocalDateTime.now().plusDays(2));
        futureBooking.setItem(existingItem);
        futureBooking.setBooker(anotherUser);
        futureBooking.setStatus(BookingStatus.APPROVED);
        bookingRepository.save(futureBooking);

        ItemResponseDto result = itemService.getItemById(existingItem.getId(), owner.getId());

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(existingItem.getId());
        assertThat(result.getName()).isEqualTo("Дрель");
        assertThat(result.getLastBooking()).isNotNull();
        assertThat(result.getNextBooking()).isNotNull();
    }

    @Test
    @DisplayName("Получение предмета по ID не владельцем -> возвращает без информации о бронированиях")
    void getItemById_whenNotOwner_thenReturnWithoutBookingInfo() {
        ItemResponseDto result = itemService.getItemById(existingItem.getId(), booker.getId());

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(existingItem.getId());
        assertThat(result.getName()).isEqualTo("Дрель");
        assertThat(result.getLastBooking()).isNull();
        assertThat(result.getNextBooking()).isNull();
    }

    @Test
    @DisplayName("Получение несуществующего предмета по ID -> исключение")
    void getItemById_whenItemNotExists_thenThrowException() {
        Executable executable = () -> itemService.getItemById(999L, owner.getId());
        assertThrows(NotFoundException.class, executable);
    }

    @Test
    @DisplayName("Получение всех предметов пользователя -> возвращает список")
    void getAllUserItems_whenUserExists_thenReturnItems() {
        Item secondItem = new Item();
        secondItem.setName("Отвертка");
        secondItem.setDescription("Крестовая отвертка");
        secondItem.setAvailable(true);
        secondItem.setOwner(owner.getId());
        itemRepository.save(secondItem);

        List<ItemResponseDto> result = itemService.getAllUserItems(owner.getId());

        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("Получение всех предметов несуществующего пользователя -> исключение")
    void getAllUserItems_whenUserNotExists_thenThrowException() {
        assertThrows(NotFoundException.class,
                () -> itemService.getAllUserItems(999L));
    }

    @Test
    @DisplayName("Поиск предметов по тексту -> возвращает доступные предметы")
    void searchItems_whenValidText_thenReturnAvailableItems() {
        Item unavailableItem = new Item();
        unavailableItem.setName("Сломанная дрель");
        unavailableItem.setDescription("Не работает");
        unavailableItem.setAvailable(false);
        unavailableItem.setOwner(owner.getId());
        itemRepository.save(unavailableItem);

        List<ItemResponseDto> result = itemService.searchItems("дрель", 0, 10);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getName()).isEqualTo("Дрель");
    }

    @Test
    @DisplayName("Поиск предметов по пустому тексту -> возвращает пустой список")
    void searchItems_whenEmptyText_thenReturnEmptyList() {
        List<ItemResponseDto> result = itemService.searchItems("", 0, 10);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Обновление предмета владельцем -> предмет обновлен")
    void updateItem_whenOwner_thenItemUpdated() {
        ItemRequestDto updateDto = new ItemRequestDto();
        updateDto.setName("Новая дрель");
        updateDto.setDescription("Очень мощная дрель");
        updateDto.setAvailable(false);

        ItemResponseDto result = itemService.updateItem(owner.getId(), existingItem.getId(), updateDto);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Новая дрель");
        assertThat(result.getDescription()).isEqualTo("Очень мощная дрель");
        assertThat(result.getAvailable()).isFalse();

        Item updatedItem = itemRepository.findById(existingItem.getId()).orElseThrow();
        assertThat(updatedItem.getName()).isEqualTo("Новая дрель");
        assertThat(updatedItem.getDescription()).isEqualTo("Очень мощная дрель");
        assertThat(updatedItem.getAvailable()).isFalse();
    }

    @Test
    @DisplayName("Обновление предмета не владельцем -> исключение")
    void updateItem_whenNotOwner_thenThrowException() {
        ItemRequestDto updateDto = new ItemRequestDto();
        updateDto.setName("Новая дрель");

        Executable executable = () -> itemService.updateItem(booker.getId(), existingItem.getId(), updateDto);
        assertThrows(AccessDeniedException.class, executable);
    }

    @Test
    @DisplayName("Частичное обновление предмета -> обновлены только указанные поля")
    void updateItem_whenPartialUpdate_thenOnlySpecifiedFieldsUpdated() {
        ItemRequestDto updateDto = new ItemRequestDto();
        updateDto.setName("Новая дрель");

        ItemResponseDto result = itemService.updateItem(owner.getId(), existingItem.getId(), updateDto);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Новая дрель");
        assertThat(result.getDescription()).isEqualTo("Мощная дрель");
        assertThat(result.getAvailable()).isTrue();
    }

    @Test
    @DisplayName("Удаление предмета владельцем -> предмет удален")
    void deleteItem_whenOwner_thenItemDeleted() {
        itemService.deleteItem(owner.getId(), existingItem.getId());

        assertFalse(itemRepository.existsById(existingItem.getId()));
    }

    @Test
    @DisplayName("Удаление предмета не владельцем -> исключение")
    void deleteItem_whenNotOwner_thenThrowException() {

        Executable executable = () -> itemService.deleteItem(booker.getId(), existingItem.getId());
        assertThrows(AccessDeniedException.class, executable);
    }

    @Test
    @DisplayName("Добавление комментария после завершенного бронирования -> комментарий создан")
    void addComment_whenValidBooking_thenCommentCreated() {
        Booking pastBooking = new Booking();
        pastBooking.setStart(LocalDateTime.now().minusDays(2));
        pastBooking.setEnd(LocalDateTime.now().minusDays(1));
        pastBooking.setItem(existingItem);
        pastBooking.setBooker(booker);
        pastBooking.setStatus(BookingStatus.APPROVED);
        bookingRepository.save(pastBooking);

        CommentRequestDto commentDto = new CommentRequestDto();
        commentDto.setText("Отличная дрель!");

        CommentResponseDto result = itemService.addComment(existingItem.getId(), commentDto, booker.getId());

        assertThat(result).isNotNull();
        assertThat(result.getText()).isEqualTo("Отличная дрель!");
        assertThat(result.getAuthorName()).isEqualTo("Арендатор");
        assertThat(result.getCreated()).isNotNull();

        List<Comment> comments = commentRepository.findAll();
        assertThat(comments).hasSize(1);
        assertThat(comments.getFirst().getText()).isEqualTo("Отличная дрель!");
    }

    @Test
    @DisplayName("Добавление комментария без завершенного бронирования -> исключение")
    void addComment_whenNoValidBooking_thenThrowException() {
        CommentRequestDto commentDto = new CommentRequestDto();
        commentDto.setText("Отличная дрель!");

        Executable executable = () -> itemService.addComment(existingItem.getId(), commentDto, booker.getId());
        assertThrows(IllegalArgumentException.class, executable);
    }

    @Test
    @DisplayName("Добавление комментария к несуществующему предмету -> исключение")
    void addComment_whenItemNotExists_thenThrowException() {
        CommentRequestDto commentDto = new CommentRequestDto();
        commentDto.setText("Отличная дрель!");

        Executable executable = () -> itemService.addComment(999L, commentDto, booker.getId());
        assertThrows(NotFoundException.class, executable);
    }

    @Test
    @DisplayName("Добавление комментария несуществующим пользователем -> исключение")
    void addComment_whenUserNotExists_thenThrowException() {
        CommentRequestDto commentDto = new CommentRequestDto();
        commentDto.setText("Отличная дрель!");

        Executable executable = () -> itemService.addComment(existingItem.getId(), commentDto, 999L);
        assertThrows(NotFoundException.class, executable);
    }

    @Test
    @DisplayName("Поиск с пагинацией -> возвращает корректное количество результатов")
    void searchItems_withPagination_thenReturnPaginatedResults() {
        for (int i = 1; i <= 5; i++) {
            Item item = new Item();
            item.setName("Дрель " + i);
            item.setDescription("Мощная дрель модель " + i);
            item.setAvailable(true);
            item.setOwner(owner.getId());
            itemRepository.save(item);
        }

        List<ItemResponseDto> result = itemService.searchItems("дрель", 0, 3);

        assertThat(result).hasSize(3);
    }
}