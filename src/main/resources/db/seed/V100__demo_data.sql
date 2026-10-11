-- Demo data for the running application (not loaded by the tests).
-- Same tables and menu as in the .feature files, so requests.http and the scenarios
-- can be followed by hand. On a fresh database the generated ids are 1, 2, 3, ...

INSERT INTO restaurant_tables (number, seats) VALUES
    (1, 2),   -- id 1
    (2, 4),   -- id 2
    (3, 8),   -- id 3
    (5, 4);   -- id 4

INSERT INTO menu_items (name, description, category, price, available) VALUES
    ('Tripe Soup',                 'Traditional sour soup with sour cream and garlic', 'APPETIZER',   22.50, TRUE),  -- id 1
    ('Cabbage Rolls with Polenta', 'Sarmale with polenta and sour cream',              'MAIN_COURSE', 38.00, TRUE),  -- id 2
    ('Papanasi',                   'Fried doughnuts with sour cream and jam',          'DESSERT',     25.00, TRUE),  -- id 3
    ('Lemonade',                   'Homemade lemonade with mint',                      'DRINK',       15.00, FALSE); -- id 4

INSERT INTO reservations (customer_name, phone, table_id, date_time, party_size)
SELECT 'Maria Ionescu', '0733111222', id, TIMESTAMP '2030-12-20 19:00:00', 6
FROM restaurant_tables WHERE number = 3;

-- One NEW order at table 5: 2 x Tripe Soup + 1 x Papanasi = 70.00 lei
INSERT INTO orders (table_id, status, created_at)
SELECT id, 'NEW', CURRENT_TIMESTAMP FROM restaurant_tables WHERE number = 5;

INSERT INTO order_items (order_id, menu_item_id, quantity, unit_price)
SELECT o.id, m.id, l.quantity, m.price
FROM orders o
         JOIN restaurant_tables t ON t.id = o.table_id AND t.number = 5
         CROSS JOIN (VALUES ('Tripe Soup', 2), ('Papanasi', 1)) AS l (name, quantity)
         JOIN menu_items m ON m.name = l.name;
