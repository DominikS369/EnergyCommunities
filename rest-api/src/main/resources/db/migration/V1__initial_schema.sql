-- Tabelle für stündliche Energieverbräuche und Produktion
CREATE TABLE IF NOT EXISTS energy_usage (
    id BIGSERIAL PRIMARY KEY,
    hour TIMESTAMP NOT NULL UNIQUE,
    community_produced DECIMAL(10, 3) NOT NULL DEFAULT 0,
    community_used DECIMAL(10, 3) NOT NULL DEFAULT 0,
    grid_used DECIMAL(10, 3) NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabelle für aktuelle Prozentsätze
CREATE TABLE IF NOT EXISTS current_percentage (
    id BIGSERIAL PRIMARY KEY,
    hour TIMESTAMP NOT NULL UNIQUE,
    community_depleted DECIMAL(5, 2) NOT NULL,
    grid_portion DECIMAL(5, 2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indices für bessere Performance
CREATE INDEX idx_energy_usage_hour ON energy_usage(hour);
CREATE INDEX idx_current_percentage_hour ON current_percentage(hour);
