package ro.tss.restaurant.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

/** Data sent by the client to place an order: the table and the ordered lines. */
public record OrderRequest(
        @NotNull(message = "Table is required")
        Long tableId,

        @NotEmpty(message = "Order must contain at least one menu item")
        @Valid
        List<Line> items
) {
    public record Line(
            @NotNull(message = "Menu item is required")
            Long menuItemId,

            @Min(value = 1, message = "Quantity must be at least 1")
            int quantity
    ) {
    }
}
