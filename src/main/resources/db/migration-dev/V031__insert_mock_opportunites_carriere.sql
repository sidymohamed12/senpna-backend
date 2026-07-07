-- ═══════════════════════════════════════════════════════════════════
-- V031 — Données de démonstration : Opportunités de carrière & Candidatures
-- SEN PharmaFlow — Ministère de la Santé du Sénégal
--
-- ⚠️  Cette migration vit dans db/migration-dev/, PAS dans
--     db/migration/ : elle n'est chargée que par le profil `dev`
--     (cf. application-dev.yml → spring.flyway.locations), jamais par
--     `prod` (application-prod.yml → classpath:db/migration seul).
--
-- Opportunités : couvre tous les statuts (BROUILLON, OUVERT, EN_COURS,
-- CLOTURE manuelle) et tous les types de contrat (CDI, CDD, STAGE,
-- FREELANCE, VOLONTARIAT, AUTRE), ainsi que les cas limites : offre
-- sans fiche de poste / sans date de début / sans e-mail de contact
-- (champs optionnels), fiche de poste au format image plutôt que PDF,
-- et surtout l'offre n°6 dont le statut persisté est encore OUVERT
-- alors que sa date_limite_candidature est déjà dépassée — exactement
-- le cas que couvre OpportuniteCarriere#getStatutEffectif() /
-- CloturerOpportunitesExpireesJob : tant que le job planifié n'est pas
-- encore passé, la ligne reste OUVERT en base mais doit être exposée
-- comme CLOTURE par l'API et exclue des listes publiques.
--
-- Candidatures : rattachées uniquement aux offres ayant été à un moment
-- OUVERT (jamais à l'offre BROUILLON n°3, qui n'a jamais été publique)
-- — une candidature réelle a pu être soumise avant qu'une offre ne
-- passe en EN_COURS ou soit clôturée. Couvre civilité M/MME, lettre de
-- motivation présente ou absente (champ optionnel), message
-- complémentaire présent ou absent, et des CV aux formats PDF/DOC/DOCX.
--
-- auteur_id référence les utilisateurs créés en V011 (aucune FK — même
-- convention que actualites.auteur_id, cf. V025/V029) ; auteur_nom est
-- un simple snapshot cohérent avec ces mêmes utilisateurs.
--
-- Prérequis : V001 (rôles), V011 (utilisateurs), V029 (opportunites_carriere)
-- et V030 (candidatures) déjà appliquées.
-- ═══════════════════════════════════════════════════════════════════

-- ───────────────────────────────────────────────────────────────────
-- 1. Opportunités de carrière
-- ───────────────────────────────────────────────────────────────────
INSERT INTO opportunites_carriere (
    id, titre, nom_entreprise, description, fiche_de_poste_url, lieu, type_contrat,
    date_debut, date_limite_candidature, auteur_id, auteur_nom, email_contact, statut,
    created_at, updated_at
) VALUES

    -- CDI — ouverte, dossier complet (fiche PDF, date de début, e-mail de contact dédié)
    ('90000000-0000-0000-0000-000000000001',
        'Pharmacien(ne) responsable de dépôt régional',
        'Pharmacie Nationale d''Approvisionnement (PNA)',
        'La PNA recrute un(e) pharmacien(ne) responsable pour superviser la gestion pharmaceutique '
        || 'd''un dépôt régional : contrôle qualité, gestion des lots, supervision de l''équipe '
        || 'magasinière et reporting mensuel au siège. Profil recherché : Doctorat en pharmacie, '
        || '3 ans d''expérience minimum en gestion de stock hospitalier ou en centrale d''achat.',
        'https://cdn.senpna.sn/fiches-de-poste/pharmacien-depot-regional.pdf',
        'Dakar, Sénégal', 'CDI',
        CURRENT_DATE + INTERVAL '60 days', CURRENT_DATE + INTERVAL '30 days',
        '20000000-0000-0000-0000-000000000001', 'Cheikh Ba',
        'recrutement.dakar@senpna.sn', 'OUVERT',
        now() - INTERVAL '10 days', now() - INTERVAL '2 days'),

    -- CDD — ouverte, cas minimal (sans fiche de poste, sans date de début, sans e-mail de contact dédié)
    ('90000000-0000-0000-0000-000000000002',
        'Magasinier(ère) — renfort saisonnier',
        'Pharmacie Régionale d''Approvisionnement de Thiès',
        'Renfort temporaire pour la réception, le rangement FEFO et la préparation des commandes '
        || 'destinées aux structures sanitaires du district. Aucune expérience préalable exigée, '
        || 'formation assurée sur site.',
        NULL,
        'Thiès, Sénégal', 'CDD',
        NULL, CURRENT_DATE + INTERVAL '15 days',
        '20000000-0000-0000-0000-000000000002', 'Mamadou Diallo',
        NULL, 'OUVERT',
        now() - INTERVAL '5 days', now() - INTERVAL '5 days'),

    -- STAGE — brouillon, jamais publiée (fiche de poste au format image plutôt que PDF)
    ('90000000-0000-0000-0000-000000000003',
        'Stage — appui à la digitalisation des inventaires',
        'Pharmacie Nationale d''Approvisionnement (PNA)',
        'Stage de fin d''études pour accompagner la bascule des inventaires papier vers l''outil de '
        || 'gestion de stock numérique, en lien avec les équipes logistique et informatique.',
        'https://cdn.senpna.sn/fiches-de-poste/stage-digitalisation-inventaires.jpg',
        'Saint-Louis, Sénégal', 'STAGE',
        CURRENT_DATE + INTERVAL '90 days', CURRENT_DATE + INTERVAL '45 days',
        '20000000-0000-0000-0000-000000000001', 'Cheikh Ba',
        'stages@senpna.sn', 'BROUILLON',
        now() - INTERVAL '1 days', now() - INTERVAL '1 days'),

    -- FREELANCE — en cours de traitement des candidatures, n'accepte plus de nouvelles candidatures
    ('90000000-0000-0000-0000-000000000004',
        'Consultant(e) — audit des processus de distribution',
        'Pharmacie Nationale d''Approvisionnement (PNA)',
        'Mission d''audit ponctuelle des processus de distribution entre le dépôt central et les '
        || 'Pharmacies Régionales d''Approvisionnement, avec recommandations d''amélioration.',
        NULL,
        'Télétravail', 'FREELANCE',
        NULL, CURRENT_DATE + INTERVAL '5 days',
        '20000000-0000-0000-0000-000000000002', 'Mamadou Diallo',
        NULL, 'EN_COURS',
        now() - INTERVAL '20 days', now() - INTERVAL '3 days'),

    -- VOLONTARIAT — clôturée manuellement avant l'échéance (pourvue plus tôt que prévu)
    ('90000000-0000-0000-0000-000000000005',
        'Volontaire — sensibilisation communautaire',
        'Pharmacie Régionale d''Approvisionnement de Ziguinchor',
        'Programme de volontariat pour animer des sessions de sensibilisation sur le bon usage des '
        || 'médicaments essentiels auprès des relais communautaires de la région.',
        'https://cdn.senpna.sn/fiches-de-poste/volontariat-sensibilisation.pdf',
        'Ziguinchor, Sénégal', 'VOLONTARIAT',
        NULL, CURRENT_DATE + INTERVAL '10 days',
        '20000000-0000-0000-0000-000000000006', 'Bineta Faye',
        'volontariat.ziguinchor@senpna.sn', 'CLOTURE',
        now() - INTERVAL '25 days', now() - INTERVAL '4 days'),

    -- AUTRE — statut persisté encore OUVERT mais date limite déjà dépassée : cf. en-tête de fichier,
    -- cas de test central de la clôture automatique (getStatutEffectif() / job planifié)
    ('90000000-0000-0000-0000-000000000006',
        'Chauffeur-livreur — dépôt central',
        'Pharmacie Nationale d''Approvisionnement (PNA)',
        'Livraison des commandes entre le dépôt central de Dakar et les Pharmacies Régionales '
        || 'd''Approvisionnement, dans le respect des règles de transport des produits pharmaceutiques.',
        NULL,
        'Kaolack, Sénégal', 'AUTRE',
        NULL, CURRENT_DATE - INTERVAL '5 days',
        '20000000-0000-0000-0000-000000000001', 'Cheikh Ba',
        NULL, 'OUVERT',
        now() - INTERVAL '40 days', now() - INTERVAL '35 days'),

    -- CDI — ouverte, description longue (proche de la limite de 8000 caractères), dossier complet
    ('90000000-0000-0000-0000-000000000007',
        'Responsable qualité et affaires réglementaires',
        'Pharmacie Nationale d''Approvisionnement (PNA)',
        'Le plan stratégique 2024-2026 de la Pharmacie Nationale d''Approvisionnement fixait quatre '
        || 'priorités majeures : la digitalisation complète de la chaîne logistique, le renforcement '
        || 'des capacités de stockage régionales, l''amélioration continue du taux de disponibilité des '
        || 'médicaments essentiels dans l''ensemble des structures sanitaires du pays, et enfin la '
        || 'formation systématique des gestionnaires de pharmacie à tous les échelons du système de '
        || 'santé sénégalais. Dans ce cadre, la PNA recrute un(e) responsable qualité et affaires '
        || 'réglementaires chargé(e) de piloter la conformité pharmaceutique du dépôt central, '
        || 'd''assurer la veille réglementaire nationale et sous-régionale (UEMOA), de coordonner les '
        || 'inspections et audits qualité, et de représenter la PNA auprès de la Direction de la '
        || 'Pharmacie et du Médicament. Profil recherché : Doctorat en pharmacie ou master en affaires '
        || 'réglementaires, 5 ans d''expérience minimum, excellente maîtrise des référentiels qualité '
        || '(BPD, BPF) et de la réglementation pharmaceutique sénégalaise et sous-régionale.',
        'https://cdn.senpna.sn/fiches-de-poste/responsable-qualite-reglementaire.pdf',
        'Télétravail', 'CDI',
        CURRENT_DATE + INTERVAL '45 days', CURRENT_DATE + INTERVAL '21 days',
        '20000000-0000-0000-0000-000000000002', 'Mamadou Diallo',
        'recrutement.qualite@senpna.sn', 'OUVERT',
        now() - INTERVAL '7 days', now() - INTERVAL '1 days');

-- ───────────────────────────────────────────────────────────────────
-- 2. Candidatures — jamais rattachées à l'offre 3 (BROUILLON, jamais publique)
-- ───────────────────────────────────────────────────────────────────
INSERT INTO candidatures (
    id, opportunite_id, civilite, nom_complet, email, telephone, cv_url,
    lettre_motivation_url, message_complementaire, consentement_rgpd, created_at, updated_at
) VALUES

    -- Offre 1 (CDI pharmacien) — dossier complet, CV + lettre de motivation, avec message
    ('91000000-0000-0000-0000-000000000001',
        '90000000-0000-0000-0000-000000000001', 'MME', 'Fatou Diagne',
        'fatou.diagne@example.sn', '+221771112233',
        'https://cdn.senpna.sn/cvs/fatou-diagne-cv.pdf',
        'https://cdn.senpna.sn/lettres-de-motivation/fatou-diagne-lettre.pdf',
        'Actuellement pharmacienne adjointe dans une officine privée de Dakar, je souhaite mettre '
        || 'mon expérience de gestion d''équipe au service du secteur public.',
        true, now() - INTERVAL '6 days', now() - INTERVAL '6 days'),

    -- Offre 1 — CV seul, sans lettre de motivation (champ optionnel), sans message
    ('91000000-0000-0000-0000-000000000002',
        '90000000-0000-0000-0000-000000000001', 'M', 'Ibrahima Sarr',
        'ibrahima.sarr@gmail.com', '+221772223344',
        'https://cdn.senpna.sn/cvs/ibrahima-sarr-cv.pdf',
        NULL, NULL,
        true, now() - INTERVAL '4 days', now() - INTERVAL '4 days'),

    -- Offre 2 (CDD magasinier) — CV au format DOCX, lettre au format DOC
    ('91000000-0000-0000-0000-000000000003',
        '90000000-0000-0000-0000-000000000002', 'MME', 'Aminata Cissé',
        'aminata.cisse@yahoo.fr', '+221773334455',
        'https://cdn.senpna.sn/cvs/aminata-cisse-cv.docx',
        'https://cdn.senpna.sn/lettres-de-motivation/aminata-cisse-lettre.doc',
        'Disponible immédiatement, je réside à Thiès et je connais bien les zones de livraison de la région.',
        true, now() - INTERVAL '3 days', now() - INTERVAL '3 days'),

    -- Offre 4 (FREELANCE, désormais EN_COURS) — soumise pendant qu'elle était encore OUVERT
    ('91000000-0000-0000-0000-000000000004',
        '90000000-0000-0000-0000-000000000004', 'M', 'Moussa Kane',
        'moussa.kane@consultingsn.com', '+221774445566',
        'https://cdn.senpna.sn/cvs/moussa-kane-cv.pdf',
        NULL, NULL,
        true, now() - INTERVAL '18 days', now() - INTERVAL '18 days'),

    -- Offre 5 (VOLONTARIAT, désormais CLOTURE) — soumise avant la clôture manuelle
    ('91000000-0000-0000-0000-000000000005',
        '90000000-0000-0000-0000-000000000005', 'MME', 'Ndeye Fall',
        'ndeye.fall@example.sn', '+221775556677',
        'https://cdn.senpna.sn/cvs/ndeye-fall-cv.pdf',
        'https://cdn.senpna.sn/lettres-de-motivation/ndeye-fall-lettre.pdf',
        'Bénévole depuis 3 ans auprès des relais communautaires de Ziguinchor.',
        true, now() - INTERVAL '22 days', now() - INTERVAL '22 days'),

    -- Offre 6 (statut persisté OUVERT mais expirée) — soumise avant le dépassement de la date limite
    ('91000000-0000-0000-0000-000000000006',
        '90000000-0000-0000-0000-000000000006', 'M', 'Omar Thiam',
        'omar.thiam@hotmail.com', '+221776667788',
        'https://cdn.senpna.sn/cvs/omar-thiam-cv.pdf',
        NULL, NULL,
        true, now() - INTERVAL '38 days', now() - INTERVAL '38 days'),

    -- Offre 7 (CDI responsable qualité) — dossier complet, second profil sur la même offre
    ('91000000-0000-0000-0000-000000000007',
        '90000000-0000-0000-0000-000000000007', 'MME', 'Coumba Mbaye',
        'coumba.mbaye@pharma-conseil.sn', '+221777778899',
        'https://cdn.senpna.sn/cvs/coumba-mbaye-cv.pdf',
        'https://cdn.senpna.sn/lettres-de-motivation/coumba-mbaye-lettre.pdf',
        'Titulaire d''un master en affaires réglementaires, 6 ans d''expérience en industrie pharmaceutique.',
        true, now() - INTERVAL '2 days', now() - INTERVAL '2 days');

-- ───────────────────────────────────────────────────────────────────
-- Vérification : 7 opportunités et 7 candidatures attendues
-- ───────────────────────────────────────────────────────────────────
DO $$
BEGIN
    IF (SELECT COUNT(*) FROM opportunites_carriere WHERE id::text LIKE '90000000%') != 7 THEN
        RAISE EXCEPTION 'Migration V031 : nombre d''opportunités de carrière de démonstration incorrect, attendu 7';
    END IF;
    IF (SELECT COUNT(*) FROM candidatures WHERE id::text LIKE '91000000%') != 7 THEN
        RAISE EXCEPTION 'Migration V031 : nombre de candidatures de démonstration incorrect, attendu 7';
    END IF;
END $$;
