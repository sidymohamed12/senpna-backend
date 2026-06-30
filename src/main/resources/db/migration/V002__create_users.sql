-- ═══════════════════════════════════════════════════════════════════
-- V002 — Utilisateurs et association multi-rôles
-- SEN PharmaFlow — Ministère de la Santé du Sénégal
--
-- Un utilisateur peut posséder PLUSIEURS rôles simultanément
-- (cf. table de jointure user_roles). Aucun endpoint de création de
-- compte n'existe pour le moment (pas de /register) : le provisionnement
-- se fait par script d'administration / migration de données.
-- ═══════════════════════════════════════════════════════════════════

-- ── Table users ───────────────────────────────────────────────────
CREATE TABLE users (
    id                          UUID         NOT NULL DEFAULT uuid_generate_v4(),
    nom                         VARCHAR(100) NOT NULL,
    prenom                      VARCHAR(100) NOT NULL,
    email                       VARCHAR(180) NOT NULL,
    telephone                   VARCHAR(20),
    password_hash               VARCHAR(100) NOT NULL,
    actif                       BOOLEAN      NOT NULL DEFAULT true,
    tentatives_echec_connexion  INTEGER      NOT NULL DEFAULT 0,
    verrouille_jusqua           TIMESTAMP,
    created_at                  TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at                  TIMESTAMP    NOT NULL DEFAULT now(),

    CONSTRAINT pk_users        PRIMARY KEY (id),
    CONSTRAINT uq_users_email  UNIQUE (email)
);

COMMENT ON TABLE  users               IS 'Utilisateurs de la plateforme — provisionnés par administration, pas d''auto-inscription';
COMMENT ON COLUMN users.nom           IS 'Nom de famille';
COMMENT ON COLUMN users.prenom        IS 'Prénom';
COMMENT ON COLUMN users.email         IS 'Identifiant de connexion';
COMMENT ON COLUMN users.telephone     IS 'Requis uniquement si l''utilisateur souhaite recevoir ses OTP par SMS';
COMMENT ON COLUMN users.password_hash IS 'Hash BCrypt (strength=12) — jamais le mot de passe en clair';

-- ── Table user_roles — association multi-rôles ──────────────────────
CREATE TABLE user_roles (
    user_id UUID NOT NULL,
    role_id UUID NOT NULL,

    CONSTRAINT pk_user_roles        PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_roles_user   FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_roles_role   FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE RESTRICT
);

COMMENT ON TABLE user_roles IS 'Association N-N — un utilisateur peut posséder plusieurs rôles simultanément';

CREATE INDEX idx_user_roles_user_id ON user_roles(user_id);
CREATE INDEX idx_user_roles_role_id ON user_roles(role_id);
