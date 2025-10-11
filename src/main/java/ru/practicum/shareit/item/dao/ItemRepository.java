package ru.practicum.shareit.item.dao;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class ItemRepository {
    private final Map<Long, Item> items = new HashMap<>();
    private final AtomicLong idCounter = new AtomicLong(1);

    public Item save(Item item) {
        Long id = idCounter.getAndIncrement();
        item.setId(id);
        items.put(id, item);
        return item;
    }

    public Item update(Long itemId, Item item) {
        if (!items.containsKey(itemId)) {
            throw new NotFoundException("Предмет с ID " + itemId + " не найден");
        }
        items.put(itemId, item);
        return item;
    }

    public Optional<Item> findById(Long itemId) {
        return Optional.ofNullable(items.get(itemId));
    }

    public List<Item> findAll() {
        return new ArrayList<>(items.values());
    }

    public List<Item> findAllByOwner(Long ownerId) {
        return items.values().stream()
                .filter(item -> ownerId.equals(item.getOwner()))
                .toList();
    }

    public void delete(Long itemId) {
        items.remove(itemId);
    }

    public boolean existsById(Long itemId) {
        return items.containsKey(itemId);
    }
}