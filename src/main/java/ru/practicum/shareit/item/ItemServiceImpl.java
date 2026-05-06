package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserService;

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
        return itemMapper.toDto(itemRepository.save(item));
    }

    @Override
    @Transactional
    public ItemDto update(Long userId, Long itemId, ItemDto itemDto) {
        userService.checkExists(userId);
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NoSuchElementException("Вещь не найдена"));

        if (!item.getOwnerId().equals(userId)) {
            throw new IllegalArgumentException("Только владелец может редактировать");
        }

        if (itemDto.getName() != null) item.setName(itemDto.getName());
        if (itemDto.getDescription() != null) item.setDescription(itemDto.getDescription());
        if (itemDto.getAvailable() != null) item.setAvailable(itemDto.getAvailable());

        return itemMapper.toDto(itemRepository.save(item));
    }

    @Override
    public ItemBookingDto getById(Long userId, Long itemId) {
        userService.checkExists(userId);
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NoSuchElementException("Вещь не найдена"));

        ItemBookingDto result = itemMapper.toBookingDto(item);
        LocalDateTime now = LocalDateTime.now();

        if (item.getOwnerId().equals(userId)) {
            bookingRepository.findLastBooking(itemId, now)
                    .ifPresent(last -> result.setLastBooking(new BookingShortDto(last.getId(), last.getBookerId())));
            bookingRepository.findNextBooking(itemId, now)
                    .ifPresent(next -> result.setNextBooking(new BookingShortDto(next.getId(), next.getBookerId())));
        }

        List<CommentDto> comments = commentRepository.findByItemIdOrderByCreatedAsc(itemId).stream()
                .map(comment -> {
                    CommentDto dto = new CommentDto();
                    dto.setId(comment.getId());
                    dto.setText(comment.getText());
                    dto.setAuthorName(userService.getById(comment.getAuthorId()).getName());
                    dto.setCreated(comment.getCreated());
                    return dto;
                })
                .collect(Collectors.toList());
        result.setComments(comments);

        return result;
    }

    @Override
    public List<ItemBookingDto> getAllByOwner(Long userId) {
        userService.checkExists(userId);
        LocalDateTime now = LocalDateTime.now();

        return itemRepository.findByOwnerIdOrderByIdAsc(userId).stream()
                .map(item -> {
                    ItemBookingDto dto = itemMapper.toBookingDto(item);
                    bookingRepository.findLastBooking(item.getId(), now)
                            .ifPresent(last -> dto.setLastBooking(new BookingShortDto(last.getId(), last.getBookerId())));
                    bookingRepository.findNextBooking(item.getId(), now)
                            .ifPresent(next -> dto.setNextBooking(new BookingShortDto(next.getId(), next.getBookerId())));

                    List<CommentDto> comments = commentRepository.findByItemIdOrderByCreatedAsc(item.getId()).stream()
                            .map(comment -> {
                                CommentDto commentDto = new CommentDto();
                                commentDto.setId(comment.getId());
                                commentDto.setText(comment.getText());
                                commentDto.setAuthorName(userService.getById(comment.getAuthorId()).getName());
                                commentDto.setCreated(comment.getCreated());
                                return commentDto;
                            })
                            .collect(Collectors.toList());
                    dto.setComments(comments);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> search(Long userId, String text) {
        userService.checkExists(userId);
        if (text == null || text.isBlank()) return List.of();
        return itemRepository.searchAvailable(text).stream()
                .map(itemMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CommentDto addComment(Long userId, Long itemId, CommentRequestDto commentRequestDto) {
        userService.checkExists(userId);
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NoSuchElementException("Вещь не найдена"));

        LocalDateTime now = LocalDateTime.now();
        if (!bookingRepository.existsCompletedBooking(userId, itemId, now)) {
            throw new IllegalArgumentException("Пользователь не брал эту вещь в аренду");
        }

        Comment comment = new Comment();
        comment.setText(commentRequestDto.getText());
        comment.setItemId(itemId);
        comment.setAuthorId(userId);
        comment.setCreated(now);

        Comment saved = commentRepository.save(comment);

        CommentDto result = new CommentDto();
        result.setId(saved.getId());
        result.setText(saved.getText());
        result.setAuthorName(userService.getById(userId).getName());
        result.setCreated(saved.getCreated());
        return result;
    }
}