package ro.tss.restaurant.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import ro.tss.restaurant.dto.ReservationRequest;
import ro.tss.restaurant.exception.BusinessRuleException;
import ro.tss.restaurant.exception.ResourceNotFoundException;
import ro.tss.restaurant.model.Reservation;
import ro.tss.restaurant.model.RestaurantTable;
import ro.tss.restaurant.repository.ReservationRepository;
import ro.tss.restaurant.repository.RestaurantTableRepository;
import ro.tss.restaurant.service.ReservationService;

/**
 * Unit tests for ReservationService, with the repositories mocked by Mockito.
 */
@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    private static final LocalDateTime SEVEN_PM = LocalDateTime.of(2030, 12, 20, 19, 0);

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private RestaurantTableRepository tableRepository;

    @InjectMocks
    private ReservationService service;

    private RestaurantTable table;

    @BeforeEach
    void setUp() {
        table = new RestaurantTable(3, 4);
        ReflectionTestUtils.setField(table, "id", 10L);
    }

    @Test
    void createsReservationWhenTableIsFree() {
        when(tableRepository.findById(10L)).thenReturn(Optional.of(table));
        when(reservationRepository.findByTableIdAndDateTimeBetween(any(), any(), any())).thenReturn(List.of());
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(inv -> inv.getArgument(0));

        var response = service.create(new ReservationRequest("Andrei Popescu", "0722123456", 10L, SEVEN_PM, 4));

        assertThat(response.tableNumber()).isEqualTo(3);
        assertThat(response.partySize()).isEqualTo(4);
    }

    @Test
    void rejectsGroupLargerThanTable() {
        when(tableRepository.findById(10L)).thenReturn(Optional.of(table));

        assertThatThrownBy(() -> service.create(new ReservationRequest("Andrei Popescu", "0722123456", 10L, SEVEN_PM, 5)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Table 3 has only 4 seats");
        verify(reservationRepository, never()).save(any());
    }

    @Test
    void rejectsOverlappingReservation() {
        Reservation existing = new Reservation("Maria Ionescu", "0722000000", table, SEVEN_PM, 2);
        ReflectionTestUtils.setField(existing, "id", 1L);
        when(tableRepository.findById(10L)).thenReturn(Optional.of(table));
        when(reservationRepository.findByTableIdAndDateTimeBetween(any(), any(), any())).thenReturn(List.of(existing));

        assertThatThrownBy(() -> service.create(
                new ReservationRequest("Andrei Popescu", "0722123456", 10L, SEVEN_PM.plusMinutes(90), 2)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("already reserved");
    }

    @Test
    void movingAReservationDoesNotConflictWithItself() {
        Reservation existing = new Reservation("Maria Ionescu", "0722000000", table, SEVEN_PM, 2);
        ReflectionTestUtils.setField(existing, "id", 1L);
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(tableRepository.findById(10L)).thenReturn(Optional.of(table));
        when(reservationRepository.findByTableIdAndDateTimeBetween(any(), any(), any())).thenReturn(List.of(existing));

        var response = service.update(1L, new ReservationRequest("Maria Ionescu", "0722000000", 10L, SEVEN_PM.plusMinutes(30), 2));

        assertThat(response.dateTime()).isEqualTo(SEVEN_PM.plusMinutes(30));
    }

    @Test
    void unknownTableGives404() {
        when(tableRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(new ReservationRequest("Andrei Popescu", "0722123456", 99L, SEVEN_PM, 2)))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
