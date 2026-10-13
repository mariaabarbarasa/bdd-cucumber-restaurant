package ro.tss.restaurant.bdd;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import ro.tss.restaurant.dto.ReservationRequest;
import ro.tss.restaurant.dto.TableRequest;
import ro.tss.restaurant.model.Reservation;
import ro.tss.restaurant.model.RestaurantTable;
import ro.tss.restaurant.repository.ReservationRepository;
import ro.tss.restaurant.repository.RestaurantTableRepository;

/**
 * Steps for tables and reservations.
 */
public class TableAndReservationSteps {

    private final TestRestTemplate rest;
    private final RestaurantTableRepository tables;
    private final ReservationRepository reservations;
    private final ScenarioContext context;

    public TableAndReservationSteps(TestRestTemplate rest, RestaurantTableRepository tables,
                                    ReservationRepository reservations, ScenarioContext context) {
        this.rest = rest;
        this.tables = tables;
        this.reservations = reservations;
        this.context = context;
    }

    // ---------- Given ----------

    @Given("the restaurant has the following tables:")
    public void theRestaurantHasTables(List<Map<String, String>> rows) {
        rows.forEach(row -> tables.save(new RestaurantTable(
                Integer.parseInt(row.get("number")), Integer.parseInt(row.get("seats")))));
    }

    @Given("{string} has reserved table {int} for {int} people on {string}")
    public void hasReserved(String customer, int tableNumber, int people, String dateTime) {
        reservations.save(new Reservation(customer, "0700000000", table(tableNumber),
                LocalDateTime.parse(dateTime), people));
    }

    // ---------- When ----------

    @When("I add table number {int} with {int} seats")
    public void addTable(int number, int seats) {
        context.setLastResponse(rest.postForEntity("/api/tables", new TableRequest(number, seats), String.class));
    }

    @When("I update table {int} to number {int} with {int} seats")
    public void updateTable(int currentNumber, int newNumber, int seats) {
        RestaurantTable table = table(currentNumber);
        context.setLastResponse(rest.exchange("/api/tables/" + table.getId(), HttpMethod.PUT,
                new HttpEntity<>(new TableRequest(newNumber, seats)), String.class));
    }

    @When("I request all tables")
    public void requestAllTables() {
        context.setLastResponse(rest.getForEntity("/api/tables", String.class));
    }

    @When("I request table {int}")
    public void requestTable(int number) {
        context.setLastResponse(rest.getForEntity("/api/tables/" + table(number).getId(), String.class));
    }

    @When("I request table with id {long}")
    public void requestTableById(long id) {
        context.setLastResponse(rest.getForEntity("/api/tables/" + id, String.class));
    }

    @When("I delete table {int}")
    public void deleteTable(int number) {
        context.setLastResponse(rest.exchange("/api/tables/" + table(number).getId(), HttpMethod.DELETE,
                null, String.class));
    }

    @When("customer {string} with phone {string} reserves table {int} for {int} people on {string}")
    public void reserve(String customer, String phone, int tableNumber, int people, String dateTime) {
        ReservationRequest request = new ReservationRequest(customer, phone, table(tableNumber).getId(),
                LocalDateTime.parse(dateTime), people);
        context.setLastResponse(rest.postForEntity("/api/reservations", request, String.class));
    }

    @When("I move the reservation of {string} to {string}")
    public void moveReservation(String customer, String dateTime) {
        Reservation r = reservationOf(customer);
        ReservationRequest request = new ReservationRequest(r.getCustomerName(), r.getPhone(),
                r.getTable().getId(), LocalDateTime.parse(dateTime), r.getPartySize());
        context.setLastResponse(rest.exchange("/api/reservations/" + r.getId(), HttpMethod.PUT,
                new HttpEntity<>(request), String.class));
    }

    @When("I move the reservation of {string} to table {int}")
    public void moveReservationToTable(String customer, int tableNumber) {
        Reservation r = reservationOf(customer);
        ReservationRequest request = new ReservationRequest(r.getCustomerName(), r.getPhone(),
                table(tableNumber).getId(), r.getDateTime(), r.getPartySize());
        context.setLastResponse(rest.exchange("/api/reservations/" + r.getId(), HttpMethod.PUT,
                new HttpEntity<>(request), String.class));
    }

    @When("I cancel the reservation of {string}")
    public void cancelReservation(String customer) {
        context.setLastResponse(rest.exchange("/api/reservations/" + reservationOf(customer).getId(),
                HttpMethod.DELETE, null, String.class));
    }

    @When("I request all reservations")
    public void requestAllReservations() {
        context.setLastResponse(rest.getForEntity("/api/reservations", String.class));
    }

    @When("I request the reservation of {string}")
    public void requestReservation(String customer) {
        context.setLastResponse(rest.getForEntity("/api/reservations/" + reservationOf(customer).getId(), String.class));
    }

    // ---------- Then ----------

    @Then("the restaurant has {int} tables")
    public void restaurantHasTables(int count) {
        assertThat(tables.count()).isEqualTo(count);
    }

    @Then("table {int} has {int} seats")
    public void tableHasSeats(int tableNumber, int seats) {
        assertThat(table(tableNumber).getSeats()).isEqualTo(seats);
    }

    @Then("table {int} has {int} reservations")
    public void tableHasReservations(int tableNumber, int count) {
        long actual = reservations.findAll().stream()
                .filter(r -> r.getTable().getNumber() == tableNumber)
                .count();
        assertThat(actual).isEqualTo(count);
    }

    @Then("the reservation of {string} is on {string}")
    public void reservationIsOn(String customer, String dateTime) {
        assertThat(reservationOf(customer).getDateTime()).isEqualTo(LocalDateTime.parse(dateTime));
    }

    @Then("the reservation of {string} is at table {int}")
    public void reservationIsAtTable(String customer, int tableNumber) {
        assertThat(reservationOf(customer).getTable().getNumber()).isEqualTo(tableNumber);
    }

    private RestaurantTable table(int number) {
        return tables.findByNumber(number)
                .orElseThrow(() -> new AssertionError("Table " + number + " does not exist"));
    }

    private Reservation reservationOf(String customer) {
        return reservations.findAll().stream()
                .filter(r -> r.getCustomerName().equals(customer))
                .findFirst()
                .orElseThrow(() -> new AssertionError("No reservation for " + customer));
    }
}
