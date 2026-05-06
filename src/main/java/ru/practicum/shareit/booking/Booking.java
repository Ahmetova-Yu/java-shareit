package ru.practicum.shareit.booking;

import java.time.LocalDateTime;

public class Booking {
    private Long id;
    private LocalDateTime start;
    private LocalDateTime end;
    private Long itemId;
    private Long bookerId;
    private BookingStatus status;

    public enum BookingStatus {
        WAITING, APPROVED, REJECTED, CANCELLED
    }
}