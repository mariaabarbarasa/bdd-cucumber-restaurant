package ro.tss.restaurant.bdd;

import io.cucumber.spring.ScenarioScope;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

/**
 * State shared between step classes during a single scenario
 * (a fresh instance for every scenario, thanks to @ScenarioScope).
 */
@Component
@ScenarioScope
public class ScenarioContext {

    private ResponseEntity<String> lastResponse;
    private Long currentOrderId;

    public ResponseEntity<String> getLastResponse() { return lastResponse; }
    public void setLastResponse(ResponseEntity<String> lastResponse) { this.lastResponse = lastResponse; }
    public Long getCurrentOrderId() { return currentOrderId; }
    public void setCurrentOrderId(Long currentOrderId) { this.currentOrderId = currentOrderId; }
}
