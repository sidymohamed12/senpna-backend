# ADR-004 — Architecture de la messagerie temps réel (chat PRA/PNA/Fournisseur/Pharmacie)

|               |                                                                              |
| ------------- | ---------------------------------------------------------------------------- |
| **Statut**    | Accepté                                                                      |
| **Date**      | 2026-07-20                                                                   |
| **Décideurs** | Équipe backend / architecture, validé avec le porteur produit                |
| **Périmètre** | Module `messagerie` (bounded context ajouté au monolithe modulaire existant) |

## Contexte

La plateforme SEN PharmaFlow doit intégrer une messagerie interne entre acteurs de la chaîne d'approvisionnement pharmaceutique, avec les caractéristiques suivantes, actées après plusieurs itérations de cadrage :

- **Communication restreinte à une matrice fixe** : PRA↔PRA, PRA↔PNA, Fournisseur↔PNA, Pharmacie↔PRA (rattachement régional). Pas de messagerie ouverte à tout utilisateur authentifié.
- **Conversations directes et groupes**, taille de groupe plafonnée à **30 participants**, avec gestion des membres (ajout, retrait, rôles PROPRIÉTAIRE/ADMIN/MEMBRE).
- **Indicateur de frappe** ("typing") requis.
- **Modification et suppression de message** par l'auteur, dans une fenêtre de **30 minutes**.
- **Traçabilité permanente** : aucune donnée n'est réellement perdue, y compris après modification/suppression côté utilisateur (cf. ADR-005 pour la conséquence sur le stockage).
- **Échelle cible** : **moins de 500 utilisateurs simultanément connectés**, déploiement national mais nombre d'acteurs institutionnels borné (PNA, PRA régionales, pharmacies, fournisseurs).
- **Contrainte de souveraineté** : plateforme `.gouv.sn`, données de santé/chaîne d'approvisionnement — exclusion de toute solution SaaS tierce hébergeant les données de conversation hors de l'infrastructure maîtrisée.
- **Capacité opérationnelle** : l'équipe DevOps peut opérer un composant d'infrastructure supplémentaire (message broker) en plus de PostgreSQL et Redis déjà en production.
- **Architecture existante** : monolithe modulaire Spring Boot 3.5.6 / Java 21, architecture hexagonale (Clean Architecture), PostgreSQL, Redis (cache + rate limiting), JWT stateless, pas de session HTTP serveur.

La question à trancher : quel transport temps réel et quelle stratégie de diffusion multi-instance pour livrer les messages, les événements de groupe et les indicateurs de frappe à des clients potentiellement connectés à une instance applicative différente de celle qui traite l'écriture.

## Décision

**Transport client : WebSocket (protocole STOMP), via `spring-boot-starter-websocket`.**

**Diffusion multi-instance, répartie selon la nature du signal :**

| Type de signal                                                                   | Canal de diffusion                                                        | Justification                                                                                                                                                                                                                 |
| -------------------------------------------------------------------------------- | ------------------------------------------------------------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Messages de chat, événements de groupe (ajout/retrait/renommage de participant)  | **RabbitMQ**, via `enableStompBrokerRelay` (relais STOMP natif de Spring) | Fiabilité et ordre requis ; routage par file dédiée sans code de relais à écrire/maintenir ; livraison possible à la reconnexion même si le destinataire était hors ligne au moment de l'envoi (file durable par utilisateur) |
| Indicateur de frappe, présence (hors périmètre immédiat mais canal réutilisable) | **Redis Pub/Sub**                                                         | Signal éphémère, haute fréquence, aucune garantie de livraison requise — ne doit pas transiter par un broker fiable qui n'apporte rien ici                                                                                    |

**Persistance** : la source de vérité reste PostgreSQL, écrite de manière synchrone et transactionnelle avant toute diffusion temps réel (« persister d'abord, diffuser ensuite » — jamais l'inverse). Le canal temps réel est un mécanisme de confort de livraison, pas la source de vérité ; un client qui se reconnecte recharge l'historique via un appel REST paginé classique.

## Alternatives considérées

### Polling / long polling REST

Rejeté d'emblée : latence perçue inadaptée à un chat, charge réseau inutile à l'échelle visée, ne permet pas nativement le typing.

### SSE (Server-Sent Events) + REST pour l'écriture

C'était le choix initial pour un besoin de messagerie contextualisée à un objet métier (fil de discussion attaché à un appel d'offres). Il a été écarté au moment où le périmètre a évolué vers une vraie messagerie avec groupes et indicateur de frappe : SSE est unidirectionnel (serveur → client), et le typing exige des signaux fréquents client → serveur, ce que SSE ne gère pas nativement (il faudrait multiplier les appels REST, perdant l'intérêt du SSE).

### Solutions SaaS managées (Pusher, Ably, Stream Chat, Firebase…)

Écartées sans ambiguïté : hébergement hors infrastructure souveraine, inadapté à une plateforme ministérielle traitant des données de santé/chaîne d'approvisionnement.

### WebSocket + relais maison via Redis Pub/Sub (sans broker dédié)

Techniquement viable à l'échelle visée (moins de 500 utilisateurs) — Redis Pub/Sub aurait suffi en débit. Écarté au profit de RabbitMQ pour deux raisons concrètes, indépendantes du débit :

1. **Pas de code de routage à écrire et maintenir** — `enableStompBrokerRelay` est le mécanisme officiellement supporté par Spring pour ce problème, avec ses cas limites déjà couverts par la communauté.
2. **Livraison différée fiable** — un message envoyé à un utilisateur hors ligne au moment de l'envoi est mis en file durable côté RabbitMQ et livré à la reconnexion, sans logique applicative supplémentaire. Avec Redis Pub/Sub seul, ce cas devrait être compensé par un rechargement REST côté client (fonctionnel, mais moins immédiat).

Ce choix a été validé explicitement par l'équipe : l'ajout de RabbitMQ n'est pas motivé par un besoin de débit (l'échelle actuelle ne le justifierait pas seule), mais par ces deux gains opérationnels concrets, jugés suffisants au regard de la capacité DevOps disponible.

### Apache Kafka

Écarté. Kafka est un log distribué optimisé pour un débit très élevé, plusieurs groupes de consommateurs indépendants relisant le même flux à des vitesses différentes, et un rejeu sur rétention longue. Aucune de ces propriétés n'est le problème posé par la messagerie :

- Le débit visé (quelques messages par utilisateur et par minute, < 500 utilisateurs) est plusieurs ordres de grandeur en dessous du seuil où Kafka apporte un avantage.
- Il n'existe qu'un seul type de consommateur du canal temps réel (les sessions WebSocket des destinataires) — pas de fan-out vers des services hétérogènes.
- Le modèle de log partitionné de Kafka n'est pas conçu pour router un message vers _une_ session applicative précise sur _une_ instance donnée : il faudrait réintroduire une couche de routage applicative maison, ce que RabbitMQ fournit nativement.
- Un `consumer group rebalance` Kafka (déclenché à chaque redémarrage d'instance) interrompt temporairement la consommation sur les partitions concernées — un comportement indésirable pour un canal de chat temps réel, alors que RabbitMQ n'a pas cet effet de bord.

Kafka redeviendrait pertinent si un bus d'événements transverse à l'ensemble de la plateforme (audit long terme multi-domaines, alimentation d'un futur entrepôt de données, découplage vers des services indépendants) était décidé — mais ce serait un chantier d'architecture globale distinct de la messagerie, à traiter comme tel le cas échéant, en complément de RabbitMQ (qui resterait sur le chemin critique temps réel) et non à sa place.

## Conséquences

**Positives**

- Bidirectionnel natif sur une seule connexion (messages + typing), sans bricolage de canaux complémentaires.
- Aucune session collante (« sticky session ») requise au niveau du load balancer : RabbitMQ route vers l'instance qui détient la session active, quelle qu'elle soit.
- Le code de routage/relais multi-instance est délégué à un mécanisme Spring éprouvé plutôt que réimplémenté.
- Livraison différée fiable aux utilisateurs hors ligne au moment de l'envoi.
- Le choix d'infrastructure reste isolé du domaine métier via le port `MessageBroadcastPort` (inversion de dépendance) — une bascule future (Redis seul à la baisse, ou ajout d'un canal d'audit Kafka en parallèle à la hausse) n'impacte aucun cas d'usage.

**Négatives / coûts à assumer**

- Nouveau composant d'infrastructure à opérer, sécuriser et monitorer (cluster RabbitMQ, idéalement 3 nœuds en HA avec quorum queues) — nouveau périmètre à intégrer aux futurs audits de sécurité (authentification inter-broker, TLS, ACLs).
- Configuration du load balancer à adapter pour le support WebSocket (upgrade `Connection: Upgrade`, timeouts longs) — impact limité mais réel sur l'infrastructure réseau existante.
- Complexité de test légèrement supérieure à un flux REST classique (tests d'intégration nécessitant un broker STOMP, ou une double abstraction de test).

## Décisions liées

- ADR-005 — Stratégie de partitionnement PostgreSQL pour les tables de messagerie (conséquence directe de l'exigence de traçabilité permanente).
