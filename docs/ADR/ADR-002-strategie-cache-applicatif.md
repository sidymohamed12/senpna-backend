# ADR 002 — Stratégie de cache applicatif (Redis clé/valeur vs préchargement mémoire vs `@Cacheable`)

## Statut

Acceptée — 2026-07-05

## Contexte

Le besoin exprimé : ajouter du cache pour les features à fort volume ou à forte fréquence de lecture
(`catalogue`, `medicament`, `stock`, `lots`, `users`, `mouvement stock`), **sans** reproduire la stratégie
utilisée pour les référentiels quasi-statiques (`region`, `fournisseur`) — c'est-à-dire sans préchargement
intégral en mémoire au démarrage — mais avec un cache Redis **à la clé**, alimenté à la demande. En parallèle,
`forme` et `famille` (référentiels quasi-statiques, faible volumétrie) devaient au contraire suivre exactement
la stratégie de préchargement déjà en place pour `region`/`fournisseur`.

Le projet suit la Clean Architecture (Uncle Bob) : chaque module métier (`medicament`, `stock`, `utilisateurs`,
`catalogue`, `organisation`, `fournisseur`) expose des ports de domaine (`in`/`out`) implémentés par des
adaptateurs d'infrastructure. Une abstraction de cache générique existe déjà : `CachePort`
(`shared.domain.port.out`), avec deux implémentations basculées par profil Spring — `RedisCacheAdapter`
(dev/prod) et `InMemoryCacheAdapter` (test) — déjà utilisée pour l'OTP (`OtpService`) et le rate limiting
(`RedisRateLimitAdapter`).

La question posée en cours de revue : pourquoi ne pas avoir utilisé l'annotation Spring `@Cacheable` sur les
services applicatifs, qui est l'approche la plus immédiate en Spring Boot ?

## Options considérées

### Option A — `@Cacheable` / `@CacheEvict` / `@CachePut` sur les services applicatifs

Annotations Spring Cache directement sur les méthodes des use cases ou des adaptateurs, avec un
`CacheManager` (typiquement `RedisCacheManager`) configuré séparément, clés dérivées par expression SpEL.

**Rejetée.** Risques identifiés :

1. **Sérialisation du domaine.** Les agrégats (`Lot`, `Medicament`, `Stock`, `User`, …) ont des constructeurs
   privés et des champs typés en Value Objects sans constructeur par défaut (`LotId`, `FournisseurId`, …).
   `@Cacheable` sérialise la valeur de retour brute (Jackson) : sans DTO intermédiaire, ça casse ; avec DTO,
   ça ne supprime pas les points 2 à 6 ci-dessous, seulement le problème de sérialisation.
2. **Piège de l'auto-invocation.** `@Cacheable` repose sur un proxy AOP Spring. Un appel interne à la même
   classe (`this.methodeCachee(...)`) contourne silencieusement le proxy — aucune erreur, aucun log, le cache
   ne s'active simplement jamais. Bug typiquement découvert en production sous charge.
3. **Clés en SpEL non vérifiées à la compilation.** `key = "#entrepotId.value + ':' + #lotId.value"` — une
   faute de frappe ou un renommage de champ ne casse qu'à l'exécution, parfois seulement sur un chemin de code
   peu exercé.
4. **Éviction multi-clés désynchronisable.** Le cas `Stock` (deux clés à invalider ensemble : par id et par
   couple entrepôt/lot) exigerait deux annotations `@CacheEvict`/`@CachePut` séparées sur la même méthode, avec
   deux expressions SpEL distinctes — rien n'empêche de modifier l'une sans l'autre.
5. **Cache stampede.** Avec un TTL court sur une donnée à fort trafic (ex. `catalogue`), l'expiration
   simultanée expose à des lectures concurrentes qui tapent toutes la base en même temps ; `sync = true` ne
   protège qu'au niveau JVM locale, pas en cluster multi-instances.
6. **Double abstraction de cache.** `@Cacheable` nécessite son propre `CacheManager`/`RedisCacheManager`,
   configuré indépendamment de `CachePort`/`RedisCacheAdapter` déjà en place — deux mécanismes de cache à
   maintenir, deux bascules dev/test/prod à synchroniser.
7. **Non testable sans contexte Spring.** Les tests unitaires du projet (`MockitoExtension`, sans
   `@SpringBootTest`) instancient les use cases à la main ; `@Cacheable` ne s'active que via le proxy AOP,
   donc invérifiable sans démarrer un contexte applicatif complet.

### Option B — DTO de cache dans la couche application + `@Cacheable`

Variante de l'option A où un DTO dédié (simple bean, sans les contraintes du domaine) porte la sérialisation,
et vit dans la couche application plutôt que dans le domaine.

**Rejetée.** Résout le problème de sérialisation (point 1 ci-dessus), mais ne résout aucun des points 2 à 6.
De plus, elle déplace une préoccupation d'infrastructure (« comment on sérialise pour le stockage ») dans la
couche application, qui devrait rester concentrée sur l'orchestration métier — glissement de responsabilité
comparable à mettre du SQL dans un contrôleur REST.

### Option C — Décorateur de port + préchargement mémoire selon la nature de la donnée (retenue)

Trois stratégies, choisies selon la nature de la donnée plutôt qu'une seule stratégie uniforme :

| Nature de la donnée | Stratégie | Exemples |
|---|---|---|
| Donnée avec repository/query port existant, volumineuse ou à écriture fréquente | **Décorateur Redis clé/valeur, cache-aside, à la demande** — implémente le même port, `@Primary`, délègue à l'adaptateur JPA concret injecté par son type concret | `medicament`, `lot`, `stock`, `mouvement stock`, `users` |
| Donnée sans persistance propre (le cache *est* le stockage) | `CachePort` injecté directement dans le service applicatif | OTP (déjà en place), tokens, rate limit |
| Référentiel quasi-statique, faible volumétrie | **Préchargement mémoire intégral au démarrage** (`ApplicationRunner`), rechargé explicitement (`reload()`) à chaque écriture | `region`, `fournisseur` (existant), `forme`, `famille` (ajoutés) |
| Vue calculée/agrégée sur des données à fort trafic en écriture | Cache Redis TTL court au niveau use case (pis-aller pragmatique — CQRS/read-model recommandé à terme) | `catalogue` |

## Décision

**Option C.** Pour chaque agrégat à mettre en cache à la demande (`medicament`, `lot`, `stock`, `mouvement
stock`, `users`) :

- Un DTO d'infrastructure dédié (`XCacheEntry`, record) fait le pont entre le cache (JSON) et le modèle de
  domaine, via `Agregat.reconstruct(...)` — symétrique à ce que fait le `Mapper` JPA existant pour la
  persistance. Ce DTO vit en `infrastructure.persistence.cache`, jamais dans le domaine ni dans l'application.
- Un décorateur (`CachingXRepositoryAdapter`) implémente le port de sortie existant (`XRepositoryPort`),
  annoté `@Primary`, injecte l'adaptateur JPA concret (jamais l'interface, pour éviter toute ambiguïté Spring
  entre les deux beans qui implémentent le même port) et délègue :
  - lecture unitaire (`findById`) : cache-aside (lecture cache → sinon base → repeuplement) ;
  - écriture (`save`) : délégation puis mise à jour immédiate de la clé de cache ;
  - lectures verrouillantes (`findByEntrepotIdAndLotIdForUpdate`, pessimiste) : **jamais** mises en cache ;
  - recherches paginées/multi-critères (`search`, FEFO, listes d'alertes) : jamais mises en cache (trop de
    combinaisons de clés, ou fraîcheur stricte requise).
- Le TTL par feature est piloté par configuration (`AppProperties.CacheProperties` / `app.cache.*`), différencié
  selon la volatilité réelle de la donnée (`mouvement-ttl` long car append-only/immuable, `stock-ttl` court car
  quantités très mouvantes).
- Le cache s'appuie sur l'abstraction déjà en place (`CachePort` → `JsonCacheSupport` pour la couche JSON),
  jamais sur un second mécanisme de cache.

Pour `forme` et `famille`, la stratégie retenue est le **préchargement mémoire complet au démarrage**,
strictement symétrique à `RegionCache`/`FournisseurCache` déjà en place : `FormeCache`/`FamilleCache`
(`ApplicationRunner`, `@Order(0)`) alimentées par un `FormeQueryPort`/`FamilleQueryPort` (implémentés par le
module `medicament`), exposées via `FormeCachePort`/`FamilleCachePort` (`shared.domain.port.out`), rechargées
(`reload()`) à chaque création/modification/archivage/désarchivage.

Pour le `catalogue` (vue calculée, pas d'agrégat propre), le cache est posé directement dans les use cases de
consultation (national, régional, inter-PRA), à une clé dérivée des paramètres de requête effectifs (y compris
la région *résolue*, jamais la région demandée brute, pour éviter toute fuite de catalogue entre régions), avec
un TTL court et **sans** invalidation croisée depuis `stock` — compromis pragmatique documenté plutôt
qu'implicite.

## Conséquences

**Positives**

- Respect strict de la Dependency Rule : le domaine ne connaît rien de Jackson, Redis ou de la sérialisation.
- OCP : l'ajout du cache n'a modifié aucune ligne des use cases existants (ils dépendent du port, pas de
  l'implémentation).
- SRP : une classe = une responsabilité ; chaque décorateur est testable isolément avec un mock du délégué.
- Aucun risque d'auto-invocation ou de proxy AOP contourné — le décorateur est un objet explicite du graphe
  d'injection, pas un aspect tissé.
- Un seul mécanisme de cache (`CachePort`) pour tout le projet, une seule bascule dev/test/prod
  (`@Profile`/`RedisAutoConfiguration` exclue en test).
- Les clés de cache sont des constantes Java concatenées explicitement, vérifiées à la compilation dans leur
  structure (pas de SpEL).

**Négatives / dettes acceptées**

- Plus de fichiers qu'une annotation (un DTO + un décorateur par agrégat) — coût jugé raisonnable au regard des
  risques évités (cf. Option A).
- Le cache du `catalogue` (TTL seul, sans invalidation croisée depuis `stock`) admet une fenêtre de fraîcheur
  d'une minute par défaut — acceptable pour une vue de consultation, mais à remplacer par un read-model
  événementiel si la volumétrie ou les exigences de fraîcheur augmentent (cf. recommandations ci-dessous).
- Pas encore de protection anti-stampede (verrou de repeuplement) ni de métriques de hit-ratio exposées —
  identifiées comme axes d'amélioration, non bloquantes pour cette itération.

## Recommandations pour les projets futurs (reproductibilité du principe)

1. Une donnée a déjà un repository/query port ? → décorateur sur ce port, jamais d'annotation de cache posée
   sur le service applicatif.
2. Une donnée n'a pas de persistance propre (jeton, compteur, OTP) ? → `CachePort` injecté directement, le
   cache est le stockage légitime.
3. Un référentiel est quasi-statique et de faible volumétrie ? → préchargement mémoire complet au démarrage,
   rechargé explicitement à l'écriture — pas de TTL à gérer, latence de lecture quasi nulle.
4. Une vue est calculée/agrégée sur des données à fort trafic en écriture ? → TTL court en dernier recours ;
   viser un read-model mis à jour par événements de domaine dès que la volumétrie le justifie.
5. Ne jamais introduire `@Cacheable` en présence d'une abstraction de cache déjà choisie pour le projet :
   deux mécanismes de cache qui cohabitent est un coût de maintenance, pas une option neutre.
