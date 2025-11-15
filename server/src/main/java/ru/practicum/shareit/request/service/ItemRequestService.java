package ru.practicum.shareit.request.service;

import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;

import java.util.List;

public interface ItemRequestService {

    ItemRequestResponseDto create(ItemRequestDto itemRequestDto, Long requestorId);

    List<ItemRequestResponseDto> getByRequestor(Long requestorId);

    List<ItemRequestResponseDto> getAll(Long requestorId, Integer from, Integer size);

    ItemRequestResponseDto getById(Long requestId, Long userId);

}