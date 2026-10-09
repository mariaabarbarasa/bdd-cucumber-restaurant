package ro.tss.restaurant.dto;

import java.math.BigDecimal;

import ro.tss.restaurant.model.Category;
import ro.tss.restaurant.model.MenuItem;

/** A menu item as returned by the API. */
public record MenuItemResponse(Long id, String name, String description, Category category,
                               BigDecimal price, boolean available) {

    public static MenuItemResponse from(MenuItem item) {
        return new MenuItemResponse(item.getId(), item.getName(), item.getDescription(),
                item.getCategory(), item.getPrice(), item.isAvailable());
    }
}
