-- V3__seed_admin_and_customer.sql
-- Default customer: customer@petstore.com / Customer123!
-- Default admin: admin@petstore.com / AdminPassword123!
-- Passwords will also be checked and bootstrapped by AdminBootstrap on application startup if needed.

INSERT INTO users (name, email, password, phone, role)
VALUES 
('Store Admin', 'admin@petstore.com', '$2a$10$w81oZ5q0Z3N7v2U0Z3wZyeuR0k3bA5.5Q3m4rR.sHw6aJ0eA6rOqm', '9876543210', 'ADMIN'),
('Rahul Sharma', 'customer@petstore.com', '$2a$10$w81oZ5q0Z3N7v2U0Z3wZyeuR0k3bA5.5Q3m4rR.sHw6aJ0eA6rOqm', '9876543211', 'CUSTOMER')
ON CONFLICT (email) DO NOTHING;

INSERT INTO addresses (user_id, name, phone, address_line1, address_line2, city, state, pincode, is_default)
SELECT u.id, 'Rahul Sharma', '9876543211', 'Flat 402, Sunshine Apartments', 'Indiranagar', 'Bengaluru', 'Karnataka', '560038', TRUE
FROM users u WHERE u.email = 'customer@petstore.com'
ON CONFLICT DO NOTHING;
