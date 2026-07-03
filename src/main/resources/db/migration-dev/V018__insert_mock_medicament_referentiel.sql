-- ═══════════════════════════════════════════════════════════════════
-- V018 — Données de démonstration : catalogue médicament (mock data)
-- SEN PharmaFlow — Ministère de la Santé du Sénégal
--
-- ⚠️  Cette migration vit dans db/migration-dev/, PAS dans
--     db/migration/ : profil `dev` uniquement (cf. V013 pour la même
--     convention sur les fournisseurs).
--
-- Prérequis : V014 à V017 (db/migration) déjà appliquées.
--
-- Couvre : familles/formes actives et archivées, médicaments complets
-- (repris des exemples du flow métier : Paracétamol, Amoxicilline,
-- Ceftriaxone), conditionnements multi-niveaux avec conversions
-- (cf. exemple §5 du modèle complémentaire : 1 carton = 100 boîtes =
-- 1000 plaquettes = 10 000 comprimés).
-- ═══════════════════════════════════════════════════════════════════

-- ── Familles thérapeutiques ──────────────────────────────────────────
INSERT INTO familles (id, code, libelle, description, actif) VALUES
    ('60000000-0000-0000-0000-000000000001', 'ANTALGIQUE',      'Antalgique',            'Médicaments contre la douleur', true),
    ('60000000-0000-0000-0000-000000000002', 'ANTIBIOTIQUE',    'Antibiotique',          'Médicaments anti-infectieux', true),
    ('60000000-0000-0000-0000-000000000003', 'ANTIPALUDEEN',    'Antipaludéen',          'Traitement du paludisme', true),
    ('60000000-0000-0000-0000-000000000004', 'ANTIHISTAMINIQUE','Antihistaminique',      NULL, false);

-- ── Formes pharmaceutiques ───────────────────────────────────────────
INSERT INTO formes (id, code, libelle, description, actif) VALUES
    ('61000000-0000-0000-0000-000000000001', 'COMPRIME',    'Comprimé',    NULL, true),
    ('61000000-0000-0000-0000-000000000002', 'FLACON_INJ',  'Flacon injectable', 'Poudre pour préparation injectable', true),
    ('61000000-0000-0000-0000-000000000003', 'SIROP',       'Sirop',       NULL, true),
    ('61000000-0000-0000-0000-000000000004', 'POMMADE',     'Pommade',     NULL, false);

-- ── Médicaments ───────────────────────────────────────────────────────
INSERT INTO medicaments (id, code, nom_commercial, dci, dosage, forme_id, famille_id, voie_administration,
    temperature_conservation, programme_sante, delai_approvisionnement_jours, necessite_ordonnance, fabricant,
    stock_minimum, stock_maximum, actif) VALUES
    -- Paracétamol — cf. exemple flow CAS 1/2
    ('62000000-0000-0000-0000-000000000001', 'PARA500', 'Doliprane', 'Paracétamol', '500 mg',
        '61000000-0000-0000-0000-000000000001', '60000000-0000-0000-0000-000000000001', 'ORALE',
        'AMBIANTE', NULL, 30, false, 'Sanofi', 20000, 200000, true),

    -- Amoxicilline — cf. exemple flow CAS 1/4
    ('62000000-0000-0000-0000-000000000002', 'AMOX500', 'Amoxicilline Sandoz', 'Amoxicilline', '500 mg',
        '61000000-0000-0000-0000-000000000001', '60000000-0000-0000-0000-000000000002', 'ORALE',
        'AMBIANTE', 'PNDS Infections respiratoires', 45, true, 'Sandoz', 5000, 80000, true),

    -- Ceftriaxone — cf. exemple flow CAS 1 (flacons, chaîne du froid)
    ('62000000-0000-0000-0000-000000000003', 'CEFTRIA1G', 'Rocéphine', 'Ceftriaxone', '1 g',
        '61000000-0000-0000-0000-000000000002', '60000000-0000-0000-0000-000000000002', 'INJECTABLE',
        'REFRIGEREE_2_8', 'PNDS Urgences', 60, true, 'Roche', 500, 5000, true),

    -- Médicament minimal (champs optionnels absents)
    ('62000000-0000-0000-0000-000000000004', 'SPCHLOR4', 'Sirop Chlorphénamine', 'Chlorphénamine', '2 mg/5ml',
        '61000000-0000-0000-0000-000000000003', '60000000-0000-0000-0000-000000000001', NULL,
        'AMBIANTE', NULL, NULL, false, NULL, NULL, NULL, true),

    -- Médicament archivé (retiré du catalogue actif)
    ('62000000-0000-0000-0000-000000000005', 'QUININE300', 'Quinine ancienne formule', 'Quinine', '300 mg',
        '61000000-0000-0000-0000-000000000001', '60000000-0000-0000-0000-000000000003', 'ORALE',
        'AMBIANTE', NULL, 30, true, 'Génériques Sénégal', 1000, 10000, false);

-- ── Conditionnements — Paracétamol (cf. modèle complémentaire §5) ─────
-- 1 carton = 100 boîtes = 1 000 plaquettes = 10 000 comprimés
INSERT INTO conditionnements (id, medicament_id, nom, niveau, quantite_unite_base, est_unite_base, actif) VALUES
    ('63000000-0000-0000-0000-000000000001', '62000000-0000-0000-0000-000000000001', 'Comprimé',   1, 1,      true,  true),
    ('63000000-0000-0000-0000-000000000002', '62000000-0000-0000-0000-000000000001', 'Plaquette',  2, 10,     false, true),
    ('63000000-0000-0000-0000-000000000003', '62000000-0000-0000-0000-000000000001', 'Boîte',      3, 100,    false, true),
    ('63000000-0000-0000-0000-000000000004', '62000000-0000-0000-0000-000000000001', 'Carton',     4, 10000,  false, true);

-- ── Conditionnements — Amoxicilline (unité de base uniquement) ────────
INSERT INTO conditionnements (id, medicament_id, nom, niveau, quantite_unite_base, est_unite_base, actif) VALUES
    ('63000000-0000-0000-0000-000000000005', '62000000-0000-0000-0000-000000000002', 'Comprimé', 1, 1,   true,  true),
    ('63000000-0000-0000-0000-000000000006', '62000000-0000-0000-0000-000000000002', 'Boîte',    2, 20,  false, true);

-- ── Conditionnements — Ceftriaxone (flacons) ──────────────────────────
INSERT INTO conditionnements (id, medicament_id, nom, niveau, quantite_unite_base, est_unite_base, actif) VALUES
    ('63000000-0000-0000-0000-000000000007', '62000000-0000-0000-0000-000000000003', 'Flacon', 1, 1,  true,  true),
    ('63000000-0000-0000-0000-000000000008', '62000000-0000-0000-0000-000000000003', 'Carton', 2, 50, false, true);

-- ───────────────────────────────────────────────────────────────────
-- Vérification de cohérence
-- ───────────────────────────────────────────────────────────────────
DO $$
BEGIN
    IF (SELECT COUNT(*) FROM familles WHERE id::text LIKE '60000000%') != 4 THEN
        RAISE EXCEPTION 'Migration V018 : nombre de familles mock incorrect, attendu 4';
    END IF;
    IF (SELECT COUNT(*) FROM formes WHERE id::text LIKE '61000000%') != 4 THEN
        RAISE EXCEPTION 'Migration V018 : nombre de formes mock incorrect, attendu 4';
    END IF;
    IF (SELECT COUNT(*) FROM medicaments WHERE id::text LIKE '62000000%') != 5 THEN
        RAISE EXCEPTION 'Migration V018 : nombre de médicaments mock incorrect, attendu 5';
    END IF;
    IF (SELECT COUNT(*) FROM conditionnements WHERE id::text LIKE '63000000%') != 8 THEN
        RAISE EXCEPTION 'Migration V018 : nombre de conditionnements mock incorrect, attendu 8';
    END IF;
END $$;

-- ═══════════════════════════════════════════════════════════════════
-- Récapitulatif
-- ═══════════════════════════════════════════════════════════════════
-- Familles   : Antalgique, Antibiotique, Antipaludéen, Antihistaminique (archivée)
-- Formes     : Comprimé, Flacon injectable, Sirop, Pommade (archivée)
-- Médicaments: Paracétamol 500mg (4 conditionnements), Amoxicilline 500mg (2),
--              Ceftriaxone 1g (2, chaîne du froid), Sirop Chlorphénamine (minimal),
--              Quinine 300mg (archivé)
-- ═══════════════════════════════════════════════════════════════════
