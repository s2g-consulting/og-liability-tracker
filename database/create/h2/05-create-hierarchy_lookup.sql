CREATE TABLE hierarchy_lookup (
    id IDENTITY PRIMARY KEY,
    company_id INTEGER,
    type VARCHAR(255),
    licence INTEGER,
    hierarchy_value VARCHAR(255)
)