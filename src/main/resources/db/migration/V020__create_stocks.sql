-- ═══════════════════════════════════════════════════════════════════
-- V019 — Stocks (ligne de stock par couple entrepôt/lot)
-- SEN PharmaFlow — Ministère de la Santé du Sénégal
--
-- Chaque entrepôt (PNA centrale ou PRA, cf. V005) possède son propre
-- stock, ventilé par lot pour permettre la traçabilité et la règle FEFO
-- (cf. doc. métier §9 et modèle métier complémentaire §2 « Stocks »).
-- Référence entrepots (V005) et lots (V018) par identifiant — sans FK
-- physique, conformément à la convention déjà en place dans le projet
-- (aucune relation JPA/SQL directe entre agrégats distincts).
--
-- medicament_id est dénormalisé depuis lots.medicament_id afin de
-- permettre le filtrage direct des stocks par médicament (cf. doc.
-- métier §9 : « Consultation des stocks par médicament ») sans jointure
-- inter-module.
--
-- Aucune modification directe des quantités n'est autorisée par
-- l'application (cf. modèle métier complémentaire §2 « Stocks ») : toute
-- variation transite par Stock.entrer()/sortir()/reserver()/... et génère
-- un mouvement_stock (V020) dans la même transaction.
-- ═══════════════════════════════════════════════════════════════════

CREATE TABLE stocks (
    id                    UUID          NOT NULL DEFAULT uuid_generate_v4(),
    entrepot_id           UUID          NOT NULL,
    lot_id                UUID          NOT NULL,
    medicament_id         UUID          NOT NULL,
    quantite_disponible   NUMERIC(14,4) NOT NULL DEFAULT 0,
    quantite_reservee     NUMERIC(14,4) NOT NULL DEFAULT 0,
    quantite_en_commande  NUMERIC(14,4) NOT NULL DEFAULT 0,
    seuil_alerte          NUMERIC(14,4),
    created_at            TIMESTAMP     NOT NULL DEFAULT now(),
    updated_at            TIMESTAMP     NOT NULL DEFAULT now(),

    CONSTRAINT pk_stocks                        PRIMARY KEY (id),
    CONSTRAINT chk_stocks_quantite_disponible    CHECK (quantite_disponible >= 0),
    CONSTRAINT chk_stocks_quantite_reservee      CHECK (quantite_reservee >= 0),
    CONSTRAINT chk_stocks_quantite_en_commande   CHECK (quantite_en_commande >= 0),
    CONSTRAINT chk_stocks_reservee_sous_dispo    CHECK (quantite_reservee <= quantite_disponible),
    CONSTRAINT chk_stocks_seuil_alerte           CHECK (seuil_alerte IS NULL OR seuil_alerte >= 0)
);

COMMENT ON TABLE  stocks                       IS 'Ligne de stock d''un lot dans un entrepôt donné';
COMMENT ON COLUMN stocks.entrepot_id           IS 'Référence logique vers entrepots.id (pas de FK physique)';
COMMENT ON COLUMN stocks.lot_id                IS 'Référence logique vers lots.id (pas de FK physique)';
COMMENT ON COLUMN stocks.medicament_id         IS 'Dénormalisé depuis lots.medicament_id — permet le filtrage par médicament sans jointure';
COMMENT ON COLUMN stocks.quantite_disponible   IS 'Quantité physiquement présente dans l''entrepôt pour ce lot';
COMMENT ON COLUMN stocks.quantite_reservee     IS 'Portion de la quantité disponible déjà affectée à des commandes validées non expédiées';
COMMENT ON COLUMN stocks.quantite_en_commande  IS 'Quantité en cours de réapprovisionnement (commandes émises, non encore réceptionnées) — informatif';
COMMENT ON COLUMN stocks.seuil_alerte          IS 'Seuil de sécurité propre à cette ligne de stock (à défaut, le seuil global du médicament s''applique côté application)';

-- Unicité de la ligne de stock pour un couple (entrepôt, lot).
CREATE UNIQUE INDEX uq_stocks_entrepot_lot ON stocks (entrepot_id, lot_id);

CREATE INDEX idx_stocks_entrepot_id    ON stocks (entrepot_id);
CREATE INDEX idx_stocks_lot_id         ON stocks (lot_id);
CREATE INDEX idx_stocks_medicament_id  ON stocks (medicament_id);

-- Index dédié à la détection de rupture / seuil atteint (cf. doc. métier
-- §17 « Alertes ») : quantité disponible à la vente = quantite_disponible
-- - quantite_reservee.
CREATE INDEX idx_stocks_dispo_vente ON stocks ((quantite_disponible - quantite_reservee));
