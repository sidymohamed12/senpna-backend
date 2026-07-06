package ministere.sante.senpna.projet.application.service;

import ministere.sante.senpna.projet.domain.exception.CategorieProjetInvalideException;
import ministere.sante.senpna.projet.domain.exception.StatutProjetInvalideException;
import ministere.sante.senpna.projet.domain.valueobject.CategorieProjet;
import ministere.sante.senpna.projet.domain.valueobject.StatutProjet;

import org.springframework.stereotype.Component;

@Component
public class ProjetCommandMapper {

    public CategorieProjet versCategorie(String valeur) {
        if (valeur == null || valeur.isBlank()) {
            throw new CategorieProjetInvalideException("null");
        }
        try {
            return CategorieProjet.valueOf(valeur.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new CategorieProjetInvalideException(valeur);
        }
    }

    /** Tolérant au null/vide — utilisé pour les filtres de recherche optionnels. */
    public CategorieProjet versCategorieOptionnelle(String valeur) {
        return (valeur == null || valeur.isBlank()) ? null : versCategorie(valeur);
    }

    /** Tolérant au null/vide — utilisé pour les filtres de recherche optionnels. */
    public StatutProjet versStatutOptionnel(String valeur) {
        if (valeur == null || valeur.isBlank()) {
            return null;
        }
        try {
            return StatutProjet.valueOf(valeur.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new StatutProjetInvalideException(valeur);
        }
    }
}
