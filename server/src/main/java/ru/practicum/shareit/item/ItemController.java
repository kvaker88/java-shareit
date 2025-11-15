package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.comment.dto.CommentRequestDto;
import ru.practicum.shareit.item.comment.dto.CommentResponseDto;
import ru.practicum.shareit.item.dto.ItemRequestDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;
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
    public ResponseEntity<ItemResponseDto> createItem(
            @RequestHeader(SHARER_USER_ID) Long userId,
            @RequestBody ItemRequestDto itemDto
    ) {
        log.info("Запрос на создание предмета пользователем {}: {}", userId, itemDto);
        ItemResponseDto item = itemService.createItem(userId, itemDto);
        return ResponseEntity.ok(item);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<ItemResponseDto> getItemById(
            @PathVariable Long itemId,
            @RequestHeader(SHARER_USER_ID) Long userId
    ) {
        log.info("Запрос на получение предмета по ID {}", itemId);
        ItemResponseDto item = itemService.getItemById(itemId, userId);
        return ResponseEntity.ok(item);
    }

    @GetMapping
    public ResponseEntity<List<ItemResponseDto>> getAllUserItems(
            @RequestHeader(SHARER_USER_ID) Long userId
    ) {
        log.info("Запрос на получение всех предметов пользователя {}", userId);
        List<ItemResponseDto> items = itemService.getAllUserItems(userId);
        return ResponseEntity.ok(items);
    }

    @GetMapping("/search")
    public ResponseEntity<List<ItemResponseDto>> searchItems(
            @RequestParam String text,
            @RequestParam Integer from,
            @RequestParam Integer size
    ) {
        log.info("Запрос на поиск предметов по тексту - {}", text);
        List<ItemResponseDto> items = itemService.searchItems(text, from, size);
        return ResponseEntity.ok(items);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<ItemResponseDto> updateItem(
            @RequestHeader(SHARER_USER_ID) Long userId,
            @PathVariable Long itemId,
            @RequestBody ItemRequestDto itemDto
    ) {
        log.info("Запрос обновления предмета пользователем {}: {}", userId, itemDto);
        ItemResponseDto item = itemService.updateItem(userId, itemId, itemDto);
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

    @PostMapping("/{itemId}/comment")
    public CommentResponseDto addComment(
            @PathVariable Long itemId,
            @RequestBody CommentRequestDto commentRequestDto,
            @RequestHeader(SHARER_USER_ID) Long userId
    ) {
        return itemService.addComment(itemId, commentRequestDto, userId);
    }
}