# Vault — mise en route (prod)

Ce guide a été mis à jour après un test complet en conditions réelles
et corrige plusieurs pièges qui ne sont pas évidents à la première
lecture de la doc Vault.

## Prérequis (déjà en place dans les fichiers livrés)

- `docker-compose.prod.yml` déclare `VAULT_ROLE_ID`/`VAULT_SECRET_ID`
  sous `environment:` du service `vault-agent` (pas seulement utilisés
  dans l'`entrypoint`) — sans ça, l'agent échoue silencieusement même
  si vous exportez les bonnes variables dans votre shell.
- `docker/vault/server.hcl` utilise `storage "file" { path = "/vault/file" }`
  (pas `/vault/data`) — c'est le seul chemin que l'image officielle
  Vault "chown" automatiquement pour l'utilisateur non-root. Un autre
  chemin donne `permission denied` au moment de `vault operator init`.
- Le `Dockerfile` de l'app se termine par
  `ENTRYPOINT ["java", "-jar", "app.jar"]`, **et** la commande du
  service `app` dans `docker-compose.prod.yml` fait `exec java -jar app.jar`
  (pas `JarLauncher`) — l'extraction Spring Boot en couches
  (`--layers`) de ce projet produit un `application/app.jar` intact,
  pas des classes éclatées, donc `JarLauncher` échoue avec
  `ClassNotFoundException`.

## 0. Démarrer Vault

```bash
docker compose -f docker-compose.prod.yml --profile vault-server up -d vault
```

⚠️ Le réseau `backend` est `internal: true` — **aucun port n'est ni ne
sera jamais publié sur l'hôte**, même en en ajoutant un dans le compose.
Toutes les commandes `vault` ci-dessous passent donc par
`docker compose exec`, jamais par un `VAULT_ADDR` pointant sur
`localhost` depuis votre machine.

Vault écoute en HTTP simple (TLS désactivé, réseau interne de
confiance) — la CLI Vault récente suppose `https://` par défaut si
`VAULT_ADDR` n'est pas fixé, d'où le
`-e VAULT_ADDR=http://127.0.0.1:8200` répété à chaque commande.

### Initialisation (une seule fois dans la vie du volume `senpna_vault_data`)

```bash
docker compose -f docker-compose.prod.yml exec -e VAULT_ADDR=http://127.0.0.1:8200 vault \
  vault operator init -key-shares=5 -key-threshold=3
```

⚠️ **Conservez les 5 unseal keys + le root token hors du repo**, dans
un gestionnaire de mots de passe séparé. Si vous relancez `down -v` ou
recréez le volume `senpna_vault_data` par erreur, ces clés deviennent
inutiles et il faut tout réinitialiser depuis cette étape.

### Descellement (à refaire après CHAQUE redémarrage du conteneur vault)

```bash
docker compose -f docker-compose.prod.yml exec -e VAULT_ADDR=http://127.0.0.1:8200 vault \
  vault operator unseal <clé 1>
docker compose -f docker-compose.prod.yml exec -e VAULT_ADDR=http://127.0.0.1:8200 vault \
  vault operator unseal <clé 2>
docker compose -f docker-compose.prod.yml exec -e VAULT_ADDR=http://127.0.0.1:8200 vault \
  vault operator unseal <clé 3>
```

C'est la contrepartie opérationnelle d'un Vault mono-nœud auto-hébergé :
pas d'auto-unseal sans KMS cloud. Pour un vrai système critique, prévoyez
soit un auto-unseal (AWS/GCP/Azure KMS), soit HCP Vault (managé).

## 1. Authentification admin (une fois, avec le root token)

```bash
docker compose -f docker-compose.prod.yml exec -e VAULT_ADDR=http://127.0.0.1:8200 vault \
  vault login <initial root token>
```

## 2. Activer le moteur KV v2 et y écrire les secrets

```bash
docker compose -f docker-compose.prod.yml exec -e VAULT_ADDR=http://127.0.0.1:8200 vault \
  vault secrets enable -path=secret kv-v2

docker compose -f docker-compose.prod.yml exec -e VAULT_ADDR=http://127.0.0.1:8200 vault \
  vault kv put secret/senpna/app \
  server_port="8080" \
  db_url="jdbc:postgresql://postgres:5432/senpna" \
  db_username="senpna_app" \
  db_password="<mot de passe fort>" \
  redis_host="redis" \
  redis_port="6379" \
  redis_password="<mot de passe fort>" \
  redis_ssl_enabled="false" \
  jwt_secret="<>= 256 bits, ex: openssl rand -base64 48>" \
  jwt_access_ttl="PT15M" \
  jwt_refresh_ttl="P7D" \
  cors_allowed_origins="https://senpharmaflow.gouv.sn" \
  rate_limit_auth="10" rate_limit_auth_window="60000" \
  rate_limit_api="120" rate_limit_api_window="60000" \
  mail_host="smtp.gmail.com" mail_port="587" \
  mail_username="no-reply@senpharmaflow.gouv.sn" \
  mail_password="<mot de passe d'application SMTP>" \
  mail_from="no-reply@senpharmaflow.gouv.sn" \
  mail_from_name="SEN PharmaFlow" \
  storage_provider="r2" \
  storage_r2_account_id="<>" storage_r2_access_key="<>" \
  storage_r2_secret_key="<>" storage_r2_public_url="https://pub-xxxxx.r2.dev" \
  storage_bucket="sen-pna" \
  cache_medicament_ttl="PT10M" cache_lot_ttl="PT5M" cache_stock_ttl="PT2M" \
  cache_mouvement_ttl="PT6H" cache_user_ttl="PT5H" cache_catalogue_ttl="PT1M" \
  cache_actualite_ttl="PT24H" cache_projet_ttl="PT24H"

# Uniquement si vous utilisez --profile db (Postgres/Redis auto-hébergés) :
docker compose -f docker-compose.prod.yml exec -e VAULT_ADDR=http://127.0.0.1:8200 vault \
  vault kv put secret/senpna/db \
  postgres_password="<même valeur que db_password ci-dessus>" \
  redis_password="<même valeur que redis_password ci-dessus>"
```

`db_username` doit correspondre au défaut de `docker-compose.prod.yml`
(`POSTGRES_USER: ${POSTGRES_USER:-senpna_app}`), sinon changez l'un des
deux pour qu'ils correspondent.

## 3. Politique + AppRole pour `vault-agent`

Le fichier de policy est sur votre machine, pas dans le conteneur : on
le transmet via `stdin` (`-T` sur `exec`, sinon Compose bufferise et
rien n'arrive).

```bash
cat docker/vault/policy/senpna-policy.hcl | \
  docker compose -f docker-compose.prod.yml exec -T -e VAULT_ADDR=http://127.0.0.1:8200 vault \
  vault policy write senpna-policy -

docker compose -f docker-compose.prod.yml exec -e VAULT_ADDR=http://127.0.0.1:8200 vault \
  vault auth enable approle

docker compose -f docker-compose.prod.yml exec -e VAULT_ADDR=http://127.0.0.1:8200 vault \
  vault write auth/approle/role/senpna-app \
  token_policies="senpna-policy" \
  token_ttl=1h token_max_ttl=4h \
  secret_id_ttl=10m secret_id_num_uses=1

# role_id : pas un secret, peut être noté quelque part de pratique
docker compose -f docker-compose.prod.yml exec -e VAULT_ADDR=http://127.0.0.1:8200 vault \
  vault read -field=role_id auth/approle/role/senpna-app/role-id
```

## 4. Générer le secret_id et démarrer la stack — dans la même séquence

Le `secret_id` expire en 10 minutes et n'est utilisable qu'une fois.
**Générez-le et lancez la stack dans la foulée**, sans copier-coller
manuel entre deux terminaux (source d'erreurs constatée en test) :

```bash
export VAULT_ADDR=http://vault:8200
export VAULT_ROLE_ID=<role_id de l'étape 3>

export VAULT_SECRET_ID=$(docker compose -f docker-compose.prod.yml exec -e VAULT_ADDR=http://127.0.0.1:8200 vault \
  vault write -f -field=secret_id auth/approle/role/senpna-app/secret-id)

echo "ROLE_ID=$VAULT_ROLE_ID"
echo "SECRET_ID=$VAULT_SECRET_ID"   # vérifiez que les deux affichent une vraie valeur avant de continuer

docker compose -f docker-compose.prod.yml --profile db up -d --build
# (retirez --profile db si Postgres/Redis sont managés en externe ;
#  --profile vault-server déjà lancé à l'étape 0, pas la peine de le
#  remettre à moins d'avoir aussi arrêté vault)
```

## 5. Vérifier

```bash
docker compose -f docker-compose.prod.yml ps
docker compose -f docker-compose.prod.yml logs vault-agent
docker compose -f docker-compose.prod.yml exec app wget -qO- http://localhost:8080/actuator/health
```

`app` n'expose aucun port sur l'hôte (seul Nginx est censé l'être, une
fois configuré avec de vrais certificats TLS) : on teste toujours
depuis l'intérieur du conteneur.

## Pièges rencontrés en test — comment les éviter

| Symptôme                                                                  | Cause                                                                              | Évité en...                                                                                                                |
| ------------------------------------------------------------------------- | ---------------------------------------------------------------------------------- | -------------------------------------------------------------------------------------------------------------------------- |
| `permission denied` sur `vault operator init`                             | Chemin de stockage `/vault/data` non préparé par l'image                           | Utiliser `/vault/file` dans `server.hcl` (déjà corrigé)                                                                    |
| `vault-agent` boucle sur `VAULT_ROLE_ID manquant` alors qu'il est exporté | Var non déclarée dans `environment:` du service                                    | Déjà corrigé — vérifiez que vous avez bien la dernière version de `docker-compose.prod.yml`                                |
| `service "vault" is not running` en pleine génération de `secret_id`      | Le conteneur vault a été supprimé/recréé entre-temps (volume neuf = tout perdu)    | Ne jamais faire `down -v` ni `--force-recreate vault` sans le vouloir — `docker compose ps` avant tout `exec`              |
| `Could not find or load main class ... JarLauncher`                       | L'extraction `--layers` de ce projet garde `app.jar` intact                        | `ENTRYPOINT`/commande = `java -jar app.jar` (déjà corrigé, dans le Dockerfile ET dans la commande du compose)              |
| `Connection to localhost:5432 refused` avec Vault en place                | Mauvais profil Spring actif (`dev` au lieu de `prod`)                              | `SPRING_PROFILES_ACTIVE=prod` obligatoire pour que les `${DB_URL}` etc. de Vault soient pris en compte                     |
| `/actuator/health` → `DOWN` sur mail                                      | Health indicator SMTP teste une vraie connexion ; secret factice/mauvais port      | Vérifier `mail_host`/`mail_port` dans le secret — ne jamais désactiver l'indicator en vraie prod, corriger la vraie valeur |
| Secret_id "déjà consommé" à la 2ᵉ tentative                               | TTL 10 min + usage unique                                                          | Régénérer un `secret_id` frais juste avant CHAQUE tentative de démarrage, dans la même séquence de commandes que le `up`   |
| `vault-agent` ne prend pas en compte un changement du compose             | Compose ne recrée pas un conteneur si le changement n'est pas détecté correctement | `--force-recreate <service>` explicitement après toute modif du fichier                                                    |

## Rotation d'un secret

1. `vault kv put secret/senpna/app jwt_secret="<nouvelle valeur>" ...`
   (en réécrivant tous les champs — `kv put` remplace la version
   entière ; `vault kv patch` pour ne changer qu'un champ).
2. `docker compose -f docker-compose.prod.yml restart app` — l'app
   relit les nouvelles valeurs au redémarrage (pas de hot-reload, cf.
   note dans `docker/vault/agent.hcl`).

## Aller plus loin

- **Rotation à chaud sans redémarrage** : ajoutez la dépendance
  `spring-cloud-vault-config` côté application et
  `spring.config.import=vault://` — Spring rafraîchit alors les
  `@ConfigurationProperties` annotées `@RefreshScope` sans redémarrer
  le process. Non fait ici pour ne pas toucher au code existant — le
  sidecar Vault Agent garde le code totalement agnostique du
  fournisseur de secrets (principe clean architecture : la
  configuration ne dépend pas du fournisseur).
- **Secrets dynamiques Postgres** (identifiants générés à la demande,
  expirant automatiquement) : le moteur `database` de Vault peut
  remplacer `db_password` statique par des creds éphémères.
- **Haute disponibilité Vault** : Raft storage multi-nœuds ou HCP
  Vault, pour ne plus dépendre d'un unseal manuel mono-nœud.
- **Certbot / TLS Nginx** : ce guide ne couvre pas l'obtention des
  certificats Let's Encrypt — un service `certbot` manque encore dans
  `docker-compose.prod.yml` si vous voulez que ce compose gère le TLS
  lui-même plutôt qu'un load balancer en amont.
