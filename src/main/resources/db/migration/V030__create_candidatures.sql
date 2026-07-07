-- ═══════════════════════════════════════════════════════════════════
-- V030 — Candidatures (formulaire de postulation)
-- SEN PharmaFlow — Ministère de la Santé du Sénégal
--
-- Une candidature est liée à une opportunité de carrière par
-- opportunite_id, avec contrainte FK ON DELETE CASCADE : contrairement à
-- l'auteur d'une actualité/opportunité (relation volontairement non
-- contrainte, cf. V025/V029), une candidature n'a aucun sens sans l'offre
-- qu'elle cible — la supprimer avec l'offre est le comportement correct.
--
-- Aucune modification après soumission (cf. CandidatureMapper /
-- CandidatureRepositoryAdapter — toujours un INSERT) : une candidature
-- est un événement immuable, sa date de soumission est created_at.
-- ═══════════════════════════════════════════════════════════════════

CREATE TABLE candidatures (
    id                      UUID          NOT NULL DEFAULT uuid_generate_v4(),
    opportunite_id          UUID          NOT NULL,
    civilite                VARCHAR(10)   NOT NULL,
    nom_complet             VARCHAR(200)  NOT NULL,
    email                   VARCHAR(255)  NOT NULL,
    telephone               VARCHAR(20)   NOT NULL,
    cv_url                  VARCHAR(1000) NOT NULL,
    lettre_motivation_url   VARCHAR(1000),
    message_complementaire  TEXT,
    consentement_rgpd       BOOLEAN       NOT NULL DEFAULT false,
    created_at              TIMESTAMP     NOT NULL DEFAULT now(),
    updated_at              TIMESTAMP     NOT NULL DEFAULT now(),

    CONSTRAINT pk_candidatures                PRIMARY KEY (id),
    CONSTRAINT fk_candidatures_opportunite     FOREIGN KEY (opportunite_id) REFERENCES opportunites_carriere (id) ON DELETE CASCADE,
    CONSTRAINT ck_candidatures_civilite        CHECK (civilite IN ('M', 'MME')),
    CONSTRAINT ck_candidatures_consentement    CHECK (consentement_rgpd = true)
);

COMMENT ON TABLE  candidatures                        IS 'Candidatures soumises à une opportunité de carrière — formulaire de postulation public';
COMMENT ON COLUMN candidatures.opportunite_id          IS 'Offre ciblée — suppression en cascade avec l''offre';
COMMENT ON COLUMN candidatures.created_at              IS 'Fait office de date de candidature (horodatage automatique) — pas de colonne dédiée';
COMMENT ON COLUMN candidatures.consentement_rgpd       IS 'Case obligatoire — la contrainte CHECK interdit toute ligne sans consentement';

CREATE INDEX idx_candidatures_opportunite_id ON candidatures (opportunite_id);
CREATE INDEX idx_candidatures_email          ON candidatures (email);
CREATE INDEX idx_candidatures_created_at     ON candidatures (created_at);
