package ro.tss.restaurant.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import ro.tss.restaurant.model.CustomerOrder;
import ro.tss.restaurant.model.OrderStatus;

public interface CustomerOrderRepository extends JpaRepository<CustomerOrder, Long> {

    List<CustomerOrder> findByStatus(OrderStatus status);

    boolean existsByTableId(Long tableId);

    boolean existsByItemsMenuItemId(Long menuItemId);
}
