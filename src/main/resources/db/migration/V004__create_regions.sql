-- ═══════════════════════════════════════════════════════════════════
-- V004 — Régions administratives
-- SEN PharmaFlow — Ministère de la Santé du Sénégal
--
-- Référentiel de base auquel sont rattachés les entrepôts de type PRA
-- (V005) et les structures sanitaires (V006).
-- ═══════════════════════════════════════════════════════════════════

CREATE TABLE regions (
    id         UUID         NOT NULL DEFAULT uuid_generate_v4(),
    code       VARCHAR(20)  NOT NULL,
    nom        VARCHAR(100) NOT NULL,
    actif      BOOLEAN      NOT NULL DEFAULT true,
    created_at TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at TIMESTAMP    NOT NULL DEFAULT now(),

    CONSTRAINT pk_regions      PRIMARY KEY (id),
    CONSTRAINT uq_regions_code UNIQUE (code)
);

COMMENT ON TABLE  regions      IS 'Découpage administratif régional — référentiel de base';
COMMENT ON COLUMN regions.code IS 'Code technique unique de la région (ex: DAKAR, THIES)';
COMMENT ON COLUMN regions.nom  IS 'Libellé d''affichage en français';

CREATE INDEX idx_regions_actif ON regions(actif);
