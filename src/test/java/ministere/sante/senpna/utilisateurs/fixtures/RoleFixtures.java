package ministere.sante.senpna.utilisateurs.fixtures;

import ministere.sante.senpna.auth.fixtures.UserFixtures;
import ministere.sante.senpna.shared.domain.projection.RoleProjection;

import java.util.UUID;

/**
 * Fabrique de données de test pour {@link RoleProjection}, utilisée par les
 * tests du module {@code utilisateurs} (gestion des utilisateurs et de
 * leurs rôles).
 *
 * <p>
 * Réutilise les identifiants de rôle déjà définis dans
 * {@link UserFixtures} afin de rester cohérent avec les fixtures
 * utilisateur partagées entre les modules {@code auth} et
 * {@code utilisateurs}.
 * </p>
 */
public final class RoleFixtures {

    public static final UUID ROLE_GESTIONNAIRE_PNA_ID = UserFixtures.ROLE_GESTIONNAIRE_PNA_ID;
    public static final UUID ROLE_PHARMACIEN_PRA_ID = UserFixtures.ROLE_PHARMACIEN_PRA_ID;
    public static final UUID ROLE_INTROUVABLE_ID = UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc");

    private RoleFixtures() {
    }

    public static RoleProjection gestionnairePna() {
        return new RoleProjection(ROLE_GESTIONNAIRE_PNA_ID, "GESTIONNAIRE_PNA", "Gestionnaire PNA");
    }

    public static RoleProjection pharmacienPra() {
        return new RoleProjection(ROLE_PHARMACIEN_PRA_ID, "PHARMACIEN_PRA", "Pharmacien PRA");
    }
}
