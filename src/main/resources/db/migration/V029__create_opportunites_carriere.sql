-- ═══════════════════════════════════════════════════════════════════
-- V029 — Opportunités de carrière (offres d'emploi)
-- SEN PharmaFlow — Ministère de la Santé du Sénégal
--
-- Une opportunité de carrière est une entité autonome de recrutement,
-- indépendante des modules métier. Elle ne référence l'auteur que par
-- simple auteur_id (sans FK), à l'image de actualites.auteur_id — voir
-- V025 — afin de ne pas lier le cycle de vie d'une offre à celui d'un
-- compte utilisateur. Le nom de l'auteur est figé (snapshot) à la
-- création : auteur_nom.
--
-- Cycle de vie éditorial (statut) : BROUILLON → OUVERT → EN_COURS →
-- CLOTURE. La clôture automatique par dépassement de
-- date_limite_candidature est calculée à la lecture (cf.
-- OpportuniteCarriere#getStatutEffectif()) et matérialisée en base par un
-- job planifié quotidien (CloturerOpportunitesExpireesJob) — le champ
-- statut peut donc être en retard d'au plus un cycle par rapport à la
-- réalité métier, jamais l'API.
-- ═══════════════════════════════════════════════════════════════════

CREATE TABLE opportunites_carriere (
    id                       UUID          NOT NULL DEFAULT uuid_generate_v4(),
    titre                    VARCHAR(200)  NOT NULL,
    nom_entreprise           VARCHAR(200)  NOT NULL,
    description              TEXT          NOT NULL,
    fiche_de_poste_url       VARCHAR(1000),
    lieu                     VARCHAR(200)  NOT NULL,
    type_contrat             VARCHAR(30)   NOT NULL,
    date_debut               DATE,
    date_limite_candidature  DATE          NOT NULL,
    auteur_id                UUID          NOT NULL,
    auteur_nom               VARCHAR(200)  NOT NULL,
    email_contact            VARCHAR(255),
    statut                   VARCHAR(20)   NOT NULL DEFAULT 'BROUILLON',
    created_at               TIMESTAMP     NOT NULL DEFAULT now(),
    updated_at               TIMESTAMP     NOT NULL DEFAULT now(),

    CONSTRAINT pk_opportunites_carriere            PRIMARY KEY (id),
    CONSTRAINT ck_opportunites_carriere_type       CHECK (type_contrat IN ('CDI', 'CDD', 'STAGE', 'FREELANCE', 'VOLONTARIAT', 'AUTRE')),
    CONSTRAINT ck_opportunites_carriere_statut     CHECK (statut IN ('BROUILLON', 'OUVERT', 'EN_COURS', 'CLOTURE'))
);

COMMENT ON TABLE  opportunites_carriere                      IS 'Offres d''emploi / opportunités de carrière publiées par la PNA';
COMMENT ON COLUMN opportunites_carriere.auteur_id             IS 'Référence non contrainte (FK) vers users.id — snapshot conservé via auteur_nom';
COMMENT ON COLUMN opportunites_carriere.email_contact         IS 'E-mail de contact RH explicite de l''offre — sinon l''e-mail du compte auteur est utilisé';
COMMENT ON COLUMN opportunites_carriere.date_limite_candidature IS 'Clôture automatique (statut effectif CLOTURE) une fois cette date dépassée';
COMMENT ON COLUMN opportunites_carriere.statut                IS 'Statut persisté — peut être en retard d''au plus un cycle du job planifié de clôture automatique';

CREATE INDEX idx_opportunites_carriere_statut               ON opportunites_carriere (statut);
CREATE INDEX idx_opportunites_carriere_type_contrat          ON opportunites_carriere (type_contrat);
CREATE INDEX idx_opportunites_carriere_auteur_id             ON opportunites_carriere (auteur_id);
CREATE INDEX idx_opportunites_carriere_date_limite           ON opportunites_carriere (date_limite_candidature);
CREATE INDEX idx_opportunites_carriere_created_at            ON opportunites_carriere (created_at);
