-- Legal notices table (Section 13 & Section 8)
CREATE TABLE legal_notices (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenancy_id UUID NOT NULL,
    notice_type VARCHAR(50) NOT NULL,
    current_rent DECIMAL(10, 2),
    proposed_rent DECIMAL(10, 2),
    starting_date DATE NOT NULL,
    grounds_json TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    served_at TIMESTAMP,
    served_method VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_notices_tenancy FOREIGN KEY (tenancy_id) REFERENCES tenancies(id) ON DELETE CASCADE
);

CREATE INDEX idx_notices_tenancy ON legal_notices(tenancy_id);
CREATE INDEX idx_notices_type ON legal_notices(notice_type);
