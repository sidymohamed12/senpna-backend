-- ═══════════════════════════════════════════════════════════════════
-- V007 — Affectation organisationnelle des utilisateurs
-- SEN PharmaFlow — Ministère de la Santé du Sénégal
--
-- Ajoute à la table users les deux colonnes d'affectation à une unité
-- organisationnelle (cf. doc. métier §2 : "Affectation d'un utilisateur
-- à : une PNA ; une PRA ; une structure sanitaire").
--
-- Un utilisateur n'est rattaché qu'à une seule unité à la fois :
-- entrepot_id (PNA centrale ou PRA) OU structure_sanitaire_id, jamais
-- les deux simultanément (cf. chk_users_affectation_exclusive).
--
-- Ces colonnes sont détenues et gérées exclusivement par le module
-- organisation (cf. UserAffectationRepositoryPort) : les modules
-- auth/utilisateurs n'en ont pas connaissance.
-- ═══════════════════════════════════════════════════════════════════

ALTER TABLE users
    ADD COLUMN entrepot_id UUID,
    ADD COLUMN structure_sanitaire_id UUID;

ALTER TABLE users
    ADD CONSTRAINT fk_users_entrepot
        FOREIGN KEY (entrepot_id) REFERENCES entrepots(id) ON DELETE SET NULL,
    ADD CONSTRAINT fk_users_structure_sanitaire
        FOREIGN KEY (structure_sanitaire_id) REFERENCES structures_sanitaires(id) ON DELETE SET NULL,
    ADD CONSTRAINT chk_users_affectation_exclusive
        CHECK (entrepot_id IS NULL OR structure_sanitaire_id IS NULL);

COMMENT ON COLUMN users.entrepot_id            IS 'Entrepôt (PNA centrale ou PRA) auquel l''utilisateur est rattaché';
COMMENT ON COLUMN users.structure_sanitaire_id IS 'Structure sanitaire à laquelle l''utilisateur est rattaché';

CREATE INDEX idx_users_entrepot_id            ON users(entrepot_id);
CREATE INDEX idx_users_structure_sanitaire_id ON users(structure_sanitaire_id);
