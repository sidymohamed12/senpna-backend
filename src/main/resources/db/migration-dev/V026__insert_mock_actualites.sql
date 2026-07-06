-- ═══════════════════════════════════════════════════════════════════
-- V026 — Données de démonstration : Actualités
-- SEN PharmaFlow — Ministère de la Santé du Sénégal
--
-- ⚠️  Cette migration vit dans db/migration-dev/, PAS dans
--     db/migration/ : elle n'est chargée que par le profil `dev`
--     (cf. application-dev.yml → spring.flyway.locations), jamais par
--     `prod` (application-prod.yml → classpath:db/migration seul).
--
-- Couvre tous les statuts (BROUILLON, PUBLIE, DESACTIVE), toutes les
-- catégories, ainsi que les cas limites : actualité sans médias, sans
-- tags, sans description (champ optionnel), avec plusieurs médias
-- mixtes (image uploadée + vidéo externe + vidéo mp4 uploadée), et une
-- description proche de la limite de 500 caractères.
--
-- auteur_id référence les utilisateurs créés en V011 (aucune FK — cf.
-- commentaire de table dans V025) ; auteur_nom est un simple snapshot
-- cohérent avec ces mêmes utilisateurs.
--
-- Prérequis : V001 (rôles), V011 (utilisateurs) et V025 (tables
-- actualites / actualite_medias / actualite_tags) déjà appliquées.
-- ═══════════════════════════════════════════════════════════════════

-- ───────────────────────────────────────────────────────────────────
-- 1. Actualités
-- ───────────────────────────────────────────────────────────────────
INSERT INTO actualites (id, categorie, titre, description, auteur_id, auteur_nom, statut, created_at, updated_at) VALUES

    -- Vie associative — publiée, avec médias (image + image) et tags
    ('70000000-0000-0000-0000-000000000001', 'VIE_ASSOCIATIVE',
        'Journée de sensibilisation au don de sang à Dakar',
        'La PNA a organisé une journée de collecte et de sensibilisation au don de sang en partenariat avec le Centre National de Transfusion Sanguine.',
        '20000000-0000-0000-0000-000000000001', 'Cheikh Ba', 'PUBLIE',
        now() - interval '30 days', now() - interval '30 days'),

    -- Projet — publiée, avec image + vidéo YouTube (hébergement externe) et tags
    ('70000000-0000-0000-0000-000000000002', 'PROJET',
        'Modernisation de la chaîne du froid dans les PRA',
        'Déploiement de nouveaux réfrigérateurs solaires dans les Pharmacies Régionales d''Approvisionnement de Thiès, Kaolack et Ziguinchor.',
        '20000000-0000-0000-0000-000000000002', 'Mamadou Diallo', 'PUBLIE',
        now() - interval '21 days', now() - interval '18 days'),

    -- Partenariat — brouillon, sans média, sans tag (cas minimal : seuls les champs obligatoires)
    ('70000000-0000-0000-0000-000000000003', 'PARTENARIAT',
        'Nouveau partenariat avec une ONG internationale',
        NULL,
        '20000000-0000-0000-0000-000000000005', 'Abdoulaye Ndoye', 'BROUILLON',
        now() - interval '2 days', now() - interval '2 days'),

    -- Événement — publiée, avec vidéo mp4 uploadée + image et tags
    ('70000000-0000-0000-0000-000000000004', 'EVENEMENT',
        'Séminaire national sur la gestion des stocks pharmaceutiques',
        'Deux jours d''échanges entre gestionnaires PNA et PRA sur les bonnes pratiques de gestion FEFO et la prévention des ruptures de stock.',
        '20000000-0000-0000-0000-000000000003', 'Aïssatou Sow', 'PUBLIE',
        now() - interval '10 days', now() - interval '9 days'),

    -- Communiqué — désactivée (masquée, ex: information devenue obsolète)
    ('70000000-0000-0000-0000-000000000005', 'COMMUNIQUE',
        'Fermeture exceptionnelle du dépôt central le 15 mars',
        'En raison de travaux de maintenance électrique, le dépôt central de la PNA sera fermé au public le 15 mars.',
        '20000000-0000-0000-0000-000000000001', 'Cheikh Ba', 'DESACTIVE',
        now() - interval '90 days', now() - interval '60 days'),

    -- Autre — brouillon, description proche de la limite de 500 caractères
    ('70000000-0000-0000-0000-000000000006', 'AUTRE',
        'Bilan à mi-parcours du plan stratégique 2024-2026',
        'Le plan stratégique 2024-2026 de la Pharmacie Nationale d''Approvisionnement fixait quatre priorités majeures : la digitalisation complète de la chaîne logistique, le renforcement des capacités de stockage régionales, l''amélioration continue du taux de disponibilité des médicaments essentiels dans l''ensemble des structures sanitaires du pays, et enfin la formation systématique des gestionnaires de pharmacie à tous les échelons du système de santé sénégalais, de la PNA jusqu''aux postes de santé les plus reculés du territoire national.',
        '20000000-0000-0000-0000-000000000006', 'Bineta Faye', 'BROUILLON',
        now() - interval '1 days', now() - interval '1 days'),

    -- Vie associative — publiée, sans description (champ optionnel), avec tags mais sans média
    ('70000000-0000-0000-0000-000000000007', 'VIE_ASSOCIATIVE',
        'La PNA célèbre ses agents partis à la retraite',
        NULL,
        '20000000-0000-0000-0000-000000000002', 'Mamadou Diallo', 'PUBLIE',
        now() - interval '5 days', now() - interval '5 days');

-- ───────────────────────────────────────────────────────────────────
-- 2. Médias — images (upload), vidéo externe (YouTube) et vidéo mp4 (upload)
-- ───────────────────────────────────────────────────────────────────
INSERT INTO actualite_medias (id, actualite_id, type, url, ordre) VALUES

    -- Actualité 1 — deux images uploadées
    ('71000000-0000-0000-0000-000000000001', '70000000-0000-0000-0000-000000000001',
        'IMAGE', 'https://cdn.senpna.sn/actualites/don-de-sang-1.jpg', 0),
    ('71000000-0000-0000-0000-000000000002', '70000000-0000-0000-0000-000000000001',
        'IMAGE', 'https://cdn.senpna.sn/actualites/don-de-sang-2.jpg', 1),

    -- Actualité 2 — image uploadée + vidéo hébergée en externe (YouTube)
    ('71000000-0000-0000-0000-000000000003', '70000000-0000-0000-0000-000000000002',
        'IMAGE', 'https://cdn.senpna.sn/actualites/chaine-du-froid.jpg', 0),
    ('71000000-0000-0000-0000-000000000004', '70000000-0000-0000-0000-000000000002',
        'VIDEO', 'https://www.youtube.com/watch?v=dQw4w9WgXcQ', 1),

    -- Actualité 4 — image uploadée + vidéo mp4 uploadée
    ('71000000-0000-0000-0000-000000000005', '70000000-0000-0000-0000-000000000004',
        'IMAGE', 'https://cdn.senpna.sn/actualites/seminaire-gestion-stocks.jpg', 0),
    ('71000000-0000-0000-0000-000000000006', '70000000-0000-0000-0000-000000000004',
        'VIDEO', 'https://cdn.senpna.sn/actualites/seminaire-gestion-stocks.mp4', 1),

    -- Actualité 5 — une seule image (actualité désactivée)
    ('71000000-0000-0000-0000-000000000007', '70000000-0000-0000-0000-000000000005',
        'IMAGE', 'https://cdn.senpna.sn/actualites/depot-central.jpg', 0);

-- Actualités 3, 6 et 7 volontairement sans média (cas « champ optionnel non renseigné »)

-- ───────────────────────────────────────────────────────────────────
-- 3. Tags / mots-clés (SEO, recherche)
-- ───────────────────────────────────────────────────────────────────
INSERT INTO actualite_tags (actualite_id, tag) VALUES
    ('70000000-0000-0000-0000-000000000001', 'don-de-sang'),
    ('70000000-0000-0000-0000-000000000001', 'sante-publique'),
    ('70000000-0000-0000-0000-000000000001', 'dakar'),

    ('70000000-0000-0000-0000-000000000002', 'chaine-du-froid'),
    ('70000000-0000-0000-0000-000000000002', 'pra'),
    ('70000000-0000-0000-0000-000000000002', 'energie-solaire'),
    ('70000000-0000-0000-0000-000000000002', 'logistique'),

    ('70000000-0000-0000-0000-000000000004', 'seminaire'),
    ('70000000-0000-0000-0000-000000000004', 'fefo'),
    ('70000000-0000-0000-0000-000000000004', 'formation'),

    ('70000000-0000-0000-0000-000000000007', 'vie-associative'),
    ('70000000-0000-0000-0000-000000000007', 'retraite'),
    ('70000000-0000-0000-0000-000000000007', 'ressources-humaines');

-- Actualités 3, 5 et 6 volontairement sans tag (cas « champ optionnel non renseigné »)

-- ───────────────────────────────────────────────────────────────────
-- Vérification : 7 actualités attendues
-- ───────────────────────────────────────────────────────────────────
DO $$
BEGIN
    IF (SELECT COUNT(*) FROM actualites WHERE id::text LIKE '70000000%') != 7 THEN
        RAISE EXCEPTION 'Migration V026 : nombre d''actualités de démonstration incorrect, attendu 7';
    END IF;
END $$;
