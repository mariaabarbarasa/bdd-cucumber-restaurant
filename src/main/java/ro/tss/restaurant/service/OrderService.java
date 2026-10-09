package ro.tss.restaurant.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.tss.restaurant.dto.OrderRequest;
import ro.tss.restaurant.dto.OrderResponse;
import ro.tss.restaurant.exception.BusinessRuleException;
import ro.tss.restaurant.exception.ResourceNotFoundException;
import ro.tss.restaurant.model.CustomerOrder;
import ro.tss.restaurant.model.MenuItem;
import ro.tss.restaurant.model.OrderStatus;
import ro.tss.restaurant.model.RestaurantTable;
import ro.tss.restaurant.repository.CustomerOrderRepository;
import ro.tss.restaurant.repository.MenuItemRepository;
import ro.tss.restaurant.repository.RestaurantTableRepository;

/**
 * Orders: placing, viewing, changing the status and deleting.
 * The actual rules (availability, transitions) live in the CustomerOrder entity.
 */
@Service
@Transactional
public class OrderService {

    private final CustomerOrderRepository orderRepository;
    private final RestaurantTableRepository tableRepository;
    private final MenuItemRepository menuRepository;

    public OrderService(CustomerOrderRepository orderRepository, RestaurantTableRepository tableRepository,
                        MenuItemRepository menuRepository) {
        this.orderRepository = orderRepository;
        this.tableRepository = tableRepository;
        this.menuRepository = menuRepository;
    }

    public OrderResponse create(OrderRequest request) {
        RestaurantTable table = tableRepository.findById(request.tableId())
                .orElseThrow(() -> new ResourceNotFoundException("Table", request.tableId()));
        CustomerOrder order = new CustomerOrder(table);
        for (OrderRequest.Line line : request.items()) {
            MenuItem item = menuRepository.findById(line.menuItemId())
                    .orElseThrow(() -> new ResourceNotFoundException("Menu item", line.menuItemId()));
            order.addItem(item, line.quantity());
        }
        return OrderResponse.from(orderRepository.save(order));
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> findAll(OrderStatus status) {
        List<CustomerOrder> orders = status == null ? orderRepository.findAll() : orderRepository.findByStatus(status);
        return orders.stream().map(OrderResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public OrderResponse findById(Long id) {
        return OrderResponse.from(getOrder(id));
    }

    public OrderResponse changeStatus(Long id, OrderStatus status) {
        CustomerOrder order = getOrder(id);
        order.changeStatus(status);
        return OrderResponse.from(order);
    }

    public void delete(Long id) {
        CustomerOrder order = getOrder(id);
        if (order.getStatus() != OrderStatus.NEW && order.getStatus() != OrderStatus.CANCELLED) {
            throw new BusinessRuleException("Only new or cancelled orders can be deleted");
        }
        orderRepository.delete(order);
    }

    private CustomerOrder getOrder(Long id) {
        return orderRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Order", id));
    }
}
