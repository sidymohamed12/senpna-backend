package ministere.sante.senpna.media.domain.port.in;

import ministere.sante.senpna.media.application.dto.GenererPresignedUrlCommand;
import ministere.sante.senpna.media.application.dto.PresignedUrlResult;

/**
 * Port d'entrée — génération d'une URL pré-signée pour upload direct.
 *
 * <p>
 * Le use case valide la demande (Content-Type, taille, type de média)
 * et délègue la génération de l'URL au {@code StoragePort}.
 * </p>
 */
public interface GenererPresignedUrlUseCase {

    /**
     * Génère une Presigned URL pour upload direct frontend → stockage.
     *
     * @param command paramètres de la demande
     * @return URL d'upload signée + URL publique finale
     */
    PresignedUrlResult generer(GenererPresignedUrlCommand command);
}
