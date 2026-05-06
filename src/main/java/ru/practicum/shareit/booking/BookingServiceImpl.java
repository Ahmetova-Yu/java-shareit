package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.model.BookingState;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingMapper bookingMapper;

    @Override
    @Transactional
    public BookingDto create(Long userId, BookingRequestDto requestDto) {
        User booker = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("Пользователь не найден"));

        Item item = itemRepository.findById(requestDto.getItemId())
                .orElseThrow(() -> new NoSuchElementException("Вещь не найдена"));

        if (item.getOwnerId().equals(userId)) {
            throw new IllegalArgumentException("Нельзя бронировать собственную вещь");
        }

        if (!item.getAvailable()) {
            throw new IllegalArgumentException("Вещь недоступна для бронирования");
        }

        if (requestDto.getEnd().isBefore(requestDto.getStart()) || requestDto.getEnd().equals(requestDto.getStart())) {
            throw new IllegalArgumentException("Дата окончания должна быть позже даты начала");
        }

        LocalDateTime now = LocalDateTime.now();
        if (requestDto.getStart().isBefore(now)) {
            throw new IllegalArgumentException("Дата начала не может быть в прошлом");
        }

        List<Booking> conflicting = bookingRepository.findConflictingBookings(
                item.getId(), requestDto.getStart(), requestDto.getEnd());
        if (!conflicting.isEmpty()) {
            throw new IllegalArgumentException("Выбранные даты уже заняты");
        }

        Booking booking = bookingMapper.toEntity(requestDto, userId);
        Booking saved = bookingRepository.save(booking);

        return bookingMapper.toDtoWithItemAndBooker(saved, item, booker);
    }

    @Override
    @Transactional
    public BookingDto approve(Long userId, Long bookingId, Boolean approved) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NoSuchElementException("Бронирование не найдено"));

        Item item = itemRepository.findById(booking.getItemId())
                .orElseThrow(() -> new NoSuchElementException("Вещь не найдена"));

        if (!item.getOwnerId().equals(userId)) {
            throw new IllegalArgumentException("Только владелец может подтвердить/отклонить бронирование");
        }

        if (booking.getStatus() != BookingStatus.WAITING) {
            throw new IllegalArgumentException("Бронирование уже обработано");
        }

        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        Booking updated = bookingRepository.save(booking);

        User booker = userRepository.findById(booking.getBookerId())
                .orElseThrow(() -> new NoSuchElementException("Пользователь не найден"));

        return bookingMapper.toDtoWithItemAndBooker(updated, item, booker);
    }

    @Override
    public BookingDto getById(Long userId, Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NoSuchElementException("Бронирование не найдено"));

        Item item = itemRepository.findById(booking.getItemId())
                .orElseThrow(() -> new NoSuchElementException("Вещь не найдена"));

        if (!booking.getBookerId().equals(userId) && !item.getOwnerId().equals(userId)) {
            throw new IllegalArgumentException("Нет доступа к этому бронированию");
        }

        User booker = userRepository.findById(booking.getBookerId())
                .orElseThrow(() -> new NoSuchElementException("Пользователь не найден"));

        return bookingMapper.toDtoWithItemAndBooker(booking, item, booker);
    }

    @Override
    public List<BookingDto> getAllByBooker(Long userId, BookingState state) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("Пользователь не найден"));

        Sort sort = Sort.by(Sort.Direction.DESC, "start");
        LocalDateTime now = LocalDateTime.now();
        List<Booking> bookings;

        if (Objects.requireNonNull(state) == BookingState.ALL) {
            bookings = bookingRepository.findByBookerId(userId, sort);
        } else if (state == BookingState.CURRENT) {
            bookings = bookingRepository.findCurrentByBookerId(userId, now);
        } else if (state == BookingState.PAST) {
            bookings = bookingRepository.findByBookerIdAndEndBefore(userId, now, sort);
        } else if (state == BookingState.FUTURE) {
            bookings = bookingRepository.findByBookerIdAndStartAfter(userId, now, sort);
        } else if (state == BookingState.WAITING) {
            bookings = bookingRepository.findByBookerIdAndStatus(userId, BookingStatus.WAITING, sort);
        } else if (state == BookingState.REJECTED) {
            bookings = bookingRepository.findByBookerIdAndStatus(userId, BookingStatus.REJECTED, sort);
        } else {
            bookings = bookingRepository.findByBookerId(userId, sort);
        }

        return enrichWithItemAndBooker(bookings);
    }

    @Override
    public List<BookingDto> getAllByOwner(Long userId, BookingState state) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("Пользователь не найден"));

        LocalDateTime now = LocalDateTime.now();
        List<Booking> bookings;

        if (Objects.requireNonNull(state) == BookingState.ALL) {
            bookings = bookingRepository.findAllByOwnerId(userId);
        } else if (state == BookingState.CURRENT) {
            bookings = bookingRepository.findCurrentByOwnerId(userId, now);
        } else if (state == BookingState.PAST) {
            bookings = bookingRepository.findPastByOwnerId(userId, now);
        } else if (state == BookingState.FUTURE) {
            bookings = bookingRepository.findFutureByOwnerId(userId, now);
        } else if (state == BookingState.WAITING) {
            bookings = bookingRepository.findAllByOwnerId(userId).stream()
                    .filter(b -> b.getStatus() == BookingStatus.WAITING)
                    .collect(Collectors.toList());
        } else if (state == BookingState.REJECTED) {
            bookings = bookingRepository.findAllByOwnerId(userId).stream()
                    .filter(b -> b.getStatus() == BookingStatus.REJECTED)
                    .collect(Collectors.toList());
        } else {
            bookings = bookingRepository.findAllByOwnerId(userId);
        }

        bookings.sort((b1, b2) -> b2.getStart().compareTo(b1.getStart()));

        return enrichWithItemAndBooker(bookings);
    }

    private List<BookingDto> enrichWithItemAndBooker(List<Booking> bookings) {
        return bookings.stream()
                .map(booking -> {
                    Item item = itemRepository.findById(booking.getItemId())
                            .orElseThrow(() -> new NoSuchElementException("Вещь не найдена"));
                    User booker = userRepository.findById(booking.getBookerId())
                            .orElseThrow(() -> new NoSuchElementException("Пользователь не найден"));
                    return bookingMapper.toDtoWithItemAndBooker(booking, item, booker);
                })
                .collect(Collectors.toList());
    }
}