package ru.practicum.shareit.booking.dto;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.user.dto.UserMapper;

@Component
@RequiredArgsConstructor
public class BookingMapper {

    private final ItemMapper itemMapper;
    private final UserMapper userMapper;

    public Booking toEntity(BookingRequestDto dto, Long bookerId) {
        if (dto == null) return null;
        Booking booking = new Booking();
        booking.setStart(dto.getStart());
        booking.setEnd(dto.getEnd());
        booking.setItemId(dto.getItemId());
        booking.setBookerId(bookerId);
        booking.setStatus(BookingStatus.WAITING);
        return booking;
    }

    public BookingDto toDto(Booking booking) {
        if (booking == null) return null;
        BookingDto dto = new BookingDto();
        dto.setId(booking.getId());
        dto.setStart(booking.getStart());
        dto.setEnd(booking.getEnd());
        dto.setStatus(booking.getStatus());
        return dto;
    }

    public BookingDto toDtoWithItemAndBooker(Booking booking,
                                             ru.practicum.shareit.item.model.Item item,
                                             ru.practicum.shareit.user.model.User booker) {
        BookingDto dto = toDto(booking);
        if (dto != null) {
            dto.setItem(itemMapper.toDto(item));
            dto.setBooker(userMapper.toDto(booker));
        }
        return dto;
    }
}