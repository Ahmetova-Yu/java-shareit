package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final ItemMapper itemMapper;
    private final UserService userService;

    @Override
    public ItemDto create(Long userId, ItemDto itemDto) {
        userService.getById(userId);

        if (itemDto.getName() == null || itemDto.getName().isBlank()) {
            throw new RuntimeException("Item name cannot be empty");
        }
        if (itemDto.getDescription() == null || itemDto.getDescription().isBlank()) {
            throw new RuntimeException("Item description cannot be empty");
        }
        if (itemDto.getAvailable() == null) {
            throw new RuntimeException("Item available status cannot be null");
        }

        Item item = itemMapper.toEntity(itemDto, userId);
        Item savedItem = itemRepository.save(item);
        return itemMapper.toDto(savedItem);
    }

    @Override
    public ItemDto update(Long userId, Long itemId, ItemDto itemDto) {
        userService.getById(userId);

        Item existingItem = itemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item not found with id: " + itemId));

        if (!existingItem.getOwnerId().equals(userId)) {
            throw new RuntimeException("Only owner can edit the item");
        }

        itemMapper.updateEntity(existingItem, itemDto);
        Item updatedItem = itemRepository.save(existingItem);
        return itemMapper.toDto(updatedItem);
    }

    @Override
    public ItemDto getById(Long itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item not found with id: " + itemId));
        return itemMapper.toDto(item);
    }

    @Override
    public List<ItemDto> getAllByOwner(Long userId) {
        userService.getById(userId);
        return itemRepository.findAllByOwnerId(userId).stream()
                .map(itemMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> search(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        return itemRepository.searchAvailable(text).stream()
                .map(itemMapper::toDto)
                .collect(Collectors.toList());
    }
}