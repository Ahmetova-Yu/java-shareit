package ru.practicum.shareit.item.dto;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.model.Item;

@Component
public class ItemMapper {

    public ItemDto toDto(Item item) {
        if (item == null) return null;
        return new ItemDto(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getAvailable()
        );
    }

    public Item toEntity(ItemDto itemDto, Long ownerId) {
        if (itemDto == null) return null;
        return new Item(
                itemDto.getId(),
                itemDto.getName(),
                itemDto.getDescription(),
                itemDto.getAvailable(),
                ownerId,
                null
        );
    }

    public ItemBookingDto toBookingDto(Item item) {
        if (item == null) return null;
        ItemBookingDto dto = new ItemBookingDto();
        dto.setId(item.getId());
        dto.setName(item.getName());
        dto.setDescription(item.getDescription());
        dto.setAvailable(item.getAvailable());
        return dto;
    }
}