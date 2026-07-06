-- ═══════════════════════════════════════════════════════════════════
-- V027 — Projets (portés ou soutenus par la PNA)
-- SEN PharmaFlow — Ministère de la Santé du Sénégal
--
-- Aucune suppression physique — seul le champ statut (BROUILLON, PUBLIE,
-- ARCHIVE, DESACTIVE) permet le retrait de la diffusion, pour préserver
-- la traçabilité éditoriale (même convention que fournisseurs.actif /
-- actualites.statut — cf. V012 et V025).
-- ═══════════════════════════════════════════════════════════════════

CREATE TABLE projets (
    id           UUID          NOT NULL DEFAULT uuid_generate_v4(),
    categorie    VARCHAR(30)   NOT NULL,
    nom          VARCHAR(200)  NOT NULL,
    description  TEXT,
    image_url    VARCHAR(1000),
    statut       VARCHAR(20)   NOT NULL DEFAULT 'BROUILLON',
    created_at   TIMESTAMP     NOT NULL DEFAULT now(),
    updated_at   TIMESTAMP     NOT NULL DEFAULT now(),

    CONSTRAINT pk_projets            PRIMARY KEY (id),
    CONSTRAINT ck_projets_categorie  CHECK (categorie IN ('ENVIRONNEMENT', 'SOCIAL', 'INNOVATION', 'EDUCATION', 'SANTE', 'AUTRE')),
    CONSTRAINT ck_projets_statut     CHECK (statut IN ('BROUILLON', 'PUBLIE', 'ARCHIVE', 'DESACTIVE'))
);

COMMENT ON TABLE  projets          IS 'Projets portés ou soutenus par la PNA — communication institutionnelle';
COMMENT ON COLUMN projets.image_url IS 'Image unique optionnelle (upload via MediaType.PROJET, cf. module media)';
COMMENT ON COLUMN projets.statut   IS 'Seul mécanisme de retrait de diffusion — pas de suppression physique';

CREATE INDEX idx_projets_categorie  ON projets (categorie);
CREATE INDEX idx_projets_statut     ON projets (statut);
CREATE INDEX idx_projets_created_at ON projets (created_at);

-- ── Objectifs — liste de valeurs, ordre préservé ──────────────────────
CREATE TABLE projet_objectifs (
    projet_id UUID         NOT NULL,
    ordre     INTEGER      NOT NULL,
    objectif  VARCHAR(400) NOT NULL,

    CONSTRAINT pk_projet_objectifs        PRIMARY KEY (projet_id, ordre),
    CONSTRAINT fk_projet_objectifs_projet FOREIGN KEY (projet_id) REFERENCES projets (id) ON DELETE CASCADE
);

COMMENT ON TABLE projet_objectifs IS 'Objectifs du projet — liste ordonnée de valeurs libres';

-- ── Impacts — liste de valeurs, ordre préservé ────────────────────────
CREATE TABLE projet_impacts (
    projet_id UUID         NOT NULL,
    ordre     INTEGER      NOT NULL,
    impact    VARCHAR(400) NOT NULL,

    CONSTRAINT pk_projet_impacts        PRIMARY KEY (projet_id, ordre),
    CONSTRAINT fk_projet_impacts_projet FOREIGN KEY (projet_id) REFERENCES projets (id) ON DELETE CASCADE
);

COMMENT ON TABLE projet_impacts IS 'Impacts du projet — liste ordonnée de valeurs libres';
