package ro.tss.restaurant.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/** Data sent by the client to create or update a reservation. */
public record ReservationRequest(
        @NotBlank(message = "Customer name is required")
        String customerName,

        @NotBlank(message = "Phone is required")
        @Pattern(regexp = "07\\d{8}", message = "Phone must have the format 07xxxxxxxx")
        String phone,

        @NotNull(message = "Table is required")
        Long tableId,

        @NotNull(message = "Date and time are required")
        @Future(message = "Reservation must be in the future")
        LocalDateTime dateTime,

        @Min(value = 1, message = "Reservation must be for at least one person")
        int partySize
) {
}
