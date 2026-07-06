-- ═══════════════════════════════════════════════════════════════════
-- V025 — Actualités (vie associative, projets, partenariats...)
-- SEN PharmaFlow — Ministère de la Santé du Sénégal
--
-- Une actualité est une entité autonome de communication, indépendante
-- des modules métier (stock, médicament, organisation...). Elle ne
-- référence l'auteur que par simple auteur_id (sans FK), à l'image de
-- fournisseurs.contact_principal / lots — voir V012 — afin de ne pas lier
-- le cycle de vie d'une actualité à celui d'un compte utilisateur
-- (suppression, changement de service...). Le nom de l'auteur est figé
-- (snapshot) à la création : auteur_nom.
--
-- Aucune suppression physique — seul le champ statut (BROUILLON, PUBLIE,
-- DESACTIVE) permet le retrait de la diffusion, pour préserver la
-- traçabilité éditoriale (même convention que fournisseurs.actif).
-- ═══════════════════════════════════════════════════════════════════

CREATE TABLE actualites (
    id           UUID          NOT NULL DEFAULT uuid_generate_v4(),
    categorie    VARCHAR(30)   NOT NULL,
    titre        VARCHAR(200)  NOT NULL,
    description  VARCHAR(500),
    auteur_id    UUID          NOT NULL,
    auteur_nom   VARCHAR(200)  NOT NULL,
    statut       VARCHAR(20)   NOT NULL DEFAULT 'BROUILLON',
    created_at   TIMESTAMP     NOT NULL DEFAULT now(),
    updated_at   TIMESTAMP     NOT NULL DEFAULT now(),

    CONSTRAINT pk_actualites            PRIMARY KEY (id),
    CONSTRAINT ck_actualites_categorie  CHECK (categorie IN ('VIE_ASSOCIATIVE', 'PROJET', 'PARTENARIAT', 'EVENEMENT', 'COMMUNIQUE', 'AUTRE')),
    CONSTRAINT ck_actualites_statut     CHECK (statut IN ('BROUILLON', 'PUBLIE', 'DESACTIVE'))
);

COMMENT ON TABLE  actualites            IS 'Actualités publiées par la PNA — vie associative, projets, partenariats';
COMMENT ON COLUMN actualites.auteur_id  IS 'Référence non contrainte (FK) vers users.id — snapshot conservé via auteur_nom';
COMMENT ON COLUMN actualites.statut     IS 'Seul mécanisme de retrait de diffusion — pas de suppression physique';

CREATE INDEX idx_actualites_categorie  ON actualites (categorie);
CREATE INDEX idx_actualites_statut     ON actualites (statut);
CREATE INDEX idx_actualites_auteur_id  ON actualites (auteur_id);
CREATE INDEX idx_actualites_created_at ON actualites (created_at);

-- ── Médias (images / vidéos) — plusieurs par actualité ────────────────
CREATE TABLE actualite_medias (
    id           UUID          NOT NULL DEFAULT uuid_generate_v4(),
    actualite_id UUID          NOT NULL,
    type         VARCHAR(20)   NOT NULL,
    url          VARCHAR(1000) NOT NULL,
    ordre        INTEGER       NOT NULL DEFAULT 0,
    created_at   TIMESTAMP     NOT NULL DEFAULT now(),
    updated_at   TIMESTAMP     NOT NULL DEFAULT now(),

    CONSTRAINT pk_actualite_medias        PRIMARY KEY (id),
    CONSTRAINT fk_actualite_medias_actualite FOREIGN KEY (actualite_id) REFERENCES actualites (id) ON DELETE CASCADE,
    CONSTRAINT ck_actualite_medias_type   CHECK (type IN ('IMAGE', 'VIDEO'))
);

COMMENT ON TABLE actualite_medias IS 'Images et vidéos (uploadées ou hébergées en externe type YouTube/Vimeo) rattachées à une actualité';

CREATE INDEX idx_actualite_medias_actualite_id ON actualite_medias (actualite_id);

-- ── Tags / mots-clés (SEO, recherche) ─────────────────────────────────
CREATE TABLE actualite_tags (
    actualite_id UUID         NOT NULL,
    tag          VARCHAR(50)  NOT NULL,

    CONSTRAINT fk_actualite_tags_actualite FOREIGN KEY (actualite_id) REFERENCES actualites (id) ON DELETE CASCADE
);

COMMENT ON TABLE actualite_tags IS 'Mots-clés associés à une actualité — utilisés pour le SEO et la recherche';

CREATE INDEX idx_actualite_tags_actualite_id ON actualite_tags (actualite_id);
CREATE INDEX idx_actualite_tags_tag          ON actualite_tags (tag);
