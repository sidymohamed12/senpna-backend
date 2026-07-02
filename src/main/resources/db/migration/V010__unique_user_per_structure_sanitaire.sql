-- ═══════════════════════════════════════════════════════════════════
-- V010 — Unicité du compte par structure sanitaire
-- SEN PharmaFlow — Ministère de la Santé du Sénégal
--
-- Une structure sanitaire ne doit avoir qu'un seul compte
-- GESTIONNAIRE_STRUCTURE rattaché (cf. CreateGestionnaireStructureAccountUseCaseImpl,
-- qui applique déjà ce garde de manière applicative via existsByEmail).
-- Cette contrainte est le filet de sécurité au niveau base de données :
-- en PostgreSQL, UNIQUE autorise plusieurs NULL, donc elle ne s'applique
-- qu'aux utilisateurs effectivement affectés à une structure.
-- ═══════════════════════════════════════════════════════════════════

ALTER TABLE users
    ADD CONSTRAINT uq_users_structure_sanitaire_id UNIQUE (structure_sanitaire_id);

COMMENT ON CONSTRAINT uq_users_structure_sanitaire_id ON users
    IS 'Une structure sanitaire ne peut avoir qu''un seul compte utilisateur rattaché';
