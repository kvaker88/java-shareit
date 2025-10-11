package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.AccessDeniedException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dao.ItemRepository;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.dao.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemService {
    private static final String ITEM_NOT_FOUND = "Предмет не найден по ID ";
    private static final String USER_NOT_OWNER = "Пользователь не является владельцем предмета";
    private static final String USER_NOT_FOUND = "Не удалось найти пользователя с ID ";

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    public ItemDto createItem(Long userId, ItemDto itemDto) {
        if (userRepository.nonExistsById(userId)) {
            throw new NotFoundException(USER_NOT_FOUND + userId);
        }

        Item item = ItemMapper.toItem(itemDto);
        item.setOwner(userId);
        Item savedItem = itemRepository.save(item);

        return ItemMapper.toItemDto(savedItem);
    }

    public ItemDto getItemById(Long itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException(ITEM_NOT_FOUND + itemId));
        return ItemMapper.toItemDto(item);
    }

    public List<ItemDto> getAllUserItems(Long userId) {
        if (userRepository.nonExistsById(userId)) {
            throw new NotFoundException(USER_NOT_FOUND + userId);
        }

        return itemRepository.findAllByOwner(userId).stream()
                .map(ItemMapper::toItemDto)
                .toList();
    }

    public List<ItemDto> searchItems(String text, Integer from, Integer size) {
        if (text == null || text.isBlank()) {
            return List.of();
        }

        String searchText = text.toLowerCase();
        return itemRepository.findAll().stream()
                .filter(item -> Boolean.TRUE.equals(item.getAvailable()))
                .filter(item -> (item.getName() != null && item.getName().toLowerCase().contains(searchText)) ||
                        (item.getDescription() != null && item.getDescription().toLowerCase().contains(searchText)))
                .skip(from)
                .limit(size)
                .map(ItemMapper::toItemDto)
                .toList();
    }

    public ItemDto updateItem(Long userId, Long itemId, ItemDto itemDto) {
        if (userRepository.nonExistsById(userId)) {
            throw new NotFoundException(USER_NOT_FOUND + userId);
        }

        Item existingItem = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException(ITEM_NOT_FOUND + itemId));

        if (!userId.equals(existingItem.getOwner())) {
            throw new AccessDeniedException(USER_NOT_OWNER);
        }

        if (itemDto.getName() != null && !itemDto.getName().isBlank()) {
            existingItem.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null && !itemDto.getDescription().isBlank()) {
            existingItem.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            existingItem.setAvailable(itemDto.getAvailable());
        }

        Item updatedItem = itemRepository.update(itemId, existingItem);
        return ItemMapper.toItemDto(updatedItem);
    }

    public void deleteItem(Long userId, Long itemId) {
        if (userRepository.nonExistsById(userId)) {
            throw new NotFoundException(USER_NOT_FOUND + userId);
        }

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException(ITEM_NOT_FOUND + itemId));

        if (!userId.equals(item.getOwner())) {
            throw new AccessDeniedException(USER_NOT_OWNER);
        }

        itemRepository.delete(itemId);
    }
}