-- ═══════════════════════════════════════════════════════════════════
-- V022 — Données de démonstration : lots, stocks, mouvements (mock data)
-- SEN PharmaFlow — Ministère de la Santé du Sénégal
--
-- ⚠️  Cette migration vit dans db/migration-dev/, PAS dans
--     db/migration/ : elle n'est chargée que par le profil `dev`
--     (cf. application-dev.yml → spring.flyway.locations), jamais par
--     `prod` (application-prod.yml → classpath:db/migration seul).
--
-- Prérequis : V011 (entrepôts + utilisateurs mock), V013 (fournisseurs
-- mock), V018 (médicaments mock) et V019-V021 (db/migration — schéma
-- lots / stocks / mouvements_stock) déjà appliquées.
--
-- Couvre les scénarios clés du module stock :
--   • FEFO      : deux lots ACTIF du même médicament (Paracétamol) dans
--                 le même entrepôt, expirations différentes.
--   • Seuil     : ligne de stock dont la quantité disponible à la vente
--                 atteint exactement le seuil d'alerte.
--   • Rupture   : une ligne à 0 avec réapprovisionnement en cours
--                 (quantite_en_commande > 0), une autre sans réappro.
--   • Lot bloqué: stock physiquement présent mais non réservable/
--                 expédiable (pharmacovigilance).
--   • Lot expiré: résidu de stock non encore détruit, tracé par un
--                 mouvement PEREMPTION.
--   • Référentiel archivé : stock résiduel d'un médicament désactivé
--                 (Quinine), à liquider.
--   • Journal   : cycle complet achat (PNA) → transfert → réception,
--                 casse, péremption — couvre 5 des 12 types de
--                 mouvement (cf. modèle métier complémentaire §3).
--
-- Les quantités de chaque ligne `stocks` sont volontairement
-- reconstituées à partir de la somme des mouvements `mouvements_stock`
-- correspondants ci-dessous, afin que le jeu de données reste cohérent
-- de bout en bout (utile pour des tests d'intégration qui rejoueraient
-- le journal).
-- ═══════════════════════════════════════════════════════════════════

-- ───────────────────────────────────────────────────────────────────
-- 1. Lots
--    Médicaments (V018) : Paracétamol=62...0001, Amoxicilline=62...0002,
--    Ceftriaxone=62...0003, Sirop Chlorphénamine=62...0004,
--    Quinine (archivé, actif=false)=62...0005.
--    Fournisseurs (V013) : Pharma Sénégal=50...0001,
--    Pharma International=50...0002, MedSupply Afrique=50...0003,
--    Ancien Fournisseur SARL (désactivé)=50...0004,
--    Générique Pharma Distribution=50...0005.
-- ───────────────────────────────────────────────────────────────────
INSERT INTO lots (id, numero_lot, medicament_id, fournisseur_id, date_fabrication, date_expiration,
    prix_achat, prix_vente, statut) VALUES

    -- Paracétamol — expiration proche (30 j) — cas FEFO (à consommer en premier)
    ('64000000-0000-0000-0000-000000000001', 'PARA-2026-A001',
        '62000000-0000-0000-0000-000000000001', '50000000-0000-0000-0000-000000000001',
        CURRENT_DATE - INTERVAL '60 days', CURRENT_DATE + INTERVAL '30 days',
        18.00, 25.00, 'ACTIF'),

    -- Paracétamol — expiration lointaine (365 j), même médicament — cas FEFO (à consommer en second)
    ('64000000-0000-0000-0000-000000000002', 'PARA-2026-B014',
        '62000000-0000-0000-0000-000000000001', '50000000-0000-0000-0000-000000000002',
        CURRENT_DATE - INTERVAL '30 days', CURRENT_DATE + INTERVAL '365 days',
        19.00, 26.00, 'ACTIF'),

    -- Amoxicilline — ACTIF, expiration à 6 mois
    ('64000000-0000-0000-0000-000000000003', 'AMOX-2026-0042',
        '62000000-0000-0000-0000-000000000002', '50000000-0000-0000-0000-000000000003',
        CURRENT_DATE - INTERVAL '90 days', CURRENT_DATE + INTERVAL '180 days',
        150.00, 200.00, 'ACTIF'),

    -- Ceftriaxone — ACTIF, chaîne du froid, expiration à ~3 mois
    ('64000000-0000-0000-0000-000000000004', 'CEFTRIA-2026-007',
        '62000000-0000-0000-0000-000000000003', '50000000-0000-0000-0000-000000000001',
        CURRENT_DATE - INTERVAL '20 days', CURRENT_DATE + INTERVAL '85 days',
        3500.00, 4500.00, 'ACTIF'),

    -- Paracétamol — EXPIRÉ (date dépassée), résidu non encore détruit (cf. mouvement PEREMPTION plus bas)
    ('64000000-0000-0000-0000-000000000005', 'PARA-2025-Z099',
        '62000000-0000-0000-0000-000000000001', '50000000-0000-0000-0000-000000000005',
        CURRENT_DATE - INTERVAL '400 days', CURRENT_DATE - INTERVAL '5 days',
        17.00, 24.00, 'EXPIRE'),

    -- Amoxicilline — BLOQUÉ (mise en quarantaine suite à une déclaration de pharmacovigilance)
    ('64000000-0000-0000-0000-000000000006', 'AMOX-2026-0055',
        '62000000-0000-0000-0000-000000000002', '50000000-0000-0000-0000-000000000001',
        CURRENT_DATE - INTERVAL '45 days', CURRENT_DATE + INTERVAL '200 days',
        155.00, 210.00, 'BLOQUE'),

    -- Sirop Chlorphénamine — ACTIF, expiration à ~1 mois (déclenche l'alerte du dernier palier)
    -- Médicament minimal : prix non renseignés côté lot également (cas champs optionnels absents)
    ('64000000-0000-0000-0000-000000000007', 'CHLOR-2025-0891',
        '62000000-0000-0000-0000-000000000004', '50000000-0000-0000-0000-000000000003',
        CURRENT_DATE - INTERVAL '300 days', CURRENT_DATE + INTERVAL '25 days',
        NULL, NULL, 'ACTIF'),

    -- Quinine — médicament archivé (actif=false côté référentiel), stock résiduel à liquider
    ('64000000-0000-0000-0000-000000000008', 'QUININE-2024-011',
        '62000000-0000-0000-0000-000000000005', '50000000-0000-0000-0000-000000000004',
        CURRENT_DATE - INTERVAL '500 days', CURRENT_DATE + INTERVAL '40 days',
        80.00, 120.00, 'ACTIF');

-- ───────────────────────────────────────────────────────────────────
-- 2. Stocks
--    Entrepôts (V011) : PNA=30...0001, PRA-Thiès=30...0002,
--    PRA-Dakar=30...0003, PRA-Kaolack (désactivée)=30...0004,
--    PRA-Ziguinchor=30...0005.
--    Quantités reconstituées à partir des mouvements de la section 3.
-- ───────────────────────────────────────────────────────────────────
INSERT INTO stocks (id, entrepot_id, lot_id, medicament_id, quantite_disponible, quantite_reservee,
    quantite_en_commande, seuil_alerte) VALUES

    -- PNA — Paracétamol (lot proche péremption) — 60 000 reçus, 5 000 transférés vers PRA-Thiès → 55 000
    ('65000000-0000-0000-0000-000000000001', '30000000-0000-0000-0000-000000000001',
        '64000000-0000-0000-0000-000000000001', '62000000-0000-0000-0000-000000000001',
        55000, 10000, 0, 20000),

    -- PNA — Paracétamol (lot lointain) — stock confortable, aucune réservation
    ('65000000-0000-0000-0000-000000000002', '30000000-0000-0000-0000-000000000001',
        '64000000-0000-0000-0000-000000000002', '62000000-0000-0000-0000-000000000001',
        150000, 0, 0, 20000),

    -- PRA-Thiès — Paracétamol (lot proche péremption, reçu par transfert) — seuil pile atteint
    ('65000000-0000-0000-0000-000000000003', '30000000-0000-0000-0000-000000000002',
        '64000000-0000-0000-0000-000000000001', '62000000-0000-0000-0000-000000000001',
        5000, 0, 0, 5000),

    -- PNA — Ceftriaxone — 1 000 reçus, 5 flacons cassés → 995
    ('65000000-0000-0000-0000-000000000004', '30000000-0000-0000-0000-000000000001',
        '64000000-0000-0000-0000-000000000004', '62000000-0000-0000-0000-000000000003',
        995, 200, 0, 500),

    -- PRA-Ziguinchor — Amoxicilline — RUPTURE, réapprovisionnement déjà demandé à la PNA
    ('65000000-0000-0000-0000-000000000005', '30000000-0000-0000-0000-000000000005',
        '64000000-0000-0000-0000-000000000003', '62000000-0000-0000-0000-000000000002',
        0, 0, 2000, 5000),

    -- PRA-Thiès — Amoxicilline (lot BLOQUÉ) — stock physiquement présent mais non réservable/expédiable
    ('65000000-0000-0000-0000-000000000006', '30000000-0000-0000-0000-000000000002',
        '64000000-0000-0000-0000-000000000006', '62000000-0000-0000-0000-000000000002',
        2000, 0, 0, 1000),

    -- PNA — Paracétamol (lot EXPIRÉ) — résidu en attente de destruction, sans seuil de suivi
    ('65000000-0000-0000-0000-000000000007', '30000000-0000-0000-0000-000000000001',
        '64000000-0000-0000-0000-000000000005', '62000000-0000-0000-0000-000000000001',
        300, 0, 0, NULL),

    -- PRA-Dakar — Sirop Chlorphénamine — reçu par transfert depuis la PNA, partiellement réservé
    ('65000000-0000-0000-0000-000000000008', '30000000-0000-0000-0000-000000000003',
        '64000000-0000-0000-0000-000000000007', '62000000-0000-0000-0000-000000000004',
        4000, 500, 0, 1000),

    -- PNA — Quinine (médicament archivé) — stock résiduel, sans seuil de suivi
    ('65000000-0000-0000-0000-000000000009', '30000000-0000-0000-0000-000000000001',
        '64000000-0000-0000-0000-000000000008', '62000000-0000-0000-0000-000000000005',
        150, 0, 0, NULL),

    -- PNA — Amoxicilline (lot MedSupply) — entièrement transféré vers Ziguinchor, ligne épuisée sans réappro
    ('65000000-0000-0000-0000-000000000010', '30000000-0000-0000-0000-000000000001',
        '64000000-0000-0000-0000-000000000003', '62000000-0000-0000-0000-000000000002',
        0, 0, 0, 1000);

-- ───────────────────────────────────────────────────────────────────
-- 3. Mouvements de stock (journal append-only)
--    Utilisateurs (V011) : ADMIN_PNA=20...0001, GESTIONNAIRE_PNA=20...0002,
--    PHARMACIEN_PNA=20...0003, MAGASINIER_PNA=20...0004,
--    ADMIN_PRA (Thiès)=20...0005, GESTIONNAIRE_PRA (Dakar)=20...0006,
--    PHARMACIEN_PRA (Thiès)=20...0007, MAGASINIER_PRA (Dakar)=20...0008.
-- ───────────────────────────────────────────────────────────────────
INSERT INTO mouvements_stock (id, type_mouvement, sens, entrepot_source_id, entrepot_destination_id, commande_id,
    lot_id, medicament_id, quantite, date_mouvement, reference_document, motif, utilisateur_id) VALUES

    -- Achat PNA — Paracétamol (lot proche péremption)
    ('66000000-0000-0000-0000-000000000001', 'ENTREE_ACHAT', 'ENTREE', NULL, '30000000-0000-0000-0000-000000000001',
        NULL, '64000000-0000-0000-0000-000000000001', '62000000-0000-0000-0000-000000000001', 60000,
        now() - INTERVAL '60 days', 'BC-2026-0001', NULL, '20000000-0000-0000-0000-000000000004'),

    -- Achat PNA — Paracétamol (lot lointain)
    ('66000000-0000-0000-0000-000000000002', 'ENTREE_ACHAT', 'ENTREE', NULL, '30000000-0000-0000-0000-000000000001',
        NULL, '64000000-0000-0000-0000-000000000002', '62000000-0000-0000-0000-000000000001', 150000,
        now() - INTERVAL '28 days', 'BC-2026-0002', NULL, '20000000-0000-0000-0000-000000000004'),

    -- Transfert PNA → PRA-Thiès — Paracétamol (proche péremption), expédition puis réception
    ('66000000-0000-0000-0000-000000000003', 'SORTIE_TRANSFERT', 'SORTIE', '30000000-0000-0000-0000-000000000001',
        '30000000-0000-0000-0000-000000000002', NULL, '64000000-0000-0000-0000-000000000001',
        '62000000-0000-0000-0000-000000000001', 5000, now() - INTERVAL '15 days', 'TRF-2026-0010', NULL,
        '20000000-0000-0000-0000-000000000004'),

    ('66000000-0000-0000-0000-000000000004', 'ENTREE_TRANSFERT', 'ENTREE', '30000000-0000-0000-0000-000000000001',
        '30000000-0000-0000-0000-000000000002', NULL, '64000000-0000-0000-0000-000000000001',
        '62000000-0000-0000-0000-000000000001', 5000, now() - INTERVAL '14 days', 'TRF-2026-0010', NULL,
        '20000000-0000-0000-0000-000000000005'),

    -- Achat PNA — Ceftriaxone
    ('66000000-0000-0000-0000-000000000005', 'ENTREE_ACHAT', 'ENTREE', NULL, '30000000-0000-0000-0000-000000000001',
        NULL, '64000000-0000-0000-0000-000000000004', '62000000-0000-0000-0000-000000000003', 1000,
        now() - INTERVAL '18 days', 'BC-2026-0003', NULL, '20000000-0000-0000-0000-000000000004'),

    -- Casse — Ceftriaxone (manutention entrepôt PNA)
    ('66000000-0000-0000-0000-000000000006', 'CASSE', 'SORTIE', '30000000-0000-0000-0000-000000000001', NULL,
        NULL, '64000000-0000-0000-0000-000000000004', '62000000-0000-0000-0000-000000000003', 5,
        now() - INTERVAL '10 days', NULL, 'Casse de 5 flacons durant la manutention', '20000000-0000-0000-0000-000000000004'),

    -- Achat PNA — Amoxicilline, puis transfert intégral vers PRA-Ziguinchor
    ('66000000-0000-0000-0000-000000000007', 'ENTREE_ACHAT', 'ENTREE', NULL, '30000000-0000-0000-0000-000000000001',
        NULL, '64000000-0000-0000-0000-000000000003', '62000000-0000-0000-0000-000000000002', 3000,
        now() - INTERVAL '85 days', 'BC-2026-0004', NULL, '20000000-0000-0000-0000-000000000004'),

    ('66000000-0000-0000-0000-000000000008', 'SORTIE_TRANSFERT', 'SORTIE', '30000000-0000-0000-0000-000000000001',
        '30000000-0000-0000-0000-000000000005', NULL, '64000000-0000-0000-0000-000000000003',
        '62000000-0000-0000-0000-000000000002', 3000, now() - INTERVAL '30 days', 'TRF-2026-0020', NULL,
        '20000000-0000-0000-0000-000000000004'),

    ('66000000-0000-0000-0000-000000000009', 'ENTREE_TRANSFERT', 'ENTREE', '30000000-0000-0000-0000-000000000001',
        '30000000-0000-0000-0000-000000000005', NULL, '64000000-0000-0000-0000-000000000003',
        '62000000-0000-0000-0000-000000000002', 3000, now() - INTERVAL '29 days', 'TRF-2026-0020', NULL,
        '20000000-0000-0000-0000-000000000006'),

    -- Distribution PRA-Ziguinchor → structures sanitaires : épuise le stock, déclenche la rupture (cf. S5)
    ('66000000-0000-0000-0000-000000000010', 'SORTIE_STRUCTURE', 'SORTIE', '30000000-0000-0000-0000-000000000005',
        NULL, NULL, '64000000-0000-0000-0000-000000000003', '62000000-0000-0000-0000-000000000002', 3000,
        now() - INTERVAL '5 days', 'CMD-2026-0100', 'Distribution aux structures sanitaires du district',
        '20000000-0000-0000-0000-000000000006'),

    -- Achat PNA — Amoxicilline (lot destiné au blocage pharmacovigilance), transfert vers PRA-Thiès
    ('66000000-0000-0000-0000-000000000011', 'ENTREE_ACHAT', 'ENTREE', NULL, '30000000-0000-0000-0000-000000000001',
        NULL, '64000000-0000-0000-0000-000000000006', '62000000-0000-0000-0000-000000000002', 2000,
        now() - INTERVAL '45 days', 'BC-2026-0005', NULL, '20000000-0000-0000-0000-000000000004'),

    ('66000000-0000-0000-0000-000000000012', 'SORTIE_TRANSFERT', 'SORTIE', '30000000-0000-0000-0000-000000000001',
        '30000000-0000-0000-0000-000000000002', NULL, '64000000-0000-0000-0000-000000000006',
        '62000000-0000-0000-0000-000000000002', 2000, now() - INTERVAL '40 days', 'TRF-2026-0030', NULL,
        '20000000-0000-0000-0000-000000000004'),

    ('66000000-0000-0000-0000-000000000013', 'ENTREE_TRANSFERT', 'ENTREE', '30000000-0000-0000-0000-000000000001',
        '30000000-0000-0000-0000-000000000002', NULL, '64000000-0000-0000-0000-000000000006',
        '62000000-0000-0000-0000-000000000002', 2000, now() - INTERVAL '39 days', 'TRF-2026-0030', NULL,
        '20000000-0000-0000-0000-000000000005'),

    -- Achat PNA — Paracétamol (ancien lot, aujourd'hui expiré)
    ('66000000-0000-0000-0000-000000000014', 'ENTREE_ACHAT', 'ENTREE', NULL, '30000000-0000-0000-0000-000000000001',
        NULL, '64000000-0000-0000-0000-000000000005', '62000000-0000-0000-0000-000000000001', 500,
        now() - INTERVAL '400 days', 'BC-2025-0099', NULL, '20000000-0000-0000-0000-000000000004'),

    -- Péremption — retrait du stock utilisable après franchissement de la date d'expiration
    ('66000000-0000-0000-0000-000000000015', 'PEREMPTION', 'SORTIE', '30000000-0000-0000-0000-000000000001', NULL,
        NULL, '64000000-0000-0000-0000-000000000005', '62000000-0000-0000-0000-000000000001', 200,
        now() - INTERVAL '3 days', NULL, 'Retrait du stock utilisable — lot périmé, en attente de destruction',
        '20000000-0000-0000-0000-000000000003'),

    -- Achat PNA — Quinine (médicament aujourd'hui archivé) — stock résiduel historique
    ('66000000-0000-0000-0000-000000000016', 'ENTREE_ACHAT', 'ENTREE', NULL, '30000000-0000-0000-0000-000000000001',
        NULL, '64000000-0000-0000-0000-000000000008', '62000000-0000-0000-0000-000000000005', 150,
        now() - INTERVAL '500 days', 'BC-2024-0050', NULL, '20000000-0000-0000-0000-000000000004'),

    -- Achat PNA — Sirop Chlorphénamine, puis transfert intégral vers PRA-Dakar
    ('66000000-0000-0000-0000-000000000017', 'ENTREE_ACHAT', 'ENTREE', NULL, '30000000-0000-0000-0000-000000000001',
        NULL, '64000000-0000-0000-0000-000000000007', '62000000-0000-0000-0000-000000000004', 4000,
        now() - INTERVAL '60 days', 'BC-2026-0006', NULL, '20000000-0000-0000-0000-000000000004'),

    ('66000000-0000-0000-0000-000000000018', 'SORTIE_TRANSFERT', 'SORTIE', '30000000-0000-0000-0000-000000000001',
        '30000000-0000-0000-0000-000000000003', NULL, '64000000-0000-0000-0000-000000000007',
        '62000000-0000-0000-0000-000000000004', 4000, now() - INTERVAL '55 days', 'TRF-2026-0040', NULL,
        '20000000-0000-0000-0000-000000000004'),

    ('66000000-0000-0000-0000-000000000019', 'ENTREE_TRANSFERT', 'ENTREE', '30000000-0000-0000-0000-000000000001',
        '30000000-0000-0000-0000-000000000003', NULL, '64000000-0000-0000-0000-000000000007',
        '62000000-0000-0000-0000-000000000004', 4000, now() - INTERVAL '54 days', 'TRF-2026-0040', NULL,
        '20000000-0000-0000-0000-000000000008');

-- ───────────────────────────────────────────────────────────────────
-- Vérification de cohérence
-- ───────────────────────────────────────────────────────────────────
DO $$
BEGIN
    IF (SELECT COUNT(*) FROM lots WHERE id::text LIKE '64000000%') != 8 THEN
        RAISE EXCEPTION 'Migration V022 : nombre de lots mock incorrect, attendu 8';
    END IF;
    IF (SELECT COUNT(*) FROM stocks WHERE id::text LIKE '65000000%') != 10 THEN
        RAISE EXCEPTION 'Migration V022 : nombre de lignes de stock mock incorrect, attendu 10';
    END IF;
    IF (SELECT COUNT(*) FROM mouvements_stock WHERE id::text LIKE '66000000%') != 19 THEN
        RAISE EXCEPTION 'Migration V022 : nombre de mouvements mock incorrect, attendu 19';
    END IF;
    IF EXISTS (SELECT 1 FROM stocks WHERE quantite_reservee > quantite_disponible) THEN
        RAISE EXCEPTION 'Migration V022 : incohérence détectée — quantité réservée supérieure à la quantité disponible';
    END IF;
END $$;

-- ═══════════════════════════════════════════════════════════════════
-- Récapitulatif des scénarios couverts
-- ═══════════════════════════════════════════════════════════════════
-- FEFO                : Paracétamol — 2 lots ACTIF à la PNA, expirations 30j / 365j
-- Seuil atteint (pile) : PRA-Thiès / Paracétamol — disponible = seuil = 5 000
-- Rupture + réappro    : PRA-Ziguinchor / Amoxicilline — 0 disponible, 2 000 en commande
-- Rupture sans réappro : PNA / Amoxicilline — 0 disponible, transfert total effectué
-- Lot bloqué            : PRA-Thiès / Amoxicilline — stock présent, non réservable/expédiable
-- Lot expiré             : PNA / Paracétamol — résidu 300, péremption tracée (mouvement)
-- Référentiel archivé    : PNA / Quinine — stock résiduel à liquider
-- Journal complet         : 19 mouvements couvrant ENTREE_ACHAT, ENTREE_TRANSFERT,
--                           SORTIE_TRANSFERT, SORTIE_STRUCTURE, CASSE, PEREMPTION
-- ═══════════════════════════════════════════════════════════════════
