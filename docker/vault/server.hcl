# ═══════════════════════════════════════════════════════════════════
# Configuration du serveur Vault auto-hébergé (profil "vault-server").
#
# ⚠️ Adaptée à un déploiement mono-nœud simple. Pour un système critique
# (chaîne d'approvisionnement pharmaceutique nationale), privilégiez à
# terme : HCP Vault (managé) ou un cluster Vault HA avec stockage Raft/
# Consul + auto-unseal via KMS cloud. Le mode "file" mono-nœud ci-dessous
# n'a pas de haute disponibilité et nécessite un unseal manuel après
# chaque redémarrage (cf. BOOTSTRAP.md).
# ═══════════════════════════════════════════════════════════════════

storage "file" {
  path = "/vault/file"
}

listener "tcp" {
  address = "0.0.0.0:8200"
  # TLS désactivé ici car Vault n'est joignable que depuis le réseau
  # Docker interne "backend" (jamais publié sur l'hôte). Si le serveur
  # devient joignable depuis en dehors de ce réseau de confiance,
  # activez tls_cert_file / tls_key_file ici.
  tls_disable = "true"
}

api_addr = "http://vault:8200"
ui       = true
