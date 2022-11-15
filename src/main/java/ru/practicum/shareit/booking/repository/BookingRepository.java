package ru.practicum.shareit.booking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.booking.model.Booking;

import java.util.Collection;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    Collection<Booking> getAllByBookerIdOrItemOwnerIdOrderByStartTimeDesc(Long bookerId, Long ownerId);

    @Query("SELECT b FROM Booking b " +
            "WHERE (b.booker.id = ?1 OR b.item.owner.id = ?2) " +
            "AND (b.approved = ?3 OR ?3 IS NULL AND b.approved IS NULL)" +
            "ORDER BY b.startTime DESC"
    )
    Collection<Booking> getAllByBookerIdOrItemOwnerIdAndApprovedIsOrderByStartTimeDesc(
            Long bookerId, Long ownerId, Boolean approved);

    @Query("SELECT b FROM Booking b " +
            "WHERE (b.booker.id = ?1 OR b.item.owner.id = ?2) " +
            "AND b.approved IS TRUE " +
            "AND CURRENT_TIMESTAMP > b.endTime " +
            "ORDER BY b.startTime DESC"
    )
    Collection<Booking> getPastBookingsByBookerIdOrOwnerId(Long bookerId, Long ownerId);

    @Query("SELECT b FROM Booking b " +
            "WHERE b.item.id = ?1 " +
            "AND b.endTime < CURRENT_TIMESTAMP " +
            "ORDER BY b.startTime DESC"
    )
    Collection<Booking> getPastBookingsByItemId(long itemId);

    @Query("SELECT b FROM Booking b " +
            "WHERE (b.booker.id = ?1 OR b.item.owner.id = ?2) " +
            "AND b.startTime < CURRENT_TIMESTAMP " +
            "AND CURRENT_TIMESTAMP < b.endTime " +
            "ORDER BY b.startTime DESC"
    )
    Collection<Booking> getCurrentBookingsByBookerIdOrOwnerId(Long bookerId, Long ownerId);

    @Query("SELECT b FROM Booking b " +
            "WHERE (b.booker.id = ?1 OR b.item.owner.id = ?2) " +
            "AND CURRENT_TIMESTAMP < b.startTime " +
            "ORDER BY b.startTime DESC"
    )
    Collection<Booking> getFutureBookingsByBookerIdOrOwnerId(Long bookerId, Long ownerId);

    Collection<Booking> getAllByItemOwnerId(long ownerId);

    @Query("SELECT b FROM Booking b " +
            "WHERE b.item.id = ?1 " +
            "AND (b.approved = TRUE OR b.approved IS NULL) " +
            "AND b.endTime > CURRENT_TIMESTAMP " +
            "ORDER BY b.startTime ASC"
    )
    Collection<Booking> getActiveBookingsByItemIdOrderByStartTimeAsc(long itemId);

    @Query("SELECT b FROM Booking b " +
            "WHERE (b.booker.id = ?1 AND b.item.id = ?2) " +
            "AND b.startTime < CURRENT_TIMESTAMP " +
            "AND b.approved IS TRUE")
    Collection<Booking> getApprovedBookingsByBookerIdAndItemIdNotInFuture(long bookerId, long itemId);
}
