# Behavior Driven Development with Cucumber

## 1. Requirements

| Tool | Version | Notes |
|---|---|---|
| JDK | **17 or newer** (tested with 21 and 25) | e.g. Eclipse Temurin 21 or Microsoft OpenJDK 21 |
| Maven | 3.9+ | IntelliJ IDEA ships with a bundled Maven, so no separate install is needed |
| IntelliJ IDEA | any recent version | optional; plugins **Gherkin** and **Cucumber for Java** recommended |

No database installation is needed: the application uses an **in-memory H2** database that is
created when the application starts and discarded when it stops. All dependencies are downloaded
by Maven on the first build (internet connection required once).

## 2. Running the application

### From IntelliJ IDEA
1. `File > Open...` and select the project folder (the one containing `pom.xml`).
2. If asked, set the project SDK: `File > Project Structure > Project > SDK` (JDK 17+).
3. Wait for the Maven import to finish.
4. Select the run configuration **RestaurantApplication** (top right) and press Run.
   Alternatively, open `src/main/java/ro/tss/restaurant/RestaurantApplication.java` and click the
   green arrow next to `main`.

### From the command line
```bash
mvn spring-boot:run
```

The API starts on **http://localhost:8080**.

### Trying the API
- Open `requests.http` in IntelliJ and run the requests from top to bottom (green arrow next to
  each request). It covers the happy paths and the error cases (400, 404, 409).
- Or use any HTTP client, e.g.:
  ```bash
  curl http://localhost:8080/api/menu
  ```
- **H2 console** (to inspect the database): http://localhost:8080/h2-console
  - JDBC URL: `jdbc:h2:mem:restaurant`, user: `sa`, password: *(empty)*

## 3. Running the tests

The project contains **95 automated tests**: 44 Cucumber scenarios and 51 JUnit tests.

| What | IntelliJ | Command line |
|---|---|---|
| All tests | run configuration **All tests** or **mvn test** | `mvn test` |
| Only the BDD scenarios | run configuration **BDD tests (Cucumber)**, or class `RunCucumberTest` | `mvn test -Dtest=RunCucumberTest` |
| One feature file / one scenario | open a `.feature` file and click the green arrow next to `Feature` or `Scenario` (requires the *Cucumber for Java* plugin) | – |
| Scenarios with a given tag | – | `mvn test -Dtest=RunCucumberTest -Dcucumber.filter.tags="@reservations"` |

Expected result:
```
Tests run: 95, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

After a run, Cucumber produces a **HTML report** (living documentation) at
`target/cucumber-reports/cucumber.html`. Open it in a browser to see every feature and scenario
with its status.

Available tags: `@menu`, `@reservations`, `@tables`, `@orders`, `@create`, `@read`, `@update`,
`@delete`, `@validation`.

## 4. What is tested and where

| Level | Location | Count | What it checks |
|---|---|---|---|
| BDD (end-to-end over HTTP) | `src/test/resources/features/*.feature` + `src/test/java/.../bdd` | 44 scenarios | the business behaviour, written in Gherkin |
| Unit – domain | `unit/CustomerOrderTest`, `unit/ReservationTest` | 25 | order rules, status transitions, 2-hour overlap boundaries |
| Unit – services (Mockito) | `unit/ReservationServiceTest`, `unit/MenuAndOrderServiceTest` | 12 | service logic with mocked repositories |
| Web layer (`@WebMvcTest`) | `web/MenuControllerTest`, `web/OrderControllerTest` | 9 | HTTP status codes, validation messages, JSON format |
| Persistence (`@DataJpaTest`) | `repository/RepositoryTest` | 5 | derived queries and database constraints |

The three feature files:
- `menu.feature` – CRUD for menu items and input validation (13 scenarios);
- `tables_and_reservations.feature` – tables, reservations, the 2-hour overlap rule tested on its
  boundary values (15 scenarios);
- `orders.feature` – placing orders, totals, price snapshot, the order lifecycle (16 scenarios).

How Cucumber is wired to Spring Boot:
- `RunCucumberTest` – JUnit 5 suite that runs the Cucumber engine on the `features` folder;
- `CucumberSpringConfiguration` – starts the whole application once on a random port;
- `ScenarioContext` – per-scenario state (`@ScenarioScope`) shared between step classes;
- `Hooks` – empties the database before every scenario, so scenarios are independent;
- `MenuSteps`, `TableAndReservationSteps`, `OrderSteps`, `ResponseSteps` – step definitions.
  `Given` steps prepare data through the repositories, `When` steps call the REST API with
  `TestRestTemplate`, `Then` steps assert on the response or on the stored data.

## 5. The API and its business rules

| Resource | Endpoints | Business rules |
|---|---|---|
| Menu | `POST/GET /api/menu`, `GET/PUT/DELETE /api/menu/{id}`, `?category=` | unique name; price > 0 with at most 2 decimals; an item used in orders cannot be deleted |
| Tables | `POST/GET /api/tables`, `GET/PUT/DELETE /api/tables/{id}` | unique number; 1–20 seats; a table with reservations or orders cannot be deleted |
| Reservations | `POST/GET /api/reservations`, `GET/PUT/DELETE /api/reservations/{id}` | the party fits at the table; no overlaps on the same table (2 hours); only in the future |
| Orders | `POST/GET /api/orders`, `GET/DELETE /api/orders/{id}`, `PUT /api/orders/{id}/status`, `?status=` | only available items; total computed by the server; price fixed at ordering time; `NEW -> IN_PREPARATION -> SERVED -> PAID` (or `NEW -> CANCELLED`); only new or cancelled orders can be deleted |

Response codes: `201` created (with `Location` header), `200` OK, `204` deleted, `400` invalid
data (one message per field), `404` not found, `409` business rule violated. Errors use the
standard *Problem Details* format (RFC 9457).

## 6. Project structure

```
src/main/java/ro/tss/restaurant
├── model/        entities and business rules (MenuItem, RestaurantTable, Reservation,
│                 CustomerOrder, OrderItem, Category, OrderStatus)
├── repository/   Spring Data JPA repositories
├── dto/          request/response records with Bean Validation
├── service/      application logic and transactions
├── web/          REST controllers
└── exception/    error types and the global exception handler
src/test/java/ro/tss/restaurant
├── bdd/          Cucumber runner, Spring configuration, hooks and step definitions
├── unit/         unit tests (JUnit 5, Mockito)
├── web/          web-layer tests (MockMvc)
└── repository/   persistence tests (H2)
src/test/resources/features   Gherkin specifications
```
