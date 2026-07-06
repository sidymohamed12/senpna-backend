-- ═══════════════════════════════════════════════════════════════════
-- V028 — Données de démonstration : Projets
-- SEN PharmaFlow — Ministère de la Santé du Sénégal
--
-- ⚠️  Cette migration vit dans db/migration-dev/, PAS dans
--     db/migration/ : elle n'est chargée que par le profil `dev`
--     (cf. application-dev.yml → spring.flyway.locations), jamais par
--     `prod` (application-prod.yml → classpath:db/migration seul).
--
-- Couvre toutes les catégories et tous les statuts (BROUILLON, PUBLIE,
-- ARCHIVE, DESACTIVE), ainsi que les cas limites : projet sans image,
-- sans objectifs, sans impacts, sans description (champ optionnel), et
-- une description proche de la limite de 500 caractères.
--
-- Prérequis : V027 (tables projets / projet_objectifs / projet_impacts)
-- déjà appliquée.
-- ═══════════════════════════════════════════════════════════════════

-- ───────────────────────────────────────────────────────────────────
-- 1. Projets
-- ───────────────────────────────────────────────────────────────────
INSERT INTO projets (id, categorie, nom, description, image_url, statut, created_at, updated_at) VALUES

    -- Santé — publié, avec image
    ('80000000-0000-0000-0000-000000000001', 'SANTE',
        'Accès aux soins essentiels en zone rurale',
        'Renforcement de la disponibilité des médicaments essentiels dans les structures sanitaires des zones rurales du Sénégal.',
        'https://cdn.senpna.sn/projets/acces-soins-ruraux.jpg', 'PUBLIE',
        now() - interval '180 days', now() - interval '150 days'),

    -- Environnement — publié, avec image
    ('80000000-0000-0000-0000-000000000002', 'ENVIRONNEMENT',
        'Chaîne du froid solaire',
        'Déploiement de réfrigérateurs solaires dans les entrepôts régionaux pour réduire la dépendance aux groupes électrogènes.',
        'https://cdn.senpna.sn/projets/chaine-froid-solaire.jpg', 'PUBLIE',
        now() - interval '90 days', now() - interval '60 days'),

    -- Social — brouillon, cas minimal (seuls les champs obligatoires)
    ('80000000-0000-0000-0000-000000000003', 'SOCIAL',
        'Programme d''accompagnement des aînés',
        NULL, NULL, 'BROUILLON',
        now() - interval '3 days', now() - interval '3 days'),

    -- Innovation — archivé, avec image, objectifs seuls (sans impact renseigné)
    ('80000000-0000-0000-0000-000000000004', 'INNOVATION',
        'Plateforme de traçabilité des lots par QR code',
        'Projet pilote de traçabilité des lots de médicaments par QR code, mené sur 2023-2024 dans la région de Thiès.',
        'https://cdn.senpna.sn/projets/tracabilite-qr-code.jpg', 'ARCHIVE',
        now() - interval '400 days', now() - interval '200 days'),

    -- Éducation — désactivé, impacts seuls (sans objectif renseigné), sans image
    ('80000000-0000-0000-0000-000000000005', 'EDUCATION',
        'Formation continue des gestionnaires de pharmacie',
        'Cycle de formations à destination des gestionnaires de pharmacie des structures sanitaires publiques.',
        NULL, 'DESACTIVE',
        now() - interval '120 days', now() - interval '45 days'),

    -- Autre — brouillon, description proche de la limite de 500 caractères
    ('80000000-0000-0000-0000-000000000006', 'AUTRE',
        'Bilan à mi-parcours du plan stratégique 2024-2026',
        'Le plan stratégique 2024-2026 de la Pharmacie Nationale d''Approvisionnement fixait quatre priorités majeures : la digitalisation complète de la chaîne logistique, le renforcement des capacités de stockage régionales, l''amélioration continue du taux de disponibilité des médicaments essentiels dans l''ensemble des structures sanitaires du pays, et enfin la formation systématique des gestionnaires de pharmacie à tous les échelons du système de santé sénégalais, jusqu''aux postes de santé les plus reculés du territoire.',
        NULL, 'BROUILLON',
        now() - interval '1 days', now() - interval '1 days'),

    -- Santé — publié, avec image, sans description (champ optionnel)
    ('80000000-0000-0000-0000-000000000007', 'SANTE',
        'Vaccination de routine — campagne 2025',
        NULL,
        'https://cdn.senpna.sn/projets/vaccination-routine-2025.jpg', 'PUBLIE',
        now() - interval '30 days', now() - interval '20 days');

-- ───────────────────────────────────────────────────────────────────
-- 2. Objectifs — liste ordonnée de valeurs
-- ───────────────────────────────────────────────────────────────────
INSERT INTO projet_objectifs (projet_id, ordre, objectif) VALUES
    ('80000000-0000-0000-0000-000000000001', 0, 'Réduire de 30% les ruptures de stock en zone rurale'),
    ('80000000-0000-0000-0000-000000000001', 1, 'Couvrir 100% des districts sanitaires prioritaires'),

    ('80000000-0000-0000-0000-000000000002', 0, 'Équiper 5 entrepôts régionaux en réfrigération solaire'),
    ('80000000-0000-0000-0000-000000000002', 1, 'Réduire de 40% la consommation de carburant'),
    ('80000000-0000-0000-0000-000000000002', 2, 'Sécuriser la chaîne du froid en cas de coupure électrique'),

    ('80000000-0000-0000-0000-000000000004', 0, 'Tester la traçabilité par QR code sur 3 conditionnements'),
    ('80000000-0000-0000-0000-000000000004', 1, 'Évaluer la faisabilité d''un déploiement national'),

    ('80000000-0000-0000-0000-000000000007', 0, 'Vacciner 95% de la cohorte cible avant fin d''année');

-- Projets 3, 5 et 6 volontairement sans objectif (cas « champ optionnel non renseigné »)

-- ───────────────────────────────────────────────────────────────────
-- 3. Impacts — liste ordonnée de valeurs
-- ───────────────────────────────────────────────────────────────────
INSERT INTO projet_impacts (projet_id, ordre, impact) VALUES
    ('80000000-0000-0000-0000-000000000001', 0, '+25% de disponibilité des médicaments essentiels'),
    ('80000000-0000-0000-0000-000000000001', 1, '12 districts sanitaires couverts'),

    ('80000000-0000-0000-0000-000000000002', 0, '-35% de consommation de carburant sur les sites pilotes'),
    ('80000000-0000-0000-0000-000000000002', 1, 'Zéro rupture de chaîne du froid depuis le déploiement'),

    ('80000000-0000-0000-0000-000000000005', 0, '80 gestionnaires formés sur l''ensemble du territoire'),
    ('80000000-0000-0000-0000-000000000005', 1, 'Réduction des erreurs de gestion de stock signalées'),

    ('80000000-0000-0000-0000-000000000007', 0, '92% de couverture vaccinale atteinte');

-- Projets 3, 4 et 6 volontairement sans impact (cas « champ optionnel non renseigné »)

-- ───────────────────────────────────────────────────────────────────
-- Vérification : 7 projets attendus
-- ───────────────────────────────────────────────────────────────────
DO $$
BEGIN
    IF (SELECT COUNT(*) FROM projets WHERE id::text LIKE '80000000%') != 7 THEN
        RAISE EXCEPTION 'Migration V028 : nombre de projets de démonstration incorrect, attendu 7';
    END IF;
END $$;
