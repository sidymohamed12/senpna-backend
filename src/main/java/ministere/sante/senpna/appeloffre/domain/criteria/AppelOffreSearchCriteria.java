package ministere.sante.senpna.appeloffre.domain.criteria;

import ministere.sante.senpna.appeloffre.domain.valueobject.StatutAppelOffre;

public record AppelOffreSearchCriteria(String recherche, StatutAppelOffre statut) {
}
