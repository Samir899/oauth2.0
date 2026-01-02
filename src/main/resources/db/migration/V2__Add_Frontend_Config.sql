ALTER TABLE smtp_configs ADD COLUMN frontend_protocol VARCHAR(255) DEFAULT 'http';
ALTER TABLE smtp_configs ADD COLUMN frontend_host VARCHAR(255) DEFAULT 'localhost';
ALTER TABLE smtp_configs ADD COLUMN frontend_port INTEGER DEFAULT 9000;

