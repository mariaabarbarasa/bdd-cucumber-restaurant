# Behavior Driven Development with Cucumber

## 1. Requirements

| Tool | Version | Notes |
|---|---|---|
| JDK | **17 or newer** (tested with 21 and 25) | e.g. Eclipse Temurin 21 or Microsoft OpenJDK 21 |
| Maven | 3.9+ | IntelliJ IDEA ships with a bundled Maven, so no separate install is needed |
| IntelliJ IDEA | any recent version | optional; plugins **Gherkin** and **Cucumber for Java** recommended |
| Docker | Docker Desktop (Mac/Windows) or Docker Engine (Linux) | **must be running** for the application and for the tests |

The application uses **PostgreSQL 17** running in a Docker container. No local PostgreSQL
installation is needed. All dependencies (and the `postgres:17-alpine` image) are downloaded on the
first build/run (internet connection required once).

## 2. The database

| What | Where | Details |
|---|---|---|
| Container | `compose.yaml` | `postgres:17-alpine`, container `restaurant-postgres`, port `5432`, data kept in the Docker volume `restaurant-data` |
| Connection | `application.properties` | `jdbc:postgresql://localhost:5432/restaurant`, user `restaurant`, password `restaurant` |
| Schema | `src/main/resources/db/migration/V1__create_schema.sql` | created by **Flyway** at start-up; Hibernate only validates it (`ddl-auto=validate`) |
| Demo data | `src/main/resources/db/seed/V100__demo_data.sql` | inserted by Flyway the first time the application starts on an empty database |

Flyway records the applied scripts in the table `flyway_schema_history`, so every script runs only
once. To change the schema, **add a new script** (`V2__...sql`, `V3__...sql`); never edit a script
that has already been applied.

Demo data (the same tables and menu as in the `.feature` files):

| Tables | Menu | Reservations | Orders |
|---|---|---|---|
| no. 1 (2 seats), no. 2 (4), no. 3 (8), no. 5 (4) | Tripe Soup 22.50, Cabbage Rolls with Polenta 38.00, Papanasi 25.00, Lemonade 15.00 (*unavailable*) | Maria Ionescu, table 3, 2030-12-20 19:00, 6 people | table 5, `NEW`: 2 x Tripe Soup + 1 x Papanasi = 70.00 |

Useful commands (from the project folder):
```bash
docker compose up -d        # start PostgreSQL by hand
docker compose stop         # stop it (data is kept)
docker compose down -v      # delete the container AND the data -> demo data again on next start
docker exec -it restaurant-postgres psql -U restaurant -d restaurant   # SQL console
```
In IntelliJ you can also inspect the database from the **Database** tool window
(`+ > Data Source > PostgreSQL`, with the connection details above).

## 3. Running the application

Start **Docker Desktop** first. When the application starts, Spring Boot runs
`docker compose up` for `compose.yaml` (module `spring-boot-docker-compose`), waits until PostgreSQL
is ready, and Flyway creates the schema and inserts the demo data. The container keeps running after
the application stops, so the data is kept between runs.

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

### Troubleshooting
- *Unable to find docker / Cannot connect to the Docker daemon*: Docker Desktop is not running, or
  the `docker` command is not on the `PATH` seen by IntelliJ (Docker Desktop installs it in
  `~/.docker/bin`). Start Docker Desktop and restart IntelliJ, or start the database by hand with
  `docker compose up -d` and run the application with `spring.docker.compose.enabled=false`.
- *Port 5432 is already in use*: another PostgreSQL is running locally. Stop it, or change the
  port in `compose.yaml` (e.g. `"5433:5432"`) and in `spring.datasource.url`.
- *Duplicate ids / 409 in `requests.http`*: the requests assume a fresh database; reset it with
  `docker compose down -v`.

### Trying the API
- Open `requests.http` in IntelliJ and run the requests from top to bottom (green arrow next to
  each request). It starts from the demo data and covers the happy paths and the error cases
  (400, 409).
- Or use any HTTP client, e.g.:
  ```bash
  curl http://localhost:8080/api/menu
  ```

## 4. Running the tests

The project contains **95 automated tests**: 44 Cucumber scenarios and 51 JUnit tests.

The tests that need a database (the Cucumber scenarios and `RepositoryTest`) run on a **separate,
throw-away PostgreSQL container** started by **Testcontainers** (`support/PostgresTestConfiguration`),
so **Docker must be running**, but the database of the application (`compose.yaml`) is not touched.
The schema is created by the same Flyway migrations as in production; the demo data is *not*
loaded (`application-test.properties`), because every scenario prepares its own data in its
`Given` steps. The first run downloads the `postgres:17-alpine` image.

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

## 5. What is tested and where

| Level | Location | Count | What it checks |
|---|---|---|---|
| BDD (end-to-end over HTTP) | `src/test/resources/features/*.feature` + `src/test/java/.../bdd` | 44 scenarios | the business behaviour, written in Gherkin |
| Unit – domain | `unit/CustomerOrderTest`, `unit/ReservationTest` | 25 | order rules, status transitions, 2-hour overlap boundaries |
| Unit – services (Mockito) | `unit/ReservationServiceTest`, `unit/MenuAndOrderServiceTest` | 12 | service logic with mocked repositories |
| Web layer (`@WebMvcTest`) | `web/MenuControllerTest`, `web/OrderControllerTest` | 9 | HTTP status codes, validation messages, JSON format |
| Persistence (`@DataJpaTest`) | `repository/RepositoryTest` | 5 | derived queries and database constraints, on PostgreSQL |

The three feature files:
- `menu.feature` – CRUD for menu items and input validation (13 scenarios);
- `tables_and_reservations.feature` – tables, reservations, the 2-hour overlap rule tested on its
  boundary values (15 scenarios);
- `orders.feature` – placing orders, totals, price snapshot, the order lifecycle (16 scenarios).

How Cucumber is wired to Spring Boot:
- `RunCucumberTest` – JUnit 5 suite that runs the Cucumber engine on the `features` folder;
- `CucumberSpringConfiguration` – starts the whole application once on a random port, with a
  PostgreSQL container (Testcontainers);
- `ScenarioContext` – per-scenario state (`@ScenarioScope`) shared between step classes;
- `Hooks` – empties the database before every scenario, so scenarios are independent;
- `MenuSteps`, `TableAndReservationSteps`, `OrderSteps`, `ResponseSteps` – step definitions.
  `Given` steps prepare data through the repositories, `When` steps call the REST API with
  `TestRestTemplate`, `Then` steps assert on the response or on the stored data.

## 6. The API and its business rules

| Resource | Endpoints | Business rules |
|---|---|---|
| Menu | `POST/GET /api/menu`, `GET/PUT/DELETE /api/menu/{id}`, `?category=` | unique name; price > 0 with at most 2 decimals; an item used in orders cannot be deleted |
| Tables | `POST/GET /api/tables`, `GET/PUT/DELETE /api/tables/{id}` | unique number; 1–20 seats; a table with reservations or orders cannot be deleted |
| Reservations | `POST/GET /api/reservations`, `GET/PUT/DELETE /api/reservations/{id}` | the party fits at the table; no overlaps on the same table (2 hours); only in the future |
| Orders | `POST/GET /api/orders`, `GET/DELETE /api/orders/{id}`, `PUT /api/orders/{id}/status`, `?status=` | only available items; total computed by the server; price fixed at ordering time; `NEW -> IN_PREPARATION -> SERVED -> PAID` (or `NEW -> CANCELLED`); only new or cancelled orders can be deleted |

Response codes: `201` created (with `Location` header), `200` OK, `204` deleted, `400` invalid
data (one message per field), `404` not found, `409` business rule violated. Errors use the
standard *Problem Details* format (RFC 9457).

## 7. Project structure

```
src/main/java/ro/tss/restaurant
├── model/        entities and business rules (MenuItem, RestaurantTable, Reservation,
│                 CustomerOrder, OrderItem, Category, OrderStatus)
├── repository/   Spring Data JPA repositories
├── dto/          request/response records with Bean Validation
├── service/      application logic and transactions
├── web/          REST controllers
└── exception/    error types and the global exception handler
src/main/resources
├── application.properties   PostgreSQL connection, Flyway, Docker Compose
├── db/migration/            Flyway schema migrations (V1__create_schema.sql, ...)
└── db/seed/                 demo data, only for the running application (V100__demo_data.sql)
src/test/java/ro/tss/restaurant
├── bdd/          Cucumber runner, Spring configuration, hooks and step definitions
├── unit/         unit tests (JUnit 5, Mockito)
├── web/          web-layer tests (MockMvc)
├── repository/   persistence tests (PostgreSQL via Testcontainers)
└── support/      PostgresTestConfiguration (the PostgreSQL test container)
src/test/resources
├── features/                    Gherkin specifications
└── application-test.properties  test profile: schema only, no demo data, no Docker Compose
compose.yaml                     PostgreSQL container for running the application
requests.http                    manual API requests (IntelliJ HTTP Client)
```
