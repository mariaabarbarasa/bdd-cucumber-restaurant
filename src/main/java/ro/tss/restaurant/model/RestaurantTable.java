package ro.tss.restaurant.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * A table in the restaurant, identified by its number and having a number of seats.
 */
@Entity
@Table(name = "restaurant_tables")
public class RestaurantTable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private int number;

    @Column(nullable = false)
    private int seats;

    protected RestaurantTable() {
        // required by JPA
    }

    public RestaurantTable(int number, int seats) {
        this.number = number;
        this.seats = seats;
    }

    public boolean canSeat(int people) {
        return people <= seats;
    }

    public Long getId() { return id; }
    public int getNumber() { return number; }
    public void setNumber(int number) { this.number = number; }
    public int getSeats() { return seats; }
    public void setSeats(int seats) { this.seats = seats; }
}
