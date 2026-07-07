package ministere.sante.senpna.carriere.application.service;

import ministere.sante.senpna.carriere.domain.exception.CiviliteInvalideException;
import ministere.sante.senpna.carriere.domain.valueobject.Civilite;

import org.springframework.stereotype.Component;

@Component
public class CandidatureCommandMapper {

    public Civilite versCivilite(String valeur) {
        if (valeur == null || valeur.isBlank()) {
            throw new CiviliteInvalideException("null");
        }
        String normalise = valeur.trim().toUpperCase().replace(".", "");
        try {
            return Civilite.valueOf(normalise);
        } catch (IllegalArgumentException e) {
            throw new CiviliteInvalideException(valeur);
        }
    }
}
