package ro.tss.restaurant.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.tss.restaurant.dto.MenuItemRequest;
import ro.tss.restaurant.dto.MenuItemResponse;
import ro.tss.restaurant.exception.BusinessRuleException;
import ro.tss.restaurant.exception.ResourceNotFoundException;
import ro.tss.restaurant.model.Category;
import ro.tss.restaurant.model.MenuItem;
import ro.tss.restaurant.repository.CustomerOrderRepository;
import ro.tss.restaurant.repository.MenuItemRepository;

/**
 * CRUD operations for the restaurant menu.
 */
@Service
@Transactional
public class MenuService {

    private final MenuItemRepository menuRepository;
    private final CustomerOrderRepository orderRepository;

    public MenuService(MenuItemRepository menuRepository, CustomerOrderRepository orderRepository) {
        this.menuRepository = menuRepository;
        this.orderRepository = orderRepository;
    }

    public MenuItemResponse create(MenuItemRequest request) {
        if (menuRepository.existsByNameIgnoreCase(request.name())) {
            throw new BusinessRuleException("A menu item named " + request.name() + " already exists");
        }
        MenuItem item = new MenuItem(request.name(), request.description(), request.category(),
                request.price(), request.available());
        return MenuItemResponse.from(menuRepository.save(item));
    }

    @Transactional(readOnly = true)
    public List<MenuItemResponse> findAll(Category category) {
        List<MenuItem> items = category == null ? menuRepository.findAll() : menuRepository.findByCategory(category);
        return items.stream().map(MenuItemResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public MenuItemResponse findById(Long id) {
        return MenuItemResponse.from(getItem(id));
    }

    public MenuItemResponse update(Long id, MenuItemRequest request) {
        MenuItem item = getItem(id);
        menuRepository.findByName(request.name())
                .filter(other -> !other.getId().equals(id))
                .ifPresent(other -> {
                    throw new BusinessRuleException("A menu item named " + request.name() + " already exists");
                });
        item.setName(request.name());
        item.setDescription(request.description());
        item.setCategory(request.category());
        item.setPrice(request.price());
        item.setAvailable(request.available());
        return MenuItemResponse.from(item);
    }

    public void delete(Long id) {
        MenuItem item = getItem(id);
        if (orderRepository.existsByItemsMenuItemId(id)) {
            throw new BusinessRuleException("Menu item is used in orders and cannot be deleted; mark it unavailable instead");
        }
        menuRepository.delete(item);
    }

    MenuItem getItem(Long id) {
        return menuRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Menu item", id));
    }
}
