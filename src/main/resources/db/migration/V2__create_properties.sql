-- Property registry table
CREATE TABLE properties (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    address_line1 VARCHAR(255) NOT NULL,
    address_line2 VARCHAR(255),
    city VARCHAR(100) NOT NULL,
    postcode VARCHAR(20) NOT NULL,
    property_type VARCHAR(50) NOT NULL DEFAULT 'FLAT',
    bedrooms INTEGER NOT NULL DEFAULT 1,
    epc_rating VARCHAR(5),
    epc_expiry_date DATE,
    gas_safety_expiry_date DATE,
    eicr_expiry_date DATE,
    prs_database_reg_number VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_properties_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_properties_user ON properties(user_id);
CREATE INDEX idx_properties_postcode ON properties(postcode);
