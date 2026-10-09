package ro.tss.restaurant.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import ro.tss.restaurant.model.Category;
import ro.tss.restaurant.model.MenuItem;

public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {

    Optional<MenuItem> findByName(String name);

    boolean existsByNameIgnoreCase(String name);

    List<MenuItem> findByCategory(Category category);
}
