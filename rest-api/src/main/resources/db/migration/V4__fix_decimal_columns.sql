ALTER TABLE current_percentage
ALTER COLUMN community_depleted TYPE double precision,
    ALTER COLUMN grid_portion TYPE double precision;

ALTER TABLE energy_usage
ALTER COLUMN community_produced TYPE double precision,
    ALTER COLUMN community_used TYPE double precision,
    ALTER COLUMN grid_used TYPE double precision;