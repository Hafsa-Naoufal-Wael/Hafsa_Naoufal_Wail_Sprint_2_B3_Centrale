-- Create ENUM type for user roles
DO $$
    BEGIN
        IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'user_role') THEN
            CREATE TYPE user_role AS ENUM ('SUPER_ADMIN', 'ADMIN', 'CLIENT');
        END IF;
    END$$;

-- Create ENUM type for order status
DO $$
    BEGIN
        IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'order_status') THEN
            CREATE TYPE order_status AS ENUM ('PENDING', 'PROCESSING', 'COMPLETED', 'CANCELLED');
        END IF;
    END$$;

-- Create the parent table 'users'
CREATE TABLE IF NOT EXISTS users (
                                     id SERIAL PRIMARY KEY,
                                     first_name VARCHAR(50) NOT NULL,
                                     last_name VARCHAR(50) NOT NULL,
                                     email VARCHAR(100) UNIQUE NOT NULL,
                                     password VARCHAR(255) NOT NULL,
                                     role user_role NOT NULL CHECK (role IN ('SUPER_ADMIN', 'ADMIN', 'CLIENT'))
);

-- Create the 'clients' table inheriting from 'users'
CREATE TABLE IF NOT EXISTS clients (
                                       delivery_address TEXT NOT NULL,
                                       payment_method VARCHAR(50) NOT NULL,
                                       CONSTRAINT clients_pkey PRIMARY KEY (id)  -- Explicitly set the primary key
) INHERITS (users);

-- Create the 'admins' table inheriting from 'users'
CREATE TABLE IF NOT EXISTS admins (
                                      access_level INTEGER NOT NULL CHECK (access_level IN (1, 2)),  -- 1 = super admin, 2 = admin
                                      CONSTRAINT admins_pkey PRIMARY KEY (id)  -- Explicitly set the primary key
) INHERITS (users);

-- Create products table
CREATE TABLE IF NOT EXISTS products (
                                        id SERIAL PRIMARY KEY,
                                        name VARCHAR(100) UNIQUE NOT NULL,
                                        description TEXT,
                                        price DECIMAL(10, 2) NOT NULL CHECK (price > 0),
                                        stock INTEGER NOT NULL CHECK (stock >= 0)
);

-- Create orders table (Ensure it comes after clients table)
CREATE TABLE IF NOT EXISTS orders (
                                      id SERIAL PRIMARY KEY,
                                      client_id INTEGER NOT NULL REFERENCES clients(id) ON DELETE CASCADE,
                                      order_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                      status order_status NOT NULL DEFAULT 'PENDING'
);

-- Create order items table
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
            WHERE rolname = 'app_user') THEN
            CREATE USER app_user WITH PASSWORD '123';
        END IF;
    END
$do$;

GRANT CONNECT ON DATABASE centrale_db TO app_user;
GRANT USAGE ON SCHEMA public TO app_user;
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO app_user;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO app_user;

-- Create function to insert user role
CREATE OR REPLACE FUNCTION insert_user_role()
    RETURNS TRIGGER AS $$
BEGIN
    IF NEW.role = 'SUPER_ADMIN' THEN
        -- Insert into admins table with access level 1
        INSERT INTO admins (id, first_name, last_name, email, password, role, access_level)
        VALUES (NEW.id, NEW.first_name, NEW.last_name, NEW.email, NEW.password, NEW.role, 1);
    ELSIF NEW.role = 'ADMIN' THEN
        -- Insert into admins table with access level 2
        INSERT INTO admins (id, first_name, last_name, email, password, role, access_level)
        VALUES (NEW.id, NEW.first_name, NEW.last_name, NEW.email, NEW.password, NEW.role, 2);
    ELSIF NEW.role = 'CLIENT' THEN
        -- Insert into clients table with default values for delivery_address and payment_method
        INSERT INTO clients (id, first_name, last_name, email, password, role, delivery_address, payment_method)
        VALUES (NEW.id, NEW.first_name, NEW.last_name, NEW.email, NEW.password, NEW.role, 'Default Address', 'Default Payment');
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Create trigger for inserting user roles
CREATE TRIGGER trg_insert_user_role
    AFTER INSERT ON users
    FOR EACH ROW
EXECUTE FUNCTION insert_user_role();
