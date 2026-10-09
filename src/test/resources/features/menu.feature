@menu
Feature: Restaurant menu management
  As a restaurant manager
  I want to add, view, update and delete the items on the menu
  So that waiters and guests always see the correct offer

  Background:
    Given the menu contains the following items:
      | name                       | category    | price | available |
      | Tripe Soup                 | APPETIZER   | 22.50 | yes       |
      | Cabbage Rolls with Polenta | MAIN_COURSE | 38.00 | yes       |
      | Papanasi                   | DESSERT     | 25.00 | yes       |
      | Lemonade                   | DRINK       | 15.00 | no        |

  @create
  Scenario: Adding a new menu item
    When I add the menu item "Grilled Mici" in category "MAIN_COURSE" with price 32.00
    Then the response status is 201
    And the response contains the name "Grilled Mici"
    And the menu contains 5 items

  @create
  Scenario: Two menu items cannot have the same name
    When I add the menu item "Papanasi" in category "DESSERT" with price 30.00
    Then the response status is 409
    And the error message contains "A menu item named Papanasi already exists"
    And the menu contains 4 items

  @read
  Scenario: Viewing the whole menu
    When I request the menu
    Then the response status is 200
    And the list contains 4 elements

  @read
  Scenario: Filtering the menu by category
    When I request the items in category "DESSERT"
    Then the response status is 200
    And the list contains 1 element

  @read
  Scenario: Viewing a menu item that does not exist
    When I request the menu item with id 999
    Then the response status is 404
    And the error message contains "was not found"

  @update
  Scenario: Changing the price of a menu item
    When I change the price of "Papanasi" to 27.50
    Then the response status is 200
    And "Papanasi" costs 27.50 lei

  @update
  Scenario: Temporarily removing an item from the offer
    When I mark "Tripe Soup" as unavailable
    Then the response status is 200
    And "Tripe Soup" is not available

  @delete
  Scenario: Deleting a menu item
    When I delete the menu item "Lemonade"
    Then the response status is 204
    And "Lemonade" is no longer on the menu

  @validation
  Scenario Outline: Rejecting a menu item with invalid data
    When I add the menu item "<name>" in category "<category>" with price <price>
    Then the response status is 400
    And the error for field "<field>" is "<message>"
    And the menu contains 4 items

    Examples:
      | name     | category  | price  | field    | message                           |
      |          | APPETIZER | 10.00  | name     | Menu item name is required        |
      | Zacusca  |           | 10.00  | category | Category is required              |
      | Zacusca  | APPETIZER | 0      | price    | Price must be positive            |
      | Zacusca  | APPETIZER | -5.00  | price    | Price must be positive            |
      | Zacusca  | APPETIZER | 10.555 | price    | Price can have at most 2 decimals |
