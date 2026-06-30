-- ═══════════════════════════════════════════════════════════════════
-- V001 — Table des rôles utilisateurs
-- SEN PharmaFlow — Ministère de la Santé du Sénégal
--
-- IMPORTANT : Cette table est gérée UNIQUEMENT par Flyway.
-- Aucun endpoint applicatif ne permet de créer / modifier / supprimer
-- des rôles. Toute évolution passe par une nouvelle migration Vxxx.
-- ═══════════════════════════════════════════════════════════════════

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ── Table roles ───────────────────────────────────────────────────
CREATE TABLE roles (
    id   UUID        NOT NULL DEFAULT uuid_generate_v4(),
    code VARCHAR(50)  NOT NULL,
    nom  VARCHAR(100) NOT NULL,

    CONSTRAINT pk_roles       PRIMARY KEY (id),
    CONSTRAINT uq_roles_code  UNIQUE (code)
);

COMMENT ON TABLE  roles      IS 'Référentiel des rôles — géré exclusivement par Flyway';
COMMENT ON COLUMN roles.code IS 'Identifiant technique Spring Security (ex: ADMIN_PNA)';
COMMENT ON COLUMN roles.nom  IS 'Libellé d''affichage en français';

-- ── Données de référence ─────────────────────────────────────────
-- PNA (Pharmacie Nationale d'Approvisionnement)
INSERT INTO roles (id, code, nom) VALUES
    ('00000000-0000-0000-0000-000000000001', 'ADMIN_PNA',        'Administrateur PNA'),
    ('00000000-0000-0000-0000-000000000002', 'GESTIONNAIRE_PNA', 'Gestionnaire PNA'),
    ('00000000-0000-0000-0000-000000000003', 'PHARMACIEN_PNA',   'Pharmacien PNA'),
    ('00000000-0000-0000-0000-000000000004', 'MAGASINIER_PNA',   'Magasinier PNA');

-- PRA (Pharmacies Régionales d'Approvisionnement)
INSERT INTO roles (id, code, nom) VALUES
    ('00000000-0000-0000-0000-000000000005', 'ADMIN_PRA',        'Administrateur PRA'),
    ('00000000-0000-0000-0000-000000000006', 'GESTIONNAIRE_PRA', 'Gestionnaire PRA'),
    ('00000000-0000-0000-0000-000000000007', 'PHARMACIEN_PRA',   'Pharmacien PRA'),
    ('00000000-0000-0000-0000-000000000008', 'MAGASINIER_PRA',   'Magasinier PRA');

-- Structures sanitaires
INSERT INTO roles (id, code, nom) VALUES
    ('00000000-0000-0000-0000-000000000009', 'GESTIONNAIRE_STRUCTURE', 'Gestionnaire Structure Sanitaire');

-- Vérification : 9 rôles attendus
DO $$
BEGIN
    IF (SELECT COUNT(*) FROM roles) != 9 THEN
        RAISE EXCEPTION 'Migration V001 : nombre de rôles incorrect, attendu 9';
    END IF;
END $$;
