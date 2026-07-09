# Vault — mise en route (prod)

Ce guide couvre : activer le moteur de secrets, y écrire les valeurs,
créer l'AppRole utilisé par `vault-agent`, et lancer le compose.

Les commandes supposent la CLI `vault` installée localement (ou
`docker compose exec vault vault ...` si vous préférez tout faire depuis
le conteneur).

## 0. Démarrer Vault (si auto-hébergé, profil "vault-server")

```bash
docker compose -f docker-compose.prod.yml --profile vault-server up -d vault
export VAULT_ADDR=http://localhost:8200   # si le port est temporairement exposé pour le bootstrap
```

### Initialisation (une seule fois dans la vie du volume `senpna_vault_data`)

```bash
vault operator init -key-shares=5 -key-threshold=3
```

⚠️ Conservez les 5 "unseal keys" et le "initial root token" **hors du
repo**, dans un coffre-fort séparé (password manager d'équipe, coffre
physique...). Quiconque réunit 3 des 5 clés peut désceller Vault.

### Descellement (à refaire après CHAQUE redémarrage du conteneur vault)

```bash
vault operator unseal <clé 1>
vault operator unseal <clé 2>
vault operator unseal <clé 3>
```

C'est la contrepartie opérationnelle d'un Vault mono-nœud auto-hébergé :
pas d'auto-unseal sans KMS cloud. Pour un vrai système critique, prévoyez
soit un auto-unseal (AWS/GCP/Azure KMS), soit HCP Vault (managé).

## 1. Authentification admin (une fois, avec le root token)

```bash
vault login <initial root token>
```

## 2. Activer le moteur KV v2 et y écrire les secrets

```bash
vault secrets enable -path=secret kv-v2

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
  mail_host="smtp.votre-fournisseur.sn" mail_port="587" \
  mail_username="no-reply@senpharmaflow.gouv.sn" \
  mail_password="<mot de passe SMTP>" \
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
vault kv put secret/senpna/db \
  postgres_password="<même valeur que db_password ci-dessus>" \
  redis_password="<même valeur que redis_password ci-dessus>"
```

## 3. Politique + AppRole pour `vault-agent`

```bash
vault policy write senpna-policy docker/vault/policy/senpna-policy.hcl

vault auth enable approle

vault write auth/approle/role/senpna-app \
  token_policies="senpna-policy" \
  token_ttl=1h \
  token_max_ttl=4h \
  secret_id_ttl=10m \
  secret_id_num_uses=1

# role_id : pas un secret, peut être noté quelque part de pratique
vault read auth/approle/role/senpna-app/role-id

# secret_id : généré à usage unique via response wrapping, à consommer
# immédiatement au démarrage du compose (TTL d'enveloppe court : 60s ici)
vault write -wrap-ttl=60s -f auth/approle/role/senpna-app/secret-id
```

La commande précédente renvoie un `wrapping_token`. Pour en extraire le
vrai `secret_id` juste avant de démarrer le compose :

```bash
vault unwrap <wrapping_token>   # affiche le secret_id, à usage unique
```

## 4. Démarrer la stack applicative

```bash
export VAULT_ADDR=http://vault:8200        # ou l'adresse de votre cluster externe
export VAULT_ROLE_ID=<role_id de l'étape 3>
export VAULT_SECRET_ID=<secret_id déballé à l'instant>

docker compose -f docker-compose.prod.yml --profile db up -d --build
# (retirez --profile db si Postgres/Redis sont managés en externe,
#  retirez --profile vault-server si Vault est un cluster externe)
```

`VAULT_ROLE_ID`/`VAULT_SECRET_ID` ne vivent que dans cette session shell
(ou dans les secrets chiffrés de votre pipeline CI/CD) — jamais dans un
fichier du repo.

## Rotation d'un secret

1. `vault kv put secret/senpna/app jwt_secret="<nouvelle valeur>" ...` (en
   réécrivant tous les champs, `kv put` remplace la version entière — vous
   pouvez aussi utiliser `vault kv patch` pour ne changer qu'un champ).
2. `docker compose -f docker-compose.prod.yml restart app` — l'app relit
   les nouvelles valeurs au redémarrage (ce n'est pas du hot-reload, cf.
   note dans `docker/vault/agent.hcl`).

## Aller plus loin

- **Rotation à chaud sans redémarrage** : ajoutez la dépendance
  `spring-cloud-vault-config` côté application et `spring.config.import=vault://`
  — Spring rafraîchit alors les `@ConfigurationProperties` annotées
  `@RefreshScope` sans redémarrer le process. Non fait ici pour ne pas
  toucher au code existant.
- **Secrets dynamiques Postgres** (identifiants générés à la demande,
  expirant automatiquement) : le moteur `database` de Vault peut
  remplacer `db_password` statique par des creds éphémères — utile si
  vous voulez éliminer même la notion de "mot de passe fixe".
- **Haute disponibilité Vault** : Raft storage multi-nœuds ou HCP Vault,
  pour ne plus dépendre d'un unseal manuel mono-nœud.
