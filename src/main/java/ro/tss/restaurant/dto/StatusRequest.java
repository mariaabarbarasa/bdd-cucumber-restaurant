package ro.tss.restaurant.dto;

import jakarta.validation.constraints.NotNull;
import ro.tss.restaurant.model.OrderStatus;

/** Data sent by the client to change the status of an order. */
public record StatusRequest(
        @NotNull(message = "Status is required")
        OrderStatus status
) {
}
