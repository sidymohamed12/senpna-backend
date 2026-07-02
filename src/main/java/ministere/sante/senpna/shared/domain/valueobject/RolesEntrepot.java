package ministere.sante.senpna.shared.domain.valueobject;

import java.util.Set;

/**
 * Classe les rôles selon le type d'entrepôt qu'ils exigent à la création
 * d'un compte — point de configuration <strong>unique</strong>, au même
 * titre que {@link RolesNationaux}.
 *
 * <ul>
 * <li>{@code ROLES_PRA} : rôles rattachés à une PRA (y compris
 * {@code ADMIN_PRA}) — exigent un entrepôt de type {@code PRA}.</li>
 * <li>{@code ROLES_PNA_AVEC_ENTREPOT} : rôles PNA non-administrateurs —
 * exigent un entrepôt de type {@code PNA_CENTRAL}. {@code ADMIN_PNA} en
 * est volontairement exclu : un administrateur national n'a besoin
 * d'aucun entrepôt pour agir.</li>
 * </ul>
 */
public final class RolesEntrepot {

    public static final Set<String> ROLES_PRA = Set.of(
            "ADMIN_PRA", "GESTIONNAIRE_PRA", "PHARMACIEN_PRA", "MAGASINIER_PRA");

    public static final Set<String> ROLES_PNA_AVEC_ENTREPOT = Set.of(
            "GESTIONNAIRE_PNA", "PHARMACIEN_PNA", "MAGASINIER_PNA");

    public static final String TYPE_PRA = "PRA";
    public static final String TYPE_PNA_CENTRAL = "PNA_CENTRAL";

    private RolesEntrepot() {
    }

    public static boolean estRolePra(String code) {
        return ROLES_PRA.contains(code);
    }

    public static boolean estRolePnaAvecEntrepot(String code) {
        return ROLES_PNA_AVEC_ENTREPOT.contains(code);
    }
}
