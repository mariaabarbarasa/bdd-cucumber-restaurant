package ro.tss.restaurant.model;

import java.util.Set;

/**
 * The lifecycle of an order:
 * NEW -> IN_PREPARATION -> SERVED -> PAID, and a NEW order can also be CANCELLED.
 */
public enum OrderStatus {
    NEW,
    IN_PREPARATION,
    SERVED,
    PAID,
    CANCELLED;

    public boolean canChangeTo(OrderStatus next) {
        return switch (this) {
            case NEW -> Set.of(IN_PREPARATION, CANCELLED).contains(next);
            case IN_PREPARATION -> next == SERVED;
            case SERVED -> next == PAID;
            case PAID, CANCELLED -> false;
        };
    }
}
