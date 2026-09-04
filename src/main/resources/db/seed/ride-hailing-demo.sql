-- Ride-hailing demo schema + seed data for a MONITORED target database (not this app's own DB).
-- Loaded automatically on startup (dev profile only) by DemoDatabaseSeeder, which also creates the
-- target database and registers it as a monitored database — see that class for the idempotency
-- guard (skipped once the `company` table already exists). Can still be run by hand for a database
-- outside that flow: psql "<target-jdbc-url>" -f src/main/resources/db/seed/ride-hailing-demo.sql
-- Gives the AI SQL Assistant / SQL Editor / PII masking / schema ERD features realistic,
-- PII-bearing tables to demonstrate against (users/drivers carry email + phone_number).

CREATE TABLE company (
    id          BIGSERIAL PRIMARY KEY,
    name        TEXT NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE company_sites (
    id          BIGSERIAL PRIMARY KEY,
    company_id  BIGINT NOT NULL REFERENCES company(id),
    name        TEXT NOT NULL,
    address     TEXT NOT NULL,
    city        TEXT NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE operator (
    id             BIGSERIAL PRIMARY KEY,
    company_id     BIGINT NOT NULL REFERENCES company(id),
    name           TEXT NOT NULL,
    contact_email  TEXT NOT NULL,
    contact_phone  TEXT NOT NULL,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE users (
    id            BIGSERIAL PRIMARY KEY,
    full_name     TEXT NOT NULL,
    email         TEXT NOT NULL UNIQUE,
    phone_number  TEXT NOT NULL,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE drivers (
    id              BIGSERIAL PRIMARY KEY,
    operator_id     BIGINT NOT NULL REFERENCES operator(id),
    full_name       TEXT NOT NULL,
    email           TEXT NOT NULL UNIQUE,
    phone_number    TEXT NOT NULL,
    license_number  TEXT NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE vehicle (
    id            BIGSERIAL PRIMARY KEY,
    operator_id   BIGINT NOT NULL REFERENCES operator(id),
    driver_id     BIGINT REFERENCES drivers(id),
    make          TEXT NOT NULL,
    model         TEXT NOT NULL,
    plate_number  TEXT NOT NULL,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE rides (
    id                    BIGSERIAL PRIMARY KEY,
    user_id               BIGINT NOT NULL REFERENCES users(id),
    company_site_id       BIGINT REFERENCES company_sites(id),
    pickup_address        TEXT NOT NULL,
    dropoff_address       TEXT NOT NULL,
    status                TEXT NOT NULL CHECK (status IN ('REQUESTED', 'MATCHED', 'CANCELLED', 'COMPLETED')),
    requested_at          TIMESTAMPTZ NOT NULL,
    created_at            TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE trips (
    id            BIGSERIAL PRIMARY KEY,
    ride_id       BIGINT NOT NULL REFERENCES rides(id),
    driver_id     BIGINT NOT NULL REFERENCES drivers(id),
    vehicle_id    BIGINT NOT NULL REFERENCES vehicle(id),
    start_time    TIMESTAMPTZ NOT NULL,
    end_time      TIMESTAMPTZ,
    distance_km   NUMERIC(6, 2) NOT NULL,
    fare_amount   NUMERIC(8, 2) NOT NULL,
    status        TEXT NOT NULL CHECK (status IN ('IN_PROGRESS', 'COMPLETED', 'CANCELLED')),
    created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- No index on trips.driver_id on purpose — gives the AI query optimizer a real target: filtering
-- trips by driver (a very natural question) does a sequential scan until an admin approves the
-- index recommendation the app drafts for it.

INSERT INTO company (name)
SELECT 'Company ' || g FROM generate_series(1, 3) g;

INSERT INTO company_sites (company_id, name, address, city)
SELECT (g % 3) + 1, 'Site ' || g, g || ' Main St', (ARRAY['Bengaluru', 'Mumbai', 'Delhi', 'Chennai', 'Pune'])[(g % 5) + 1]
FROM generate_series(1, 10) g;

INSERT INTO operator (company_id, name, contact_email, contact_phone)
SELECT (g % 3) + 1, 'Operator ' || g, 'operator' || g || '@fleetco.example.com', '+91-9' || LPAD(g::text, 9, '0')
FROM generate_series(1, 10) g;

INSERT INTO users (full_name, email, phone_number, created_at)
SELECT 'Rider ' || g, 'rider' || g || '@example.com', '+91-8' || LPAD(g::text, 9, '0'),
       NOW() - (random() * INTERVAL '180 days')
FROM generate_series(1, 100) g;

INSERT INTO drivers (operator_id, full_name, email, phone_number, license_number, created_at)
SELECT (g % 10) + 1, 'Driver ' || g, 'driver' || g || '@fleetco.example.com', '+91-7' || LPAD(g::text, 9, '0'),
       'DL' || LPAD(g::text, 8, '0'), NOW() - (random() * INTERVAL '365 days')
FROM generate_series(1, 50) g;

INSERT INTO vehicle (operator_id, driver_id, make, model, plate_number)
SELECT (g % 10) + 1, g, (ARRAY['Toyota', 'Honda', 'Maruti', 'Hyundai', 'Tata'])[(g % 5) + 1],
       (ARRAY['Innova', 'City', 'Swift', 'i20', 'Nexon'])[(g % 5) + 1],
       'KA' || LPAD((g % 99)::text, 2, '0') || 'AB' || LPAD(g::text, 4, '0')
FROM generate_series(1, 50) g;

INSERT INTO rides (user_id, company_site_id, pickup_address, dropoff_address, status, requested_at)
SELECT (g % 100) + 1, (g % 10) + 1, (g % 500) || ' Park Ave', (g % 500) || ' Lake Rd',
       (ARRAY['COMPLETED', 'COMPLETED', 'COMPLETED', 'CANCELLED', 'MATCHED'])[(g % 5) + 1],
       NOW() - (random() * INTERVAL '90 days')
FROM generate_series(1, 500) g;

INSERT INTO trips (ride_id, driver_id, vehicle_id, start_time, end_time, distance_km, fare_amount, status)
SELECT r.id, (r.id % 50) + 1, (r.id % 50) + 1, r.requested_at + INTERVAL '5 minutes',
       r.requested_at + INTERVAL '5 minutes' + (random() * INTERVAL '45 minutes'),
       round((random() * 25 + 1)::numeric, 2), round((random() * 800 + 50)::numeric, 2), 'COMPLETED'
FROM rides r
WHERE r.status = 'COMPLETED';
