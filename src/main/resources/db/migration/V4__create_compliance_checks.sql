-- Compliance checks & certificate tracker table
CREATE TABLE compliance_checks (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    property_id UUID NOT NULL,
    check_type VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'VALID',
    expiry_date DATE,
    certificate_ref VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_compliance_property FOREIGN KEY (property_id) REFERENCES properties(id) ON DELETE CASCADE,
    CONSTRAINT uk_compliance_property_type UNIQUE (property_id, check_type)
);

CREATE INDEX idx_compliance_property ON compliance_checks(property_id);
CREATE INDEX idx_compliance_status ON compliance_checks(status);
