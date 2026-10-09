package ro.tss.restaurant.bdd;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.en.Then;

/**
 * Generic steps that check HTTP responses, reused by all .feature files.
 */
public class ResponseSteps {

    private final ScenarioContext context;
    private final ObjectMapper mapper;

    public ResponseSteps(ScenarioContext context, ObjectMapper mapper) {
        this.context = context;
        this.mapper = mapper;
    }

    @Then("the response status is {int}")
    public void responseStatus(int status) {
        assertThat(context.getLastResponse().getStatusCode().value())
                .as("response body: %s", context.getLastResponse().getBody())
                .isEqualTo(status);
    }

    @Then("the response contains the name {string}")
    public void responseContainsName(String name) throws Exception {
        assertThat(body().get("name").asText()).isEqualTo(name);
    }

    @Then("the list contains {int} element(s)")
    public void listContains(int count) throws Exception {
        assertThat(body().isArray()).isTrue();
        assertThat(body().size()).isEqualTo(count);
    }

    @Then("the error message contains {string}")
    public void errorMessageContains(String fragment) throws Exception {
        assertThat(body().get("detail").asText()).contains(fragment);
    }

    @Then("the error for field {string} is {string}")
    public void errorForField(String field, String message) throws Exception {
        assertThat(body().path("errors").path(field).asText()).isEqualTo(message);
    }

    private JsonNode body() throws Exception {
        return mapper.readTree(context.getLastResponse().getBody());
    }
}
