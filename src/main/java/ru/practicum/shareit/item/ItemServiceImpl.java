package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.dto.UserDto;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final CommentRepository commentRepository;
    private final BookingRepository bookingRepository;
    private final ItemMapper itemMapper;
    private final UserService userService;

    @Override
    @Transactional
    public ItemDto create(Long userId, ItemDto itemDto) {
        userService.checkExists(userId);

        Item item = itemMapper.toEntity(itemDto, userId);
        Item savedItem = itemRepository.save(item);
        return itemMapper.toDto(savedItem);
    }

    @Override
    @Transactional
    public ItemDto update(Long userId, Long itemId, ItemDto itemDto) {
        userService.checkExists(userId);

        Item existingItem = itemRepository.findById(itemId)
                .orElseThrow(() -> new NoSuchElementException("Вещь не найдена с id: " + itemId));

        if (!existingItem.getOwnerId().equals(userId)) {
            throw new IllegalArgumentException("Только владелец может редактировать вещь");
        }

        if (itemDto.getName() != null) {
            existingItem.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null) {
            existingItem.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            existingItem.setAvailable(itemDto.getAvailable());
        }

        Item updatedItem = itemRepository.save(existingItem);
        return itemMapper.toDto(updatedItem);
    }

    @Override
    public ItemBookingDto getById(Long userId, Long itemId) {
        userService.checkExists(userId);

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NoSuchElementException("Вещь не найдена с id: " + itemId));

        ItemBookingDto result = itemMapper.toBookingDto(item);

        // Добавляем даты бронирования для владельца
        if (item.getOwnerId().equals(userId)) {
            addBookingDates(result, itemId);
        }

        // Добавляем комментарии
        addComments(result, itemId);

        return result;
    }

    @Override
    public List<ItemBookingDto> getAllByOwner(Long userId) {
        userService.checkExists(userId);

        List<Item> items = itemRepository.findByOwnerIdOrderByIdAsc(userId);
        LocalDateTime now = LocalDateTime.now();

        return items.stream()
                .map(item -> {
                    ItemBookingDto dto = itemMapper.toBookingDto(item);
                    addBookingDates(dto, item.getId());
                    addComments(dto, item.getId());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> search(Long userId, String text) {
        userService.checkExists(userId);

        if (text == null || text.isBlank()) {
            return List.of();
        }

        return itemRepository.searchAvailable(text).stream()
                .map(itemMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CommentDto addComment(Long userId, Long itemId, CommentRequestDto commentRequestDto) {
        userService.checkExists(userId);

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NoSuchElementException("Вещь не найдена с id: " + itemId));

        LocalDateTime now = LocalDateTime.now();
        boolean hasCompletedBooking = bookingRepository.existsCompletedBooking(userId, itemId, now);

        if (!hasCompletedBooking) {
            throw new ValidationException("Пользователь не брал эту вещь в аренду или аренда еще не завершена");
        }

        UserDto author = userService.getById(userId);

        Comment comment = new Comment();
        comment.setText(commentRequestDto.getText());
        comment.setItemId(itemId);
        comment.setAuthorId(userId);
        comment.setCreated(now);

        Comment savedComment = commentRepository.save(comment);

        CommentDto result = new CommentDto();
        result.setId(savedComment.getId());
        result.setText(savedComment.getText());
        result.setAuthorName(author.getName());
        result.setCreated(savedComment.getCreated());

        return result;
    }

    private void addBookingDates(ItemBookingDto dto, Long itemId) {
        LocalDateTime now = LocalDateTime.now();

        List<Booking> lastBookings = bookingRepository.findLastBooking(itemId, now);
        if (!lastBookings.isEmpty()) {
            Booking lastBooking = lastBookings.get(0);
            dto.setLastBooking(new BookingShortDto(lastBooking.getId(), lastBooking.getBookerId()));
        }

        List<Booking> nextBookings = bookingRepository.findNextBooking(itemId, now);
        if (!nextBookings.isEmpty()) {
            Booking nextBooking = nextBookings.get(0);
            dto.setNextBooking(new BookingShortDto(nextBooking.getId(), nextBooking.getBookerId()));
        }
    }

    private void addComments(ItemBookingDto dto, Long itemId) {
        List<Comment> comments = commentRepository.findByItemIdOrderByCreatedAsc(itemId);
        List<CommentDto> commentDtos = comments.stream()
                .map(comment -> {
                    UserDto author = userService.getById(comment.getAuthorId());
                    CommentDto commentDto = new CommentDto();
                    commentDto.setId(comment.getId());
                    commentDto.setText(comment.getText());
                    commentDto.setAuthorName(author.getName());
                    commentDto.setCreated(comment.getCreated());
                    return commentDto;
                })
                .collect(Collectors.toList());
        dto.setComments(commentDtos);
    }
}