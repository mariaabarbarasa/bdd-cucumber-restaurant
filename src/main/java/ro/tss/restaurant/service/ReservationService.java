package ro.tss.restaurant.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.tss.restaurant.dto.ReservationRequest;
import ro.tss.restaurant.dto.ReservationResponse;
import ro.tss.restaurant.exception.BusinessRuleException;
import ro.tss.restaurant.exception.ResourceNotFoundException;
import ro.tss.restaurant.model.Reservation;
import ro.tss.restaurant.model.RestaurantTable;
import ro.tss.restaurant.repository.ReservationRepository;
import ro.tss.restaurant.repository.RestaurantTableRepository;

/**
 * Table reservations. Rules:
 * 1) the party size cannot exceed the number of seats at the table;
 * 2) a table cannot have two reservations that start less than 2 hours apart.
 */
@Service
@Transactional
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final RestaurantTableRepository tableRepository;

    public ReservationService(ReservationRepository reservationRepository,
                              RestaurantTableRepository tableRepository) {
        this.reservationRepository = reservationRepository;
        this.tableRepository = tableRepository;
    }

    public ReservationResponse create(ReservationRequest request) {
        RestaurantTable table = getTable(request.tableId());
        checkRules(table, request, null);
        Reservation reservation = new Reservation(request.customerName(), request.phone(), table,
                request.dateTime(), request.partySize());
        return ReservationResponse.from(reservationRepository.save(reservation));
    }

    @Transactional(readOnly = true)
    public List<ReservationResponse> findAll() {
        return reservationRepository.findAll().stream().map(ReservationResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public ReservationResponse findById(Long id) {
        return ReservationResponse.from(getReservation(id));
    }

    public ReservationResponse update(Long id, ReservationRequest request) {
        Reservation reservation = getReservation(id);
        RestaurantTable table = getTable(request.tableId());
        checkRules(table, request, id);
        reservation.setCustomerName(request.customerName());
        reservation.setPhone(request.phone());
        reservation.setTable(table);
        reservation.setDateTime(request.dateTime());
        reservation.setPartySize(request.partySize());
        return ReservationResponse.from(reservation);
    }

    public void delete(Long id) {
        reservationRepository.delete(getReservation(id));
    }

    private void checkRules(RestaurantTable table, ReservationRequest request, Long ignoredId) {
        if (!table.canSeat(request.partySize())) {
            throw new BusinessRuleException("Table " + table.getNumber() + " has only " + table.getSeats() + " seats");
        }
        LocalDateTime start = request.dateTime();
        boolean occupied = reservationRepository
                .findByTableIdAndDateTimeBetween(table.getId(),
                        start.minus(Reservation.DURATION), start.plus(Reservation.DURATION))
                .stream()
                .filter(other -> !other.getId().equals(ignoredId))
                .anyMatch(other -> other.overlaps(start));
        if (occupied) {
            throw new BusinessRuleException("Table " + table.getNumber() + " is already reserved in the selected time slot");
        }
    }

    private RestaurantTable getTable(Long id) {
        return tableRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Table", id));
    }

    private Reservation getReservation(Long id) {
        return reservationRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Reservation", id));
    }
}
