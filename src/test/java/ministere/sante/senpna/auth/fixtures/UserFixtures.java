package ministere.sante.senpna.auth.fixtures;

import ministere.sante.senpna.shared.domain.model.User;
import ministere.sante.senpna.shared.domain.valueobject.Email;
import ministere.sante.senpna.shared.domain.valueobject.HashedPassword;
import ministere.sante.senpna.shared.domain.valueobject.Nom;
import ministere.sante.senpna.shared.domain.valueobject.Phone;
import ministere.sante.senpna.shared.domain.valueobject.Prenom;
import ministere.sante.senpna.shared.domain.valueobject.UserId;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

/**
 * Fabrique de données de test pour {@link User}.
 *
 * <p>
 * Centralise la construction des fixtures plutôt que de dupliquer
 * {@code User.builder()...build()} dans chaque classe de test. Les
 * méthodes builder permettent de surcharger uniquement les champs
 * pertinents pour chaque scénario.
 * </p>
 */
public final class UserFixtures {

    // ── Identifiants fixes ────────────────────────────────────────────────
    public static final UUID USER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    public static final UUID ROLE_GESTIONNAIRE_PNA_ID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    public static final UUID ROLE_PHARMACIEN_PRA_ID = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");

    public static final String EMAIL = "mamadou.diallo@sante.gouv.sn";
    public static final String NOM = "Diallo";
    public static final String PRENOM = "Mamadou";
    public static final String TELEPHONE = "+221771234567";
    public static final String PASSWORD_HASH = "$2a$12$hashedpasswordfortest000000000000000000000000000000000";
    public static final String PASSWORD_BRUT = "MotDePasse@2024";

    private UserFixtures() {
    }

    // ── Factories ─────────────────────────────────────────────────────────

    /** Utilisateur actif avec un rôle, sans téléphone. */
    public static User actif() {
        return build(true, 0, null, Set.of(ROLE_GESTIONNAIRE_PNA_ID), null);
    }

    /** Utilisateur actif avec téléphone (pour les tests canal SMS). */
    public static User actifAvecTelephone() {
        return User.builder()
            .id(UserId.of(USER_ID))
            .nom(Nom.of(NOM))
            .prenom(Prenom.of(PRENOM))
            .email(Email.of(EMAIL))
            .telephone(Phone.of(TELEPHONE))
            .hashedPassword(HashedPassword.of(PASSWORD_HASH))
            .actif(true)
            .roleIds(Set.of(ROLE_GESTIONNAIRE_PNA_ID))
            .tentativesEchecConnexion(0)
            .verrouilleJusqua(null)
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();
    }

    /** Utilisateur désactivé. */
    public static User inactif() {
        return build(false, 0, null, Set.of(ROLE_GESTIONNAIRE_PNA_ID), null);
    }

    /** Utilisateur dont le compte est verrouillé pour 15 min. */
    public static User verrouille() {
        return build(true, 5, Instant.now().plusSeconds(900), Set.of(ROLE_GESTIONNAIRE_PNA_ID), null);
    }

    /** Utilisateur avec N échecs de connexion (non encore verrouillé). */
    public static User avecEchecs(int tentatives) {
        return build(true, tentatives, null, Set.of(ROLE_GESTIONNAIRE_PNA_ID), null);
    }

    /** Utilisateur avec plusieurs rôles (multi-rôles). */
    public static User multiRoles() {
        return build(true, 0, null,
                Set.of(ROLE_GESTIONNAIRE_PNA_ID, ROLE_PHARMACIEN_PRA_ID), null);
    }

    /** Utilisateur sans rôle. */
    public static User sansRole() {
        return build(true, 0, null, Set.of(), null);
    }

    // ── Builder interne ───────────────────────────────────────────────────

    private static User build(boolean actif, int tentatives, Instant verrouilleJusqua,
            Set<UUID> roleIds, Phone telephone) {
        return User.builder()
            .id(UserId.of(USER_ID))
            .nom(Nom.of(NOM))
            .prenom(Prenom.of(PRENOM))
            .email(Email.of(EMAIL))
            .telephone(telephone)
            .hashedPassword(HashedPassword.of(PASSWORD_HASH))
            .actif(actif)
            .roleIds(roleIds)
            .tentativesEchecConnexion(tentatives)
            .verrouilleJusqua(verrouilleJusqua)
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();
    }
}
