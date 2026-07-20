# ADR-005 — Stratégie de partitionnement PostgreSQL des tables de messagerie

|               |                                                                   |
| ------------- | ----------------------------------------------------------------- |
| **Statut**    | Accepté                                                           |
| **Date**      | 2026-07-20                                                        |
| **Décideurs** | Équipe backend / architecture                                     |
| **Périmètre** | Tables `messages` et `messages_historique` du module `messagerie` |
| **Dépend de** | ADR-004 — Architecture de la messagerie temps réel                |

## Contexte

La messagerie a une exigence explicite de **traçabilité permanente** : aucun message n'est jamais réellement supprimé, y compris lorsqu'un utilisateur exerce son droit de modification/suppression applicative (fenêtre de 30 minutes après l'envoi).

Cette exigence se traduit par une séparation en deux tables :

- **`messages`** — état courant tel qu'affiché à l'utilisateur (contenu actuel, statut `ENVOYE`/`MODIFIE`/`SUPPRIME`). Mutable.
- **`messages_historique`** — journal append-only de chaque version d'un message (contenu original avant modification, horodatage, auteur de l'action). Jamais purgée, jamais mise à jour après écriture.

Conséquence directe : ces deux tables croissent **indéfiniment**, sans mécanisme de purge, sur une plateforme dont l'horizon de vie se compte en années. `messages_historique` croît structurellement plus vite que `messages` (une ligne par message initial **et** une ligne supplémentaire par action de modification/suppression).

Sans stratégie de partitionnement, les conséquences attendues à moyen terme sont connues et documentées dans l'écosystème PostgreSQL :

- Dégradation progressive des performances de lecture même sur les conversations récentes, à mesure que les index grossissent sur l'ensemble de l'historique.
- `VACUUM`/`ANALYZE` de plus en plus coûteux sur une table monolithique.
- Impossibilité d'archiver ou de purger sélectivement une période sans un `DELETE` massif, verrouillant et fragmentant la table.

L'échelle utilisateur (< 500 utilisateurs simultanés) n'élimine pas ce risque : la traçabilité permanente signifie que le volume cumulé continue de croître même si le nombre d'utilisateurs actifs reste stable — c'est un problème de temps, pas de charge instantanée.

## Décision

**Partitionnement natif PostgreSQL par intervalle (`PARTITION BY RANGE`) sur la colonne de date de création (`cree_le`), appliqué aux deux tables `messages` et `messages_historique`, avec une granularité de partition de 1 à 2 ans.**

Éléments de la décision :

1. **Clé de partitionnement : la date, pas l'identifiant de conversation ou d'utilisateur.** Les requêtes dominantes (chargement d'historique d'une conversation récente, requêtes d'audit sur une période) sont naturellement bornées dans le temps ; un partitionnement par date maximise l'élagage de partitions (_partition pruning_) sur les cas d'usage réels.
2. **`messages_historique` est prioritaire dans la mise en œuvre** — elle croît plus vite et concentre l'essentiel du risque de dégradation à long terme.
3. **Gestion automatisée via l'extension `pg_partman`** plutôt qu'un script de création de partitions maison, pour éliminer le risque opérationnel d'oubli (absence de partition future = échecs d'insertion).
4. **Décision prise dès la conception du schéma**, avant toute mise en production — un repartitionnement a posteriori sur une table déjà volumineuse est significativement plus coûteux et risqué (réécriture complète des données) qu'une mise en place native dès la migration initiale.

## Alternatives considérées

### Absence de partitionnement (table unique)

Rejeté. Contredit directement l'exigence de traçabilité permanente combinée à l'absence de purge : c'est précisément le scénario où l'absence de partitionnement fait le plus mal à moyen terme.

### Partitionnement par `conversation_id` (hash ou liste)

Rejeté comme stratégie principale. Les requêtes d'audit et de conformité (probables sur une plateforme ministérielle) sont typiquement bornées dans le temps ("tous les messages de telle période"), pas par conversation. Un partitionnement par date sert mieux ce cas d'usage sans empêcher un index secondaire performant sur `conversation_id` à l'intérieur de chaque partition.

### Archivage vers une table/froide séparée (déplacement applicatif des anciennes données)

Non retenu comme solution principale. Casserait la source de vérité unique (une requête d'audit devrait interroger deux emplacements) pour un gain de simplicité de mise en œuvre initiale. Reste une option complémentaire future (déplacement de partitions anciennes vers un stockage moins coûteux) — le partitionnement natif rend cette évolution triviale (`DETACH PARTITION`) si elle devient nécessaire, sans redécision architecturale.

### Sharding applicatif (bases séparées par région/PRA)

Rejeté sans hésitation — sur-ingénierie manifeste à l'échelle visée (< 500 utilisateurs). Introduirait une complexité transactionnelle et opérationnelle sans aucun signal actuel qui la justifie (contraire au principe YAGNI déjà appliqué aux autres décisions de cette messagerie).

## Conséquences

**Positives**

- Élagage de partitions (_partition pruning_) : les requêtes bornées dans le temps (l'immense majorité de l'usage réel) n'examinent que les partitions concernées, indépendamment du volume historique cumulé.
- Maintenance (`VACUUM`, réindexation) parallélisable et bornée par partition plutôt que sur une table monolithique toujours croissante.
- Possibilité future de détacher/archiver des partitions anciennes vers un stockage moins coûteux sans réécriture de données ni changement de schéma applicatif.
- Décision alignée avec un pattern déjà présent ailleurs dans la base de code (le journal des mouvements de stock est également conçu comme append-only) — cohérence architecturale.

**Négatives / coûts à assumer**

- Contrainte de modélisation : la clé de partitionnement (`cree_le`) doit figurer dans toute contrainte d'unicité, y compris la clé primaire (`PRIMARY KEY (id, cree_le)` plutôt que `PRIMARY KEY (id)`) — impact sur le mapping JPA/Hibernate et sur les clés étrangères entre `messages` et `messages_historique`.
- Dépendance opérationnelle supplémentaire (`pg_partman`) à installer, configurer et surveiller (création des partitions futures avant qu'elles ne soient nécessaires).
- Toute requête ne filtrant pas sur `cree_le` perd le bénéfice de l'élagage — discipline à maintenir dans le code d'accès aux données (cf. document d'intégration associé).

## Décisions liées

- ADR-004 — Architecture de la messagerie temps réel.
- Guide d'intégration — _Mise en œuvre du partitionnement PostgreSQL pour les tables de messagerie_ (document non-ADR, détaille la mise en œuvre technique de cette décision).
