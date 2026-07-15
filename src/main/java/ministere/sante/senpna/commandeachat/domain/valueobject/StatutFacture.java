package ministere.sante.senpna.commandeachat.domain.valueobject;

/**
 * Statut d'une facture fournisseur (cf. doc. métier « Soumission de
 * Factures » : dépôt et suivi, workflow de validation, suivi du statut de
 * paiement).
 */
public enum StatutFacture {
    SOUMISE,
    VALIDEE,
    REJETEE,
    PAYEE
}
