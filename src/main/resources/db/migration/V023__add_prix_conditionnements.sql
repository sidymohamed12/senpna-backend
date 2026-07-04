-- ═══════════════════════════════════════════════════════════════════
-- V023 — Prix par conditionnement
-- SEN PharmaFlow — Ministère de la Santé du Sénégal
--
-- On ne vend pas au comprimé mais à la boîte, au carton... Le prix se
-- négocie donc par conditionnement (cf. modèle métier complémentaire §5)
-- et non plus (uniquement) au niveau du lot. Un conditionnement sans prix
-- n'est simplement pas proposé à la commande — cf. catalogue, qui ne
-- liste que les conditionnements ayant un prix défini.
-- ═══════════════════════════════════════════════════════════════════

ALTER TABLE conditionnements
    ADD COLUMN prix_achat NUMERIC(14,2),
    ADD COLUMN prix_vente NUMERIC(14,2);

ALTER TABLE conditionnements
    ADD CONSTRAINT chk_conditionnements_prix_achat CHECK (prix_achat IS NULL OR prix_achat >= 0),
    ADD CONSTRAINT chk_conditionnements_prix_vente CHECK (prix_vente IS NULL OR prix_vente >= 0),
    ADD CONSTRAINT chk_conditionnements_prix_ensemble
        CHECK ((prix_achat IS NULL) = (prix_vente IS NULL)),
    ADD CONSTRAINT chk_conditionnements_prix_vente_gte_achat
        CHECK (prix_achat IS NULL OR prix_vente >= prix_achat);

COMMENT ON COLUMN conditionnements.prix_achat IS 'Prix d''achat pour ce conditionnement — NULL si ce conditionnement n''est pas commercialisé';
COMMENT ON COLUMN conditionnements.prix_vente IS 'Prix de vente pour ce conditionnement — NULL si ce conditionnement n''est pas commercialisé';

-- Utilisé par le catalogue pour ne lister que les conditionnements achetables.
CREATE INDEX idx_conditionnements_prix_vente ON conditionnements (medicament_id)
    WHERE prix_vente IS NOT NULL AND actif = true;
