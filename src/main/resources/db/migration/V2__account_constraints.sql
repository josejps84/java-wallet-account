-- Backfill rows created before these columns were mandatory.
UPDATE account SET current_balance = 0 WHERE current_balance IS NULL;
UPDATE account SET currency = 'USD' WHERE currency IS NULL;
UPDATE account SET status = 1 WHERE status IS NULL;

ALTER TABLE account
    ALTER COLUMN current_balance SET NOT NULL,
    ALTER COLUMN currency SET NOT NULL,
    ALTER COLUMN status SET NOT NULL,
    ADD CONSTRAINT account_balance_non_negative CHECK (current_balance >= 0),
    ADD CONSTRAINT account_status_valid CHECK (status IN (0, 1));

CREATE INDEX IF NOT EXISTS idx_account_user_id ON account (user_id);
