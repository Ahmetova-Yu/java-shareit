package ru.practicum.shareit.booking;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByBookerId(Long bookerId, Sort sort);

    @Query("SELECT b FROM Booking b WHERE b.bookerId = :bookerId AND b.start < :now AND b.end > :now")
    List<Booking> findCurrentByBookerId(@Param("bookerId") Long bookerId, @Param("now") LocalDateTime now);

    List<Booking> findByBookerIdAndEndBefore(Long bookerId, LocalDateTime now, Sort sort);

    List<Booking> findByBookerIdAndStartAfter(Long bookerId, LocalDateTime now, Sort sort);

    List<Booking> findByBookerIdAndStatus(Long bookerId, BookingStatus status, Sort sort);

    @Query("SELECT b FROM Booking b WHERE b.itemId IN (SELECT i.id FROM Item i WHERE i.ownerId = :ownerId)")
    List<Booking> findAllByOwnerId(@Param("ownerId") Long ownerId);

    @Query("SELECT b FROM Booking b WHERE b.itemId IN (SELECT i.id FROM Item i WHERE i.ownerId = :ownerId) AND b.start < :now AND b.end > :now")
    List<Booking> findCurrentByOwnerId(@Param("ownerId") Long ownerId, @Param("now") LocalDateTime now);

    @Query("SELECT b FROM Booking b WHERE b.itemId IN (SELECT i.id FROM Item i WHERE i.ownerId = :ownerId) AND b.end < :now")
    List<Booking> findPastByOwnerId(@Param("ownerId") Long ownerId, @Param("now") LocalDateTime now);

    @Query("SELECT b FROM Booking b WHERE b.itemId IN (SELECT i.id FROM Item i WHERE i.ownerId = :ownerId) AND b.start > :now")
    List<Booking> findFutureByOwnerId(@Param("ownerId") Long ownerId, @Param("now") LocalDateTime now);

    @Query("SELECT b FROM Booking b WHERE b.itemId = :itemId AND b.start < :now AND b.status = 'APPROVED' ORDER BY b.start DESC")
    List<Booking> findLastBooking(@Param("itemId") Long itemId, @Param("now") LocalDateTime now);

    @Query("SELECT b FROM Booking b WHERE b.itemId = :itemId AND b.start > :now AND b.status = 'APPROVED' ORDER BY b.start ASC")
    List<Booking> findNextBooking(@Param("itemId") Long itemId, @Param("now") LocalDateTime now);

    @Query("SELECT COUNT(b) > 0 FROM Booking b WHERE b.bookerId = :userId AND b.itemId = :itemId AND b.end < :now AND b.status = 'APPROVED'")
    boolean existsCompletedBooking(@Param("userId") Long userId, @Param("itemId") Long itemId, @Param("now") LocalDateTime now);

    @Query("SELECT b FROM Booking b WHERE b.itemId = :itemId AND b.status = 'APPROVED' AND b.start < :end AND b.end > :start")
    List<Booking> findConflictingBookings(@Param("itemId") Long itemId,
                                          @Param("start") LocalDateTime start,
                                          @Param("end") LocalDateTime end);
}