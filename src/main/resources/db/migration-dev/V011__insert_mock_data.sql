-- ═══════════════════════════════════════════════════════════════════
-- V011 — Données de démonstration (mock data)
-- SEN PharmaFlow — Ministère de la Santé du Sénégal
--
-- ⚠️  Cette migration vit dans db/migration-dev/, PAS dans
--     db/migration/ : elle n'est chargée que par le profil `dev`
--     (cf. application-dev.yml → spring.flyway.locations), jamais par
--     `prod` (application-prod.yml → classpath:db/migration seul).
--
-- Peuple la base avec un jeu de données couvrant tous les rôles, tous
-- les types d'entrepôt/structure sanitaire et tous les statuts
-- d'adhésion, ainsi que les cas limites (compte désactivé, verrouillé,
-- multi-rôles, sans rôle...).
--
-- Mot de passe en clair pour TOUS les utilisateurs ci-dessous :
--   Password123!
-- (hash bcrypt strength=12 généré et vérifié avec BCryptPasswordEncoder)
--
-- Prérequis : V001 (rôles) et V009 (14 régions) déjà appliquées.
-- ═══════════════════════════════════════════════════════════════════

-- ───────────────────────────────────────────────────────────────────
-- 1. Région supplémentaire — cas "région désactivée"
-- ───────────────────────────────────────────────────────────────────
INSERT INTO regions (id, code, nom, actif) VALUES
    ('19000000-0000-0000-0000-000000000001', 'TEST_INACTIVE', 'Région de test (désactivée)', false);

-- ───────────────────────────────────────────────────────────────────
-- 2. Entrepôts — PNA centrale + PRA, tous les cas
--    (responsable_user_id renseigné en section 6, après création des
--    utilisateurs)
-- ───────────────────────────────────────────────────────────────────
INSERT INTO entrepots (id, code, nom, type, region_id, adresse, telephone, actif) VALUES
    -- PNA centrale (unique, pas de région)
    ('30000000-0000-0000-0000-000000000001', 'PNA-CENTRAL', 'Pharmacie Nationale d''Approvisionnement',
        'PNA_CENTRAL', NULL, 'Route de l''Aéroport, Dakar', '+221338601234', true),
    -- PRA active, avec adresse/téléphone complets
    ('30000000-0000-0000-0000-000000000002', 'PRA-THIES', 'PRA de Thiès',
        'PRA', '10000000-0000-0000-0000-000000000013', 'Route de Fandène, Thiès', '+221338211234', true),
    -- PRA active, sans responsable
    ('30000000-0000-0000-0000-000000000003', 'PRA-DAKAR', 'PRA de Dakar',
        'PRA', '10000000-0000-0000-0000-000000000001', 'Avenue Cheikh Anta Diop, Dakar', '+221338601111', true),
    -- PRA désactivée
    ('30000000-0000-0000-0000-000000000004', 'PRA-KAOLACK', 'PRA de Kaolack',
        'PRA', '10000000-0000-0000-0000-000000000005', 'Route de Ndoffane, Kaolack', '+221339411234', false),
    -- PRA sans adresse ni téléphone (champs optionnels non renseignés)
    ('30000000-0000-0000-0000-000000000005', 'PRA-ZIGUINCHOR', 'PRA de Ziguinchor',
        'PRA', '10000000-0000-0000-0000-000000000014', NULL, NULL, true);

-- ───────────────────────────────────────────────────────────────────
-- 3. Structures sanitaires — tous les types × tous les statuts
--    d'adhésion
-- ───────────────────────────────────────────────────────────────────
INSERT INTO structures_sanitaires
    (id, code, nom, type, region_id, pra_id, district, adresse, telephone, email,
     responsable_nom, responsable_prenom, statut_adhesion, motif_rejet, actif) VALUES
    -- Hôpital, adhésion validée, rattaché à une région ET une PRA
    ('40000000-0000-0000-0000-000000000001', 'HOP-THIES-01', 'Hôpital Régional de Thiès',
        'HOPITAL', '10000000-0000-0000-0000-000000000013', '30000000-0000-0000-0000-000000000002',
        'Thiès', 'Avenue Lamine Guèye, Thiès', '+221338211111', 'contact@hopital-thies.sn',
        'Ndiaye', 'Awa', 'VALIDEE', NULL, true),
    -- District sanitaire, demande en attente de validation (pas encore de PRA)
    ('40000000-0000-0000-0000-000000000002', 'DS-DAKAR-01', 'District Sanitaire de Dakar-Nord',
        'DISTRICT_SANITAIRE', '10000000-0000-0000-0000-000000000001', NULL,
        'Dakar', 'Rue 12, Grand Dakar', '+221338250000', 'contact@ds-dakarnord.sn',
        'Fall', 'Moussa', 'EN_ATTENTE_VALIDATION', NULL, false),
    -- Centre de santé, validé mais PRA pas encore affectée (cas limite)
    ('40000000-0000-0000-0000-000000000003', 'CS-DAKAR-02', 'Centre de Santé de Grand-Yoff',
        'CENTRE_SANTE', '10000000-0000-0000-0000-000000000001', NULL,
        'Dakar', 'Grand-Yoff, Dakar', '+221338270000', 'contact@cs-grandyoff.sn',
        'Sarr', 'Fatou', 'VALIDEE', NULL, true),
    -- Poste de santé, adhésion rejetée (motif renseigné)
    ('40000000-0000-0000-0000-000000000004', 'PS-KAOLACK-01', 'Poste de Santé de Ndoffane',
        'POSTE_SANTE', '10000000-0000-0000-0000-000000000005', NULL,
        'Kaolack', 'Ndoffane, Kaolack', '+221339410000', 'contact@ps-ndoffane.sn',
        'Diouf', 'Ibrahima', 'REJETEE', 'Dossier incomplet — pièces justificatives manquantes', false),
    -- ONG, validée puis désactivée ultérieurement (structure fermée)
    ('40000000-0000-0000-0000-000000000005', 'ONG-THIES-01', 'ONG Santé pour Tous',
        'ONG', '10000000-0000-0000-0000-000000000013', '30000000-0000-0000-0000-000000000002',
        'Thiès', 'Quartier Randoulène, Thiès', '+221338219999', 'contact@santepourtous.sn',
        'Cissé', 'Aminata', 'VALIDEE', NULL, false);

-- ───────────────────────────────────────────────────────────────────
-- 4. Utilisateurs — tous les rôles × tous les états
--    (entrepot_id / structure_sanitaire_id jamais renseignés
--    simultanément — cf. chk_users_affectation_exclusive)
-- ───────────────────────────────────────────────────────────────────
INSERT INTO users
    (id, nom, prenom, email, telephone, password_hash, actif,
     tentatives_echec_connexion, verrouille_jusqua, entrepot_id, structure_sanitaire_id) VALUES

    -- ADMIN_PNA — actif, rattaché à la PNA centrale, sans téléphone
    ('20000000-0000-0000-0000-000000000001', 'Ba', 'Cheikh', 'cheikh.ba@sante.gouv.sn', NULL,
        '$2b$12$2.eJbGcDbl.RksTcI0mSMOPkBonuAfccBOtlm8IrywZMHk41ahMG6', true,
        0, NULL, '30000000-0000-0000-0000-000000000001', NULL),

    -- GESTIONNAIRE_PNA — actif, rattaché à la PNA centrale
    ('20000000-0000-0000-0000-000000000002', 'Diallo', 'Mamadou', 'mamadou.diallo@sante.gouv.sn', NULL,
        '$2b$12$2.eJbGcDbl.RksTcI0mSMOPkBonuAfccBOtlm8IrywZMHk41ahMG6', true,
        0, NULL, '30000000-0000-0000-0000-000000000001', NULL),

    -- PHARMACIEN_PNA — actif, avec téléphone (canal OTP SMS)
    ('20000000-0000-0000-0000-000000000003', 'Sow', 'Aïssatou', 'aissatou.sow@sante.gouv.sn', '+221771234567',
        '$2b$12$2.eJbGcDbl.RksTcI0mSMOPkBonuAfccBOtlm8IrywZMHk41ahMG6', true,
        0, NULL, '30000000-0000-0000-0000-000000000001', NULL),

    -- MAGASINIER_PNA — actif, rattaché à la PNA centrale
    ('20000000-0000-0000-0000-000000000004', 'Kane', 'Ousmane', 'ousmane.kane@sante.gouv.sn', NULL,
        '$2b$12$2.eJbGcDbl.RksTcI0mSMOPkBonuAfccBOtlm8IrywZMHk41ahMG6', true,
        0, NULL, '30000000-0000-0000-0000-000000000001', NULL),

    -- ADMIN_PRA — actif, rattaché à la PRA de Thiès (devient son responsable)
    ('20000000-0000-0000-0000-000000000005', 'Ndoye', 'Abdoulaye', 'abdoulaye.ndoye@sante.gouv.sn', NULL,
        '$2b$12$2.eJbGcDbl.RksTcI0mSMOPkBonuAfccBOtlm8IrywZMHk41ahMG6', true,
        0, NULL, '30000000-0000-0000-0000-000000000002', NULL),

    -- GESTIONNAIRE_PRA — actif, rattaché à la PRA de Dakar
    ('20000000-0000-0000-0000-000000000006', 'Faye', 'Bineta', 'bineta.faye@sante.gouv.sn', NULL,
        '$2b$12$2.eJbGcDbl.RksTcI0mSMOPkBonuAfccBOtlm8IrywZMHk41ahMG6', true,
        0, NULL, '30000000-0000-0000-0000-000000000003', NULL),

    -- PHARMACIEN_PRA — actif, avec téléphone, rattaché à la PRA de Thiès
    ('20000000-0000-0000-0000-000000000007', 'Gueye', 'Modou', 'modou.gueye@sante.gouv.sn', '+221776543210',
        '$2b$12$2.eJbGcDbl.RksTcI0mSMOPkBonuAfccBOtlm8IrywZMHk41ahMG6', true,
        0, NULL, '30000000-0000-0000-0000-000000000002', NULL),

    -- MAGASINIER_PRA — actif, rattaché à la PRA de Dakar
    ('20000000-0000-0000-0000-000000000008', 'Diop', 'Khady', 'khady.diop@sante.gouv.sn', NULL,
        '$2b$12$2.eJbGcDbl.RksTcI0mSMOPkBonuAfccBOtlm8IrywZMHk41ahMG6', true,
        0, NULL, '30000000-0000-0000-0000-000000000003', NULL),

    -- GESTIONNAIRE_STRUCTURE — actif, rattaché à une structure sanitaire
    -- (jamais un entrepot_id en même temps qu'un structure_sanitaire_id)
    ('20000000-0000-0000-0000-000000000009', 'Ndiaye', 'Awa', 'awa.ndiaye@hopital-thies.sn', NULL,
        '$2b$12$2.eJbGcDbl.RksTcI0mSMOPkBonuAfccBOtlm8IrywZMHk41ahMG6', true,
        0, NULL, NULL, '40000000-0000-0000-0000-000000000001'),

    -- Compte DÉSACTIVÉ — cas "utilisateur inactif"
    ('20000000-0000-0000-0000-000000000010', 'Sy', 'Ibrahima', 'ibrahima.sy@sante.gouv.sn', NULL,
        '$2b$12$2.eJbGcDbl.RksTcI0mSMOPkBonuAfccBOtlm8IrywZMHk41ahMG6', false,
        0, NULL, NULL, NULL),

    -- Compte VERROUILLÉ — cas "trop de tentatives échouées" (verrouillé 15 min)
    ('20000000-0000-0000-0000-000000000011', 'Thiam', 'Ndeye', 'ndeye.thiam@sante.gouv.sn', NULL,
        '$2b$12$2.eJbGcDbl.RksTcI0mSMOPkBonuAfccBOtlm8IrywZMHk41ahMG6', true,
        5, now() + interval '15 minutes', NULL, NULL),

    -- MULTI-RÔLES — cumule ADMIN_PRA et PHARMACIEN_PRA (cf. user_roles ci-dessous)
    ('20000000-0000-0000-0000-000000000012', 'Camara', 'Seydou', 'seydou.camara@sante.gouv.sn', NULL,
        '$2b$12$2.eJbGcDbl.RksTcI0mSMOPkBonuAfccBOtlm8IrywZMHk41ahMG6', true,
        0, NULL, '30000000-0000-0000-0000-000000000003', NULL),

    -- SANS RÔLE — cas limite (compte provisionné, rôle pas encore assigné)
    ('20000000-0000-0000-0000-000000000013', 'Sarr', 'Fatou', 'fatou.sarr@sante.gouv.sn', NULL,
        '$2b$12$2.eJbGcDbl.RksTcI0mSMOPkBonuAfccBOtlm8IrywZMHk41ahMG6', true,
        0, NULL, NULL, NULL),

    -- Échecs de connexion en cours, PAS ENCORE verrouillé (2 échecs < seuil)
    ('20000000-0000-0000-0000-000000000014', 'Diagne', 'Alioune', 'alioune.diagne@sante.gouv.sn', NULL,
        '$2b$12$2.eJbGcDbl.RksTcI0mSMOPkBonuAfccBOtlm8IrywZMHk41ahMG6', true,
        2, NULL, NULL, NULL);

-- ───────────────────────────────────────────────────────────────────
-- 5. Association utilisateurs ↔ rôles (les IDs de rôle viennent de V001)
-- ───────────────────────────────────────────────────────────────────
INSERT INTO user_roles (user_id, role_id) VALUES
    ('20000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000001'), -- ADMIN_PNA
    ('20000000-0000-0000-0000-000000000002', '00000000-0000-0000-0000-000000000002'), -- GESTIONNAIRE_PNA
    ('20000000-0000-0000-0000-000000000003', '00000000-0000-0000-0000-000000000003'), -- PHARMACIEN_PNA
    ('20000000-0000-0000-0000-000000000004', '00000000-0000-0000-0000-000000000004'), -- MAGASINIER_PNA
    ('20000000-0000-0000-0000-000000000005', '00000000-0000-0000-0000-000000000005'), -- ADMIN_PRA
    ('20000000-0000-0000-0000-000000000006', '00000000-0000-0000-0000-000000000006'), -- GESTIONNAIRE_PRA
    ('20000000-0000-0000-0000-000000000007', '00000000-0000-0000-0000-000000000007'), -- PHARMACIEN_PRA
    ('20000000-0000-0000-0000-000000000008', '00000000-0000-0000-0000-000000000008'), -- MAGASINIER_PRA
    ('20000000-0000-0000-0000-000000000009', '00000000-0000-0000-0000-000000000009'), -- GESTIONNAIRE_STRUCTURE
    ('20000000-0000-0000-0000-000000000010', '00000000-0000-0000-0000-000000000002'), -- (inactif) GESTIONNAIRE_PNA
    ('20000000-0000-0000-0000-000000000011', '00000000-0000-0000-0000-000000000006'), -- (verrouillé) GESTIONNAIRE_PRA
    ('20000000-0000-0000-0000-000000000012', '00000000-0000-0000-0000-000000000005'), -- (multi) ADMIN_PRA
    ('20000000-0000-0000-0000-000000000012', '00000000-0000-0000-0000-000000000007'), -- (multi) PHARMACIEN_PRA
    ('20000000-0000-0000-0000-000000000014', '00000000-0000-0000-0000-000000000008'); -- MAGASINIER_PRA
    -- Utilisateur 013 (sans rôle) : volontairement aucune ligne ici.

-- ───────────────────────────────────────────────────────────────────
-- 6. Rattache les responsables d'entrepôt maintenant que les
--    utilisateurs existent (entrepots.responsable_user_id n'a pas de
--    contrainte FK, mais on respecte l'ordre logique de création).
-- ───────────────────────────────────────────────────────────────────
UPDATE entrepots SET responsable_user_id = '20000000-0000-0000-0000-000000000001'
    WHERE id = '30000000-0000-0000-0000-000000000001'; -- PNA centrale ← Cheikh Ba (ADMIN_PNA)

UPDATE entrepots SET responsable_user_id = '20000000-0000-0000-0000-000000000005'
    WHERE id = '30000000-0000-0000-0000-000000000002'; -- PRA Thiès ← Abdoulaye Ndoye (ADMIN_PRA)

-- ───────────────────────────────────────────────────────────────────
-- 7. Vérifications de cohérence
-- ───────────────────────────────────────────────────────────────────
DO $$
BEGIN
    IF (SELECT COUNT(*) FROM users WHERE id::text LIKE '20000000%') != 14 THEN
        RAISE EXCEPTION 'Migration V011 : nombre d''utilisateurs mock incorrect, attendu 14';
    END IF;
    IF (SELECT COUNT(*) FROM entrepots WHERE id::text LIKE '30000000%') != 5 THEN
        RAISE EXCEPTION 'Migration V011 : nombre d''entrepôts mock incorrect, attendu 5';
    END IF;
    IF (SELECT COUNT(*) FROM structures_sanitaires WHERE id::text LIKE '40000000%') != 5 THEN
        RAISE EXCEPTION 'Migration V011 : nombre de structures sanitaires mock incorrect, attendu 5';
    END IF;
END $$;

-- ═══════════════════════════════════════════════════════════════════
-- Récapitulatif des comptes de test (mot de passe unique : Password123!)
-- ═══════════════════════════════════════════════════════════════════
-- cheikh.ba@sante.gouv.sn         → ADMIN_PNA (actif)
-- mamadou.diallo@sante.gouv.sn    → GESTIONNAIRE_PNA (actif)
-- aissatou.sow@sante.gouv.sn      → PHARMACIEN_PNA (actif, tel. SMS)
-- ousmane.kane@sante.gouv.sn      → MAGASINIER_PNA (actif)
-- abdoulaye.ndoye@sante.gouv.sn   → ADMIN_PRA (actif, resp. PRA Thiès)
-- bineta.faye@sante.gouv.sn       → GESTIONNAIRE_PRA (actif)
-- modou.gueye@sante.gouv.sn       → PHARMACIEN_PRA (actif, tel. SMS)
-- khady.diop@sante.gouv.sn        → MAGASINIER_PRA (actif)
-- awa.ndiaye@hopital-thies.sn     → GESTIONNAIRE_STRUCTURE (actif)
-- ibrahima.sy@sante.gouv.sn       → GESTIONNAIRE_PNA (DÉSACTIVÉ)
-- ndeye.thiam@sante.gouv.sn       → GESTIONNAIRE_PRA (VERROUILLÉ 15 min)
-- seydou.camara@sante.gouv.sn     → ADMIN_PRA + PHARMACIEN_PRA (multi-rôles)
-- fatou.sarr@sante.gouv.sn        → SANS RÔLE (cas limite)
-- alioune.diagne@sante.gouv.sn    → MAGASINIER_PRA (2 échecs, pas verrouillé)
-- ═══════════════════════════════════════════════════════════════════
