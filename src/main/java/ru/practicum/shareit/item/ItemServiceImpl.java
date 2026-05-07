package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.UserRepository;

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
    private final UserRepository userRepository;

    @Override
    @Transactional
    public ItemDto create(Long userId, ItemDto itemDto) {
        userService.checkExists(userId);
        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("Пользователь не найден"));
        Item item = itemMapper.toEntity(itemDto, owner);
        return itemMapper.toDto(itemRepository.save(item));
    }

    @Override
    @Transactional
    public ItemDto update(Long userId, Long itemId, ItemDto itemDto) {
        userService.checkExists(userId);
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NoSuchElementException("Вещь не найдена"));

        if (!item.getOwner().getId().equals(userId)) {
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

        if (item.getOwner().getId().equals(userId)) {
            List<Booking> lastBookings = bookingRepository.findLastBooking(itemId, now);
            if (!lastBookings.isEmpty()) {
                Booking last = lastBookings.getFirst();
                result.setLastBooking(new BookingShortDto(last.getId(), last.getBooker().getId()));
            }

            List<Booking> nextBookings = bookingRepository.findNextBooking(itemId, now);
            if (!nextBookings.isEmpty()) {
                Booking next = nextBookings.getFirst();
                result.setNextBooking(new BookingShortDto(next.getId(), next.getBooker().getId()));
            }
        }

        List<CommentDto> comments = commentRepository.findByItemIdOrderByCreatedAsc(itemId).stream()
                .map(comment -> {
                    CommentDto dto = new CommentDto();
                    dto.setId(comment.getId());
                    dto.setText(comment.getText());
                    dto.setAuthorName(comment.getAuthor().getName());
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

                    List<Booking> lastBookings = bookingRepository.findLastBooking(item.getId(), now);
                    if (!lastBookings.isEmpty()) {
                        Booking last = lastBookings.getFirst();
                        dto.setLastBooking(new BookingShortDto(last.getId(), last.getBooker().getId()));
                    }

                    List<Booking> nextBookings = bookingRepository.findNextBooking(item.getId(), now);
                    if (!nextBookings.isEmpty()) {
                        Booking next = nextBookings.getFirst();
                        dto.setNextBooking(new BookingShortDto(next.getId(), next.getBooker().getId()));
                    }

                    List<CommentDto> comments = commentRepository.findByItemIdOrderByCreatedAsc(item.getId()).stream()
                            .map(comment -> {
                                CommentDto commentDto = new CommentDto();
                                commentDto.setId(comment.getId());
                                commentDto.setText(comment.getText());
                                commentDto.setAuthorName(comment.getAuthor().getName());
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

        User author = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("Пользователь не найден"));

        LocalDateTime now = LocalDateTime.now();
        if (!bookingRepository.existsCompletedBooking(userId, itemId, now)) {
            throw new IllegalArgumentException("Пользователь не брал эту вещь в аренду");
        }

        Comment comment = new Comment();
        comment.setText(commentRequestDto.getText());
        comment.setItem(item);
        comment.setAuthor(author);
        comment.setCreated(now);

        Comment saved = commentRepository.save(comment);

        CommentDto result = new CommentDto();
        result.setId(saved.getId());
        result.setText(saved.getText());
        result.setAuthorName(saved.getAuthor().getName());
        result.setCreated(saved.getCreated());
        return result;
    }
}