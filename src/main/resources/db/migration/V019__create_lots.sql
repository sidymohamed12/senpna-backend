-- ═══════════════════════════════════════════════════════════════════
-- V019 — Lots de médicaments
-- SEN PharmaFlow — Ministère de la Santé du Sénégal
--
-- Traçabilité complète des lots (cf. doc. métier §8 et modèle métier
-- complémentaire §2 « Lots »). Référence medicaments (V016) et
-- fournisseurs (V012) par identifiant — sans FK physique, au même titre
-- que les autres références inter-modules déjà en place (medicaments →
-- familles/formes, conditionnements → medicaments). Aucune suppression
-- physique — seul le statut permet le retrait (BLOQUE / EXPIRE).
-- ═══════════════════════════════════════════════════════════════════

CREATE TABLE lots (
    id                UUID          NOT NULL DEFAULT uuid_generate_v4(),
    numero_lot        VARCHAR(50)   NOT NULL,
    medicament_id     UUID          NOT NULL,
    fournisseur_id    UUID          NOT NULL,
    date_fabrication  DATE,
    date_expiration   DATE          NOT NULL,
    prix_achat        NUMERIC(14,2),
    prix_vente        NUMERIC(14,2),
    statut            VARCHAR(20)   NOT NULL DEFAULT 'ACTIF',
    created_at        TIMESTAMP     NOT NULL DEFAULT now(),
    updated_at        TIMESTAMP     NOT NULL DEFAULT now(),

    CONSTRAINT pk_lots                  PRIMARY KEY (id),
    CONSTRAINT chk_lots_statut          CHECK (statut IN ('ACTIF', 'BLOQUE', 'EXPIRE')),
    CONSTRAINT chk_lots_prix_achat      CHECK (prix_achat IS NULL OR prix_achat >= 0),
    CONSTRAINT chk_lots_prix_vente      CHECK (prix_vente IS NULL OR prix_vente >= 0),
    CONSTRAINT chk_lots_fabrication     CHECK (date_fabrication IS NULL OR date_fabrication <= date_expiration)
);

COMMENT ON TABLE  lots                   IS 'Lots de médicaments — traçabilité complète (fabrication, péremption, prix, statut)';
COMMENT ON COLUMN lots.medicament_id     IS 'Référence logique vers medicaments.id (pas de FK physique)';
COMMENT ON COLUMN lots.fournisseur_id    IS 'Référence logique vers fournisseurs.id (pas de FK physique)';
COMMENT ON COLUMN lots.statut            IS 'ACTIF (réservable/expédiable) | BLOQUE (quarantaine, ex. pharmacovigilance) | EXPIRE (terminal)';

-- Unicité du numéro de lot pour un médicament donné.
CREATE UNIQUE INDEX uq_lots_medicament_numero ON lots (medicament_id, LOWER(numero_lot));

CREATE INDEX idx_lots_medicament_id    ON lots (medicament_id);
CREATE INDEX idx_lots_fournisseur_id   ON lots (fournisseur_id);
CREATE INDEX idx_lots_statut           ON lots (statut);
CREATE INDEX idx_lots_date_expiration  ON lots (date_expiration);

-- Index composite pour la sélection FEFO (lots ACTIF triés par expiration
-- croissante pour un médicament donné) — utilisé par la réservation
-- automatique (cf. ReserverStockFefoUseCase).
CREATE INDEX idx_lots_fefo ON lots (medicament_id, statut, date_expiration);
