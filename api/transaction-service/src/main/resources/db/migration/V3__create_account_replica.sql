CREATE TABLE account_replica (
    id       UUID        PRIMARY KEY,
    user_id  UUID        NOT NULL,
    currency VARCHAR(3)  NOT NULL,
    status   VARCHAR(20) NOT NULL
);