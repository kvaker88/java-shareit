package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.comment.dto.CommentRequestDto;
import ru.practicum.shareit.item.comment.dto.CommentResponseDto;
import ru.practicum.shareit.item.dto.ItemRequestDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;

import java.util.List;

public interface ItemService {

    ItemResponseDto createItem(Long userId, ItemRequestDto itemRequestDto);

    ItemResponseDto updateItem(Long userId, Long itemId, ItemRequestDto itemRequestDto);

    ItemResponseDto getItemById(Long itemId, Long userId);

    List<ItemResponseDto> getAllUserItems(Long userId);

    List<ItemResponseDto> searchItems(String text, Integer from, Integer size);

    CommentResponseDto addComment(Long itemId, CommentRequestDto commentRequestDto, Long userId);

    void deleteItem(Long userId, Long itemId);
}