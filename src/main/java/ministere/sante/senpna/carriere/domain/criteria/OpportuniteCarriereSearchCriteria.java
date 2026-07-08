package ministere.sante.senpna.carriere.domain.criteria;

import ministere.sante.senpna.carriere.domain.valueobject.StatutOpportunite;
import ministere.sante.senpna.carriere.domain.valueobject.TypeContrat;

public record OpportuniteCarriereSearchCriteria(
                String recherche,
                TypeContrat typeContrat,
                StatutOpportunite statut,
                boolean publicOnly) {
}
