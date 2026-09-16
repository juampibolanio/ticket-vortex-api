CREATE TABLE users(
    id UUID PRIMARY KEY,
    first_name VARCHAR(120) NOT NULL,
    last_name VARCHAR(120) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    document_number VARCHAR(50) NOT NULL UNIQUE ,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,

    CONSTRAINT chk_users_role CHECK ( role IN ('CUSTOMER', 'ADMIN') )
);

CREATE TABLE events(
    id UUID PRIMARY KEY,
    title VARCHAR(120) NOT NULL UNIQUE,
    description VARCHAR(500),
    date TIMESTAMP NOT NULL,
    location VARCHAR(120) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE zones(
    id UUID PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    description VARCHAR(250),
    price NUMERIC NOT NULL,
    capacity INTEGER NOT NULL,
    event_id UUID NOT NULL,
    version INTEGER NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,

    CONSTRAINT fk_event FOREIGN KEY (event_id) REFERENCES events(id)
);

CREATE TABLE reservations(
    id UUID PRIMARY KEY,
    status VARCHAR(20) NOT NULL DEFAULT 'RESERVED',
    user_id UUID NOT NULL,
    zone_id UUID NOT NULL,
    idempotency_key UUID ,
    transaction_id UUID,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,

    CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_zone FOREIGN KEY (zone_id) REFERENCES zones(id),

    CONSTRAINT chk_reservations_status CHECK ( status IN ('RESERVED', 'CONFIRMED', 'CANCELLED', 'EXPIRED') )
);

CREATE INDEX idx_reservations_zone_id ON reservations(zone_id)
