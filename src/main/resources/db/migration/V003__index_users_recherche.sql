-- ═══════════════════════════════════════════════════════════════════
-- V003 — Index de performance pour l'administration des utilisateurs
-- SEN PharmaFlow — Ministère de la Santé du Sénégal
--
-- Supporte la recherche paginée de GET /api/users (filtre texte sur
-- nom/prénom/email, filtre par statut actif, filtre par rôle).
-- ═══════════════════════════════════════════════════════════════════

-- Filtre par statut actif — utilisé sur quasi tous les listings admin.
CREATE INDEX idx_users_actif ON users(actif);

-- Recherche texte insensible à la casse (nom, prénom, email).
-- Index fonctionnels sur LOWER(...) : accélèrent les prédicats
-- LIKE générés par UserSpecifications.recherche().
CREATE INDEX idx_users_nom_lower    ON users (LOWER(nom));
CREATE INDEX idx_users_prenom_lower ON users (LOWER(prenom));
CREATE INDEX idx_users_email_lower  ON users (LOWER(email));

-- Tri par défaut du listing (created_at DESC).
CREATE INDEX idx_users_created_at ON users(created_at DESC);
