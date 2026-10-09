package ro.tss.restaurant.dto;

import java.time.LocalDateTime;

import ro.tss.restaurant.model.Reservation;

/** A reservation as returned by the API. */
public record ReservationResponse(Long id, String customerName, String phone, int tableNumber,
                                  LocalDateTime dateTime, int partySize) {

    public static ReservationResponse from(Reservation r) {
        return new ReservationResponse(r.getId(), r.getCustomerName(), r.getPhone(),
                r.getTable().getNumber(), r.getDateTime(), r.getPartySize());
    }
}
