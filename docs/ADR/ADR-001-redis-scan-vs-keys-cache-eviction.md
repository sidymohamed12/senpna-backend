# ADR-009 — Utilisation de SCAN plutôt que KEYS pour l'invalidation de cache Redis

## Statut

Accepté

## Date

2026-06-30

## Contexte

`CachePort` (port de sortie du domaine, `shared/domain/port/out/CachePort.java`)
expose une méthode `evictByPrefix(String prefix)` permettant d'invalider un
groupe de clés en une seule opération — par exemple tout le cache du
catalogue médicaments (`medicament:catalogue:*`) après une mise à jour, ou
toutes les sessions d'un utilisateur (`auth:token:{userId}:*`) après une
déconnexion forcée.

L'implémentation Redis de ce port (`RedisCacheAdapter`,
`shared/infrastructure/cache/`) doit identifier l'ensemble des clés
correspondant à un pattern avant de les supprimer. Deux mécanismes Redis
permettent ce parcours du keyspace : `KEYS pattern*` et `SCAN cursor MATCH
pattern*`.

Le choix entre les deux n'est pas neutre : il engage la disponibilité de
l'ensemble du système, pas seulement la fonctionnalité de cache.

### Caractéristiques de `KEYS`

Redis exécute les commandes sur un seul thread. `KEYS pattern*` parcourt
**l'intégralité** du keyspace en une seule opération atomique et bloquante.
Pendant son exécution, **aucune autre commande Redis ne peut s'exécuter** —
ni les lectures de cache d'autres modules, ni les opérations du
`RedisRateLimitAdapter` (script Lua de rate limiting), si elles partagent
la même instance Redis.

La documentation officielle Redis déconseille explicitement `KEYS` en
production, quel que soit le volume de données, précisément à cause de ce
comportement bloquant.

### Caractéristiques de `SCAN`

`SCAN` parcourt le keyspace par lots configurables (`COUNT`), via un
curseur itératif, sans jamais bloquer le thread Redis au-delà du temps de
traitement d'un lot. Le coût est un nombre d'allers-retours réseau plus
élevé qu'un appel `KEYS` unique, en échange d'une garantie de
non-blocage absolue.

## Décision

`RedisCacheAdapter.evictByPrefix()` utilise `SCAN` (via
`RedisConnection.scan(ScanOptions)`) plutôt que `KEYS`, avec un lot
(`COUNT`) de 200 clés par itération, suivi d'une suppression batchée
(`DEL`) une fois le parcours terminé.

Cette implémentation est utilisée **indépendamment du volume actuel de
clés en cache**. La décision n'est pas conditionnée par "le volume est
encore petit aujourd'hui" — c'est précisément ce raisonnement que cet ADR
rejette (voir section Alternatives rejetées).

## Justification

### 1. Le port domaine ne doit jamais exposer un risque de disponibilité implicite

`CachePort` est un contrat du domaine. Rien dans sa signature
(`evictByPrefix(String prefix): void`) ne laisse supposer qu'un appel
pourrait geler l'ensemble des opérations Redis du système, y compris
celles d'autres bounded contexts (rate limiting, autres caches métier).
Une implémentation basée sur `KEYS` violerait silencieusement ce contrat :
le code appelant (un use case d'invalidation de cache médicament, par
exemple) n'a aucun moyen de savoir que cet appel a un effet de bord sur
la latence globale de l'application.

`SCAN` respecte l'invariance attendue d'un port d'infrastructure : son
coût est local à l'opération, jamais global au système.

### 2. Open/Closed Principle — le comportement doit scaler sans modification du code

SEN PharmaFlow est conçu pour grandir : davantage de PRA, davantage de
structures sanitaires, un catalogue médicament plus large, davantage
d'utilisateurs simultanés. Une implémentation `KEYS`, fonctionnelle à
faible volume, deviendrait un point de défaillance silencieux à mesure
que le système scale — sans qu'aucun signal de compilation ou de test ne
le révèle à l'avance.

`SCAN` garantit un comportement constant indépendamment du volume de
clés. Le composant infrastructure reste fermé à la modification face à
la croissance du système — exactement ce qu'exige l'OCP.

### 3. Coût mesuré, bénéfice disproportionné

Pour les volumes actuels de SEN PharmaFlow (catalogue médicaments, rôles,
sessions actives — de l'ordre de centaines à quelques milliers de clés
par groupe de préfixe), le surcoût de `SCAN` par rapport à `KEYS` est de
l'ordre de quelques millisecondes supplémentaires par invalidation. Ce
coût est négligeable au regard du risque écarté : un gel total de Redis,
potentiellement en heure de pointe (par exemple lors d'une réception de
commande PRA impliquant plusieurs validations de stock simultanées).

### 4. Fail-open cohérent avec le reste de l'infrastructure cache

`RedisCacheAdapter` applique déjà une stratégie fail-open sur `get`,
`put` et `evict` : toute erreur Redis est journalisée puis absorbée,
sans jamais faire échouer l'opération métier appelante. L'implémentation
`SCAN` suit la même discipline — une erreur de scan est capturée et
journalisée, l'invalidation échoue silencieusement plutôt que de
propager une exception vers le use case appelant.

## Alternatives rejetées

### Garder `KEYS` tant que le volume reste faible

Rejeté. Ce raisonnement reporte un risque de production sur un jugement
("c'est encore petit") qui n'est ni vérifié par un test, ni alerté par
un monitoring, ni révisé automatiquement à mesure que le système évolue.
Le jour où le volume dépasse le seuil implicite tolérable, l'incident se
manifeste en production, pas en revue de code. Un choix d'architecture
ne doit pas dépendre d'une promesse de vigilance future.

### Structures de tags Redis (Redis Sets pour indexer les clés par préfixe)

Envisageable à plus long terme si le volume de clés par groupe devient
réellement très important (dizaines de milliers et plus). Complexité
d'implémentation et de maintenance non justifiée au regard du volume
actuel et prévisible de SEN PharmaFlow. À reconsidérer si un ADR futur
constate que `SCAN` devient un goulot d'étranglement mesuré (et non
supposé).

### `UNLINK` au lieu de `DEL` pour la suppression batchée

Envisagé pour la suppression non-bloquante des clés collectées (`UNLINK`
délègue la libération mémoire à un thread Redis asynchrone, contrairement
à `DEL` qui est synchrone). Non retenu pour l'instant : le volume de
clés supprimées par invalidation reste suffisamment faible pour que `DEL`
soit instantané. À réévaluer si les lots `DEL` venaient à dépasser
plusieurs milliers de clés.

## Conséquences

### Positives

- Aucun appel à `CachePort.evictByPrefix()` ne peut bloquer Redis,
  quel que soit le volume de clés concerné, présent ou futur.
- Le comportement de l'adapter est indépendant de la croissance du
  système — pas de dette technique latente à anticiper.
- Cohérence avec la stratégie fail-open déjà en place sur le reste de
  `RedisCacheAdapter`.

### Négatives / compromis acceptés

- Légère augmentation de la latence d'invalidation par rapport à `KEYS`
  (de l'ordre de quelques millisecondes pour les volumes actuels),
  jugée négligeable au regard du risque écarté.
- Code légèrement plus complexe que l'appel direct
  `redisTemplate.keys(pattern)` — compensé par le Javadoc détaillé sur
  `scanKeys()` expliquant le choix.

## Implémentation

Fichier concerné :
`shared/infrastructure/cache/RedisCacheAdapter.java`

Méthode publique inchangée (contrat du port respecté) :
```java
void evictByPrefix(String prefix);
```

Implémentation interne basée sur `RedisConnection.scan(ScanOptions)` avec
un lot de 200 clés par itération (`SCAN_BATCH_SIZE`), exécutée sur une
connexion "collante" (`executeWithStickyConnection`) pour garantir la
validité du curseur tout au long du parcours, suivie d'une suppression
batchée unique via `redisTemplate.delete(List<String>)`.

## Références

- [Redis documentation — KEYS command](https://redis.io/docs/latest/commands/keys/)
  — avertissement officiel sur l'usage en production
- [Redis documentation — SCAN command](https://redis.io/docs/latest/commands/scan/)
- `shared/domain/port/out/CachePort.java` — contrat du domaine
- `shared/infrastructure/cache/InMemoryCacheAdapter.java` — implémentation
  de test, non concernée par cet ADR (pas de keyspace Redis)
