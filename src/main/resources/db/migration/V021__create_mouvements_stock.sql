-- ═══════════════════════════════════════════════════════════════════
-- V020 — Mouvements de stock (journal append-only)
-- SEN PharmaFlow — Ministère de la Santé du Sénégal
--
-- Historise toute variation de stock (cf. doc. métier §13 et §18).
-- Table strictement append-only : aucun UPDATE ni DELETE applicatif —
-- seule la couche persistence utilise INSERT (cf.
-- MouvementStockRepositoryAdapter.save()). Référence entrepots (V005),
-- lots (V018) et utilisateurs (V002) par identifiant — sans FK physique.
-- commande_id référence une future table `commandes` (module `commande`
-- non encore implémenté) — également sans FK physique, colonne nullable
-- en attendant.
-- ═══════════════════════════════════════════════════════════════════

CREATE TABLE mouvements_stock (
    id                        UUID          NOT NULL DEFAULT uuid_generate_v4(),
    type_mouvement            VARCHAR(30)   NOT NULL,
    sens                      VARCHAR(10)   NOT NULL,
    entrepot_source_id        UUID,
    entrepot_destination_id   UUID,
    commande_id               UUID,
    lot_id                    UUID          NOT NULL,
    medicament_id             UUID          NOT NULL,
    quantite                  NUMERIC(14,4) NOT NULL,
    date_mouvement            TIMESTAMP     NOT NULL DEFAULT now(),
    reference_document        VARCHAR(100),
    motif                     VARCHAR(255),
    utilisateur_id            UUID          NOT NULL,
    created_at                TIMESTAMP     NOT NULL DEFAULT now(),
    updated_at                TIMESTAMP     NOT NULL DEFAULT now(),

    CONSTRAINT pk_mouvements_stock              PRIMARY KEY (id),
    CONSTRAINT chk_mvt_sens                     CHECK (sens IN ('ENTREE', 'SORTIE')),
    CONSTRAINT chk_mvt_type                     CHECK (type_mouvement IN (
                                                     'ENTREE_ACHAT', 'ENTREE_TRANSFERT', 'SORTIE_TRANSFERT',
                                                     'SORTIE_STRUCTURE', 'RETOUR', 'AJUSTEMENT', 'PERTE', 'CASSE',
                                                     'VOL', 'PEREMPTION', 'INVENTAIRE', 'DON')),
    CONSTRAINT chk_mvt_quantite                 CHECK (quantite > 0),
    CONSTRAINT chk_mvt_sortie_source            CHECK (sens <> 'SORTIE' OR entrepot_source_id IS NOT NULL),
    CONSTRAINT chk_mvt_entree_destination       CHECK (sens <> 'ENTREE' OR entrepot_destination_id IS NOT NULL),
    CONSTRAINT chk_mvt_transfert_deux_entrepots CHECK (
        type_mouvement NOT IN ('ENTREE_TRANSFERT', 'SORTIE_TRANSFERT')
        OR (entrepot_source_id IS NOT NULL AND entrepot_destination_id IS NOT NULL)
    )
);

COMMENT ON TABLE  mouvements_stock                     IS 'Journal append-only des mouvements de stock — source de vérité de la traçabilité';
COMMENT ON COLUMN mouvements_stock.sens                IS 'ENTREE (augmente le stock destination) | SORTIE (diminue le stock source)';
COMMENT ON COLUMN mouvements_stock.type_mouvement       IS 'Nature du mouvement — ne détermine pas seul le sens (ex : AJUSTEMENT, RETOUR possibles dans les deux sens)';
COMMENT ON COLUMN mouvements_stock.entrepot_source_id      IS 'Référence logique vers entrepots.id — obligatoire si sens = SORTIE';
COMMENT ON COLUMN mouvements_stock.entrepot_destination_id IS 'Référence logique vers entrepots.id — obligatoire si sens = ENTREE';
COMMENT ON COLUMN mouvements_stock.commande_id          IS 'Référence logique vers une future table commandes.id (module non encore implémenté)';
COMMENT ON COLUMN mouvements_stock.lot_id               IS 'Référence logique vers lots.id (pas de FK physique)';
COMMENT ON COLUMN mouvements_stock.medicament_id        IS 'Dénormalisé depuis lots.medicament_id — permet le filtrage par médicament sans jointure';
COMMENT ON COLUMN mouvements_stock.utilisateur_id       IS 'Référence logique vers users.id — utilisateur responsable du mouvement (traçabilité, cf. doc. métier §18)';

CREATE INDEX idx_mvt_lot_id                ON mouvements_stock (lot_id);
CREATE INDEX idx_mvt_medicament_id         ON mouvements_stock (medicament_id);
CREATE INDEX idx_mvt_entrepot_source_id    ON mouvements_stock (entrepot_source_id);
CREATE INDEX idx_mvt_entrepot_dest_id      ON mouvements_stock (entrepot_destination_id);
CREATE INDEX idx_mvt_type                  ON mouvements_stock (type_mouvement);
CREATE INDEX idx_mvt_utilisateur_id        ON mouvements_stock (utilisateur_id);
CREATE INDEX idx_mvt_date_mouvement        ON mouvements_stock (date_mouvement);
CREATE INDEX idx_mvt_commande_id           ON mouvements_stock (commande_id);
