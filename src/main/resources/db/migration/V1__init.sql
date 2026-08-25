/* CREATE TABLE IF NOT EXISTS accounts (
    email VARCHAR(255) PRIMARY KEY,
    balance NUMERIC(19, 2) NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    last_updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL
);
 */
CREATE TABLE IF NOT EXISTS account (
    account_nmbr VARCHAR(50) PRIMARY KEY,
    current_balance DECIMAL(19, 4),
    currency VARCHAR(3),
    status INT,
    user_id BIGINT,
    date_creation TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    last_update TIMESTAMP WITHOUT TIME ZONE NOT NULL
);
