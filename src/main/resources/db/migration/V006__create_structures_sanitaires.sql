-- ═══════════════════════════════════════════════════════════════════
-- V006 — Structures sanitaires (établissements bénéficiaires)
-- SEN PharmaFlow — Ministère de la Santé du Sénégal
--
-- Une structure sanitaire naît avec une demande d'adhésion à l'état
-- EN_ATTENTE_VALIDATION (inactive) ; son rattachement à une région et
-- à une PRA (region_id / pra_id) n'est renseigné qu'après validation.
-- ═══════════════════════════════════════════════════════════════════

CREATE TABLE structures_sanitaires (
    id               UUID         NOT NULL DEFAULT uuid_generate_v4(),
    code             VARCHAR(30)  NOT NULL,
    nom              VARCHAR(150) NOT NULL,
    type             VARCHAR(30)  NOT NULL,
    region_id        UUID,
    pra_id           UUID,
    district         VARCHAR(100),
    adresse          VARCHAR(255),
    telephone        VARCHAR(20),
    email            VARCHAR(180),
    responsable      VARCHAR(150),
    statut_adhesion  VARCHAR(30)  NOT NULL DEFAULT 'EN_ATTENTE_VALIDATION',
    motif_rejet      VARCHAR(255),
    actif            BOOLEAN      NOT NULL DEFAULT false,
    created_at       TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at       TIMESTAMP    NOT NULL DEFAULT now(),

    CONSTRAINT pk_structures_sanitaires        PRIMARY KEY (id),
    CONSTRAINT uq_structures_sanitaires_code   UNIQUE (code),
    CONSTRAINT fk_structures_sanitaires_region FOREIGN KEY (region_id) REFERENCES regions(id) ON DELETE RESTRICT,
    CONSTRAINT fk_structures_sanitaires_pra    FOREIGN KEY (pra_id) REFERENCES entrepots(id) ON DELETE RESTRICT,
    CONSTRAINT chk_structures_sanitaires_type CHECK (
        type IN ('HOPITAL', 'DISTRICT_SANITAIRE', 'CENTRE_SANTE', 'POSTE_SANTE', 'ONG')
    ),
    CONSTRAINT chk_structures_sanitaires_statut CHECK (
        statut_adhesion IN ('EN_ATTENTE_VALIDATION', 'VALIDEE', 'REJETEE')
    )
);

COMMENT ON TABLE  structures_sanitaires                 IS 'Établissements de santé bénéficiaires du réseau';
COMMENT ON COLUMN structures_sanitaires.statut_adhesion IS 'Cycle de vie de la demande d''adhésion';
COMMENT ON COLUMN structures_sanitaires.actif           IS 'true uniquement après validation de l''adhésion';

CREATE INDEX idx_structures_sanitaires_type    ON structures_sanitaires(type);
CREATE INDEX idx_structures_sanitaires_region  ON structures_sanitaires(region_id);
CREATE INDEX idx_structures_sanitaires_pra     ON structures_sanitaires(pra_id);
CREATE INDEX idx_structures_sanitaires_statut  ON structures_sanitaires(statut_adhesion);
CREATE INDEX idx_structures_sanitaires_actif   ON structures_sanitaires(actif);
