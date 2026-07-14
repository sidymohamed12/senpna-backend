-- ═══════════════════════════════════════════════════════════════════
-- V031 — Espace fournisseur : rôle FOURNISSEUR et affectation
-- SEN PharmaFlow — Ministère de la Santé du Sénégal
--
-- Un compte utilisateur "espace fournisseur" est un utilisateur normal
-- (table users) affecté à un fournisseur, exactement comme un compte PRA
-- est affecté à un entrepôt et un compte structure sanitaire à une
-- structure_sanitaire (cf. V007). L'affectation reste exclusive : un
-- utilisateur n'est jamais rattaché à plus d'une unité organisationnelle
-- à la fois — appliqué au niveau applicatif par
-- UserAffectationRepositoryPort, chaque méthode affecterXxx remettant les
-- deux autres colonnes à NULL.
-- ═══════════════════════════════════════════════════════════════════

ALTER TABLE users
    ADD COLUMN fournisseur_id UUID;

COMMENT ON COLUMN users.fournisseur_id IS
    'Fournisseur auquel ce compte est rattaché — espace fournisseur uniquement, exclusif de entrepot_id/structure_sanitaire_id';

CREATE INDEX idx_users_fournisseur_id ON users (fournisseur_id) WHERE fournisseur_id IS NOT NULL;

INSERT INTO roles (id, code, nom) VALUES
    ('00000000-0000-0000-0000-000000000010', 'FOURNISSEUR', 'Fournisseur');
