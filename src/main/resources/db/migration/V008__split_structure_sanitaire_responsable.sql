-- ═══════════════════════════════════════════════════════════════════
-- V008 — Scission du responsable de structure sanitaire (nom/prénom)
-- SEN PharmaFlow — Ministère de la Santé du Sénégal
--
-- Le compte GESTIONNAIRE_STRUCTURE créé automatiquement à la validation
-- de l'adhésion (cf. AdhesionValideeEvent) exige un nom et un prénom
-- séparés (comme tout compte utilisateur) — un champ libre unique
-- "responsable" ne suffisait pas.
-- ═══════════════════════════════════════════════════════════════════

ALTER TABLE structures_sanitaires
    ADD COLUMN responsable_nom    VARCHAR(100),
    ADD COLUMN responsable_prenom VARCHAR(100);

UPDATE structures_sanitaires
SET responsable_nom = COALESCE(NULLIF(TRIM(responsable), ''), 'Inconnu')
WHERE responsable_nom IS NULL;

ALTER TABLE structures_sanitaires
    DROP COLUMN responsable;

COMMENT ON COLUMN structures_sanitaires.responsable_nom    IS 'Nom du responsable désigné par la structure';
COMMENT ON COLUMN structures_sanitaires.responsable_prenom IS 'Prénom du responsable désigné par la structure';
