package ministere.sante.senpna.shared.infrastructure.util;

/**
 * Échappe les caractères spéciaux SQL {@code LIKE} ({@code %}, {@code _},
 * {@code \}) dans un terme de recherche fourni par l'utilisateur.
 *
 * <h3>Pourquoi</h3>
 * <p>
 * Les requêtes construites via {@code CriteriaBuilder#like(...)} sont déjà
 * protégées contre l'injection SQL classique (le terme est lié en tant que
 * paramètre préparé, jamais concaténé dans le texte du SQL). En revanche,
 * si le texte saisi par l'utilisateur contient lui-même des métacaractères
 * {@code LIKE} ({@code %} ou {@code _}), il peut altérer la sémantique de
 * la recherche (ex : {@code "%"} seul retourne tout le référentiel) et,
 * pour des colonnes non indexées, dégrader les performances de façon
 * disproportionnée (déni de service applicatif léger). L'échappement de
 * ces caractères avant de les entourer de {@code %...%} garantit une
 * recherche "contains" strictement littérale — conforme aux recommandations
 * OWASP de validation des entrées.
 * </p>
 */
public final class LikePatternEscaper {

    private static final char ESCAPE_CHAR = '\\';

    private LikePatternEscaper() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Échappe {@code \}, {@code %} et {@code _} dans {@code texte}.
     * Ne construit PAS le motif {@code %...%} — cela reste à la charge de
     * l'appelant, qui doit aussi préciser le caractère d'échappement au
     * {@code CriteriaBuilder} via {@code like(expr, pattern, ESCAPE_CHAR)}.
     */
    public static String escape(String texte) {
        if (texte == null) {
            return null;
        }
        return texte
                .replace(String.valueOf(ESCAPE_CHAR), ESCAPE_CHAR + "" + ESCAPE_CHAR)
                .replace("%", ESCAPE_CHAR + "%")
                .replace("_", ESCAPE_CHAR + "_");
    }

    public static char escapeChar() {
        return ESCAPE_CHAR;
    }
}
