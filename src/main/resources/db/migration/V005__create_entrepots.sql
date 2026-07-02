-- ═══════════════════════════════════════════════════════════════════
-- V005 — Entrepôts (PNA centrale / PRA)
-- SEN PharmaFlow — Ministère de la Santé du Sénégal
--
-- Une PRA (Pharmacie Régionale d'Approvisionnement) est obligatoirement
-- rattachée à une région (cf. contrainte chk_entrepots_pra_region).
-- La PNA centrale, entrepôt unique de type PNA_CENTRAL, est provisionnée
-- par script d'administration (pas de rattachement régional).
-- ═══════════════════════════════════════════════════════════════════

CREATE TABLE entrepots (
    id                   UUID         NOT NULL DEFAULT uuid_generate_v4(),
    code                 VARCHAR(30)  NOT NULL,
    nom                  VARCHAR(150) NOT NULL,
    type                 VARCHAR(20)  NOT NULL,
    region_id            UUID,
    adresse              VARCHAR(255),
    telephone            VARCHAR(20),
    responsable_user_id  UUID,
    actif                BOOLEAN      NOT NULL DEFAULT true,
    created_at           TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at           TIMESTAMP    NOT NULL DEFAULT now(),

    CONSTRAINT pk_entrepots          PRIMARY KEY (id),
    CONSTRAINT uq_entrepots_code     UNIQUE (code),
    CONSTRAINT fk_entrepots_region   FOREIGN KEY (region_id) REFERENCES regions(id) ON DELETE RESTRICT,
    CONSTRAINT chk_entrepots_type        CHECK (type IN ('PNA_CENTRAL', 'PRA')),
    CONSTRAINT chk_entrepots_pra_region  CHECK (type <> 'PRA' OR region_id IS NOT NULL)
);

COMMENT ON TABLE  entrepots            IS 'Entrepôts de stockage — PNA centrale ou PRA';
COMMENT ON COLUMN entrepots.type       IS 'PNA_CENTRAL (unique, national) ou PRA (régional)';
COMMENT ON COLUMN entrepots.region_id  IS 'Obligatoire pour une PRA — non applicable à la PNA centrale';

CREATE INDEX idx_entrepots_type      ON entrepots(type);
CREATE INDEX idx_entrepots_region_id ON entrepots(region_id);
CREATE INDEX idx_entrepots_actif     ON entrepots(actif);
