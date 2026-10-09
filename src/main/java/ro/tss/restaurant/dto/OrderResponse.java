package ro.tss.restaurant.dto;

import java.math.BigDecimal;
import java.util.List;

import ro.tss.restaurant.model.CustomerOrder;
import ro.tss.restaurant.model.OrderItem;
import ro.tss.restaurant.model.OrderStatus;

/** An order as returned by the API, including its lines and the computed total. */
public record OrderResponse(Long id, int tableNumber, OrderStatus status, List<Line> items, BigDecimal total) {

    public record Line(String name, int quantity, BigDecimal unitPrice, BigDecimal subtotal) {

        static Line from(OrderItem item) {
            return new Line(item.getMenuItem().getName(), item.getQuantity(),
                    item.getUnitPrice(), item.subtotal());
        }
    }

    public static OrderResponse from(CustomerOrder order) {
        return new OrderResponse(order.getId(), order.getTable().getNumber(), order.getStatus(),
                order.getItems().stream().map(Line::from).toList(), order.total());
    }
}
