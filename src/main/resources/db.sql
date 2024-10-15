-- Create ENUM types
CREATE TYPE user_role AS ENUM ('ADMIN', 'CLIENT');
CREATE TYPE order_status AS ENUM ('PENDING', 'PROCESSING', 'SHIPPED', 'DELIVERED', 'CANCELLED');

-- Create tables
CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role user_role NOT NULL
);

CREATE TABLE admins (
    id SERIAL PRIMARY KEY,
    user_id INTEGER UNIQUE NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    access_level INTEGER NOT NULL
);

CREATE TABLE clients (
    id SERIAL PRIMARY KEY,
    user_id INTEGER UNIQUE NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    delivery_address TEXT NOT NULL,
    payment_method VARCHAR(50) NOT NULL
);

CREATE TABLE products (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) UNIQUE NOT NULL,
    description TEXT,
    price DECIMAL(10, 2) NOT NULL CHECK (price > 0),
    stock INTEGER NOT NULL CHECK (stock >= 0)
);

CREATE TABLE orders (
    id SERIAL PRIMARY KEY,
    client_id INTEGER NOT NULL REFERENCES clients(id),
    order_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status order_status NOT NULL DEFAULT 'PENDING'
);

CREATE TABLE order_items (
    id SERIAL PRIMARY KEY,
    order_id INTEGER NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    product_id INTEGER NOT NULL REFERENCES products(id),
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    price_at_order DECIMAL(10, 2) NOT NULL CHECK (price_at_order > 0)
);

-- Create indexes
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_products_name ON products(name);
CREATE INDEX idx_orders_client_id ON orders(client_id);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_order_items_order_id ON order_items(order_id);

-- Grant permissions
CREATE USER app_user WITH PASSWORD 'app_password';
GRANT CONNECT ON DATABASE ecommerce_db TO app_user;
GRANT USAGE ON SCHEMA public TO app_user;
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO app_user;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO app_user;

-- Insert sample data (4 items for each table)
-- Users
INSERT INTO users (first_name, last_name, email, password, role) VALUES
('Admin', 'User', 'admin@example.com', '$2a$10$XQxJXDQzM5.CCjXZjZX5.OQY0XLBGvZVZ5Xp5QGmzqRZBDKzOKCHi', 'ADMIN'), --passowrd: passowrd
('John', 'Doe', 'john@example.com', '$2a$10$XQxJXDQzM5.CCjXZjZX5.OQY0XLBGvZVZ5Xp5QGmzqRZBDKzOKCHi', 'CLIENT'), --password: passowrd
('Jane', 'Smith', 'jane@example.com', '$2a$10$XQxJXDQzM5.CCjXZjZX5.OQY0XLBGvZVZ5Xp5QGmzqRZBDKzOKCHi', 'CLIENT'), --password: passowrd
('Alice', 'Johnson', 'alice@example.com', '$2a$10$XQxJXDQzM5.CCjXZjZX5.OQY0XLBGvZVZ5Xp5QGmzqRZBDKzOKCHi', 'CLIENT'); --password: passowrd

-- Admins
INSERT INTO admins (user_id, access_level) VALUES
(1, 1);

-- Clients
INSERT INTO clients (user_id, delivery_address, payment_method) VALUES
(2, '123 Main St, City, Country', 'Credit Card'),
(3, '456 Elm St, Town, Country', 'PayPal'),
(4, '789 Oak St, Village, Country', 'Bank Transfer');

-- Products (Dairy products)
INSERT INTO products (name, description, price, stock) VALUES
('Fresh Milk', 'Whole milk from local farms', 2.99, 100),
('Aged Cheddar', 'Sharp cheddar cheese aged for 12 months', 5.99, 50),
('Greek Yogurt', 'Creamy Greek-style yogurt', 3.49, 75),
('Butter', 'Unsalted butter from grass-fed cows', 4.29, 60);

-- Orders
INSERT INTO orders (client_id, status) VALUES
(1, 'PENDING'),
(2, 'PROCESSING'),
(3, 'SHIPPED'),
(1, 'DELIVERED');

-- Order Items
INSERT INTO order_items (order_id, product_id, quantity, price_at_order) VALUES
(1, 1, 2, 2.99),
(1, 3, 1, 3.49),
(2, 2, 1, 5.99),
(3, 4, 2, 4.29),
(4, 1, 3, 2.99),
(4, 2, 1, 5.99);