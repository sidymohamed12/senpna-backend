# Tests HTTP — SEN PharmaFlow

Fichiers `.http`, un par contrôleur REST, pour l'extension **VS Code
"REST Client"** (Huachao Mao — `humao.rest-client`). Si tu ne l'as pas
encore : Marketplace → cherche "REST Client" → Install.

## Fichiers

| Fichier                         | Contrôleur                       | Endpoints                                                               |
| ------------------------------- | -------------------------------- | ----------------------------------------------------------------------- |
| `01-auth.http`                  | `AuthController`                 | login, forgot-password, verify, reset-password, resend-otp, refresh, me |
| `02-regions.http`               | `RegionsController`              | créer / lister / obtenir une région                                     |
| `03-pras.http`                  | `PrasController`                 | créer / modifier / (dés)activer / lister / obtenir une PRA              |
| `04-entrepots.http`             | `EntrepotsController`            | consultation (PNA + PRA confondues)                                     |
| `05-structures-sanitaires.http` | `StructuresSanitairesController` | créer / modifier / cycle d'adhésion / rattachements / lister            |
| `06-users.http`                 | `UsersController`                | créer / modifier / (dés)activer / gérer les rôles                       |
| `07-affectations.http`          | `AffectationsController`         | affecter/retirer un utilisateur à un entrepôt ou une structure          |
| `08-fournisseurs.http`          | `FournisseursController`         | créer / modifier / (dés)activer / lister / obtenir un fournisseur       |
| `09-familles.http`              | `FamillesController`             | créer / modifier / (dés)archiver / lister / obtenir une famille         |
| `10-formes.http`                | `FormesController`               | créer / modifier / (dés)archiver / lister / obtenir une forme           |
| `11-medicaments.http`           | `MedicamentsController`          | créer / modifier / (dés)archiver / lister / obtenir un médicament       |
| `12-conditionnements.http`      | `ConditionnementsController`     | créer / modifier / (dés)archiver / lister / obtenir un conditionnement  |

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
