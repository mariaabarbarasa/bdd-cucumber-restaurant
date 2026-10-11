package ro.tss.restaurant.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import ro.tss.restaurant.model.Category;
import ro.tss.restaurant.model.CustomerOrder;
import ro.tss.restaurant.model.MenuItem;
import ro.tss.restaurant.model.Reservation;
import ro.tss.restaurant.model.RestaurantTable;
import ro.tss.restaurant.support.PostgresTestConfiguration;

/**
 * Persistence layer tests on PostgreSQL (Testcontainers, schema created by Flyway):
 * queries derived from method names and the database constraints.
 * Every test runs in a transaction that is rolled back at the end.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Import(PostgresTestConfiguration.class)
class RepositoryTest {

    @Autowired private MenuItemRepository menu;
    @Autowired private RestaurantTableRepository tables;
    @Autowired private ReservationRepository reservations;
    @Autowired private CustomerOrderRepository orders;

    private MenuItem cabbageRolls;
    private RestaurantTable table3;

    @BeforeEach
    void setUp() {
        cabbageRolls = menu.save(new MenuItem("Cabbage Rolls", null, Category.MAIN_COURSE, new BigDecimal("38.00"), true));
        menu.save(new MenuItem("Papanasi", null, Category.DESSERT, new BigDecimal("25.00"), true));
        table3 = tables.save(new RestaurantTable(3, 8));
    }

    @Test
    void findsByCategory() {
        assertThat(menu.findByCategory(Category.DESSERT)).extracting(MenuItem::getName).containsExactly("Papanasi");
    }

    @Test
    void nameCheckIgnoresCase() {
        assertThat(menu.existsByNameIgnoreCase("CABBAGE ROLLS")).isTrue();
    }

    @Test
    void menuItemNameIsUniqueInDatabase() {
        assertThatThrownBy(() -> menu.saveAndFlush(
                new MenuItem("Cabbage Rolls", null, Category.MAIN_COURSE, BigDecimal.TEN, true)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void findsReservationsInTimeWindow() {
        LocalDateTime seven = LocalDateTime.of(2030, 12, 20, 19, 0);
        reservations.save(new Reservation("Maria Ionescu", "0722000000", table3, seven, 4));

        assertThat(reservations.findByTableIdAndDateTimeBetween(table3.getId(), seven.minusHours(2), seven.plusHours(2)))
                .hasSize(1);
        assertThat(reservations.findByTableIdAndDateTimeBetween(table3.getId(), seven.plusHours(3), seven.plusHours(5)))
                .isEmpty();
    }

    @Test
    void detectsMenuItemUsedInOrders() {
        CustomerOrder order = new CustomerOrder(table3);
        order.addItem(cabbageRolls, 2);
        orders.save(order);

        assertThat(orders.existsByItemsMenuItemId(cabbageRolls.getId())).isTrue();
        assertThat(orders.existsByTableId(table3.getId())).isTrue();
    }
}
