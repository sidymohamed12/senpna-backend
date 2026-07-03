-- ═══════════════════════════════════════════════════════════════════
-- V016 — Médicaments
-- SEN PharmaFlow — Ministère de la Santé du Sénégal
--
-- Catalogue national des médicaments (cf. doc. métier §6). Référence
-- familles (V014) et formes (V015) par identifiant — sans FK physique,
-- au même titre que fournisseurs / lots (relation gérée par la couche
-- applicative). Aucune suppression physique — seul le champ actif
-- permet le retrait, afin de préserver la traçabilité des lots,
-- commandes et mouvements de stock déjà rattachés (modules futurs).
-- ═══════════════════════════════════════════════════════════════════

CREATE TABLE medicaments (
    id                              UUID          NOT NULL DEFAULT uuid_generate_v4(),
    code                            VARCHAR(30)   NOT NULL,
    nom_commercial                  VARCHAR(200)  NOT NULL,
    dci                             VARCHAR(200)  NOT NULL,
    dosage                          VARCHAR(50)   NOT NULL,
    forme_id                        UUID          NOT NULL,
    famille_id                      UUID          NOT NULL,
    voie_administration             VARCHAR(20),
    temperature_conservation        VARCHAR(30)   NOT NULL DEFAULT 'AMBIANTE',
    programme_sante                 VARCHAR(150),
    delai_approvisionnement_jours   INTEGER,
    necessite_ordonnance            BOOLEAN       NOT NULL DEFAULT false,
    fabricant                       VARCHAR(150),
    stock_minimum                   INTEGER,
    stock_maximum                   INTEGER,
    actif                           BOOLEAN       NOT NULL DEFAULT true,
    created_at                      TIMESTAMP     NOT NULL DEFAULT now(),
    updated_at                      TIMESTAMP     NOT NULL DEFAULT now(),

    CONSTRAINT pk_medicaments                     PRIMARY KEY (id),
    CONSTRAINT chk_medicaments_delai              CHECK (delai_approvisionnement_jours IS NULL OR delai_approvisionnement_jours >= 0),
    CONSTRAINT chk_medicaments_stock_minimum       CHECK (stock_minimum IS NULL OR stock_minimum >= 0),
    CONSTRAINT chk_medicaments_stock_maximum       CHECK (stock_maximum IS NULL OR stock_maximum >= 0),
    CONSTRAINT chk_medicaments_stock_min_max       CHECK (stock_minimum IS NULL OR stock_maximum IS NULL OR stock_minimum <= stock_maximum)
);

COMMENT ON TABLE  medicaments               IS 'Catalogue national des médicaments';
COMMENT ON COLUMN medicaments.dci           IS 'Dénomination Commune Internationale';
COMMENT ON COLUMN medicaments.forme_id      IS 'Référence logique vers formes.id (pas de FK physique)';
COMMENT ON COLUMN medicaments.famille_id    IS 'Référence logique vers familles.id (pas de FK physique)';
COMMENT ON COLUMN medicaments.actif         IS 'Seul mécanisme de retrait — pas de suppression physique';

-- Unicité du code insensible à la casse.
CREATE UNIQUE INDEX uq_medicaments_code ON medicaments (LOWER(code));

CREATE INDEX idx_medicaments_forme_id   ON medicaments (forme_id);
CREATE INDEX idx_medicaments_famille_id ON medicaments (famille_id);
CREATE INDEX idx_medicaments_actif      ON medicaments (actif);
CREATE INDEX idx_medicaments_dci        ON medicaments (LOWER(dci));
