package ro.tss.restaurant.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

/**
 * An order placed at a table. It holds the business rules of an order:
 * only available menu items, allowed status transitions, computed total.
 * ("Order" is a reserved word in SQL, hence the class name CustomerOrder.)
 */
@Entity
@Table(name = "orders")
public class CustomerOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private RestaurantTable table;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "order_id")
    private List<OrderItem> items = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status = OrderStatus.NEW;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    protected CustomerOrder() {
        // required by JPA
    }

    public CustomerOrder(RestaurantTable table) {
        this.table = table;
    }

    public void addItem(MenuItem menuItem, int quantity) {
        if (!menuItem.isAvailable()) {
            throw new IllegalStateException("Menu item '" + menuItem.getName() + "' is not available");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        items.add(new OrderItem(menuItem, quantity));
    }

    public void changeStatus(OrderStatus next) {
        if (!status.canChangeTo(next)) {
            throw new IllegalStateException("Order cannot change from " + status + " to " + next);
        }
        status = next;
    }

    public BigDecimal total() {
        return items.stream().map(OrderItem::subtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public Long getId() { return id; }
    public RestaurantTable getTable() { return table; }
    public List<OrderItem> getItems() { return items; }
    public OrderStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
