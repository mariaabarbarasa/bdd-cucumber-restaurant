package ro.tss.restaurant.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import ro.tss.restaurant.model.Reservation;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    /** Reservations of a table that start within a given interval (used to detect overlaps). */
    List<Reservation> findByTableIdAndDateTimeBetween(Long tableId, LocalDateTime from, LocalDateTime to);

    boolean existsByTableId(Long tableId);
}
