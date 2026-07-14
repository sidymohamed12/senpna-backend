-- ═══════════════════════════════════════════════════════════════════
-- V032 — Appels d'offres et offres fournisseurs
-- SEN PharmaFlow — Ministère de la Santé du Sénégal
--
-- Deux agrégats distincts (cf. doc. métier §b, module appeloffre) :
--   - appels_offres / appel_offre_lignes  : besoin exprimé par la PNA ;
--   - offres_fournisseurs / offre_lignes  : réponse d'un fournisseur.
-- Aucune FK entre les deux tables « lignes » et leur parent : par
-- convention du projet (cf. lots.medicament_id), les références
-- inter/intra-agrégat restent de simples colonnes UUID, la cohérence
-- étant garantie par l'agrégat applicatif, pas par le SGBD — cela évite
-- un couplage au schéma qui gênerait l'évolution indépendante des deux
-- agrégats.
-- ═══════════════════════════════════════════════════════════════════

CREATE TABLE appels_offres (
    id           UUID          NOT NULL DEFAULT uuid_generate_v4(),
    reference    VARCHAR(50)   NOT NULL,
    objet        VARCHAR(255)  NOT NULL,
    date_cloture DATE          NOT NULL,
    statut       VARCHAR(20)   NOT NULL DEFAULT 'BROUILLON',
    created_at   TIMESTAMP     NOT NULL DEFAULT now(),
    updated_at   TIMESTAMP     NOT NULL DEFAULT now(),

    CONSTRAINT pk_appels_offres PRIMARY KEY (id)
);

COMMENT ON TABLE appels_offres IS 'Appels d''offres lancés par la PNA (cf. doc. métier §b)';

CREATE UNIQUE INDEX uq_appels_offres_reference ON appels_offres (LOWER(reference));
CREATE INDEX idx_appels_offres_statut ON appels_offres (statut);
CREATE INDEX idx_appels_offres_date_cloture ON appels_offres (date_cloture);

CREATE TABLE appel_offre_lignes (
    id                UUID          NOT NULL DEFAULT uuid_generate_v4(),
    appel_offre_id    UUID          NOT NULL,
    medicament_id     UUID          NOT NULL,
    designation       VARCHAR(255)  NOT NULL,
    quantite_estimee  NUMERIC(14,2) NOT NULL,
    unite_base        VARCHAR(30)   NOT NULL,
    created_at        TIMESTAMP     NOT NULL DEFAULT now(),
    updated_at        TIMESTAMP     NOT NULL DEFAULT now(),

    CONSTRAINT pk_appel_offre_lignes PRIMARY KEY (id)
);

CREATE INDEX idx_appel_offre_lignes_appel_offre_id ON appel_offre_lignes (appel_offre_id);

CREATE TABLE offres_fournisseurs (
    id             UUID          NOT NULL DEFAULT uuid_generate_v4(),
    appel_offre_id UUID          NOT NULL,
    fournisseur_id UUID          NOT NULL,
    commentaire    VARCHAR(1000),
    statut         VARCHAR(20)   NOT NULL DEFAULT 'SOUMISE',
    created_at     TIMESTAMP     NOT NULL DEFAULT now(),
    updated_at     TIMESTAMP     NOT NULL DEFAULT now(),

    CONSTRAINT pk_offres_fournisseurs PRIMARY KEY (id)
);

COMMENT ON TABLE offres_fournisseurs IS 'Offres soumises par les fournisseurs en réponse à un appel d''offres';

CREATE INDEX idx_offres_fournisseurs_appel_offre_id ON offres_fournisseurs (appel_offre_id);
CREATE INDEX idx_offres_fournisseurs_fournisseur_id ON offres_fournisseurs (fournisseur_id);

-- Un fournisseur ne peut avoir qu'une offre SOUMISE active par AO — les
-- offres RETIREE/RETENUE/REJETEE, statuts terminaux, ne sont pas
-- concernées par cette contrainte (index partiel).
CREATE UNIQUE INDEX uq_offres_fournisseurs_soumise
    ON offres_fournisseurs (appel_offre_id, fournisseur_id)
    WHERE statut = 'SOUMISE';

CREATE TABLE offre_lignes (
    id                    UUID          NOT NULL DEFAULT uuid_generate_v4(),
    offre_id              UUID          NOT NULL,
    ligne_appel_offre_id  UUID          NOT NULL,
    prix_unitaire         NUMERIC(14,2) NOT NULL,
    delai_livraison_jours INTEGER       NOT NULL,
    created_at            TIMESTAMP     NOT NULL DEFAULT now(),
    updated_at            TIMESTAMP     NOT NULL DEFAULT now(),

    CONSTRAINT pk_offre_lignes PRIMARY KEY (id)
);

CREATE INDEX idx_offre_lignes_offre_id ON offre_lignes (offre_id);
