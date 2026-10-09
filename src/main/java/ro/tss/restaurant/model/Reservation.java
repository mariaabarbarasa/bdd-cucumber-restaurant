package ro.tss.restaurant.model;

import java.time.Duration;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * A table reservation for a customer at a given date and time.
 * A reservation occupies the table for {@link #DURATION}.
 */
@Entity
@Table(name = "reservations")
public class Reservation {

    public static final Duration DURATION = Duration.ofHours(2);

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String customerName;

    @Column(nullable = false)
    private String phone;

    @ManyToOne(optional = false)
    private RestaurantTable table;

    @Column(nullable = false)
    private LocalDateTime dateTime;

    @Column(nullable = false)
    private int partySize;

    protected Reservation() {
        // required by JPA
    }

    public Reservation(String customerName, String phone, RestaurantTable table,
                       LocalDateTime dateTime, int partySize) {
        this.customerName = customerName;
        this.phone = phone;
        this.table = table;
        this.dateTime = dateTime;
        this.partySize = partySize;
    }

    /** Two reservations overlap if they start less than 2 hours apart. */
    public boolean overlaps(LocalDateTime otherStart) {
        return Duration.between(dateTime, otherStart).abs().compareTo(DURATION) < 0;
    }

    public Long getId() { return id; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public RestaurantTable getTable() { return table; }
    public void setTable(RestaurantTable table) { this.table = table; }
    public LocalDateTime getDateTime() { return dateTime; }
    public void setDateTime(LocalDateTime dateTime) { this.dateTime = dateTime; }
    public int getPartySize() { return partySize; }
    public void setPartySize(int partySize) { this.partySize = partySize; }
}
