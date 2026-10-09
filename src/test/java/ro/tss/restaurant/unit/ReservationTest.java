package ro.tss.restaurant.unit;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import ro.tss.restaurant.model.Reservation;
import ro.tss.restaurant.model.RestaurantTable;

/**
 * Boundary values for the rule "a reservation occupies the table for 2 hours".
 */
class ReservationTest {

    private final RestaurantTable table = new RestaurantTable(3, 8);
    private final Reservation reservation =
            new Reservation("Maria Ionescu", "0722000000", table, LocalDateTime.of(2030, 12, 20, 19, 0), 6);

    @ParameterizedTest(name = "a reservation at {0} overlaps: {1}")
    @CsvSource({
            "2030-12-20T16:59, false",
            "2030-12-20T17:00, false",
            "2030-12-20T17:01, true",
            "2030-12-20T19:00, true",
            "2030-12-20T20:59, true",
            "2030-12-20T21:00, false"
    })
    void overlapBoundaries(LocalDateTime otherStart, boolean overlaps) {
        assertThat(reservation.overlaps(otherStart)).isEqualTo(overlaps);
    }

    @Test
    void tableCapacity() {
        assertThat(table.canSeat(8)).isTrue();
        assertThat(table.canSeat(9)).isFalse();
    }
}
