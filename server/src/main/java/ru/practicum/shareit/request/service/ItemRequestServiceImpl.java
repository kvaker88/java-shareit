package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemRequestServiceImpl implements ItemRequestService {
    private static final String USER_NOT_FOUND = "Не удалось найти пользователя с ID ";

    private final ItemRequestRepository itemRequestRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final ItemRequestMapper itemRequestMapper;

    @Override
    @Transactional
    public ItemRequestResponseDto create(ItemRequestDto itemRequestDto, Long requestorId) {
        User requestor = userRepository.findById(requestorId)
                .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND + requestorId));

        ItemRequest itemRequest = itemRequestMapper.toItemRequest(itemRequestDto, requestor);
        ItemRequest savedRequest = itemRequestRepository.save(itemRequest);

        return toItemRequestResponseDto(savedRequest);
    }

    @Override
    public List<ItemRequestResponseDto> getByRequestor(Long requestorId) {
        userRepository.findById(requestorId).orElseThrow(() ->
                new NotFoundException(USER_NOT_FOUND + requestorId));

        List<ItemRequest> requests = itemRequestRepository.findByRequestorIdOrderByCreatedDesc(requestorId);
        return requests.stream()
                .map(this::toItemRequestResponseDto)
                .toList();
    }

    @Override
    public List<ItemRequestResponseDto> getAll(Long requestorId, Integer from, Integer size) {
        userRepository.findById(requestorId).orElseThrow(() ->
                new NotFoundException(USER_NOT_FOUND + requestorId));

        Pageable pageable = PageRequest.of(from / size, size);
        List<ItemRequest> requests = itemRequestRepository.findByRequestorIdNotOrderByCreatedDesc(
                requestorId, pageable);

        return requests.stream()
                .map(this::toItemRequestResponseDto)
                .toList();
    }

    @Override
    public ItemRequestResponseDto getById(Long requestId, Long userId) {
        userRepository.findById(userId).orElseThrow(() -> new NotFoundException(USER_NOT_FOUND + userId));
        ItemRequest itemRequest = itemRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Запрос не найден"));
        return toItemRequestResponseDto(itemRequest);
    }

    private ItemRequestResponseDto toItemRequestResponseDto(ItemRequest itemRequest) {
        List<Item> items = itemRepository.findByRequestId(itemRequest.getId());
        return itemRequestMapper.toItemRequestResponseDto(itemRequest, items);
    }
}