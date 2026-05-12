package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.*;
import java.util.List;

public interface ItemService {
    ItemDto create(Long userId, ItemDto itemDto);
    ItemDto update(Long userId, Long itemId, ItemDto itemDto);
    ItemBookingDto getById(Long userId, Long itemId);
    List<ItemBookingDto> getAllByOwner(Long userId);
    List<ItemDto> search(Long userId, String text);
    CommentDto addComment(Long userId, Long itemId, CommentRequestDto commentRequestDto);
}