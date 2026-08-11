-- Tenancies table
CREATE TABLE tenancies (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    property_id UUID NOT NULL,
    tenant_name VARCHAR(255) NOT NULL,
    tenant_email VARCHAR(255),
    tenant_phone VARCHAR(50),
    start_date DATE NOT NULL,
    monthly_rent DECIMAL(10, 2) NOT NULL,
    rent_due_day INTEGER NOT NULL DEFAULT 1,
    deposit_amount DECIMAL(10, 2),
    deposit_scheme_name VARCHAR(100),
    deposit_reference VARCHAR(100),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_tenancies_property FOREIGN KEY (property_id) REFERENCES properties(id) ON DELETE CASCADE
);

CREATE INDEX idx_tenancies_property ON tenancies(property_id);
CREATE INDEX idx_tenancies_active ON tenancies(is_active);
