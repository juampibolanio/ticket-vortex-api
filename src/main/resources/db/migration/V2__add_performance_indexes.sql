CREATE INDEX idx_reservations_idempotency_key ON reservations(idempotency_key);

CREATE INDEX idx_reservations_status_expires_at ON reservations(status, expires_at);

CREATE INDEX idx_reservations_user_id ON reservations(user_id);