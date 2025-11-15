package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentRequestDto;
import ru.practicum.shareit.item.dto.ItemRequestDto;

@Controller
@RequestMapping(path = "/items")
@RequiredArgsConstructor
@Slf4j
@Validated
public class ItemController {
    private final ItemClient itemClient;
    private static final String SHARER_USER_ID = "X-Sharer-User-Id";

    @PostMapping
    public ResponseEntity<Object> create(
            @RequestHeader(SHARER_USER_ID) Long userId,
            @Valid @RequestBody ItemRequestDto itemDto
    ) {
        log.info("Создание предмета {}, userId={}", itemDto, userId);
        return itemClient.create(itemDto, userId);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<Object> update(
            @RequestHeader(SHARER_USER_ID) Long userId,
            @PathVariable Long itemId,
            @RequestBody ItemRequestDto itemDto
    ) {
        log.info("Обновление предмета {}, itemId={}, userId={}", itemDto, itemId, userId);
        return itemClient.update(itemId, itemDto, userId);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> getById(
            @PathVariable Long itemId,
            @RequestHeader(SHARER_USER_ID) Long userId
    ) {
        log.info("Получение предмета, itemId={}, userId={}", itemId, userId);
        return itemClient.getById(itemId, userId);
    }

    @GetMapping
    public ResponseEntity<Object> getByOwnerId(
            @RequestHeader(SHARER_USER_ID) Long userId
    ) {
        log.info("Получение предметов по владельцу, userId={}", userId);
        return itemClient.getByOwnerId(userId);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> search(
            @RequestParam String text,
            @Min(value = 0, message = "Параметр 'from' не может быть отрицательным")
            @RequestParam(defaultValue = "0") Integer from,
            @Min(value = 1, message = "Параметр 'size' должен быть не менее 1")
            @RequestParam(defaultValue = "10") Integer size
    ) {
        log.info("Поиск предметов, text={}, from={}, size={}", text, from, size);
        return itemClient.search(text, from, size);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> addComment(
            @PathVariable Long itemId,
            @Valid @RequestBody CommentRequestDto commentRequestDto,
            @RequestHeader(SHARER_USER_ID) Long userId
    ) {
        log.info("Добавление комментария {}, userId={}", itemId, userId);
        return itemClient.addComment(itemId, commentRequestDto, userId);
    }
}