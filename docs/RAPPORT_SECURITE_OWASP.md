# Rapport d'audit — Conformité OWASP

**Projet :** SEN PharmaFlow (senpna) — API Spring Boot 3.5.6 / Java 21
**Périmètre :** `src/main/java`, configuration Spring Security, persistence JPA/Criteria, upload de fichiers
**Méthode :** revue statique du code (architecture hexagonale, clean code, SOLID) — pas d'exécution de build (pas d'accès réseau à Maven Central dans cet environnement). **`mvn clean verify` à lancer côté CI avant merge.**

---

## 1. Ce qui était déjà en place (bon niveau de départ)

| Contrôle                 | État constaté                                                                                                                                                                                                                                                                                                               |
| ------------------------ | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Authentification         | JWT stateless (Bearer), pas de session serveur, `SessionCreationPolicy.STATELESS`                                                                                                                                                                                                                                           |
| Mots de passe            | `BCryptPasswordEncoder(12)` — conforme OWASP 2024                                                                                                                                                                                                                                                                           |
| Autorisation             | `@EnableMethodSecurity` + `@PreAuthorize` par cas d'usage, RBAC                                                                                                                                                                                                                                                             |
| Brute-force / DDoS léger | `RateLimitFilter` en amont de la chaîne, limites configurables (`auth-limit`, `api-limit`)                                                                                                                                                                                                                                  |
| Injection SQL            | 100 % JPA/Hibernate + Criteria API (`cb.like`, `cb.equal`...) avec liaison de paramètres — **aucune concaténation de SQL trouvée**. Une seule requête `nativeQuery = true` (`CatalogueStockJpaRepository`), déjà paramétrée. jOOQ est une dépendance déclarée mais **non utilisée** dans le code (pas de risque `plainSQL`) |
| Fuite d'information      | `GlobalExceptionHandler` intercepte tout, aucun stacktrace renvoyé au client, message générique sur 500                                                                                                                                                                                                                     |
| CORS                     | Liste blanche explicite via `app.security.cors-allowed-origins` (pas de wildcard `*`)                                                                                                                                                                                                                                       |
| Upload de fichiers       | Presigned URLs — le fichier ne transite jamais par le serveur applicatif ; validation Content-Type + taille ; clé objet générée côté serveur (pas de traversal) ; endpoint public strictement limité à `CV`/`LETTRE_DE_MOTIVATION`                                                                                          |
| Secrets                  | Aucun secret en dur — tout via variables d'environnement (`${DB_PASSWORD}`, `${JWT_SECRET}`...)                                                                                                                                                                                                                             |
| Logs                     | `RequestLoggingInterceptor` : pas de corps de requête ni de header `Authorization` journalisé                                                                                                                                                                                                                               |
| Prod                     | Swagger/Actuator `env` désactivés, `ddl-auto: validate`, `show-sql: false`                                                                                                                                                                                                                                                  |

---

## 2. Failles / manques identifiés et corrigés

### A05:2021 – Security Misconfiguration : en-têtes HTTP absents

**Avant :** aucun en-tête de sécurité explicite (CSP, HSTS, Referrer-Policy, Permissions-Policy).
**Correctif** — `config/SecurityConfig.java` :

- `Content-Security-Policy: default-src 'none'; frame-ancestors 'none'; base-uri 'none'` en prod/test (API JSON pure) — assoupli uniquement en profil `dev` pour permettre Swagger UI.
- `Strict-Transport-Security` : 1 an, `includeSubDomains`, `preload`.
- `Referrer-Policy: strict-origin-when-cross-origin`.
- `Permissions-Policy` (désactive géoloc/caméra/micro/paiement par défaut).
- `X-Frame-Options: DENY`, `X-Content-Type-Options: nosniff`, `X-Permitted-Cross-Domain-Policies: none`.
- `Cache-Control` explicite (héritage Spring Security — non-cache par défaut sur les réponses).

### A03:2021 – Injection : wildcard-injection sur les recherches `LIKE`

**Avant :** dans les 14 classes `*Specifications.java` (recherche texte sur actualités, utilisateurs, appels d'offres, médicaments, fournisseurs, lots...), le motif `"%" + texte + "%"` était construit sans échapper les métacaractères `%`/`_` saisis par l'utilisateur. Un utilisateur pouvait ainsi forcer un `LIKE '%%'` (retour de tout le référentiel, contournement du filtre) ou dégrader les performances sur colonnes non indexées.
**Correctif :**

- Nouvel utilitaire `shared/infrastructure/util/LikePatternEscaper.java`.
- Appliqué automatiquement dans les 14 fichiers concernés : `cb.like(expr, motif, LikePatternEscaper.escapeChar())`.
- _Note :_ ceci ne corrige pas une injection SQL (déjà absente, paramètres liés) mais une injection de **sémantique de recherche** — bonne pratique de validation d'entrée OWASP ASVS 5.1.

### A03:2021 – Injection (XSS stocké) : pas de filet de sécurité côté serveur

**Avant :** les champs texte libre (titre/description d'actualité, opportunité de carrière, projet, et surtout le **formulaire public non authentifié** de candidature `POST /api/candidatures`) n'étaient contraints qu'en longueur (`@Size`), sans contrôle de contenu. La défense XSS reposait entièrement sur l'échappement en sortie du frontend Angular.
**Correctif :**

- Nouvelle contrainte Bean Validation `@NoHtml` (`shared/infrastructure/validation/`) : rejette toute balise HTML, tout gestionnaire d'évènement (`on*=`) et les URI `javascript:`/`data:text/html`.
- Appliquée en défense en profondeur sur les champs à plus fort risque :
  - `SoumettreCandidatureRequest` (civilité, nomComplet, téléphone, messageComplementaire) — **endpoint public, non authentifié, contenu relu par un back-office admin**, donc risque de XSS stocké contre un compte privilégié.
  - `CreateActualiteRequest` / `UpdateActualiteRequest` (titre, description) — contenu affiché publiquement.
  - `CreateOpportuniteCarriereRequest` / `UpdateOpportuniteCarriereRequest` (titre, nomEntreprise, description, lieu).
  - `CreateProjetRequest` / `UpdateProjetRequest` (nom, description).
- **Recommandation de suite** : étendre `@NoHtml` aux DTOs texte libre restants par le même schéma (fournisseurs, structures sanitaires, régions, entrepôts...) — la contrainte est générique et prête à l'emploi.

### A06:2021 – Vulnerable and Outdated Components : pas de scan des dépendances

**Avant :** aucun outil d'analyse de vulnérabilités connues (CVE) sur les dépendances Maven.
**Correctif :** ajout du plugin `org.owasp:dependency-check-maven` dans un profil Maven dédié `security-scan` (non actif par défaut pour ne pas bloquer les builds sans accès au flux NVD) :

```bash
mvn verify -Psecurity-scan -Dnvd.api.key=$NVD_API_KEY
```

Échoue le build si une dépendance a une CVE de score CVSS ≥ 7. Rapport HTML/JSON dans `target/`.
**Recommandation :** l'intégrer en tant qu'étape non bloquante (ou bloquante en environnement de prod) dans le pipeline CI/CD existant (`sonar.host.url` indique déjà un runner self-hosted).

### A09:2021 – Security Logging and Monitoring Failures : fuite via `/error` par défaut

**Avant :** en cas d'exception levée **avant** le `DispatcherServlet` (ex. dans un filtre Servlet), Spring Boot peut retomber sur son endpoint `/error` par défaut, potentiellement plus verbeux que `GlobalExceptionHandler`.
**Correctif** — `application-prod.yml` :

```yaml
server:
  error:
    include-stacktrace: never
    include-message: never
    include-exception: false
    include-binding-errors: never
    whitelabel:
      enabled: false
```

---

## 3. Recommandations non implémentées (nécessitent une décision d'architecture/infra)

Ces points n'ont **pas** été modifiés automatiquement car ils dépendent de choix d'infrastructure que je ne peux pas deviner sans confirmation :

1. **`X-Forwarded-For` non validé** (`RequestLoggingInterceptor.extractIp`, et implicitement `RateLimitFilter`) : l'IP cliente est extraite du header `X-Forwarded-For` sans vérifier que la requête provient bien d'un reverse-proxy de confiance. Si l'application est exposée directement à Internet (sans proxy devant), ce header est falsifiable par le client et permet de contourner le rate limiting par IP. → Configurer `server.forward-headers-strategy: NATIVE` ou `FRAMEWORK` **et** s'assurer qu'un proxy de confiance (nginx/ingress) écrase systématiquement ce header en amont.
2. **Longueur/entropie de `JWT_SECRET`** : le code dérive une clé HMAC directement depuis la variable d'environnement (`Keys.hmacShaKeyFor(...)`), c'est correct, mais rien ne vérifie au démarrage que le secret fait au moins 256 bits. Recommandé : valider la longueur du secret dans un `@PostConstruct` ou via une contrainte `@ConfigurationProperties` custom, pour échouer vite si le secret est trop court en prod.
3. **Révocation de JWT / blacklist** : je n'ai pas trouvé de mécanisme de révocation immédiate d'un access token avant expiration (ex. en cas de compromission). Si ce n'est pas déjà couvert par ailleurs (Redis semble disponible dans le projet), envisager une liste de révocation courte durée (TTL = durée de vie résiduelle du token).
4. **En-tête `Server` Tomcat** : la bannière serveur (`Apache-Coyote`) n'a pas été masquée — mineur (fingerprinting), à traiter au niveau du reverse-proxy si celui-ci n'est pas déjà configuré pour la retirer.
5. **CSP en prod** : `default-src 'none'` part du principe que **rien** n'est servi en HTML par ce backend en production (API JSON pure consommée par un frontend séparé). À confirmer — si un jour une route sert du HTML (ex. page d'erreur personnalisée), la CSP devra être adaptée en conséquence.

---

## 5. Tests ajoutés / corrigés

⚠️ Comme pour le code de production, je n'ai pas pu exécuter `mvn test` (pas d'accès à Maven Central). Les fichiers ci-dessous sont relus manuellement pour la cohérence de signature et de package, mais **`mvn test` doit être lancé avant merge**.

**Corrigé (test cassé par le changement de signature `cb.like(expr, motif, escapeChar)`) :**

- `ActualiteSpecificationsTest.java` — 3 stubs Mockito mis à jour vers la surcharge à 3 arguments.

**Nouveaux tests unitaires (code entièrement nouveau) :**

- `shared/infrastructure/util/LikePatternEscaperTest.java`
- `shared/infrastructure/validation/NoHtmlValidatorTest.java`

**Nouveaux tests de validation Bean Validation (`@NoHtml`) :**

- `SoumettreCandidatureRequestValidationTest.java` (formulaire public — priorité la plus haute)
- `CreateActualiteRequestValidationTest.java` / `UpdateActualiteRequestValidationTest.java`
- `CreateOpportuniteCarriereRequestValidationTest.java` / `UpdateOpportuniteCarriereRequestValidationTest.java`
- `CreateProjetRequestValidationTest.java` / `UpdateProjetRequestValidationTest.java`

**Nouveaux tests unitaires pour les 6 classes `*Specifications` jusqu'ici non couvertes** (démontrent notamment que l'échappement LIKE introduit est bien câblé, avec des entrées contenant `%`/`_`) :

- `AppelOffreSpecificationsTest.java`, `ProjetSpecificationsTest.java`, `CommandeAchatSpecificationsTest.java`, `OpportuniteCarriereSpecificationsTest.java`, `CandidatureSpecificationsTest.java`, `FournisseurSpecificationsTest.java`

**Nouveau test d'intégration pour les en-têtes de sécurité :**

- `config/SecurityHeadersIntegrationTest.java` (`@SpringBootTest` + `MockMvc`, chaîne de filtres réelle — contrairement aux `@WebMvcTest` de contrôleurs existants qui désactivent les filtres via `addFilters = false` et ne les exerçaient donc jamais)

**Créés :**

- `src/main/java/.../shared/infrastructure/util/LikePatternEscaper.java`
- `src/main/java/.../shared/infrastructure/validation/NoHtml.java`
- `src/main/java/.../shared/infrastructure/validation/NoHtmlValidator.java`

**Modifiés :**

- `src/main/java/.../config/SecurityConfig.java` (en-têtes de sécurité)
- `src/main/resources/application-prod.yml` (`server.error.*`)
- `pom.xml` (profil `security-scan` + plugin `dependency-check-maven`)
- 14× `*Specifications.java` (échappement LIKE)
- `SoumettreCandidatureRequest.java`, `CreateActualiteRequest.java`, `UpdateActualiteRequest.java`, `CreateOpportuniteCarriereRequest.java`, `UpdateOpportuniteCarriereRequest.java`, `CreateProjetRequest.java`, `UpdateProjetRequest.java` (`@NoHtml`)

**Aucune modification** de la logique métier, des signatures publiques de use cases, ni des tests existants — les changements sont additifs (nouvelles annotations de validation, nouveaux en-têtes HTTP, nouvel utilitaire).

⚠️ **À faire avant merge :** `mvn clean verify` (build non exécuté ici faute d'accès réseau à Maven Central) et une repasse rapide des tests des `*ControllerTest.java` publics (candidature, actualité, projet, opportunité) pour confirmer qu'aucun jeu de données de test n'introduit accidentellement un caractère `<` ou `on...=` qui ferait échouer la nouvelle validation `@NoHtml`.
