-- ═══════════════════════════════════════════════════════════════════
-- V011 — Fournisseurs de médicaments
-- SEN PharmaFlow — Ministère de la Santé du Sénégal
--
-- Un fournisseur est une entité autonome : il n'est référencé (par simple
-- fournisseurId, sans FK) que par les futurs modules medicament (lots) et
-- commande (achats fournisseurs). Aucune suppression physique — seul le
-- champ actif permet le retrait, pour préserver la traçabilité des lots
-- et commandes déjà rattachés.
-- ═══════════════════════════════════════════════════════════════════

CREATE TABLE fournisseurs (
    id                   UUID         NOT NULL DEFAULT uuid_generate_v4(),
    nom                  VARCHAR(150) NOT NULL,
    adresse              VARCHAR(255),
    telephone            VARCHAR(20),
    email                VARCHAR(150),
    contact_principal    VARCHAR(150),
    actif                BOOLEAN      NOT NULL DEFAULT true,
    created_at           TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at           TIMESTAMP    NOT NULL DEFAULT now(),

    CONSTRAINT pk_fournisseurs PRIMARY KEY (id)
);

COMMENT ON TABLE  fournisseurs                   IS 'Fournisseurs de médicaments — partenaires commerciaux de la PNA';
COMMENT ON COLUMN fournisseurs.actif             IS 'Seul mécanisme de retrait — pas de suppression physique';
COMMENT ON COLUMN fournisseurs.contact_principal IS 'Nom du contact commercial référent chez le fournisseur';

-- Unicité du nom insensible à la casse — évite les doublons de saisie
-- (« Laboratoire A » vs « laboratoire a »).
CREATE UNIQUE INDEX uq_fournisseurs_nom ON fournisseurs (LOWER(nom));

CREATE INDEX idx_fournisseurs_actif ON fournisseurs (actif);
