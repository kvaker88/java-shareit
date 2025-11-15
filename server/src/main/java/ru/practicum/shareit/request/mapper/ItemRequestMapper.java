package ru.practicum.shareit.request.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class ItemRequestMapper {

    public ItemRequest toItemRequest(ru.practicum.shareit.request.dto.ItemRequestDto itemRequestDto, User requestor) {
        ItemRequest itemRequest = new ItemRequest();
        itemRequest.setDescription(itemRequestDto.getDescription());
        itemRequest.setRequestor(requestor);
        itemRequest.setCreated(LocalDateTime.now());
        return itemRequest;
    }

    public ItemRequestResponseDto toItemRequestResponseDto(ItemRequest itemRequest, List<Item> items) {
        ItemRequestResponseDto dto = new ItemRequestResponseDto();
        dto.setId(itemRequest.getId());
        dto.setDescription(itemRequest.getDescription());
        dto.setCreated(itemRequest.getCreated());

        List<ru.practicum.shareit.item.dto.ItemRequestDto> itemDto = items.stream()
                .map(this::toItemDto)
                .toList();
        dto.setItems(itemDto);

        return dto;
    }

    private ru.practicum.shareit.item.dto.ItemRequestDto toItemDto(Item item) {
        return new ru.practicum.shareit.item.dto.ItemRequestDto(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getAvailable(),
                item.getRequestId()
        );
    }
}