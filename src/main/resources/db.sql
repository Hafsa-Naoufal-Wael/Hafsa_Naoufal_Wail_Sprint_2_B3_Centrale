-- Create ENUM types if they don't exist
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'user_role') THEN
CREATE TYPE user_role AS ENUM ('ADMIN', 'CLIENT');
END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'order_status') THEN
CREATE TYPE order_status AS ENUM ('PENDING', 'PROCESSING', 'SHIPPED', 'DELIVERED', 'CANCELLED');
END IF;
END$$;

-- Create tables if they don't exist
CREATE TABLE IF NOT EXISTS users (
                                     id SERIAL PRIMARY KEY,
                                     first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role user_role NOT NULL
    );

CREATE TABLE IF NOT EXISTS admins (
                                      id SERIAL PRIMARY KEY,
                                      user_id INTEGER UNIQUE NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    access_level INTEGER NOT NULL
    );

CREATE TABLE IF NOT EXISTS clients (
                                       id SERIAL PRIMARY KEY,
                                       user_id INTEGER UNIQUE NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    delivery_address TEXT NOT NULL,
    payment_method VARCHAR(50) NOT NULL
    );

CREATE TABLE IF NOT EXISTS products (
                                        id SERIAL PRIMARY KEY,
                                        name VARCHAR(100) UNIQUE NOT NULL,
    description TEXT,
    price DECIMAL(10, 2) NOT NULL CHECK (price > 0),
    stock INTEGER NOT NULL CHECK (stock >= 0)
    );

CREATE TABLE IF NOT EXISTS orders (
                                      id SERIAL PRIMARY KEY,
                                      client_id INTEGER NOT NULL REFERENCES clients(id),
    order_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status order_status NOT NULL DEFAULT 'PENDING'
    );

CREATE TABLE IF NOT EXISTS order_items (
                                           id SERIAL PRIMARY KEY,
                                           order_id INTEGER NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    product_id INTEGER NOT NULL REFERENCES products(id),
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    price_at_order DECIMAL(10, 2) NOT NULL CHECK (price_at_order > 0)
    );

-- Create indexes if they don't exist
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_products_name ON products(name);
CREATE INDEX IF NOT EXISTS idx_orders_client_id ON orders(client_id);
CREATE INDEX IF NOT EXISTS idx_orders_status ON orders(status);
CREATE INDEX IF NOT EXISTS idx_order_items_order_id ON order_items(order_id);

-- Grant permissions (create user if not exists)
DO
$do$
BEGIN
   IF NOT EXISTS (
      SELECT FROM pg_catalog.pg_roles
      WHERE  rolname = 'app_user') THEN
      CREATE USER app_user WITH PASSWORD '123';
END IF;
END
$do$;

GRANT CONNECT ON DATABASE centrale_db TO app_user;
GRANT USAGE ON SCHEMA public TO app_user;
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO app_user;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO app_user;

-- Insert sample data (4 items for each table)
-- Users
INSERT INTO users (first_name, last_name, email, password, role) VALUES
                                                                     ('Admin', 'User', 'admin@example.com', '$2a$10$XQxJXDQzM5.CCjXZjZX5.OQY0XLBGvZVZ5Xp5QGmzqRZBDKzOKCHi', 'ADMIN'),
                                                                     ('John', 'Doe', 'john@example.com', '$2a$10$XQxJXDQzM5.CCjXZjZX5.OQY0XLBGvZVZ5Xp5QGmzqRZBDKzOKCHi', 'CLIENT'),
                                                                     ('Jane', 'Smith', 'jane@example.com', '$2a$10$XQxJXDQzM5.CCjXZjZX5.OQY0XLBGvZVZ5Xp5QGmzqRZBDKzOKCHi', 'CLIENT'),
                                                                     ('Alice', 'Johnson', 'alice@example.com', '$2a$10$XQxJXDQzM5.CCjXZjZX5.OQY0XLBGvZVZ5Xp5QGmzqRZBDKzOKCHi', 'CLIENT')
    ON CONFLICT (email) DO NOTHING;

-- Admins
INSERT INTO admins (user_id, access_level) VALUES
    (1, 1)
    ON CONFLICT (user_id) DO NOTHING;

-- Clients
INSERT INTO clients (user_id, delivery_address, payment_method) VALUES
                                                                    (2, '123 Main St, City, Country', 'Credit Card'),
                                                                    (3, '456 Elm St, Town, Country', 'PayPal'),
                                                                    (4, '789 Oak St, Village, Country', 'Bank Transfer')
    ON CONFLICT (user_id) DO NOTHING;

-- Products (Dairy products)
INSERT INTO products (name, description, price, stock) VALUES
                                                           ('Fresh Milk', 'Whole milk from local farms', 2.99, 100),
                                                           ('Aged Cheddar', 'Sharp cheddar cheese aged for 12 months', 5.99, 50),
                                                           ('Greek Yogurt', 'Creamy Greek-style yogurt', 3.49, 75),
                                                           ('Butter', 'Unsalted butter from grass-fed cows', 4.29, 60)
    ON CONFLICT (name) DO NOTHING;

-- Orders
INSERT INTO orders (client_id, status) VALUES
                                           (1, 'PENDING'),
                                           (2, 'PROCESSING'),
                                           (3, 'SHIPPED'),
                                           (1, 'DELIVERED')
    ON CONFLICT DO NOTHING;

-- Order Items
INSERT INTO order_items (order_id, product_id, quantity, price_at_order) VALUES
                                                                             (1, 1, 2, 2.99),
                                                                             (1, 3, 1, 3.49),
                                                                             (2, 2, 1, 5.99),
                                                                             (3, 4, 2, 4.29),
                                                                             (4, 1, 3, 2.99),
                                                                             (4, 2, 1, 5.99)
    ON CONFLICT DO NOTHING;
