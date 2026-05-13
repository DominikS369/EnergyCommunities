-- Test-Daten einfügen
INSERT INTO energy_usage (hour, community_produced, community_used, grid_used) VALUES
    ('2026-05-06 08:00:00', 12.40, 11.80, 0.60),
    ('2026-05-06 12:00:00', 18.05, 18.05, 1.07),
    ('2026-05-06 18:00:00', 15.02, 14.03, 2.05)
ON CONFLICT (hour) DO NOTHING;

INSERT INTO current_percentage (hour, community_depleted, grid_portion) VALUES
    ('2026-05-06 12:00:00', 100.00, 5.63)
ON CONFLICT (hour) DO NOTHING;

