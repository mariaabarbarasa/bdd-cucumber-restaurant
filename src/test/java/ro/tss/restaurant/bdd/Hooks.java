package ro.tss.restaurant.bdd;

import io.cucumber.java.Before;
import ro.tss.restaurant.repository.CustomerOrderRepository;
import ro.tss.restaurant.repository.MenuItemRepository;
import ro.tss.restaurant.repository.ReservationRepository;
import ro.tss.restaurant.repository.RestaurantTableRepository;

/**
 * Before every scenario the database is emptied, so scenarios are independent.
 * The order matters: tables that reference other tables (orders, reservations) go first.
 */
public class Hooks {

    private final CustomerOrderRepository orders;
    private final ReservationRepository reservations;
    private final MenuItemRepository menu;
    private final RestaurantTableRepository tables;

    public Hooks(CustomerOrderRepository orders, ReservationRepository reservations,
                 MenuItemRepository menu, RestaurantTableRepository tables) {
        this.orders = orders;
        this.reservations = reservations;
        this.menu = menu;
        this.tables = tables;
    }

    @Before
    public void cleanDatabase() {
        orders.deleteAll();
        reservations.deleteAll();
        menu.deleteAll();
        tables.deleteAll();
    }
}
