-- ═══════════════════════════════════════════════════════════════════
-- V033 — Commandes d'achat fournisseur et factures
-- SEN PharmaFlow — Ministère de la Santé du Sénégal
--
-- Périmètre volontairement restreint aux commandes ACHAT_FOURNISSEUR
-- (cf. doc. métier flows CAS 1) — les autres types de commandes
-- (transferts PNA→PRA, inter-PRA, distributions vers structures
-- sanitaires) relèvent d'un futur module `commande` générique, non de
-- l'espace fournisseur.
-- ═══════════════════════════════════════════════════════════════════

CREATE TABLE commandes_achat (
    id                              UUID          NOT NULL DEFAULT uuid_generate_v4(),
    reference                       VARCHAR(50)   NOT NULL,
    fournisseur_id                  UUID          NOT NULL,
    entrepot_destination_id         UUID          NOT NULL,
    statut                          VARCHAR(30)   NOT NULL DEFAULT 'EN_ATTENTE_VALIDATION',
    date_accuse_reception_fournisseur TIMESTAMP,
    delai_livraison_confirme_jours  INTEGER,
    date_livraison_confirmee        DATE,
    avis_date_expedition            DATE,
    avis_transporteur               VARCHAR(150),
    avis_numero_suivi               VARCHAR(100),
    avis_date_livraison_estimee     DATE,
    motif_rejet                     VARCHAR(500),
    commentaire                     VARCHAR(1000),
    created_at                      TIMESTAMP     NOT NULL DEFAULT now(),
    updated_at                      TIMESTAMP     NOT NULL DEFAULT now(),

    CONSTRAINT pk_commandes_achat PRIMARY KEY (id)
);

COMMENT ON TABLE commandes_achat IS
    'Bons de commande d''achat fournisseur (type ACHAT_FOURNISSEUR — cf. doc. métier flows CAS 1)';

CREATE UNIQUE INDEX uq_commandes_achat_reference ON commandes_achat (LOWER(reference));
CREATE INDEX idx_commandes_achat_fournisseur_id ON commandes_achat (fournisseur_id);
CREATE INDEX idx_commandes_achat_statut ON commandes_achat (statut);

CREATE TABLE commande_achat_lignes (
    id                 UUID          NOT NULL DEFAULT uuid_generate_v4(),
    commande_achat_id  UUID          NOT NULL,
    medicament_id      UUID          NOT NULL,
    conditionnement_id UUID          NOT NULL,
    quantite_commandee NUMERIC(14,2) NOT NULL,
    prix_unitaire      NUMERIC(14,2) NOT NULL,
    numero_lot         VARCHAR(50),
    date_fabrication   DATE,
    date_expiration    DATE,
    certificat_analyse_url VARCHAR(1000),
    quantite_expediee  NUMERIC(14,2),
    quantite_recue     NUMERIC(14,2) NOT NULL DEFAULT 0,
    quantite_refusee   NUMERIC(14,2) NOT NULL DEFAULT 0,
    motif_refus        VARCHAR(500),
    created_at         TIMESTAMP     NOT NULL DEFAULT now(),
    updated_at         TIMESTAMP     NOT NULL DEFAULT now(),

    CONSTRAINT pk_commande_achat_lignes PRIMARY KEY (id)
);

CREATE INDEX idx_commande_achat_lignes_commande_id ON commande_achat_lignes (commande_achat_id);

CREATE TABLE factures (
    id                    UUID          NOT NULL DEFAULT uuid_generate_v4(),
    commande_achat_id     UUID          NOT NULL,
    fournisseur_id        UUID          NOT NULL,
    numero_facture        VARCHAR(50)   NOT NULL,
    montant               NUMERIC(14,2) NOT NULL,
    date_emission         DATE          NOT NULL,
    date_echeance         DATE,
    piece_jointe_media_id VARCHAR(100),
    statut                VARCHAR(20)   NOT NULL DEFAULT 'SOUMISE',
    motif_rejet           VARCHAR(500),
    created_at            TIMESTAMP     NOT NULL DEFAULT now(),
    updated_at            TIMESTAMP     NOT NULL DEFAULT now(),

    CONSTRAINT pk_factures PRIMARY KEY (id)
);

COMMENT ON TABLE factures IS 'Factures soumises par les fournisseurs pour une commande d''achat livrée';

CREATE INDEX idx_factures_commande_achat_id ON factures (commande_achat_id);
CREATE INDEX idx_factures_fournisseur_id ON factures (fournisseur_id);
CREATE INDEX idx_factures_statut ON factures (statut);
