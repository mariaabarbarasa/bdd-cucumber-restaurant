package ro.tss.restaurant.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.tss.restaurant.dto.TableRequest;
import ro.tss.restaurant.dto.TableResponse;
import ro.tss.restaurant.exception.BusinessRuleException;
import ro.tss.restaurant.exception.ResourceNotFoundException;
import ro.tss.restaurant.model.RestaurantTable;
import ro.tss.restaurant.repository.CustomerOrderRepository;
import ro.tss.restaurant.repository.ReservationRepository;
import ro.tss.restaurant.repository.RestaurantTableRepository;

/**
 * CRUD operations for the restaurant tables.
 */
@Service
@Transactional
public class TableService {

    private final RestaurantTableRepository tableRepository;
    private final ReservationRepository reservationRepository;
    private final CustomerOrderRepository orderRepository;

    public TableService(RestaurantTableRepository tableRepository, ReservationRepository reservationRepository,
                        CustomerOrderRepository orderRepository) {
        this.tableRepository = tableRepository;
        this.reservationRepository = reservationRepository;
        this.orderRepository = orderRepository;
    }

    public TableResponse create(TableRequest request) {
        if (tableRepository.existsByNumber(request.number())) {
            throw new BusinessRuleException("Table number " + request.number() + " already exists");
        }
        return TableResponse.from(tableRepository.save(new RestaurantTable(request.number(), request.seats())));
    }

    @Transactional(readOnly = true)
    public List<TableResponse> findAll() {
        return tableRepository.findAll().stream().map(TableResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public TableResponse findById(Long id) {
        return TableResponse.from(getTable(id));
    }

    public TableResponse update(Long id, TableRequest request) {
        RestaurantTable table = getTable(id);
        tableRepository.findByNumber(request.number())
                .filter(other -> !other.getId().equals(id))
                .ifPresent(other -> {
                    throw new BusinessRuleException("Table number " + request.number() + " already exists");
                });
        table.setNumber(request.number());
        table.setSeats(request.seats());
        return TableResponse.from(table);
    }

    public void delete(Long id) {
        RestaurantTable table = getTable(id);
        if (reservationRepository.existsByTableId(id) || orderRepository.existsByTableId(id)) {
            throw new BusinessRuleException("Table " + table.getNumber() + " has reservations or orders and cannot be deleted");
        }
        tableRepository.delete(table);
    }

    RestaurantTable getTable(Long id) {
        return tableRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Table", id));
    }
}
