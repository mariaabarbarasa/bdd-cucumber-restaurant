package ro.tss.restaurant.bdd;

import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import ro.tss.restaurant.support.PostgresTestConfiguration;

/**
 * Connects Cucumber to Spring: the whole application starts once, on a random port,
 * and the steps talk to it over HTTP, exactly like a real client.
 * The database is a PostgreSQL container started by Testcontainers.
 */
@CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(PostgresTestConfiguration.class)
public class CucumberSpringConfiguration {
}
