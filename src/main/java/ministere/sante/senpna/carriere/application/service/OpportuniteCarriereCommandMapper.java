package ministere.sante.senpna.carriere.application.service;

import ministere.sante.senpna.carriere.domain.exception.StatutOpportuniteInvalideException;
import ministere.sante.senpna.carriere.domain.exception.TypeContratInvalideException;
import ministere.sante.senpna.carriere.domain.valueobject.StatutOpportunite;
import ministere.sante.senpna.carriere.domain.valueobject.TypeContrat;

import org.springframework.stereotype.Component;

@Component
public class OpportuniteCarriereCommandMapper {

    public TypeContrat versTypeContrat(String valeur) {
        if (valeur == null || valeur.isBlank()) {
            throw new TypeContratInvalideException("null");
        }
        try {
            return TypeContrat.valueOf(valeur.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new TypeContratInvalideException(valeur);
        }
    }

    /** Tolérant au {@code null}/vide — utilisé pour les filtres de recherche optionnels. */
    public TypeContrat versTypeContratOptionnel(String valeur) {
        return (valeur == null || valeur.isBlank()) ? null : versTypeContrat(valeur);
    }

    /** Tolérant au {@code null}/vide — utilisé pour les filtres de recherche optionnels. */
    public StatutOpportunite versStatutOptionnel(String valeur) {
        if (valeur == null || valeur.isBlank()) {
            return null;
        }
        try {
            return StatutOpportunite.valueOf(valeur.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new StatutOpportuniteInvalideException(valeur);
        }
    }
}
