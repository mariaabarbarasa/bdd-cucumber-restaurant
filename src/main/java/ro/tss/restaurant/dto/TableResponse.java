package ro.tss.restaurant.dto;

import ro.tss.restaurant.model.RestaurantTable;

/** A table as returned by the API. */
public record TableResponse(Long id, int number, int seats) {

    public static TableResponse from(RestaurantTable table) {
        return new TableResponse(table.getId(), table.getNumber(), table.getSeats());
    }
}
