package ro.tss.restaurant.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import ro.tss.restaurant.model.Category;
import ro.tss.restaurant.model.CustomerOrder;
import ro.tss.restaurant.model.MenuItem;
import ro.tss.restaurant.model.OrderStatus;
import ro.tss.restaurant.model.RestaurantTable;

/**
 * Unit tests for the rules in the CustomerOrder entity and the OrderStatus enum.
 * They run in milliseconds, without Spring and without a database.
 */
class CustomerOrderTest {

    private MenuItem soup;
    private MenuItem papanasi;
    private CustomerOrder order;

    @BeforeEach
    void setUp() {
        soup = new MenuItem("Tripe Soup", null, Category.APPETIZER, new BigDecimal("22.50"), true);
        papanasi = new MenuItem("Papanasi", null, Category.DESSERT, new BigDecimal("25.00"), true);
        order = new CustomerOrder(new RestaurantTable(5, 4));
    }

    @Test
    @DisplayName("A new order has status NEW and a zero total")
    void newOrderIsEmpty() {
        assertThat(order.getStatus()).isEqualTo(OrderStatus.NEW);
        assertThat(order.total()).isEqualByComparingTo("0");
    }

    @Test
    @DisplayName("The total is the sum of quantity x price for every line")
    void totalIsSumOfLines() {
        order.addItem(soup, 2);
        order.addItem(papanasi, 1);

        assertThat(order.total()).isEqualByComparingTo("70.00");
    }

    @Test
    @DisplayName("An unavailable menu item cannot be added")
    void unavailableItemIsRejected() {
        MenuItem lemonade = new MenuItem("Lemonade", null, Category.DRINK, new BigDecimal("15.00"), false);

        assertThatThrownBy(() -> order.addItem(lemonade, 1))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("is not available");
        assertThat(order.getItems()).isEmpty();
    }

    @Test
    @DisplayName("Quantity must be positive")
    void quantityMustBePositive() {
        assertThatThrownBy(() -> order.addItem(soup, 0))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("The price is fixed when the order is placed")
    void priceIsSnapshotted() {
        order.addItem(soup, 1);

        soup.setPrice(new BigDecimal("30.00"));

        assertThat(order.total()).isEqualByComparingTo("22.50");
    }

    @ParameterizedTest(name = "{0} -> {1} allowed = {2}")
    @DisplayName("Status transitions")
    @CsvSource({
            "NEW, IN_PREPARATION, true",
            "NEW, CANCELLED, true",
            "IN_PREPARATION, SERVED, true",
            "SERVED, PAID, true",
            "NEW, SERVED, false",
            "NEW, PAID, false",
            "IN_PREPARATION, NEW, false",
            "IN_PREPARATION, CANCELLED, false",
            "SERVED, NEW, false",
            "PAID, NEW, false",
            "PAID, CANCELLED, false",
            "CANCELLED, NEW, false"
    })
    void statusTransitions(OrderStatus from, OrderStatus to, boolean allowed) {
        assertThat(from.canChangeTo(to)).isEqualTo(allowed);
    }

    @Test
    @DisplayName("A forbidden transition throws and keeps the status")
    void forbiddenTransitionKeepsStatus() {
        assertThatThrownBy(() -> order.changeStatus(OrderStatus.PAID))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Order cannot change from NEW to PAID");
        assertThat(order.getStatus()).isEqualTo(OrderStatus.NEW);
    }
}
