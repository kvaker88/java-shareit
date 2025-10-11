package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;

import java.util.List;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
@Slf4j
public class ItemController {
    private static final String SHARER_USER_ID = "X-Sharer-User-Id";

    private final ItemService itemService;

    @PostMapping
    public ResponseEntity<ItemDto> createItem(
            @RequestHeader(SHARER_USER_ID) Long userId,
            @Valid @RequestBody ItemDto itemDto
    ) {
        log.info("Запрос на создание предмета пользователем {}: {}", userId, itemDto);
        ItemDto item = itemService.createItem(userId, itemDto);
        return ResponseEntity.ok(item);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<ItemDto> getItemById(
            @PathVariable Long itemId
    ) {
        log.info("Запрос на получение предмета по ID {}", itemId);
        ItemDto item = itemService.getItemById(itemId);
        return ResponseEntity.ok(item);
    }

    @GetMapping
    public ResponseEntity<List<ItemDto>> getAllUserItems(
            @RequestHeader(SHARER_USER_ID) Long userId
    ) {
        log.info("Запрос на получение всех предметов пользователя {}", userId);
        List<ItemDto> items = itemService.getAllUserItems(userId);
        return ResponseEntity.ok(items);
    }

    @GetMapping("/search")
    public ResponseEntity<List<ItemDto>> searchItems(
            @RequestParam String text,
            @RequestParam(defaultValue = "0") Integer from,
            @RequestParam(defaultValue = "10") Integer size
    ) {
        log.info("Запрос на поиск предметов по тексту - {}", text);
        List<ItemDto> items = itemService.searchItems(text, from, size);
        return ResponseEntity.ok(items);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<ItemDto> updateItem(
            @RequestHeader(SHARER_USER_ID) Long userId,
            @PathVariable Long itemId,
            @RequestBody ItemDto itemDto
    ) {
        log.info("Запрос обновления предмета пользователем {}: {}", userId, itemDto);
        ItemDto item = itemService.updateItem(userId, itemId, itemDto);
        return ResponseEntity.ok(item);
    }

    @DeleteMapping("/{itemId}")
    public ResponseEntity<Void> deleteItem(
            @RequestHeader(SHARER_USER_ID) Long userId,
            @PathVariable Long itemId
    ) {
        log.info("Запрос удаления предмета пользователем {}: {}", userId, itemId);
        itemService.deleteItem(userId, itemId);
        return ResponseEntity.noContent().build();
    }
}