-- Create enum types (these will be managed by JPA/Hibernate in the Java code)
CREATE TYPE user_role AS ENUM ('ADMIN', 'CLIENT');
CREATE TYPE order_status AS ENUM ('PENDING', 'PROCESSING', 'SHIPPED', 'DELIVERED', 'CANCELLED');

-- Create tables (these will be managed by JPA/Hibernate, but this is how they might look)
CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role user_role NOT NULL
);

CREATE TABLE admins (
    id INTEGER PRIMARY KEY,
    access_level INTEGER NOT NULL,
    FOREIGN KEY (id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE clients (
    id INTEGER PRIMARY KEY,
    phone_number VARCHAR(20),
    address TEXT,
    delivery_address TEXT,
    payment_method VARCHAR(50),
    FOREIGN KEY (id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE products (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    price DECIMAL(10, 2) NOT NULL,
    stock INTEGER NOT NULL
);

CREATE TABLE orders (
    id SERIAL PRIMARY KEY,
    client_id INTEGER NOT NULL,
    order_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status order_status NOT NULL,
    total NUMERIC(19, 2) NOT NULL,
    shipping_address TEXT,
    FOREIGN KEY (client_id) REFERENCES clients(id)
);

CREATE TABLE order_items (
    id SERIAL PRIMARY KEY,
    order_id INTEGER NOT NULL,
    product_id INTEGER NOT NULL,
    quantity INTEGER NOT NULL,
    price_at_order NUMERIC(19, 2) NOT NULL,
    FOREIGN KEY (order_id) REFERENCES orders(id),
    FOREIGN KEY (product_id) REFERENCES products(id)
);

-- Create indexes
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_products_name ON products(name);
CREATE INDEX idx_orders_client_id ON orders(client_id);
CREATE INDEX idx_orders_status ON orders(status);

-- Grant permissions (adjust as needed)
-- Check if the role already exists and create it if it doesn't
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'centrale') THEN
        CREATE ROLE centrale WITH LOGIN PASSWORD 'centrale';
    END IF;
END
$$;

-- Grant permissions (these will be applied regardless of whether the role was just created or already existed)
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO centrale;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO centrale;

-- Insert sample data (this would typically be done through the application using JPA/Hibernate)
INSERT INTO users (first_name, last_name, email, password, role) VALUES
('John', 'Doe', 'john.doe@example.com', 'hashed_password_1', 'ADMIN'),
('Jane', 'Smith', 'jane.smith@example.com', 'hashed_password_2', 'ADMIN'),
('Alice', 'Johnson', 'alice.johnson@example.com', 'hashed_password_3', 'CLIENT'),
('Bob', 'Brown', 'bob.brown@example.com', 'hashed_password_4', 'CLIENT');

INSERT INTO admins (id, access_level) VALUES
(1, 1),
(2, 2);

INSERT INTO clients (id, phone_number, address, delivery_address, payment_method) VALUES
(3, '123-456-7890', '123 Main St, City, Country', '123 Main St, City, Country', 'Credit Card'),
(4, '987-654-3210', '456 Elm St, Town, Country', '456 Elm St, Town, Country', 'PayPal');

INSERT INTO products (name, description, price, stock) VALUES
('Laptop', 'High-performance laptop', 999.99, 50),
('Smartphone', 'Latest model smartphone', 699.99, 100),
('Headphones', 'Noise-cancelling headphones', 199.99, 200),
('Tablet', '10-inch tablet with stylus', 349.99, 75);

INSERT INTO orders (client_id, status, total, shipping_address) VALUES
(3, 'PENDING', 1199.98, '123 Main St, City, Country'),
(3, 'PROCESSING', 699.99, '123 Main St, City, Country'),
(4, 'SHIPPED', 349.99, '456 Elm St, Town, Country'),
(4, 'DELIVERED', 1699.98, '456 Elm St, Town, Country');

INSERT INTO order_items (order_id, product_id, quantity, price_at_order) VALUES
(1, 1, 1, 999.99),
(1, 3, 1, 199.99),
(2, 2, 1, 699.99),
(3, 4, 1, 349.99),
(4, 1, 1, 999.99),
(4, 2, 1, 699.99);