-- ═══════════════════════════════════════════════════════════════════
-- V017 — Conditionnements
-- SEN PharmaFlow — Ministère de la Santé du Sénégal
--
-- Niveaux d'emballage d'un médicament (cf. doc. métier §7 et §5 du
-- modèle métier complémentaire). Chaque médicament actif doit posséder
-- exactement un conditionnement marqué est_unite_base = true — invariant
-- inter-lignes vérifié par la couche applicative (use cases), la base
-- ne peut pas exprimer une contrainte « au plus un par medicament_id »
-- combinée à un flag booléen sans un index partiel dédié (ci-dessous).
-- Aucune suppression physique — retrait via actif uniquement.
-- ═══════════════════════════════════════════════════════════════════

CREATE TABLE conditionnements (
    id                   UUID          NOT NULL DEFAULT uuid_generate_v4(),
    medicament_id        UUID          NOT NULL,
    nom                  VARCHAR(100)  NOT NULL,
    niveau               INTEGER       NOT NULL,
    quantite_unite_base  NUMERIC(14,4) NOT NULL,
    est_unite_base       BOOLEAN       NOT NULL DEFAULT false,
    actif                BOOLEAN       NOT NULL DEFAULT true,
    created_at           TIMESTAMP     NOT NULL DEFAULT now(),
    updated_at           TIMESTAMP     NOT NULL DEFAULT now(),

    CONSTRAINT pk_conditionnements             PRIMARY KEY (id),
    CONSTRAINT chk_conditionnements_niveau     CHECK (niveau >= 1),
    CONSTRAINT chk_conditionnements_quantite   CHECK (quantite_unite_base > 0),
    CONSTRAINT chk_conditionnements_unite_base CHECK (NOT est_unite_base OR quantite_unite_base = 1)
);

COMMENT ON TABLE  conditionnements                     IS 'Niveaux d''emballage (conditionnement) d''un médicament';
COMMENT ON COLUMN conditionnements.medicament_id       IS 'Référence logique vers medicaments.id (pas de FK physique)';
COMMENT ON COLUMN conditionnements.quantite_unite_base IS 'Équivalence en unités de base (ex : 1 carton = 1000 comprimés → 1000)';
COMMENT ON COLUMN conditionnements.est_unite_base      IS 'Unité dans laquelle le stock réel du médicament est conservé — une seule par médicament actif';
COMMENT ON COLUMN conditionnements.actif               IS 'Seul mécanisme de retrait — pas de suppression physique';

-- Un médicament ne peut avoir qu'un seul nom de conditionnement (ex: "Carton").
CREATE UNIQUE INDEX uq_conditionnements_medicament_nom
    ON conditionnements (medicament_id, LOWER(nom));

-- Un médicament ne peut avoir qu'un seul niveau donné (ordre d'emballage).
CREATE UNIQUE INDEX uq_conditionnements_medicament_niveau
    ON conditionnements (medicament_id, niveau);

-- Un médicament ne peut avoir qu'une seule unité de base active — index
-- partiel : seules les lignes est_unite_base = true et actif = true sont
-- concernées par l'unicité.
CREATE UNIQUE INDEX uq_conditionnements_medicament_unite_base_active
    ON conditionnements (medicament_id)
    WHERE est_unite_base = true AND actif = true;

CREATE INDEX idx_conditionnements_medicament_id ON conditionnements (medicament_id);
CREATE INDEX idx_conditionnements_actif         ON conditionnements (actif);
