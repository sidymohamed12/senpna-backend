-- ═══════════════════════════════════════════════════════════════════
-- V013 — Données de démonstration : fournisseurs (mock data)
-- SEN PharmaFlow — Ministère de la Santé du Sénégal
--
-- ⚠️  Cette migration vit dans db/migration-dev/, PAS dans
--     db/migration/ : elle n'est chargée que par le profil `dev`
--     (cf. application-dev.yml → spring.flyway.locations), jamais par
--     `prod` (application-prod.yml → classpath:db/migration seul).
--
-- Prérequis : V012__create_fournisseurs.sql (db/migration) déjà
-- appliquée — la table fournisseurs doit exister avant cette insertion.
--
-- Couvre les cas : fournisseur complet, fournisseur minimal (champs
-- optionnels absents), fournisseur désactivé, et un nom volontairement
-- proche d'un autre (recherche insensible à la casse / accents).
-- ═══════════════════════════════════════════════════════════════════

INSERT INTO fournisseurs (id, nom, adresse, telephone, email, contact_principal, actif) VALUES
    -- Fournisseur complet, actif
    ('50000000-0000-0000-0000-000000000001', 'Laboratoire Pharma Sénégal',
        'Zone Industrielle, Dakar', '+221338601000', 'contact@pharma-senegal.sn', 'Cheikh Diop', true),

    -- Fournisseur complet, actif — nom proche du précédent (test recherche)
    ('50000000-0000-0000-0000-000000000002', 'Laboratoire Pharma International',
        'Route de Rufisque, Dakar', '+221338602000', 'contact@pharma-international.sn', 'Fatou Ba', true),

    -- Fournisseur actif, sans adresse ni téléphone ni contact (champs optionnels absents)
    ('50000000-0000-0000-0000-000000000003', 'MedSupply Afrique',
        NULL, NULL, 'contact@medsupply-afrique.com', NULL, true),

    -- Fournisseur désactivé — cas "fournisseur retiré"
    ('50000000-0000-0000-0000-000000000004', 'Ancien Fournisseur SARL',
        'Rue 10, Thiès', '+221338211999', 'contact@ancien-fournisseur.sn', 'Moussa Sarr', false),

    -- Fournisseur actif, avec contact principal mais sans email
    ('50000000-0000-0000-0000-000000000005', 'Générique Pharma Distribution',
        'Avenue Bourguiba, Dakar', '+221338603000', NULL, 'Aïssatou Fall', true);

-- ───────────────────────────────────────────────────────────────────
-- Vérification de cohérence
-- ───────────────────────────────────────────────────────────────────
DO $$
BEGIN
    IF (SELECT COUNT(*) FROM fournisseurs WHERE id::text LIKE '50000000%') != 5 THEN
        RAISE EXCEPTION 'Migration V013 : nombre de fournisseurs mock incorrect, attendu 5';
    END IF;
END $$;

-- ═══════════════════════════════════════════════════════════════════
-- Récapitulatif des fournisseurs de test
-- ═══════════════════════════════════════════════════════════════════
-- Laboratoire Pharma Sénégal        → actif, complet
-- Laboratoire Pharma International  → actif, complet (nom proche du précédent)
-- MedSupply Afrique                 → actif, sans adresse/téléphone/contact
-- Ancien Fournisseur SARL           → DÉSACTIVÉ
-- Générique Pharma Distribution     → actif, sans email
-- ═══════════════════════════════════════════════════════════════════
