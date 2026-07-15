package ministere.sante.senpna.commandeachat.domain.criteria;

import ministere.sante.senpna.commandeachat.domain.valueobject.StatutCommandeAchat;

public record CommandeAchatSearchCriteria(String recherche, StatutCommandeAchat statut) {
}
