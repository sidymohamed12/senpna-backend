# ADR-003 : Pas de cache pour les listes/recherches — uniquement un cache par ID

## Statut

Accepté

## Contexte

Plusieurs agrégats du système exposent un décorateur de cache Redis (`Caching*RepositoryAdapter`) au-dessus de leur port de persistance, suivant tous le même schéma :

| Adapter de cache                         | Clé                    | Préfixe          |
| ---------------------------------------- | ---------------------- | ---------------- |
| `CachingMedicamentRepositoryAdapter`     | `medicament:id:<uuid>` | `medicament:id:` |
| `CachingLotRepositoryAdapter`            | `lot:id:<uuid>`        | `lot:id:`        |
| `CachingMouvementStockRepositoryAdapter` | `mouvement:id:<uuid>`  | `mouvement:id:`  |
| `CachingActualiteRepositoryAdapter`      | `actualite:id:<uuid>`  | `actualite:id:`  |
| `CachingUserManagementRepositoryAdapter` | `user:id:<uuid>`       | `user:id:`       |
| `CachingProjetRepositoryAdapter`         | `projet:id:<uuid>`     | `projet:id:`     |

Dans chacun de ces adapters, la méthode `search(criteria, pageRequest)` (recherche paginée/filtrée) délègue **directement** à l'implémentation JPA sous-jacente, sans passer par le cache :

```java
@Override
public PageResult<X> search(XSearchCriteria criteria, PageRequest pageRequest) {
    return delegate.search(criteria, pageRequest);
}
```

Seule la lecture par identifiant (`findById`) bénéficie du cache-aside Redis. La question se pose, de façon récurrente à chaque nouvel agrégat caché, de savoir s'il faut étendre le cache aux listes/recherches.

## Décision

**Aucune méthode de recherche/liste (`search`, `findAll` paginé, ou équivalent) n'est mise en cache**, quel que soit l'agrégat concerné. Cette règle s'applique de façon transversale à tous les décorateurs `Caching*RepositoryAdapter` du projet, actuels et futurs.

Seule la lecture ponctuelle par identifiant (`findById`) est mise en cache, à la demande, sous une clé unique par entité (`<agrégat>:id:<uuid>`).

## Justification

### 1. Explosion combinatoire des clés

Une clé de cache pour un résultat de `search` devrait encoder l'ensemble des critères (filtres, tri, page, taille de page), par exemple :

```
medicament:search:dci=paracetamol&famille=X&actif=true&page=2&size=20&sort=nom
lot:search:medicamentId=Y&entrepotId=Z&expire=false&page=0&size=50
user:search:role=PHARMACIEN&region=X&actif=true&page=1
```

Avec plusieurs filtres combinables, plusieurs tris possibles et la pagination, le nombre de clés distinctes croît de façon combinatoire et peut largement dépasser le nombre de lignes de la table elle-même. Deux appels formulent rarement une requête strictement identique : le taux de hit attendu est faible, ce qui limite fortement le bénéfice — pour n'importe lequel des agrégats listés ci-dessus.

### 2. Coût et imprécision de l'invalidation

Une écriture (`save`, création, modification, changement de statut) sur une entité peut affecter le résultat de n'importe quelle recherche existante en cache, si l'entité modifiée matche les critères de cette recherche. Contrairement au cache par ID — où une écriture invalide une clé unique et identifiable (`<agrégat>:id:<id>`) — il n'existe pas de moyen simple de cibler uniquement les entrées de `search` réellement impactées par une écriture donnée.

La seule stratégie d'invalidation correcte serait de purger tout le préfixe `<agrégat>:search:*` (mécanisme déjà disponible via `CachePort#evictByPrefix`, basé sur un `SCAN` Redis) à **chaque** écriture. Combiné à un taux de hit déjà faible (point 1), le cache serait vidé en permanence pour un bénéfice quasiment nul, tout en ajoutant de la complexité et un risque de servir des données de liste obsolètes le temps que l'invalidation se propage.

### 3. Le cache par ID n'a pas ce problème

Le cache-aside par ID reste, lui, systématiquement rentable pour ces agrégats :

- Une seule clé par entité → invalidation triviale et précise à chaque écriture (`mettreEnCache` après `save`).
- Le taux de hit est structurellement bon dès qu'une même entité est relue plusieurs fois en peu de temps (cas fréquent : consultation d'une fiche médicament, d'un lot, d'un mouvement de stock, d'un profil utilisateur après authentification, etc.).
- Le format `<agrégat>:id:<uuid>` est stable, prévisible, et ne dépend d'aucun paramètre de requête.

### 4. Cohérence avec les référentiels quasi-statiques

Les caches de référentiels quasi-statiques (`FormeCache`, `RegionCache`, `FamilleCache`, `FournisseurCache`) suivent une stratégie différente et volontairement distincte : chargement intégral en mémoire au démarrage, rechargement complet à chaque écriture. Cette approche n'est viable que parce que ces référentiels sont petits (dizaines/centaines de lignes) et changent rarement — ce qui n'est le cas d'aucun des agrégats du tableau ci-dessus (volumes plus importants et/ou écritures plus fréquentes). Il ne faut donc pas confondre les deux stratégies ni tenter d'appliquer celle des référentiels statiques aux listes de ces agrégats.

### 5. Le cache reste un accélérateur, jamais une source de vérité

Conformément au principe déjà appliqué dans `RedisCacheAdapter` et `JsonCacheSupport` (toute erreur de cache dégrade silencieusement vers un accès base plutôt que de faire échouer l'appel), ajouter un cache de liste à faible taux de hit et à invalidation grossière irait à l'encontre de cet objectif : complexité opérationnelle ajoutée sans gain de fiabilité ni de performance garanti.

## Conséquences

### Positives

- Règle simple, uniforme et prévisible à travers tous les `Caching*RepositoryAdapter` : « cache par ID uniquement ». Facile à appliquer sur un nouvel agrégat, facile à faire respecter en revue de code.
- Invalidation toujours précise et bon marché (une clé par entité), sans risque de données de liste obsolètes servies aux utilisateurs.
- Pas de risque de saturation de la mémoire Redis par un grand nombre de clés de recherche peu réutilisées, quel que soit l'agrégat.

### Négatives / limites acceptées

- Chaque appel à `search` (listes, écrans de recherche, exports, filtres) tape systématiquement la base de données, sans accélération applicative, pour tous les agrégats concernés.
- Si le volume d'appels à `search` devient un goulot d'étranglement en production sur un agrégat en particulier, cette décision devra être réévaluée **pour cet agrégat spécifiquement**, et non généralisée d'office aux autres.

### Compensations déjà en place ou envisageables

- Optimisation côté base de données : index sur les colonnes filtrées/triées les plus utilisées par chaque `search`.
- Cache HTTP (`ETag` / `Cache-Control`) côté client si les mêmes écrans de recherche sont rappelés fréquemment sans changement de critères.
- Si un besoin précis émerge sur un agrégat donné (ex. « page 1, sans filtre, tri par défaut » très fréquemment consultée), un cache **ciblé** sur ce petit nombre de clés stables et prévisibles pourrait être ajouté au cas par cas, sans généraliser à toutes les combinaisons de `search`.

## Alternatives considérées et écartées

| Alternative                                                                                                                 | Raison de l'écarter                                                                                                                |
| --------------------------------------------------------------------------------------------------------------------------- | ---------------------------------------------------------------------------------------------------------------------------------- |
| Cacher chaque résultat de `search` par clé composite (filtres + pagination + tri), pour un ou plusieurs agrégats            | Explosion du nombre de clés, taux de hit trop faible, invalidation imprécise                                                       |
| Purger tout le préfixe `<agrégat>:search:*` à chaque écriture, sans limiter les clés cachées                                | Techniquement possible (`evictByPrefix` existe déjà) mais purge quasi permanente pour un gain quasi nul — complexité non justifiée |
| Appliquer aux agrégats volumineux/dynamiques la stratégie des référentiels quasi-statiques (chargement intégral en mémoire) | Non viable : volumes trop importants et/ou écritures trop fréquentes pour un rechargement complet à chaque modification            |

## Portée

Cette décision s'applique à tous les décorateurs de cache existants (`Medicament`, `Lot`, `MouvementStock`, `Actualite`, `User`, `Projet`) et sert de règle par défaut pour tout nouvel agrégat qui introduirait un décorateur `Caching*RepositoryAdapter` sur le même modèle. Toute exception (cache ciblé sur une liste précise) doit faire l'objet d'un ADR dédié justifiant le cas d'usage spécifique.

## Références

- `medicament/infrastructure/persistence/adapter/CachingMedicamentRepositoryAdapter.java`
- `stock/infrastructure/persistence/adapter/CachingLotRepositoryAdapter.java`
- `stock/infrastructure/persistence/adapter/CachingMouvementStockRepositoryAdapter.java`
- `actualite/infrastructure/persistence/adapter/CachingActualiteRepositoryAdapter.java`
- `auth/infrastructure/persistence/adapter/CachingUserManagementRepositoryAdapter.java`
- `projet/infrastructure/persistence/adapter/CachingProjetRepositoryAdapter.java`
- `shared/infrastructure/cache/RedisCacheAdapter.java` / `JsonCacheSupport.java`
- `shared/infrastructure/cache/FormeCache.java` (stratégie contrastée pour les référentiels quasi-statiques)
