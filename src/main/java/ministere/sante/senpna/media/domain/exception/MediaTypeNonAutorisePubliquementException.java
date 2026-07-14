package ministere.sante.senpna.media.domain.exception;

import ministere.sante.senpna.shared.domain.exception.ErrorCategory;
import ministere.sante.senpna.shared.domain.exception.SenPnaException;

/**
 * Levée quand une URL pré-signée est demandée sans authentification
 * ({@code POST /api/medias/presigned-url/public}) pour un {@code MediaType}
 * qui n'est pas éligible à un usage public (cf.
 * {@code MediaType#estUtilisablePubliquement()}) — seules les pièces
 * jointes d'un formulaire public (CV, lettre de motivation) le sont ;
 * tout autre type de média reste réservé à l'endpoint authentifié standard.
 */
public class MediaTypeNonAutorisePubliquementException extends SenPnaException {

    public MediaTypeNonAutorisePubliquementException(String mediaType) {
        super("Le type de média '" + mediaType
                + "' ne peut pas être demandé sans authentification", "MEDIA_TYPE_NON_AUTORISE_PUBLIQUEMENT", ErrorCategory.FORBIDDEN);
    }
}
