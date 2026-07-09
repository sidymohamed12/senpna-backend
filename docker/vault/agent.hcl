# ═══════════════════════════════════════════════════════════════════
# Vault Agent — configuration (aucun secret ici, committable en l'état)
#
# Adresse du serveur Vault : PAS codée en dur. Vault Agent lit la
# variable d'environnement standard VAULT_ADDR (passée au conteneur
# vault-agent dans docker-compose.prod.yml), qu'il s'agisse du service
# "vault" auto-hébergé du compose ou d'un cluster Vault externe/managé
# (HCP Vault, Vault HA…). Rien à changer ici dans ce dernier cas.
# ═══════════════════════════════════════════════════════════════════

auto_auth {
  method "approle" {
    mount_path = "auth/approle"
    config = {
      role_id_file_path   = "/vault/runtime/role_id"
      secret_id_file_path = "/vault/runtime/secret_id"
      # Le secret_id n'est utile qu'une fois : supprimé du volume tmpfs
      # dès qu'il a été consommé pour obtenir le token d'agent.
      remove_secret_id_file_after_reading = true
    }
  }

  sink "file" {
    config = {
      path = "/vault/runtime/agent-token"
    }
  }
}

# ── Secrets applicatifs (toujours rendus) ─────────────────────────────
# Note : ces secrets sont injectés comme variables d'environnement au
# démarrage du process Java, qui ne les relit jamais après coup. Une
# rotation dans Vault ne prend donc effet qu'au prochain redémarrage du
# conteneur "app" (`docker compose restart app`) — ce n'est pas du
# hot-reload. Si vous avez besoin de rotation à chaud sans redémarrage,
# la vraie solution est Spring Cloud Vault côté application (voir
# docker/vault/BOOTSTRAP.md, section "Aller plus loin").
template {
  source      = "/vault/config/templates/app.env.ctmpl"
  destination = "/vault/secrets/app.env"
  perms       = "0444"
}

# ── Secrets Postgres/Redis auto-hébergés (profil "db" uniquement) ─────
# Sans effet si vous n'utilisez pas --profile db (base/redis managés).
template {
  source      = "/vault/config/templates/db.env.ctmpl"
  destination = "/vault/secrets/db.env"
  perms       = "0444"
}

# Fichier valeur brute (sans KEY=VALUE) pour l'image Postgres officielle,
# qui sait nativement lire POSTGRES_PASSWORD_FILE.
template {
  source      = "/vault/config/templates/postgres_password.txt.ctmpl"
  destination = "/vault/secrets/postgres_password.txt"
  perms       = "0444"
}
