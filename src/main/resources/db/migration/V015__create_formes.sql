-- ═══════════════════════════════════════════════════════════════════
-- V015 — Formes pharmaceutiques
-- SEN PharmaFlow — Ministère de la Santé du Sénégal
--
-- Référentiel national des formes galéniques (cf. doc. métier §6).
-- Mêmes principes que familles (V014) : référencée par formeId sans FK
-- physique, retrait exclusivement logique via actif.
-- ═══════════════════════════════════════════════════════════════════

CREATE TABLE formes (
    id          UUID         NOT NULL DEFAULT uuid_generate_v4(),
    code        VARCHAR(30)  NOT NULL,
    libelle     VARCHAR(150) NOT NULL,
    description VARCHAR(500),
    actif       BOOLEAN      NOT NULL DEFAULT true,
    created_at  TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at  TIMESTAMP    NOT NULL DEFAULT now(),

    CONSTRAINT pk_formes PRIMARY KEY (id)
);

COMMENT ON TABLE  formes       IS 'Formes pharmaceutiques — référentiel national des formes galéniques';
COMMENT ON COLUMN formes.actif IS 'Seul mécanisme de retrait — pas de suppression physique';

-- Unicité du code insensible à la casse.
CREATE UNIQUE INDEX uq_formes_code ON formes (LOWER(code));

CREATE INDEX idx_formes_actif ON formes (actif);
