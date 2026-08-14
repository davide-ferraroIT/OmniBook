-- Test di migrazione per Flyway
-- Aggiungiamo una colonna fittizia alla tabella tenants per dimostrare che Flyway funziona

ALTER TABLE tenants ADD COLUMN IF NOT EXISTS flyway_test_column VARCHAR(255);
