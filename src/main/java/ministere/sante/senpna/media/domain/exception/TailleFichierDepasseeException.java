package ministere.sante.senpna.media.domain.exception;

import ministere.sante.senpna.shared.domain.exception.ErrorCategory;
import ministere.sante.senpna.shared.domain.exception.SenPnaException;

/**
 * Levée quand la taille déclarée du fichier dépasse la limite
 * autorisée pour le type de média demandé.
 */
public class TailleFichierDepasseeException extends SenPnaException {

    public TailleFichierDepasseeException(long tailleRecue, long tailleMax) {
        super(
                String.format(
                        "Fichier trop lourd : %s déclaré, maximum autorisé : %s.",
                        formatBytes(tailleRecue),
                        formatBytes(tailleMax)),
                "TAILLE_FICHIER_DEPASSEE", ErrorCategory.VALIDATION);
    }

    private static String formatBytes(long bytes) {
        if (bytes >= 1024 * 1024) {
            return String.format("%.1fMB", bytes / (1024.0 * 1024.0));
        }
        return String.format("%dKB", bytes / 1024);
    }
}
