@reservations
Feature: Restaurant tables and reservations
  As a receptionist
  I want to manage the tables and record reservations without overlaps
  So that no guest arrives at a table that is already taken

  Business rules: a reservation occupies the table for 2 hours;
  the party size cannot exceed the number of seats at the table.

  Background:
    Given the restaurant has the following tables:
      | number | seats |
      | 1      | 2     |
      | 2      | 4     |
      | 3      | 8     |

  @tables
  Scenario: Adding a new table
    When I add table number 4 with 6 seats
    Then the response status is 201
    And the restaurant has 4 tables

  @tables
  Scenario: Two tables cannot have the same number
    When I add table number 2 with 6 seats
    Then the response status is 409
    And the error message contains "Table number 2 already exists"

  Scenario: Reserving a free table
    When customer "Andrei Popescu" with phone "0722123456" reserves table 2 for 4 people on "2030-12-20T19:00"
    Then the response status is 201
    And table 2 has 1 reservations

  Scenario: A reservation is refused if the party does not fit at the table
    When customer "Andrei Popescu" with phone "0722123456" reserves table 1 for 5 people on "2030-12-20T19:00"
    Then the response status is 409
    And the error message contains "Table 1 has only 2 seats"
    And table 1 has 0 reservations

  Scenario Outline: A table cannot be reserved twice in the same time slot
    Given "Maria Ionescu" has reserved table 3 for 6 people on "2030-12-20T19:00"
    When customer "Andrei Popescu" with phone "0722123456" reserves table 3 for 2 people on "<date and time>"
    Then the response status is <status>

    Examples: boundary values of the 2-hour interval
      | date and time    | status | explanation                      |
      | 2030-12-20T17:00 | 201    | ends exactly when the other one starts |
      | 2030-12-20T17:01 | 409    | overlaps by one minute           |
      | 2030-12-20T19:00 | 409    | same time                        |
      | 2030-12-20T20:59 | 409    | starts before 21:00              |
      | 2030-12-20T21:00 | 201    | starts exactly when the table is free |

  Scenario: Moving a reservation to another time
    Given "Maria Ionescu" has reserved table 3 for 6 people on "2030-12-20T19:00"
    When I move the reservation of "Maria Ionescu" to "2030-12-20T21:30"
    Then the response status is 200
    And the reservation of "Maria Ionescu" is on "2030-12-20T21:30"

  Scenario: Cancelling a reservation
    Given "Maria Ionescu" has reserved table 3 for 6 people on "2030-12-20T19:00"
    When I cancel the reservation of "Maria Ionescu"
    Then the response status is 204
    And table 3 has 0 reservations

  Scenario: A table with reservations cannot be deleted
    Given "Maria Ionescu" has reserved table 3 for 6 people on "2030-12-20T19:00"
    When I delete table 3
    Then the response status is 409
    And the restaurant has 3 tables

  @validation
  Scenario Outline: Invalid reservation data
    When customer "<customer>" with phone "<phone>" reserves table 2 for 2 people on "<date and time>"
    Then the response status is 400
    And the error for field "<field>" is "<message>"

    Examples:
      | customer       | phone      | date and time    | field        | message                                 |
      |                | 0722123456 | 2030-12-20T19:00 | customerName | Customer name is required               |
      | Andrei Popescu | 12345      | 2030-12-20T19:00 | phone        | Phone must have the format 07xxxxxxxx   |
      | Andrei Popescu | 0722123456 | 2020-01-10T19:00 | dateTime     | Reservation must be in the future       |
