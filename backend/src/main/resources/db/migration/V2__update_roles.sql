DO $$
DECLARE
    r RECORD;
BEGIN
    -- Trova e rimuovi eventuali check constraint esistenti sulla tabella users
    FOR r IN (
        SELECT conname
        FROM pg_constraint
        INNER JOIN pg_class ON conrelid = pg_class.oid
        WHERE pg_class.relname = 'users'
          AND contype = 'c'
    ) LOOP
        EXECUTE 'ALTER TABLE users DROP CONSTRAINT ' || quote_ident(r.conname);
    END LOOP;
END $$;

UPDATE users SET role = 'ADMIN' WHERE role = 'SUPER_ADMIN';
UPDATE users SET role = 'SHOP' WHERE role = 'TENANT_ADMIN';

ALTER TABLE users ADD CONSTRAINT users_role_check CHECK (role in ('ADMIN','SHOP','CUSTOMER'));
