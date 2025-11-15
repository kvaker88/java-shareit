package ru.practicum.shareit.request.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import({ItemRequestServiceImpl.class, ItemRequestMapper.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ItemRequestServiceImplTest {

    @Autowired
    private ItemRequestServiceImpl itemRequestService;

    @Autowired
    private ItemRequestRepository itemRequestRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    private User requestor;
    private User anotherUser;
    private ItemRequest existingRequest;

    @BeforeEach
    void setUp() {
        requestor = new User();
        requestor.setName("Запросчик");
        requestor.setEmail("requestor@yandex.ru");
        requestor = userRepository.save(requestor);

        anotherUser = new User();
        anotherUser.setName("Другой пользователь");
        anotherUser.setEmail("another@yandex.ru");
        anotherUser = userRepository.save(anotherUser);

        existingRequest = new ItemRequest();
        existingRequest.setDescription("Нужна дрель для ремонта");
        existingRequest.setRequestor(requestor);
        existingRequest.setCreated(LocalDateTime.now().minusDays(1));
        existingRequest = itemRequestRepository.save(existingRequest);

        Item item = new Item();
        item.setName("Дрель");
        item.setDescription("Мощная дрель");
        item.setAvailable(true);
        item.setOwner(anotherUser.getId());
        item.setRequestId(existingRequest.getId());
        itemRepository.save(item);
    }

    @Test
    @DisplayName("Создание запроса с валидными данными -> запрос создан")
    void create_whenValidData_thenRequestCreated() {
        ItemRequestDto requestDto = new ItemRequestDto();
        requestDto.setDescription("Нужен молоток");

        ItemRequestResponseDto result = itemRequestService.create(requestDto, requestor.getId());

        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getDescription()).isEqualTo("Нужен молоток");
        assertThat(result.getCreated()).isNotNull();
        assertThat(result.getItems()).isEmpty();

        assertTrue(itemRequestRepository.findById(result.getId()).isPresent());
    }

    @Test
    @DisplayName("Создание запроса несуществующим пользователем -> исключение")
    void create_whenUserNotExists_thenThrowException() {
        ItemRequestDto requestDto = new ItemRequestDto();
        requestDto.setDescription("Нужен молоток");

        assertThrows(NotFoundException.class,
                () -> itemRequestService.create(requestDto, 999L));
    }

    @Test
    @DisplayName("Получение запросов пользователя -> возвращает список запросов")
    void getByRequestor_whenUserExists_thenReturnRequests() {
        List<ItemRequestResponseDto> result = itemRequestService.getByRequestor(requestor.getId());

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getDescription()).isEqualTo("Нужна дрель для ремонта");
        assertThat(result.getFirst().getItems()).hasSize(1);
        assertThat(result.getFirst().getItems().getFirst().getName()).isEqualTo("Дрель");
    }

    @Test
    @DisplayName("Получение запросов несуществующим пользователем -> исключение")
    void getByRequestor_whenUserNotExists_thenThrowException() {
        assertThrows(NotFoundException.class,
                () -> itemRequestService.getByRequestor(999L));
    }

    @Test
    @DisplayName("Получение всех запросов кроме своих -> возвращает пустой список когда нет других запросов")
    void getAll_whenNoOtherRequests_thenReturnEmptyList() {
        List<ItemRequestResponseDto> result = itemRequestService.getAll(requestor.getId(), 0, 10);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Получение всех запросов с пагинацией -> возвращает запросы других пользователей")
    void getAll_whenOtherRequestsExist_thenReturnOtherRequests() {
        ItemRequest otherRequest = new ItemRequest();
        otherRequest.setDescription("Нужна отвертка");
        otherRequest.setRequestor(anotherUser);
        otherRequest.setCreated(LocalDateTime.now());
        itemRequestRepository.save(otherRequest);

        List<ItemRequestResponseDto> result = itemRequestService.getAll(requestor.getId(), 0, 10);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getDescription()).isEqualTo("Нужна отвертка");
        assertThat(result.getFirst().getItems()).isEmpty();
    }

    @Test
    @DisplayName("Получение всех запросов несуществующим пользователем -> исключение")
    void getAll_whenUserNotExists_thenThrowException() {
        assertThrows(NotFoundException.class,
                () -> itemRequestService.getAll(999L, 0, 10));
    }

    @Test
    @DisplayName("Получение запроса по ID -> возвращает запрос с предметами")
    void getById_whenRequestExists_thenReturnRequestWithItems() {
        ItemRequestResponseDto result = itemRequestService.getById(existingRequest.getId(), requestor.getId());

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(existingRequest.getId());
        assertThat(result.getDescription()).isEqualTo("Нужна дрель для ремонта");
        assertThat(result.getItems()).hasSize(1);
        assertThat(result.getItems().getFirst().getName()).isEqualTo("Дрель");
        assertThat(result.getItems().getFirst().getDescription()).isEqualTo("Мощная дрель");
    }

    @Test
    @DisplayName("Получение запроса по ID несуществующим пользователем -> исключение")
    void getById_whenUserNotExists_thenThrowException() {
        Executable executable = () -> itemRequestService.getById(existingRequest.getId(), 999L);
                assertThrows(NotFoundException.class, executable);
    }

    @Test
    @DisplayName("Получение несуществующего запроса по ID -> исключение")
    void getById_whenRequestNotExists_thenThrowException() {
        Executable executable = () -> itemRequestService.getById(999L, requestor.getId());
        assertThrows(NotFoundException.class, executable);
    }

    @Test
    @DisplayName("Проверка пагинации при получении всех запросов")
    void getAll_withPagination_thenReturnPaginatedResults() {
        for (int i = 1; i <= 5; i++) {
            ItemRequest request = new ItemRequest();
            request.setDescription("Запрос " + i);
            request.setRequestor(anotherUser);
            request.setCreated(LocalDateTime.now().minusHours(i));
            itemRequestRepository.save(request);
        }

        List<ItemRequestResponseDto> result = itemRequestService.getAll(requestor.getId(), 0, 2);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getDescription()).isEqualTo("Запрос 1");
        assertThat(result.get(1).getDescription()).isEqualTo("Запрос 2");
    }

    @Test
    @DisplayName("Создание нескольких запросов и проверка порядка при получении")
    void getByRequestor_whenMultipleRequests_thenReturnInCorrectOrder() {
        ItemRequest newRequest = new ItemRequest();
        newRequest.setDescription("Новый запрос");
        newRequest.setRequestor(requestor);
        newRequest.setCreated(LocalDateTime.now());
        itemRequestRepository.save(newRequest);

        List<ItemRequestResponseDto> result = itemRequestService.getByRequestor(requestor.getId());

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getDescription()).isEqualTo("Новый запрос");
        assertThat(result.get(1).getDescription()).isEqualTo("Нужна дрель для ремонта");
    }
}