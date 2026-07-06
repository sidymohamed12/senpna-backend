package ministere.sante.senpna.actualite.application.service;

import ministere.sante.senpna.actualite.domain.command.ActualiteCommands.MediaInput;
import ministere.sante.senpna.actualite.domain.exception.CategorieActualiteInvalideException;
import ministere.sante.senpna.actualite.domain.exception.StatutActualiteInvalideException;
import ministere.sante.senpna.actualite.domain.exception.TypeMediaInvalideException;
import ministere.sante.senpna.actualite.domain.model.ActualiteMedia;
import ministere.sante.senpna.actualite.domain.valueobject.CategorieActualite;
import ministere.sante.senpna.actualite.domain.valueobject.StatutActualite;
import ministere.sante.senpna.actualite.domain.valueobject.TypeMedia;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ActualiteCommandMapper {

    public CategorieActualite versCategorie(String valeur) {
        if (valeur == null || valeur.isBlank()) {
            throw new CategorieActualiteInvalideException("null");
        }
        try {
            return CategorieActualite.valueOf(valeur.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new CategorieActualiteInvalideException(valeur);
        }
    }

    public List<ActualiteMedia> versMedias(List<MediaInput> medias) {
        if (medias == null || medias.isEmpty()) {
            return List.of();
        }
        List<ActualiteMedia> resultat = new ArrayList<>();
        int ordre = 0;
        for (MediaInput media : medias) {
            resultat.add(ActualiteMedia.creer(versTypeMedia(media.type()), media.url(), ordre++));
        }
        return resultat;
    }

    /**
     * Tolérant au {@code null}/vide — utilisé pour les filtres de recherche
     * optionnels.
     */
    public CategorieActualite versCategorieOptionnelle(String valeur) {
        return (valeur == null || valeur.isBlank()) ? null : versCategorie(valeur);
    }

    /**
     * Tolérant au {@code null}/vide — utilisé pour les filtres de recherche
     * optionnels.
     */
    public StatutActualite versStatutOptionnel(String valeur) {
        if (valeur == null || valeur.isBlank()) {
            return null;
        }
        try {
            return StatutActualite.valueOf(valeur.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new StatutActualiteInvalideException(valeur);
        }
    }

    private TypeMedia versTypeMedia(String valeur) {
        if (valeur == null || valeur.isBlank()) {
            throw new TypeMediaInvalideException("null");
        }
        try {
            return TypeMedia.valueOf(valeur.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new TypeMediaInvalideException(valeur);
        }
    }
}
