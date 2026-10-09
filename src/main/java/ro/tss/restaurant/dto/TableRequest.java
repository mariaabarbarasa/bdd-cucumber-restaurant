package ro.tss.restaurant.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/** Data sent by the client to create or update a table. */
public record TableRequest(
        @Min(value = 1, message = "Table number must be at least 1")
        int number,

        @Min(value = 1, message = "A table has at least one seat")
        @Max(value = 20, message = "A table has at most 20 seats")
        int seats
) {
}
