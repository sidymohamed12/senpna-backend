# Tests HTTP — SEN PharmaFlow

Fichiers `.http`, un par contrôleur REST, pour l'extension **VS Code
"REST Client"** (Huachao Mao — `humao.rest-client`). Si tu ne l'as pas
encore : Marketplace → cherche "REST Client" → Install.

## Fichiers

| Fichier                         | Contrôleur                       | Endpoints                                                                                                                                     |
| ------------------------------- | -------------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------- |
| `01-auth.http`                  | `AuthController`                 | login, forgot-password, verify, reset-password, resend-otp, refresh, me                                                                       |
| `02-regions.http`               | `RegionsController`              | créer / lister / obtenir une région                                                                                                           |
| `03-pras.http`                  | `PrasController`                 | créer / modifier / (dés)activer / lister / obtenir une PRA                                                                                    |
| `04-entrepots.http`             | `EntrepotsController`            | consultation (PNA + PRA confondues)                                                                                                           |
| `05-structures-sanitaires.http` | `StructuresSanitairesController` | créer / modifier / cycle d'adhésion / rattachements / lister                                                                                  |
| `06-users.http`                 | `UsersController`                | créer / modifier / (dés)activer / gérer les rôles                                                                                             |
| `07-affectations.http`          | `AffectationsController`         | affecter/retirer un utilisateur à un entrepôt ou une structure                                                                                |
| `08-fournisseurs.http`          | `FournisseursController`         | créer / modifier / (dés)activer / lister / obtenir un fournisseur                                                                             |
| `09-familles.http`              | `FamillesController`             | créer / modifier / (dés)archiver / lister / obtenir une famille                                                                               |
| `10-formes.http`                | `FormesController`               | créer / modifier / (dés)archiver / lister / obtenir une forme                                                                                 |
| `11-medicaments.http`           | `MedicamentsController`          | créer / modifier / (dés)archiver / lister / obtenir un médicament                                                                             |
| `12-conditionnements.http`      | `ConditionnementsController`     | créer / modifier / (dés)archiver / lister / obtenir un conditionnement                                                                        |
| `13-lots.http`                  | `LotsController`                 | cf. fichier (hors périmètre de ce tableau initial)                                                                                            |
| `14-stocks.http`                | `StocksController`               | cf. fichier (hors périmètre de ce tableau initial)                                                                                            |
| `15-mouvements-stock.http`      | `MouvementsStockController`      | cf. fichier (hors périmètre de ce tableau initial)                                                                                            |
| `16-catalogue-national.http`    | `CatalogueNationalController`    | catalogue PNA (stock agrégé de la PNA centrale) — visible PNA + PRA                                                                           |
| `17-catalogue-inter-pra.http`   | `CatalogueInterPraController`    | disponibilités de toutes les PRA, ventilées par PRA — visible PNA + PRA                                                                       |
| `18-catalogue-regional.http`    | `CatalogueRegionalController`    | catalogue régional (stock de la PRA d'une région) — visible par les structures sanitaires de cette région                                     |
| `19-actualites.http`            | `ActualitesController`           | créer / modifier / cycle éditorial (brouillon → publié → désactivé) / lister / obtenir une actualité, y compris l'accès public `/public`      |
| `20-projets.http`               | `ProjetsController`              | créer / modifier / cycle éditorial / lister / obtenir un projet, y compris l'accès public `/public`                                           |
| `21-opportunites-carriere.http` | `OpportunitesCarriereController` | créer / modifier / cycle éditorial (brouillon → ouvert → en cours → clôturé) / lister / obtenir une offre, y compris l'accès public `/public` |
| `22-candidatures.http`          | `CandidaturesController`         | soumettre une candidature (endpoint public) / lister / obtenir une candidature (back-office)                                                  |
| `23-appels-offres.http`         | `AppelOffresController` + `EspaceFournisseurAppelOffresController` + `EspaceFournisseurOffresController` | cycle complet PNA (création → publication → clôture → attribution) et espace fournisseur (consultation, soumission d'offre, retrait) |
| `24-commandes-achat.http`       | `CommandesAchatController` + `EspaceFournisseurCommandesAchatController` | cycle complet PNA (création → validation/rejet → réception totale/partielle) et espace fournisseur (accusé de réception, confirmation de délai, avis d'expédition, factures) |

**Chaque fichier est autonome** : il contient ses propres requêtes de
login en haut (section `0a`, `0b`...) et réutilise leurs tokens pour le
reste du fichier. Tu peux ouvrir n'importe lequel indépendamment des
autres — pas besoin de les exécuter dans un ordre précis, ni de fichier
d'environnement séparé à configurer.

## Prérequis

1. L'application tourne en profil `dev` (`SPRING_PROFILES_ACTIVE=dev`)
   sur `http://localhost:8080`.
2. La migration `V011__insert_mock_data.sql` (`db/migration-dev`) a bien
   été appliquée — les comptes de test utilisés ici en dépendent
   (mot de passe unique : `Password123!`).
3. Pour `08-fournisseurs.http` : la migration
   `V013__insert_mock_fournisseurs.sql` (`db/migration-dev`) doit aussi
   être appliquée — elle peuple la table `fournisseurs` avec les 5
   fournisseurs de démonstration référencés dans ce fichier.
4. Pour `09-familles.http` à `12-conditionnements.http` : la migration
   `V018__insert_mock_medicament_referentiel.sql` (`db/migration-dev`)
   doit aussi être appliquée — elle peuple familles, formes,
   médicaments (Paracétamol, Amoxicilline, Ceftriaxone, etc.) et leurs
   conditionnements de démonstration référencés dans ces fichiers.
5. Pour `16-catalogue-national.http` à `18-catalogue-regional.http` :
   les migrations `V022__insert_mock_stock_data.sql` (lots/stocks) et
   `V024__insert_mock_conditionnements_prix.sql` (prix de démonstration
   des conditionnements — cf. `db/migration-dev`) doivent aussi être
   appliquées. Sans `V024`, les conditionnements listés dans les lignes
   de catalogue seraient vides (aucun prix défini).
6. Pour `19-actualites.http` : la migration
   `V026__insert_mock_actualites.sql` (`db/migration-dev`) doit aussi
   être appliquée — elle peuple `actualites` avec les 7 actualités de
   démonstration référencées dans ce fichier.
7. Pour `20-projets.http` : la migration
   `V028__insert_mock_projets.sql` (`db/migration-dev`) doit aussi être
   appliquée — elle peuple `projets` avec les 7 projets de démonstration
   référencés dans ce fichier.
8. Pour `21-opportunites-carriere.http` et `22-candidatures.http` : la
   migration `V031__insert_mock_opportunites_carriere.sql`
   (`db/migration-dev`) doit aussi être appliquée — elle peuple
   `opportunites_carriere` (7 offres, tous statuts et types de contrat)
   et `candidatures` (7 candidatures) référencées dans ces deux
   fichiers.
9. Pour `23-appels-offres.http` et `24-commandes-achat.http` : aucune
   migration mock dédiée n'existe pour ces deux modules — les deux
   fichiers créent eux-mêmes, à chaque exécution, les comptes espace
   fournisseur nécessaires (rôle `FOURNISSEUR`, cf. `V031`) via
   `POST /api/users`, avec un email unique (`{{$timestamp}}`) pour
   rester rejouables. Seuls les référentiels médicament (`V018`) et
   fournisseur (`V013`) sont réutilisés.

## Comment exécuter une requête

Dans un fichier `.http` ouvert dans VS Code, un lien **"Send Request"**
apparaît au-dessus de chaque requête (ou `Ctrl+Alt+R` / `Cmd+Alt+R` le
curseur placé dedans). La réponse s'ouvre dans un panneau à côté.

Exécute d'abord la requête de login en haut du fichier (`# @name login`
ou `# @name loginAdminPna`, etc.), **puis** les requêtes suivantes qui
en dépendent — l'extension retient les réponses précédentes tant que le
fichier reste ouvert dans l'éditeur, via la syntaxe
`{{nomDeRequete.response.body.$.chemin.json}}`.

## Convention de chaque fichier

Chaque requête est numérotée et commentée : d'abord les cas nominaux,
puis les cas limites (champs optionnels, pagination, filtres), puis les
cas d'erreur attendus (400 validation, 401 sans token, 403 rôle
insuffisant, 404 introuvable, 409/422 règles métier).

## Cas particulier : OTP (mot de passe oublié)

En profil `dev`, l'OTP est **réellement envoyé par email**
(`SmtpEmailOtpSenderAdapter`), pas loggé en console. Après avoir lancé
la requête _"forgot-password"_ dans `01-auth.http`, va chercher le code
reçu et colle-le manuellement dans la requête _"verify"_ avant de
l'exécuter.

## Notes

- Aucun `context-path` n'est configuré : les URLs sont bien
  `http://localhost:8080/api/...`.
- Header d'authentification : `Authorization: Bearer <token>`.
- Le corps de toutes les réponses suit le format
  `{ status, type, message, timestamp, results, pagination? }` — voir
  `RestResponse.java`. D'où le chemin `results.xxx` (et non `data.xxx`)
  dans les variables chaînées.
- `{{$timestamp}}` est une variable dynamique intégrée à l'extension
  (timestamp Unix courant) — utilisée ici pour générer des codes/emails
  uniques à chaque exécution et éviter les doublons.
- **Rôles d'écriture du module `medicament`** (`09-familles.http` à
  `12-conditionnements.http`) : contrairement à `08-fournisseurs.http`
  (écriture réservée à `ADMIN_PNA`/`GESTIONNAIRE_PNA`), le catalogue
  médicament autorise aussi `PHARMACIEN_PNA` en écriture (validation
  pharmaceutique du référentiel). La lecture, elle, est ouverte à tous
  les rôles PNA et PRA ainsi qu'à `GESTIONNAIRE_STRUCTURE` — il n'existe
  donc pas de cas 403 sur les endpoints `GET` de ce module, seulement du
  401 sans authentification.
- **Feature catalogue** (`16-catalogue-national.http` à
  `18-catalogue-regional.http`) : ce n'est pas une table, c'est une vue
  calculée à la volée sur les lignes de stock déjà existantes. Trois
  règles de visibilité distinctes, chacune avec son propre contrôleur :
  catalogue national et inter-PRA réservés aux acteurs PNA/PRA (jamais
  une structure sanitaire, 403 sinon) ; catalogue régional réservé aux
  structures sanitaires de la région concernée (le paramètre `regionId`
  n'est utilisable que par un acteur PNA — pour tout autre acteur il est
  silencieusement ignoré et remplacé par sa propre région).
- **Feature carrière** (`21-opportunites-carriere.http` et
  `22-candidatures.http`) : `POST /api/candidatures` est le seul
  endpoint d'écriture public de tout le projet (aucune authentification,
  candidat externe) — les pièces jointes (CV, lettre de motivation,
  fiche de poste) ne transitent jamais par l'API : elles sont uploadées
  en direct vers le stockage via le flux Presigned URL de `MediaController`
  (`POST /api/medias/presigned-url` pour la fiche de poste, variante
  publique `POST /api/medias/presigned-url/public` restreinte à `CV` et
  `LETTRE_DE_MOTIVATION` pour le candidat) ; ces deux fichiers `.http`
  ne testent donc que les endpoints `opportunites`/`candidatures`
  eux-mêmes, avec des URLs déjà uploadées en dur dans les requêtes.
  Une offre est clôturée automatiquement dès que sa date limite de
  candidature est dépassée, même si son statut persisté en base reste
  `OUVERT` jusqu'au prochain passage du job planifié — d'où les cas de
  test dédiés sur l'offre 6 (cf. `V031`) dans les deux fichiers.
  `21-opportunites-carriere.http` fait volontairement transiter les
  offres 2, 3 et 5 par plusieurs statuts au fil de ses requêtes
  numérotées ; `22-candidatures.http` n'utilise que les offres 1, 4, 6
  et 7, jamais mutées par l'autre fichier, pour rester valide quel que
  soit l'ordre d'exécution des deux fichiers.
