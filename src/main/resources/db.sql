-- Create ENUM types if they don't exist
DO $$ 
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'user_role') THEN
        CREATE TYPE user_role AS ENUM ('SUPER_ADMIN', 'ADMIN', 'CLIENT');
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'order_status') THEN
        CREATE TYPE order_status AS ENUM ('PENDING', 'PROCESSING', 'COMPLETED', 'CANCELLED');
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

CREATE TABLE IF NOT EXISTS clients (
    id INTEGER PRIMARY KEY,
    delivery_address TEXT NOT NULL,
    payment_method VARCHAR(50) NOT NULL
) INHERITS (users);

CREATE TABLE IF NOT EXISTS admins (
    id INTEGER PRIMARY KEY,
    access_level INTEGER NOT NULL CHECK (access_level IN (1, 2))
) INHERITS (users);

CREATE TABLE IF NOT EXISTS products (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) UNIQUE NOT NULL,
    description TEXT,
    price DECIMAL(10, 2) NOT NULL CHECK (price > 0),
    stock INTEGER NOT NULL CHECK (stock >= 0)
);

CREATE TABLE IF NOT EXISTS orders (
    id SERIAL PRIMARY KEY,
    client_id INTEGER NOT NULL,
    order_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status order_status NOT NULL DEFAULT 'PENDING',
    shipping_address TEXT,
    total DECIMAL(10, 2) NOT NULL CHECK (total >= 0),
    CONSTRAINT fk_client FOREIGN KEY (client_id) REFERENCES clients(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS order_items (
    id SERIAL PRIMARY KEY,
    order_id INTEGER NOT NULL,
    product_id INTEGER NOT NULL,
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    price_at_order DECIMAL(10, 2) NOT NULL CHECK (price_at_order > 0),
    CONSTRAINT fk_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    CONSTRAINT fk_product FOREIGN KEY (product_id) REFERENCES products(id)
);

-- Create indexes if they don't exist
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_products_name ON products(name);
CREATE INDEX IF NOT EXISTS idx_orders_client_id ON orders(client_id);
CREATE INDEX IF NOT EXISTS idx_orders_status ON orders(status);
CREATE INDEX IF NOT EXISTS idx_order_items_order_id ON order_items(order_id);

-- Grant permissions (create user if not exists)
DO $$ 
BEGIN
    IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'app_user') THEN
        CREATE USER app_user WITH PASSWORD '123';
    END IF;
END$$;

GRANT CONNECT ON DATABASE centrale_db TO app_user;
GRANT USAGE ON SCHEMA public TO app_user;
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO app_user;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO app_user;

-- Create function to insert user role
CREATE OR REPLACE FUNCTION insert_user_role()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.role = 'SUPER_ADMIN' OR NEW.role = 'ADMIN' THEN
        INSERT INTO admins (id, first_name, last_name, email, password, role, access_level)
        VALUES (NEW.id, NEW.first_name, NEW.last_name, NEW.email, NEW.password, NEW.role, 
                CASE WHEN NEW.role = 'SUPER_ADMIN' THEN 1 ELSE 2 END);
    ELSIF NEW.role = 'CLIENT' THEN
        INSERT INTO clients (id, first_name, last_name, email, password, role, delivery_address, payment_method)
        VALUES (NEW.id, NEW.first_name, NEW.last_name, NEW.email, NEW.password, NEW.role, 'Default Address', 'Default Payment');
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Create trigger for insert_user_role function
DROP TRIGGER IF EXISTS user_role_insert_trigger ON users;
CREATE TRIGGER user_role_insert_trigger
AFTER INSERT ON users
FOR EACH ROW
EXECUTE FUNCTION insert_user_role();

-- Insert sample data
DO $$
BEGIN
    -- Insert sample users
    INSERT INTO users (first_name, last_name, email, password, role)
    VALUES 
        ('John', 'Doe', 'john@example.com', 'password123', 'CLIENT'),
        ('Jane', 'Smith', 'jane@example.com', 'password456', 'ADMIN'),
        ('Mike', 'Johnson', 'mike@example.com', 'password789', 'SUPER_ADMIN')
    ON CONFLICT (email) DO NOTHING;

    -- Insert sample products
    INSERT INTO products (name, description, price, stock)
    VALUES
        ('Laptop', 'High-performance laptop', 999.99, 50),
        ('Smartphone', 'Latest model smartphone', 699.99, 100),
        ('Headphones', 'Noise-cancelling headphones', 199.99, 75)
    ON CONFLICT (name) DO NOTHING;

    -- Insert sample orders
    INSERT INTO orders (client_id, order_date, status, shipping_address, total)
    SELECT 
        c.id,
        CURRENT_TIMESTAMP - (RANDOM() * INTERVAL '7 days'),
        (ARRAY['PENDING', 'PROCESSING', 'COMPLETED', 'CANCELLED'])[floor(random() * 4 + 1)::int]::order_status,
        'Sample Address ' || c.id,
        (RANDOM() * 1000)::numeric(10,2)
    FROM clients c
    ORDER BY RANDOM()
    LIMIT 3;

    -- Insert sample order items
    WITH order_data AS (SELECT id FROM orders ORDER BY RANDOM() LIMIT 3)
    INSERT INTO order_items (order_id, product_id, quantity, price_at_order)
    SELECT 
        od.id,
        p.id,
        floor(random() * 3 + 1)::int,
        p.price
    FROM order_data od
    CROSS JOIN products p;

END $$;
