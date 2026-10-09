@orders
Feature: Customer orders
  As a waiter
  I want to record the orders of each table and follow them until payment
  So that the kitchen knows what to prepare and the bill is correct

  Background:
    Given the restaurant has the following tables:
      | number | seats |
      | 5      | 4     |
    And the menu contains the following items:
      | name                       | category    | price | available |
      | Tripe Soup                 | APPETIZER   | 22.50 | yes       |
      | Cabbage Rolls with Polenta | MAIN_COURSE | 38.00 | yes       |
      | Papanasi                   | DESSERT     | 25.00 | yes       |
      | Lemonade                   | DRINK       | 15.00 | no        |

  Scenario: Placing an order computes the total
    When table 5 orders:
      | item       | quantity |
      | Tripe Soup | 2        |
      | Papanasi   | 1        |
    Then the response status is 201
    And the order has status "NEW"
    And the order total is 70.00 lei

  Scenario: An unavailable item cannot be ordered
    When table 5 orders:
      | item       | quantity |
      | Tripe Soup | 1        |
      | Lemonade   | 2        |
    Then the response status is 409
    And the error message contains "Menu item 'Lemonade' is not available"
    And there are no orders

  Scenario: An order must contain at least one item
    When table 5 sends an order without items
    Then the response status is 400
    And the error for field "items" is "Order must contain at least one menu item"

  Scenario: A price increase does not change an order already placed
    Given table 5 has ordered 1 x "Cabbage Rolls with Polenta"
    When I change the price of "Cabbage Rolls with Polenta" to 45.00
    Then the order total is 38.00 lei

  Scenario Outline: The lifecycle of an order
    Given table 5 has an order with status "<from>"
    When I change the order status to "<to>"
    Then the response status is <status>

    Examples: allowed transitions
      | from           | to             | status |
      | NEW            | IN_PREPARATION | 200    |
      | IN_PREPARATION | SERVED         | 200    |
      | SERVED         | PAID           | 200    |
      | NEW            | CANCELLED      | 200    |

    Examples: forbidden transitions
      | from           | to             | status |
      | NEW            | PAID           | 409    |
      | IN_PREPARATION | CANCELLED      | 409    |
      | PAID           | NEW            | 409    |
      | CANCELLED      | IN_PREPARATION | 409    |

  Scenario: Viewing orders by status
    Given table 5 has an order with status "NEW"
    And table 5 has an order with status "PAID"
    When I request the orders with status "NEW"
    Then the response status is 200
    And the list contains 1 element

  Scenario: A served order can no longer be deleted
    Given table 5 has an order with status "SERVED"
    When I delete the order
    Then the response status is 409
    And the error message contains "Only new or cancelled orders can be deleted"

  Scenario: Deleting a new order
    Given table 5 has an order with status "NEW"
    When I delete the order
    Then the response status is 204
    And there are no orders

  Scenario: A menu item used in orders cannot be deleted from the menu
    Given table 5 has ordered 1 x "Cabbage Rolls with Polenta"
    When I delete the menu item "Cabbage Rolls with Polenta"
    Then the response status is 409
    And the error message contains "mark it unavailable instead"
