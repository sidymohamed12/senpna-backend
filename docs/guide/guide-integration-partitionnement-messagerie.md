# Guide d'intégration — Partitionnement PostgreSQL des tables de messagerie

> Ce document n'est **pas** une décision d'architecture (voir ADR-002 pour le "pourquoi") — c'est le **comment** : mise en œuvre technique, migrations, configuration `pg_partman`, mapping JPA/Hibernate, discipline de requêtage et runbook opérationnel.

## 1. Prérequis

- PostgreSQL ≥ 12 (partitionnement déclaratif natif mature ; ≥ 11 fonctionne mais avec des limitations sur les contraintes d'unicité et le rattachement de partitions par défaut).
- Extension `pg_partman` installée sur l'instance (`CREATE EXTENSION pg_partman;`), disponible sur la plupart des offres managées (RDS, Cloud SQL) ou à installer manuellement en auto-hébergé.
- Un mécanisme de planification pour `pg_partman.run_maintenance()` : `pg_cron` (préféré si disponible sur l'instance) ou un job externe (cron système / scheduler applicatif) appelant la fonction à intervalle régulier.

## 2. Contrainte de modélisation à connaître avant d'écrire le schéma

PostgreSQL exige que **la colonne de partitionnement fasse partie de toute contrainte d'unicité**, y compris la clé primaire. Concrètement :

```sql
-- INCORRECT sur une table partitionnée par cree_le :
-- PRIMARY KEY (id)

-- CORRECT :
PRIMARY KEY (id, cree_le)
```

Ceci a deux conséquences à anticiper dès la conception, pas après :

1. **Les clés étrangères vers une table partitionnée doivent référencer la clé composite complète.** `messages_historique.message_id` référençant `messages.id` devra soit dupliquer `messages_cree_le` dans `messages_historique` pour former la FK composite, soit — option retenue ici — se passer de contrainte FK native et garantir l'intégrité au niveau applicatif (cas fréquent avec du partitionnement PostgreSQL ; documenté ci-dessous).
2. **Le mapping JPA/Hibernate doit exposer une clé composite** (`@IdClass` ou `@EmbeddedId`), même si l'identifiant fonctionnel côté domaine reste un simple UUID.

## 3. Migrations Flyway

### 3.1 Table parent partitionnée — `messages`

```sql
-- V{n}__creer_table_messages_partitionnee.sql

CREATE TABLE messages (
    id              UUID        NOT NULL DEFAULT gen_random_uuid(),
    conversation_id UUID        NOT NULL,
    auteur_id       UUID        NOT NULL,
    contenu_actuel  TEXT        NOT NULL,
    statut          VARCHAR(20) NOT NULL DEFAULT 'ENVOYE', -- ENVOYE | MODIFIE | SUPPRIME
    cree_le         TIMESTAMPTZ NOT NULL DEFAULT now(),
    modifie_le      TIMESTAMPTZ,
    PRIMARY KEY (id, cree_le)
) PARTITION BY RANGE (cree_le);

-- Index de recherche courants — créés sur la table parent,
-- PostgreSQL les propage automatiquement à chaque partition.
CREATE INDEX idx_messages_conversation_date
    ON messages (conversation_id, cree_le DESC);

CREATE INDEX idx_messages_auteur
    ON messages (auteur_id, cree_le DESC);
```

### 3.2 Table parent partitionnée — `messages_historique`

```sql
-- V{n+1}__creer_table_messages_historique_partitionnee.sql

CREATE TABLE messages_historique (
    id              UUID        NOT NULL DEFAULT gen_random_uuid(),
    message_id      UUID        NOT NULL,
    action          VARCHAR(20) NOT NULL, -- CREATION | MODIFICATION | SUPPRESSION
    contenu         TEXT        NOT NULL, -- contenu tel qu'il était AVANT l'action
    auteur_action_id UUID       NOT NULL,
    horodatage      TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (id, horodatage)
) PARTITION BY RANGE (horodatage);

CREATE INDEX idx_historique_message
    ON messages_historique (message_id, horodatage DESC);
```

> **Intégrité `message_id` → `messages.id` sans FK composite native** : comme évoqué en §2, une contrainte `FOREIGN KEY` classique n'est pas possible ici sans dupliquer `cree_le`. Deux options :
>
> - **Retenue par défaut** : pas de FK au niveau base, l'intégrité est garantie applicativement — toute écriture dans `messages_historique` transite exclusivement par le use case domaine (`ModifierMessageUseCase`/`SupprimerMessageUseCase`), jamais par un accès direct à la table. C'est cohérent avec l'architecture hexagonale déjà en place (le domaine, pas la base, porte les invariants).
> - **Alternative si une garantie DB stricte est requise** : dupliquer `messages_cree_le` en tant que colonne `message_cree_le` dans `messages_historique` et déclarer `FOREIGN KEY (message_id, message_cree_le) REFERENCES messages(id, cree_le)`. Fonctionnel mais alourdit le schéma pour un gain marginal ici — à réévaluer seulement si un besoin de contrainte forte au niveau base apparaît.

### 3.3 Configuration `pg_partman`

```sql
-- V{n+2}__configurer_pg_partman_messagerie.sql

SELECT partman.create_parent(
    p_parent_table      => 'public.messages',
    p_control           => 'cree_le',
    p_type              => 'range',
    p_interval          => '2 years',
    p_premake           => 2          -- pré-crée 2 partitions futures d'avance
);

SELECT partman.create_parent(
    p_parent_table      => 'public.messages_historique',
    p_control           => 'horodatage',
    p_type              => 'range',
    p_interval          => '2 years',
    p_premake           => 2
);

-- Configuration de la rétention : AUCUNE purge automatique.
-- Explicite ici pour éviter qu'une valeur par défaut de pg_partman
-- ne supprime silencieusement des partitions anciennes — contraire
-- à l'exigence de traçabilité permanente (cf. ADR-002).
UPDATE partman.part_config
SET retention = NULL,
    retention_keep_table = true
WHERE parent_table IN ('public.messages', 'public.messages_historique');
```

⚠️ **Point d'attention critique** : `pg_partman` propose par défaut des mécanismes de rétention qui **suppriment** les partitions anciennes. La configuration ci-dessus les désactive explicitement (`retention = NULL`). Toute évolution future de cette configuration doit être validée contre l'exigence de traçabilité — ne jamais activer de purge automatique sur ces deux tables sans une décision explicite documentée (nouvel ADR).

### 3.4 Planification de la maintenance

```sql
-- Si pg_cron est disponible sur l'instance :
SELECT cron.schedule(
    'pg_partman_maintenance_messagerie',
    '0 3 * * *',  -- tous les jours à 3h
    $$SELECT partman.run_maintenance_proc()$$
);
```

Si `pg_cron` n'est pas disponible (cas de certaines offres managées), planifier l'appel équivalent via un job externe (scheduler applicatif Spring `@Scheduled`, ou cron système exécutant `psql -c "SELECT partman.run_maintenance_proc();"`). Dans ce cas, ajouter une alerte de supervision si le job n'a pas tourné depuis plus de 48h — l'absence de partition future fait échouer les insertions, pas juste dégrader la performance.

## 4. Mapping JPA / Hibernate

```java
@Embeddable
public class MessageId implements Serializable {
    private UUID id;
    private Instant creeLe;
    // equals/hashCode obligatoires pour une clé composite
}

@Entity
@Table(name = "messages")
public class MessageJpaEntity {

    @EmbeddedId
    private MessageId id;

    private UUID conversationId;
    private UUID auteurId;
    private String contenuActuel;

    @Enumerated(EnumType.STRING)
    private StatutMessage statut;

    private Instant modifieLe;

    // cree_le est déjà exposé via id.creeLe — éviter de le dupliquer
    // en colonne séparée pour ne pas désynchroniser les deux valeurs.
}
```

Points d'attention spécifiques à Hibernate sur une table partitionnée :

- **Ne jamais laisser Hibernate générer l'identifiant côté séquence** (`@GeneratedValue(strategy = IDENTITY)` est incompatible avec une clé composite partition-aware) — génération UUID côté application ou `DEFAULT gen_random_uuid()` côté base, comme dans les migrations ci-dessus.
- **`ddl-auto` doit rester à `validate` en profil `prod`** (déjà le cas dans la configuration existante du projet) : Hibernate ne sait pas générer de `PARTITION BY RANGE` — le schéma partitionné est et reste porté exclusivement par les migrations Flyway.
- Le repository Spring Data (`MessageRepository extends JpaRepository<MessageJpaEntity, MessageId>`) fonctionne normalement avec une clé composite ; aucune adaptation particulière au-delà du type de clé.

## 5. Discipline de requêtage — condition de l'élagage de partitions

L'élagage de partitions (_partition pruning_) ne se déclenche **que** si la requête contient un prédicat exploitable sur la colonne de partitionnement (`cree_le` / `horodatage`). Règle à appliquer systématiquement dans les repositories/spécifications JPA de ce module :

```java
// À ÉVITER — balaie potentiellement toutes les partitions
List<MessageJpaEntity> findByConversationId(UUID conversationId);

// À PRÉFÉRER — permet l'élagage sur les requêtes bornées dans le temps
// (le cas d'usage réel : "les N derniers messages", "messages de telle période")
List<MessageJpaEntity> findByConversationIdAndCreeLeBetween(
        UUID conversationId, Instant debut, Instant fin);
```

En pratique, la pagination d'historique de conversation (déjà présente ailleurs dans le projet via `PageRequest`/`PageResult`) doit systématiquement porter une borne de date, même large (ex. "depuis la création de la conversation à aujourd'hui") plutôt qu'une requête sans aucun filtre temporel.

**Vérification en développement** : confirmer l'élagage via `EXPLAIN ANALYZE` sur une requête représentative — le plan doit lister uniquement les partitions concernées (`Seq Scan on messages_p2026_2027`, pas toutes les partitions existantes).

## 6. Runbook opérationnel

| Vérification                                                                            | Fréquence                                           | Action si anomalie                                                                                                                          |
| --------------------------------------------------------------------------------------- | --------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------- |
| Nombre de partitions futures pré-créées (`p_premake`)                                   | Hebdomadaire (alerte automatisée à privilégier)     | Vérifier que `run_maintenance_proc()` s'exécute bien (cron/pg_cron actif)                                                                   |
| Taille des partitions les plus récentes                                                 | Mensuelle                                           | Si croissance anormale, vérifier l'absence de boucle applicative générant des messages en masse (cf. rate limiting déjà en place sur l'API) |
| Confirmation qu'aucune politique de rétention `pg_partman` n'a été réactivée par erreur | À chaque modification de configuration `pg_partman` | Revalider contre l'exigence de traçabilité permanente avant toute mise en production d'un changement                                        |
| Requêtes lentes sur `messages`/`messages_historique` (via `pg_stat_statements`)         | Continue (monitoring existant)                      | Vérifier que les requêtes en cause portent bien un prédicat de date (cf. §5)                                                                |

## 7. Stratégie de test

- **Test d'intégration** (`@DataJpaTest` avec PostgreSQL réel — le partitionnement n'est pas supporté par H2, donc ces tests spécifiques ne peuvent pas tourner sur le profil `test` actuel du projet ; prévoir un profil dédié avec Testcontainers PostgreSQL) : vérifie que l'insertion, la lecture paginée et l'élagage fonctionnent sur un schéma partitionné réel.
- **Test de non-régression sur la configuration `pg_partman`** : vérifier après chaque déploiement que `retention IS NULL` sur les deux tables — un simple `SELECT` de contrôle en pipeline CI/CD contre un environnement de recette suffit à prévenir une réactivation accidentelle de purge.
