-- ═══════════════════════════════════════════════════════════════════
-- V014 — Familles thérapeutiques
-- SEN PharmaFlow — Ministère de la Santé du Sénégal
--
-- Référentiel national de classification des médicaments (cf. doc.
-- métier §6). Référencée uniquement par familleId (sans FK physique)
-- depuis la table medicaments — cohérence assurée par la couche
-- applicative (use cases), pas par la base, afin de garder chaque
-- agrégat indépendamment persistable (règle DDD « un agrégat = une
-- transaction »). Aucune suppression physique — seul le champ actif
-- permet le retrait, pour préserver l'intégrité des médicaments déjà
-- rattachés.
-- ═══════════════════════════════════════════════════════════════════

CREATE TABLE familles (
    id          UUID         NOT NULL DEFAULT uuid_generate_v4(),
    code        VARCHAR(30)  NOT NULL,
    libelle     VARCHAR(150) NOT NULL,
    description VARCHAR(500),
    actif       BOOLEAN      NOT NULL DEFAULT true,
    created_at  TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at  TIMESTAMP    NOT NULL DEFAULT now(),

    CONSTRAINT pk_familles PRIMARY KEY (id)
);

COMMENT ON TABLE  familles       IS 'Familles thérapeutiques — référentiel national de classification des médicaments';
COMMENT ON COLUMN familles.actif IS 'Seul mécanisme de retrait — pas de suppression physique';

-- Unicité du code insensible à la casse.
CREATE UNIQUE INDEX uq_familles_code ON familles (LOWER(code));

CREATE INDEX idx_familles_actif ON familles (actif);
