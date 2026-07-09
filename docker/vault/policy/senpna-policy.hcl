# Politique Vault pour l'AppRole "senpna-app" — lecture seule, chemins
# strictement scopés (principe du moindre privilège).

path "secret/data/senpna/app" {
  capabilities = ["read"]
}

path "secret/data/senpna/db" {
  capabilities = ["read"]
}

# Explicitement rien d'autre : pas de "list", pas d'écriture, pas
# d'accès au reste de l'arborescence secret/senpna/*.
