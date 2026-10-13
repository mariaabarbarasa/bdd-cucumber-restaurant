package ro.tss.restaurant.bdd;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import ro.tss.restaurant.dto.OrderRequest;
import ro.tss.restaurant.dto.StatusRequest;
import ro.tss.restaurant.model.CustomerOrder;
import ro.tss.restaurant.model.MenuItem;
import ro.tss.restaurant.model.OrderStatus;
import ro.tss.restaurant.model.RestaurantTable;
import ro.tss.restaurant.repository.CustomerOrderRepository;
import ro.tss.restaurant.repository.MenuItemRepository;
import ro.tss.restaurant.repository.RestaurantTableRepository;

/**
 * Steps for orders. The id of the current order is kept in the ScenarioContext,
 * so scenarios talk about "the order", not about technical ids.
 */
public class OrderSteps {

    private static final String ORDERS_URL = "/api/orders";

    private final TestRestTemplate rest;
    private final CustomerOrderRepository orders;
    private final RestaurantTableRepository tables;
    private final MenuItemRepository menu;
    private final ScenarioContext context;
    private final ObjectMapper mapper;

    public OrderSteps(TestRestTemplate rest, CustomerOrderRepository orders, RestaurantTableRepository tables,
                      MenuItemRepository menu, ScenarioContext context, ObjectMapper mapper) {
        this.rest = rest;
        this.orders = orders;
        this.tables = tables;
        this.menu = menu;
        this.context = context;
        this.mapper = mapper;
    }

    // ---------- Given ----------

    @Given("table {int} has ordered {int} x {string}")
    public void hasOrdered(int tableNumber, int quantity, String itemName) {
        CustomerOrder order = new CustomerOrder(table(tableNumber));
        order.addItem(menuItem(itemName), quantity);
        context.setCurrentOrderId(orders.save(order).getId());
    }

    /** Puts the order directly into the wanted status (data setup, not the behaviour under test). */
    @Given("table {int} has an order with status {string}")
    public void hasOrderWithStatus(int tableNumber, String status) {
        CustomerOrder order = new CustomerOrder(table(tableNumber));
        order.addItem(menuItem("Tripe Soup"), 1);
        ReflectionTestUtils.setField(order, "status", OrderStatus.valueOf(status));
        context.setCurrentOrderId(orders.save(order).getId());
    }

    // ---------- When ----------

    @When("table {int} orders:")
    public void tableOrders(int tableNumber, List<Map<String, String>> rows) throws Exception {
        List<OrderRequest.Line> lines = rows.stream()
                .map(row -> new OrderRequest.Line(menuItem(row.get("item")).getId(),
                        Integer.parseInt(row.get("quantity"))))
                .toList();
        send(new OrderRequest(table(tableNumber).getId(), lines));
    }

    @When("table {int} sends an order without items")
    public void emptyOrder(int tableNumber) throws Exception {
        send(new OrderRequest(table(tableNumber).getId(), List.of()));
    }

    @When("I change the order status to {string}")
    public void changeStatus(String status) {
        context.setLastResponse(rest.exchange(ORDERS_URL + "/" + context.getCurrentOrderId() + "/status",
                HttpMethod.PUT, new HttpEntity<>(new StatusRequest(OrderStatus.valueOf(status))), String.class));
    }

    @When("I request the current order")
    public void requestCurrentOrder() {
        context.setLastResponse(rest.getForEntity(ORDERS_URL + "/" + context.getCurrentOrderId(), String.class));
    }

    @When("I request order {long}")
    public void requestOrder(long id) {
        context.setLastResponse(rest.getForEntity(ORDERS_URL + "/" + id, String.class));
    }

    @When("I request all orders")
    public void requestAllOrders() {
        context.setLastResponse(rest.getForEntity(ORDERS_URL, String.class));
    }

    @When("I request the orders with status {string}")
    public void requestOrdersByStatus(String status) {
        context.setLastResponse(rest.getForEntity(ORDERS_URL + "?status={s}", String.class, status));
    }

    @When("I delete the order")
    public void deleteOrder() {
        context.setLastResponse(rest.exchange(ORDERS_URL + "/" + context.getCurrentOrderId(),
                HttpMethod.DELETE, null, String.class));
    }

    // ---------- Then ----------

    @Then("the order has status {string}")
    public void orderHasStatus(String status) throws Exception {
        assertThat(currentOrder().get("status").asText()).isEqualTo(status);
    }

    @Then("the order total is {bigdecimal} lei")
    public void orderTotal(BigDecimal total) throws Exception {
        assertThat(currentOrder().get("total").decimalValue()).isEqualByComparingTo(total);
    }

    @Then("there are no orders")
    public void noOrders() {
        assertThat(orders.count()).isZero();
    }

    private void send(OrderRequest request) throws Exception {
        ResponseEntity<String> response = rest.postForEntity(ORDERS_URL, request, String.class);
        context.setLastResponse(response);
        if (response.getStatusCode().is2xxSuccessful()) {
            context.setCurrentOrderId(mapper.readTree(response.getBody()).get("id").asLong());
        }
    }

    /** Reads the current order through the API, as a client would see it. */
    private JsonNode currentOrder() throws Exception {
        String body = rest.getForObject(ORDERS_URL + "/" + context.getCurrentOrderId(), String.class);
        return mapper.readTree(body);
    }

    private RestaurantTable table(int number) {
        return tables.findByNumber(number).orElseThrow(() -> new AssertionError("Table " + number + " does not exist"));
    }

    private MenuItem menuItem(String name) {
        return menu.findByName(name).orElseThrow(() -> new AssertionError("Menu item " + name + " does not exist"));
    }
}
