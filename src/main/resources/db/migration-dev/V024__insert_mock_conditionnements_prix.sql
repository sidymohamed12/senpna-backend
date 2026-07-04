-- ═══════════════════════════════════════════════════════════════════
-- V024 (dev) — Prix de démonstration pour les conditionnements
-- SEN PharmaFlow — Ministère de la Santé du Sénégal
--
-- Données de démonstration uniquement (profil dev) : renseigne un prix
-- d'achat/vente sur les conditionnements « vendables » (pas l'unité de
-- base, gardée pour le seul comptage de stock) créés en V018, afin que
-- le catalogue (cf. feature catalogue) ait des conditionnements à
-- afficher dès l'environnement de développement.
-- ═══════════════════════════════════════════════════════════════════

UPDATE conditionnements SET prix_achat = 500,   prix_vente = 650   WHERE id = '63000000-0000-0000-0000-000000000002'; -- Plaquette
UPDATE conditionnements SET prix_achat = 4500,  prix_vente = 6000  WHERE id = '63000000-0000-0000-0000-000000000003'; -- Boîte
UPDATE conditionnements SET prix_achat = 15000, prix_vente = 20000 WHERE id = '63000000-0000-0000-0000-000000000006'; -- Boîte
UPDATE conditionnements SET prix_achat = 25000, prix_vente = 32000 WHERE id = '63000000-0000-0000-0000-000000000008'; -- Carton
