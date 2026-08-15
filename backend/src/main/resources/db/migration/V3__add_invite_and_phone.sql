ALTER TABLE tenants ADD COLUMN invite_code VARCHAR(255);
UPDATE tenants SET invite_code = slug WHERE invite_code IS NULL;
ALTER TABLE tenants ADD CONSTRAINT uk_tenant_invite_code UNIQUE (invite_code);

ALTER TABLE users ADD COLUMN phone VARCHAR(50);
