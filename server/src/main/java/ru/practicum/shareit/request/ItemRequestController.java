package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestResponseDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.util.List;

@RestController
@RequestMapping("/requests")
@RequiredArgsConstructor
@Validated
public class ItemRequestController {

    private final ItemRequestService itemRequestService;

    @PostMapping
    public ItemRequestResponseDto create(
            @RequestBody ItemRequestDto itemRequestDto,
            @RequestHeader("X-Sharer-User-Id") Long requestorId
    ) {
        return itemRequestService.create(itemRequestDto, requestorId);
    }

    @GetMapping
    public List<ItemRequestResponseDto> getByRequestor(
            @RequestHeader("X-Sharer-User-Id") Long requestorId
    ) {
        return itemRequestService.getByRequestor(requestorId);
    }

    @GetMapping("/all")
    public List<ItemRequestResponseDto> getAll(
            @RequestHeader("X-Sharer-User-Id") Long requestorId,
            Integer from,
            Integer size
    ) {
        return itemRequestService.getAll(requestorId, from, size);
    }

    @GetMapping("/{requestId}")
    public ItemRequestResponseDto getById(
            @PathVariable Long requestId,
            @RequestHeader("X-Sharer-User-Id") Long userId
    ) {
        return itemRequestService.getById(requestId, userId);
    }
}