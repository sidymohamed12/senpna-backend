-- ═══════════════════════════════════════════════════════════════════
-- V009 — Données de référence : 14 régions du Sénégal
-- SEN PharmaFlow — Ministère de la Santé du Sénégal
--
-- Peuplement initial de la table `regions` (V004) avec le découpage
-- administratif officiel du Sénégal (14 régions).
-- ═══════════════════════════════════════════════════════════════════

INSERT INTO regions (id, code, nom) VALUES
    ('10000000-0000-0000-0000-000000000001', 'DAKAR',        'Dakar'),
    ('10000000-0000-0000-0000-000000000002', 'DIOURBEL',     'Diourbel'),
    ('10000000-0000-0000-0000-000000000003', 'FATICK',       'Fatick'),
    ('10000000-0000-0000-0000-000000000004', 'KAFFRINE',     'Kaffrine'),
    ('10000000-0000-0000-0000-000000000005', 'KAOLACK',      'Kaolack'),
    ('10000000-0000-0000-0000-000000000006', 'KEDOUGOU',     'Kédougou'),
    ('10000000-0000-0000-0000-000000000007', 'KOLDA',        'Kolda'),
    ('10000000-0000-0000-0000-000000000008', 'LOUGA',        'Louga'),
    ('10000000-0000-0000-0000-000000000009', 'MATAM',        'Matam'),
    ('10000000-0000-0000-0000-000000000010', 'SAINT_LOUIS',  'Saint-Louis'),
    ('10000000-0000-0000-0000-000000000011', 'SEDHIOU',      'Sédhiou'),
    ('10000000-0000-0000-0000-000000000012', 'TAMBACOUNDA',  'Tambacounda'),
    ('10000000-0000-0000-0000-000000000013', 'THIES',        'Thiès'),
    ('10000000-0000-0000-0000-000000000014', 'ZIGUINCHOR',   'Ziguinchor');

-- Vérification : 14 régions attendues
DO $$
BEGIN
    IF (SELECT COUNT(*) FROM regions) != 14 THEN
        RAISE EXCEPTION 'Migration V009 : nombre de régions incorrect, attendu 14';
    END IF;
END $$;
