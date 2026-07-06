package ministere.sante.senpna.media.domain.exception;

import ministere.sante.senpna.shared.domain.exception.ValidationException;

/**
 * Levée quand le Content-Type du fichier n'est pas autorisé
 * pour le type de média demandé.
 */
public class ContentTypeNonAutoriseException extends ValidationException {

    public ContentTypeNonAutoriseException(String contentTypeRecu, String typesAutorises) {
        super(
                "Type de fichier non autorisé : '" + contentTypeRecu + "'. " +
                        "Types acceptés : " + typesAutorises,
                "CONTENT_TYPE_NON_AUTORISE");
    }
}
