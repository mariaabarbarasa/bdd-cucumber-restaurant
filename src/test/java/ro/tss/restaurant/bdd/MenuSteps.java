package ro.tss.restaurant.bdd;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import ro.tss.restaurant.dto.MenuItemRequest;
import ro.tss.restaurant.model.Category;
import ro.tss.restaurant.model.MenuItem;
import ro.tss.restaurant.repository.MenuItemRepository;

/**
 * Steps for the menu. "Given" prepares data directly in the database,
 * "When" sends HTTP requests to the API, "Then" checks the effect.
 */
public class MenuSteps {

    private static final String MENU_URL = "/api/menu";

    private final TestRestTemplate rest;
    private final MenuItemRepository repository;
    private final ScenarioContext context;

    public MenuSteps(TestRestTemplate rest, MenuItemRepository repository, ScenarioContext context) {
        this.rest = rest;
        this.repository = repository;
        this.context = context;
    }

    // ---------- Given ----------

    @Given("the menu contains the following items:")
    public void theMenuContains(List<Map<String, String>> rows) {
        rows.forEach(row -> repository.save(new MenuItem(
                row.get("name"),
                null,
                Category.valueOf(row.get("category")),
                new BigDecimal(row.get("price")),
                "yes".equals(row.get("available")))));
    }

    // ---------- When ----------

    @When("I add the menu item {string} in category {string} with price {bigdecimal}")
    public void addMenuItem(String name, String category, BigDecimal price) {
        Category parsedCategory = category.isBlank() ? null : Category.valueOf(category);
        MenuItemRequest request = new MenuItemRequest(name, null, parsedCategory, price, true);
        context.setLastResponse(rest.postForEntity(MENU_URL, request, String.class));
    }

    @When("I request the menu")
    public void requestMenu() {
        context.setLastResponse(rest.getForEntity(MENU_URL, String.class));
    }

    @When("I request the items in category {string}")
    public void requestCategory(String category) {
        context.setLastResponse(rest.getForEntity(MENU_URL + "?category={c}", String.class, category));
    }

    @When("I request the menu item with id {long}")
    public void requestMenuItem(long id) {
        context.setLastResponse(rest.getForEntity(MENU_URL + "/" + id, String.class));
    }

    @When("I request the menu item named {string}")
    public void requestMenuItemByName(String name) {
        context.setLastResponse(rest.getForEntity(MENU_URL + "/" + find(name).getId(), String.class));
    }

    @When("I rename the menu item {string} to {string}")
    public void renameMenuItem(String currentName, String newName) {
        MenuItem item = find(currentName);
        update(item, new MenuItemRequest(newName, item.getDescription(), item.getCategory(),
                item.getPrice(), item.isAvailable()));
    }

    @When("I change the price of {string} to {bigdecimal}")
    public void changePrice(String name, BigDecimal price) {
        MenuItem item = find(name);
        update(item, new MenuItemRequest(item.getName(), item.getDescription(), item.getCategory(),
                price, item.isAvailable()));
    }

    @When("I mark {string} as unavailable")
    public void markUnavailable(String name) {
        MenuItem item = find(name);
        update(item, new MenuItemRequest(item.getName(), item.getDescription(), item.getCategory(),
                item.getPrice(), false));
    }

    @When("I delete the menu item {string}")
    public void deleteMenuItem(String name) {
        context.setLastResponse(rest.exchange(MENU_URL + "/" + find(name).getId(), HttpMethod.DELETE,
                null, String.class));
    }

    // ---------- Then ----------

    @Then("the menu contains {int} items")
    public void menuContains(int count) {
        assertThat(repository.count()).isEqualTo(count);
    }

    @Then("{string} costs {bigdecimal} lei")
    public void itemCosts(String name, BigDecimal price) {
        assertThat(find(name).getPrice()).isEqualByComparingTo(price);
    }

    @Then("{string} is not available")
    public void isNotAvailable(String name) {
        assertThat(find(name).isAvailable()).isFalse();
    }

    @Then("{string} is no longer on the menu")
    public void isNoLongerOnMenu(String name) {
        assertThat(repository.findByName(name)).isEmpty();
    }

    private void update(MenuItem item, MenuItemRequest request) {
        context.setLastResponse(rest.exchange(MENU_URL + "/" + item.getId(), HttpMethod.PUT,
                new HttpEntity<>(request), String.class));
    }

    private MenuItem find(String name) {
        return repository.findByName(name)
                .orElseThrow(() -> new AssertionError("Menu item '" + name + "' does not exist"));
    }
}
