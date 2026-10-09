package ro.tss.restaurant.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import ro.tss.restaurant.model.Category;

/** Data sent by the client to create or update a menu item. */
public record MenuItemRequest(
        @NotBlank(message = "Menu item name is required")
        String name,

        String description,

        @NotNull(message = "Category is required")
        Category category,

        @NotNull(message = "Price is required")
        @DecimalMin(value = "0.01", message = "Price must be positive")
        @Digits(integer = 8, fraction = 2, message = "Price can have at most 2 decimals")
        BigDecimal price,

        boolean available
) {
}
