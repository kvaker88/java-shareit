package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.AccessDeniedException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.comment.dto.CommentRequestDto;
import ru.practicum.shareit.item.comment.dto.CommentResponseDto;
import ru.practicum.shareit.item.comment.model.Comment;
import ru.practicum.shareit.item.comment.repository.CommentRepository;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemServiceImpl implements ItemService {
    private static final String ITEM_NOT_FOUND = "Предмет не найден по ID ";
    private static final String USER_NOT_OWNER = "Пользователь не является владельцем предмета";
    private static final String USER_NOT_FOUND = "Не удалось найти пользователя с ID ";

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private final ItemMapper itemMapper;

    // Константы для сортировки
    private static final Sort END_DESC = Sort.by(Sort.Direction.DESC, "end");
    private static final Sort START_ASC = Sort.by(Sort.Direction.ASC, "start");
    private static final Sort START_DESC = Sort.by(Sort.Direction.DESC, "start");

    @Override
    @Transactional
    public ItemResponseDto createItem(Long userId, ItemRequestDto itemRequestDto) {
        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND + userId));

        Item item = itemMapper.toItem(itemRequestDto);
        item.setOwner(owner.getId());
        Item savedItem = itemRepository.save(item);

        return itemMapper.toItemResponseDto(savedItem);
    }

    @Override
    public ItemResponseDto getItemById(Long itemId, Long userId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException(ITEM_NOT_FOUND + itemId));

        ItemResponseDto dto = itemMapper.toItemResponseDto(item);

        if (item.getOwner().equals(userId)) {
            addBookingInfoToDto(dto, itemId);
        }

        addCommentsToDto(dto, itemId);

        return dto;
    }

    @Override
    public List<ItemResponseDto> getAllUserItems(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException(USER_NOT_FOUND + userId);
        }

        List<Item> items = itemRepository.findByOwner(userId);
        LocalDateTime now = LocalDateTime.now();

        List<Long> itemIds = items
                .stream()
                .map(Item::getId)
                .toList();

        // Добавляем Sort параметры
        Map<Long, List<Booking>> lastBookingsMap = bookingRepository.findLastBookingsForItems(itemIds, now, END_DESC)
                .stream()
                .collect(Collectors.groupingBy(booking -> booking.getItem().getId()));

        Map<Long, List<Booking>> nextBookingsMap = bookingRepository.findNextBookingsForItems(itemIds, now, START_ASC)
                .stream()
                .collect(Collectors.groupingBy(booking -> booking.getItem().getId()));

        Map<Long, List<Comment>> commentsMap = commentRepository.findByItemIdIn(itemIds)
                .stream()
                .collect(Collectors.groupingBy(comment -> comment.getItem().getId()));

        return items.stream()
                .map(item -> {
                    ItemResponseDto dto = itemMapper.toItemResponseDto(item);
                    addBookingInfoToDto(dto, item.getId(), lastBookingsMap, nextBookingsMap);
                    addCommentsToDto(dto, item.getId(), commentsMap);
                    return dto;
                })
                .toList();
    }

    @Override
    public List<ItemResponseDto> searchItems(String text, Integer from, Integer size) {
        if (text == null || text.isBlank()) {
            return Collections.emptyList();
        }

        return itemRepository.searchAvailableItems(text.toLowerCase(),
                        Pageable.ofSize(size).withPage(from / size))
                .stream()
                .map(item -> {
                    ItemResponseDto dto = itemMapper.toItemResponseDto(item);
                    dto.setLastBooking(null);
                    dto.setNextBooking(null);
                    dto.setComments(Collections.emptyList());
                    return dto;
                })
                .toList();
    }

    @Override
    @Transactional
    public ItemResponseDto updateItem(Long userId, Long itemId, ItemRequestDto itemRequestDto) {
        Item existingItem = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException(ITEM_NOT_FOUND + itemId));

        if (!existingItem.getOwner().equals(userId)) {
            throw new AccessDeniedException(USER_NOT_OWNER);
        }

        if (itemRequestDto.getName() != null && !itemRequestDto.getName().isBlank()) {
            existingItem.setName(itemRequestDto.getName());
        }
        if (itemRequestDto.getDescription() != null && !itemRequestDto.getDescription().isBlank()) {
            existingItem.setDescription(itemRequestDto.getDescription());
        }
        if (itemRequestDto.getAvailable() != null) {
            existingItem.setAvailable(itemRequestDto.getAvailable());
        }

        Item updatedItem = itemRepository.save(existingItem);
        return itemMapper.toItemResponseDto(updatedItem);
    }

    @Override
    @Transactional
    public void deleteItem(Long userId, Long itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException(ITEM_NOT_FOUND + itemId));

        if (!item.getOwner().equals(userId)) {
            throw new AccessDeniedException(USER_NOT_OWNER);
        }

        itemRepository.deleteById(itemId);
    }

    @Override
    @Transactional
    public CommentResponseDto addComment(Long itemId, CommentRequestDto commentRequestDto, Long userId) {
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND + userId));
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException(ITEM_NOT_FOUND + itemId));

        List<Booking> userBookings = bookingRepository.findByBookerIdAndItemIdAndEndBefore(
                userId, itemId, LocalDateTime.now());

        boolean hasValidBooking = userBookings.stream()
                .anyMatch(booking -> booking.getStatus() == BookingStatus.APPROVED);

        if (!hasValidBooking) {
            throw new IllegalArgumentException(
                    "Пользователь может комментировать только те товары, которые он заказывал в прошлом");
        }

        Comment comment = new Comment();
        comment.setText(commentRequestDto.getText());
        comment.setItem(item);
        comment.setAuthor(author);
        comment.setCreated(LocalDateTime.now());

        Comment savedComment = commentRepository.save(comment);
        return toCommentResponseDto(savedComment);
    }

    private void addBookingInfoToDto(ItemResponseDto dto, Long itemId) {
        LocalDateTime now = LocalDateTime.now();

        // Добавляем Sort параметры
        List<Booking> lastBookings = bookingRepository.findLastBookingForItem(itemId, now, END_DESC);
        List<Booking> nextBookings = bookingRepository.findNextBookingForItem(itemId, now, START_ASC);

        if (!lastBookings.isEmpty()) {
            dto.setLastBooking(createBookingInfo(lastBookings.getFirst()));
        }

        if (!nextBookings.isEmpty()) {
            dto.setNextBooking(createBookingInfo(nextBookings.getFirst()));
        }
    }

    private void addBookingInfoToDto(ItemResponseDto dto, Long itemId,
                                     Map<Long, List<Booking>> lastBookingsMap,
                                     Map<Long, List<Booking>> nextBookingsMap) {
        List<Booking> lastBookings = lastBookingsMap.getOrDefault(itemId, Collections.emptyList());
        List<Booking> nextBookings = nextBookingsMap.getOrDefault(itemId, Collections.emptyList());

        if (!lastBookings.isEmpty()) {
            dto.setLastBooking(createBookingInfo(lastBookings.getFirst()));
        }

        if (!nextBookings.isEmpty()) {
            dto.setNextBooking(createBookingInfo(nextBookings.getFirst()));
        }
    }

    private ItemResponseDto.BookingInfo createBookingInfo(Booking booking) {
        ItemResponseDto.BookingInfo bookingInfo = new ItemResponseDto.BookingInfo();
        bookingInfo.setId(booking.getId());
        bookingInfo.setBookerId(booking.getBooker().getId());
        return bookingInfo;
    }

    private void addCommentsToDto(ItemResponseDto dto, Long itemId) {
        List<Comment> comments = commentRepository.findByItemId(itemId);
        List<CommentResponseDto> commentDto = comments.stream()
                .map(this::toCommentResponseDto)
                .toList();
        dto.setComments(commentDto);
    }

    private void addCommentsToDto(ItemResponseDto dto, Long itemId,
                                  Map<Long, List<Comment>> commentsMap) {
        List<Comment> comments = commentsMap.getOrDefault(itemId, Collections.emptyList());
        List<CommentResponseDto> commentDto = comments.stream()
                .map(this::toCommentResponseDto)
                .toList();
        dto.setComments(commentDto);
    }

    private CommentResponseDto toCommentResponseDto(Comment comment) {
        CommentResponseDto dto = new CommentResponseDto();
        dto.setId(comment.getId());
        dto.setText(comment.getText());
        dto.setAuthorName(comment.getAuthor().getName());
        dto.setCreated(comment.getCreated());
        return dto;
    }
}