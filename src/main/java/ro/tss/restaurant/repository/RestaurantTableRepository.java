package ro.tss.restaurant.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import ro.tss.restaurant.model.RestaurantTable;

public interface RestaurantTableRepository extends JpaRepository<RestaurantTable, Long> {

    Optional<RestaurantTable> findByNumber(int number);

    boolean existsByNumber(int number);
}
