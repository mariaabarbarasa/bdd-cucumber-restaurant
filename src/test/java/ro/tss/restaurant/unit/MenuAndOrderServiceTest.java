package ro.tss.restaurant.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import ro.tss.restaurant.dto.MenuItemRequest;
import ro.tss.restaurant.dto.OrderRequest;
import ro.tss.restaurant.exception.BusinessRuleException;
import ro.tss.restaurant.exception.ResourceNotFoundException;
import ro.tss.restaurant.model.Category;
import ro.tss.restaurant.model.CustomerOrder;
import ro.tss.restaurant.model.MenuItem;
import ro.tss.restaurant.model.OrderStatus;
import ro.tss.restaurant.model.RestaurantTable;
import ro.tss.restaurant.repository.CustomerOrderRepository;
import ro.tss.restaurant.repository.MenuItemRepository;
import ro.tss.restaurant.repository.RestaurantTableRepository;
import ro.tss.restaurant.service.MenuService;
import ro.tss.restaurant.service.OrderService;

/**
 * Unit tests (Mockito) for MenuService and OrderService.
 */
class MenuAndOrderServiceTest {

    @Nested
    @ExtendWith(MockitoExtension.class)
    class Menu {

        @Mock
        private MenuItemRepository menuRepository;

        @Mock
        private CustomerOrderRepository orderRepository;

        @InjectMocks
        private MenuService service;

        @Test
        void createsMenuItem() {
            when(menuRepository.existsByNameIgnoreCase("Grilled Mici")).thenReturn(false);
            when(menuRepository.save(any(MenuItem.class))).thenAnswer(inv -> inv.getArgument(0));

            var response = service.create(new MenuItemRequest("Grilled Mici", null, Category.MAIN_COURSE,
                    new BigDecimal("32.00"), true));

            assertThat(response.name()).isEqualTo("Grilled Mici");
        }

        @Test
        void rejectsDuplicateNameIgnoringCase() {
            when(menuRepository.existsByNameIgnoreCase("papanasi")).thenReturn(true);

            assertThatThrownBy(() -> service.create(new MenuItemRequest("papanasi", null, Category.DESSERT,
                    new BigDecimal("25.00"), true)))
                    .isInstanceOf(BusinessRuleException.class);
            verify(menuRepository, never()).save(any());
        }

        @Test
        void cannotDeleteItemUsedInOrders() {
            MenuItem cabbageRolls = new MenuItem("Cabbage Rolls", null, Category.MAIN_COURSE, new BigDecimal("38.00"), true);
            when(menuRepository.findById(2L)).thenReturn(Optional.of(cabbageRolls));
            when(orderRepository.existsByItemsMenuItemId(2L)).thenReturn(true);

            assertThatThrownBy(() -> service.delete(2L)).isInstanceOf(BusinessRuleException.class);
            verify(menuRepository, never()).delete(any());
        }

        @Test
        void unknownItemGives404() {
            when(menuRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.findById(99L)).isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @ExtendWith(MockitoExtension.class)
    class Orders {

        @Mock
        private CustomerOrderRepository orderRepository;

        @Mock
        private RestaurantTableRepository tableRepository;

        @Mock
        private MenuItemRepository menuRepository;

        @InjectMocks
        private OrderService service;

        @Test
        void createsOrderWithComputedTotal() {
            MenuItem soup = new MenuItem("Tripe Soup", null, Category.APPETIZER, new BigDecimal("22.50"), true);
            when(tableRepository.findById(1L)).thenReturn(Optional.of(new RestaurantTable(5, 4)));
            when(menuRepository.findById(7L)).thenReturn(Optional.of(soup));
            when(orderRepository.save(any(CustomerOrder.class))).thenAnswer(inv -> inv.getArgument(0));

            var response = service.create(new OrderRequest(1L, List.of(new OrderRequest.Line(7L, 2))));

            assertThat(response.total()).isEqualByComparingTo("45.00");
            assertThat(response.status()).isEqualTo(OrderStatus.NEW);
        }

        @Test
        void unknownMenuItemGives404() {
            when(tableRepository.findById(1L)).thenReturn(Optional.of(new RestaurantTable(5, 4)));
            when(menuRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.create(new OrderRequest(1L, List.of(new OrderRequest.Line(99L, 1)))))
                    .isInstanceOf(ResourceNotFoundException.class);
            verify(orderRepository, never()).save(any());
        }

        @Test
        void servedOrderCannotBeDeleted() {
            CustomerOrder order = new CustomerOrder(new RestaurantTable(5, 4));
            ReflectionTestUtils.setField(order, "status", OrderStatus.SERVED);
            when(orderRepository.findById(3L)).thenReturn(Optional.of(order));

            assertThatThrownBy(() -> service.delete(3L)).isInstanceOf(BusinessRuleException.class);
            verify(orderRepository, never()).delete(any());
        }
    }
}
