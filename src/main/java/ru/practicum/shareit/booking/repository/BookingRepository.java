package ru.practicum.shareit.booking.repository;

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

    List<Booking> findByBookerIdAndStatus(Long bookerId, BookingStatus status, Sort sort);

    List<Booking> findByBookerIdAndEndBefore(Long bookerId, LocalDateTime end, Sort sort);

    List<Booking> findByBookerIdAndStartAfter(Long bookerId, LocalDateTime start, Sort sort);

    List<Booking> findByBookerIdAndItemIdAndEndBefore(Long bookerId, Long itemId, LocalDateTime end);

    List<Booking> findByItemIdIn(List<Long> itemIds, Sort sort);

    List<Booking> findByItemIdInAndStatus(List<Long> itemIds, BookingStatus status, Sort sort);

    List<Booking> findByItemIdInAndEndBefore(List<Long> itemIds, LocalDateTime end, Sort sort);

    List<Booking> findByItemIdInAndStartAfter(List<Long> itemIds, LocalDateTime start, Sort sort);

    @Query("SELECT b FROM Booking b WHERE b.item.id = :itemId " +
            "AND b.status = ru.practicum.shareit.booking.model.BookingStatus.APPROVED " +
            "AND b.end < :now")
    List<Booking> findLastBookingForItem(@Param("itemId") Long itemId, @Param("now") LocalDateTime now, Sort sort);

    @Query("SELECT b FROM Booking b WHERE b.item.id = :itemId " +
            "AND b.status = ru.practicum.shareit.booking.model.BookingStatus.APPROVED " +
            "AND b.start > :now")
    List<Booking> findNextBookingForItem(@Param("itemId") Long itemId, @Param("now") LocalDateTime now, Sort sort);

    @Query("SELECT b FROM Booking b WHERE b.booker.id = :bookerId " +
            "AND b.start <= :now AND b.end >= :now")
    List<Booking> findCurrentBookingsByBooker(@Param("bookerId") Long bookerId,
                                              @Param("now") LocalDateTime now, Sort sort);

    @Query("SELECT b FROM Booking b WHERE b.item.id IN :itemIds " +
            "AND b.start <= :now AND b.end >= :now")
    List<Booking> findCurrentBookingsByItems(@Param("itemIds") List<Long> itemIds,
                                             @Param("now") LocalDateTime now, Sort sort);

    @Query("SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END FROM Booking b " +
            "WHERE b.item.id = :itemId AND b.status = ru.practicum.shareit.booking.model.BookingStatus.APPROVED " +
            "AND (:start < b.end AND :end > b.start)")
    boolean existsOverlappingBookings(@Param("itemId") Long itemId,
                                      @Param("start") LocalDateTime start,
                                      @Param("end") LocalDateTime end);

    @Query("SELECT b FROM Booking b WHERE b.item.id IN :itemIds " +
            "AND b.status = ru.practicum.shareit.booking.model.BookingStatus.APPROVED " +
            "AND b.end < :now")
    List<Booking> findLastBookingsForItems(@Param("itemIds") List<Long> itemIds,
                                           @Param("now") LocalDateTime now, Sort sort);

    @Query("SELECT b FROM Booking b WHERE b.item.id IN :itemIds " +
            "AND b.status = ru.practicum.shareit.booking.model.BookingStatus.APPROVED " +
            "AND b.start > :now")
    List<Booking> findNextBookingsForItems(@Param("itemIds") List<Long> itemIds,
                                           @Param("now") LocalDateTime now, Sort sort);
}