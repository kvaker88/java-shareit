package ru.practicum.shareit.booking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByBookerIdOrderByStartDesc(Long bookerId);

    List<Booking> findByBookerIdAndStatusOrderByStartDesc(Long bookerId, BookingStatus status);

    List<Booking> findByBookerIdAndEndBeforeOrderByStartDesc(Long bookerId, LocalDateTime end);

    List<Booking> findByBookerIdAndStartAfterOrderByStartDesc(Long bookerId, LocalDateTime start);

    List<Booking> findByBookerIdAndItemIdAndEndBefore(Long bookerId, Long itemId, LocalDateTime end);

    List<Booking> findByItemIdInOrderByStartDesc(List<Long> itemIds);

    List<Booking> findByItemIdInAndStatusOrderByStartDesc(List<Long> itemIds, BookingStatus status);

    List<Booking> findByItemIdInAndEndBeforeOrderByStartDesc(List<Long> itemIds, LocalDateTime end);

    List<Booking> findByItemIdInAndStartAfterOrderByStartDesc(List<Long> itemIds, LocalDateTime start);

    @Query("SELECT b FROM Booking b WHERE b.item.id = :itemId " +
            "AND b.status = ru.practicum.shareit.booking.model.BookingStatus.APPROVED " +
            "AND b.end < :now ORDER BY b.end DESC")
    List<Booking> findLastBookingForItem(@Param("itemId") Long itemId, @Param("now") LocalDateTime now);

    @Query("SELECT b FROM Booking b WHERE b.item.id = :itemId " +
            "AND b.status = ru.practicum.shareit.booking.model.BookingStatus.APPROVED " +
            "AND b.start > :now ORDER BY b.start ASC")
    List<Booking> findNextBookingForItem(@Param("itemId") Long itemId, @Param("now") LocalDateTime now);

    @Query("SELECT b FROM Booking b WHERE b.booker.id = :bookerId " +
            "AND b.start <= :now AND b.end >= :now ORDER BY b.start DESC")
    List<Booking> findCurrentBookingsByBooker(@Param("bookerId") Long bookerId, @Param("now") LocalDateTime now);

    @Query("SELECT b FROM Booking b WHERE b.item.id IN :itemIds " +
            "AND b.start <= :now AND b.end >= :now ORDER BY b.start DESC")
    List<Booking> findCurrentBookingsByItems(@Param("itemIds") List<Long> itemIds, @Param("now") LocalDateTime now);

    @Query("SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END FROM Booking b " +
            "WHERE b.item.id = :itemId AND b.status = ru.practicum.shareit.booking.model.BookingStatus.APPROVED " +
            "AND (:start < b.end AND :end > b.start)")
    boolean existsOverlappingBookings(@Param("itemId") Long itemId,
                                      @Param("start") LocalDateTime start,
                                      @Param("end") LocalDateTime end);

    @Query("SELECT b FROM Booking b WHERE b.item.id IN :itemIds " +
            "AND b.status = ru.practicum.shareit.booking.model.BookingStatus.APPROVED " +
            "AND b.end < :now ORDER BY b.end DESC")
    List<Booking> findLastBookingsForItems(@Param("itemIds") List<Long> itemIds, @Param("now") LocalDateTime now);

    @Query("SELECT b FROM Booking b WHERE b.item.id IN :itemIds " +
            "AND b.status = ru.practicum.shareit.booking.model.BookingStatus.APPROVED " +
            "AND b.start > :now ORDER BY b.start ASC")
    List<Booking> findNextBookingsForItems(@Param("itemIds") List<Long> itemIds, @Param("now") LocalDateTime now);
}