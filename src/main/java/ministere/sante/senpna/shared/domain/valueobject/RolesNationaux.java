package ministere.sante.senpna.shared.domain.valueobject;

import java.util.Set;

/**
 * Référentiel des codes de rôle considérés comme « nationaux » (PNA) —
 * portée illimitée sur l'ensemble des régions, entrepôts et comptes.
 *
 * <p>
 * Point de configuration <strong>unique</strong> : toute feature qui doit
 * distinguer un acteur national d'un acteur régional (PRA) — gestion des
 * comptes, gestion des entrepôts, etc. — s'appuie sur cette classe plutôt
 * que de dupliquer sa propre liste de rôles. Modifier l'ensemble des rôles
 * PNA (ex: retirer {@code MAGASINIER_PNA} de la portée nationale) se fait
 * ici, une seule fois, pour tout le système.
 * </p>
 */
public final class RolesNationaux {

    /**
     * Codes techniques des rôles PNA (cf. migration {@code V001__create_roles.sql}).
     */
    public static final Set<String> CODES = Set.of(
            "ADMIN_PNA", "GESTIONNAIRE_PNA", "PHARMACIEN_PNA", "MAGASINIER_PNA");

    private RolesNationaux() {
    }

    public static boolean estRoleNational(String code) {
        return CODES.contains(code);
    }

    /**
     * @return {@code true} si au moins un des codes fournis est un rôle
     *         national — un utilisateur multi-rôles est considéré national
     *         dès qu'il possède un seul rôle PNA.
     */
    public static boolean contientRoleNational(Set<String> codes) {
        return codes.stream().anyMatch(CODES::contains);
    }
}
