-- A closed account accepts no more bookings. Set by the Ledger when it approves a closure request.
ALTER TABLE balances ADD COLUMN closed BOOLEAN NOT NULL DEFAULT FALSE;