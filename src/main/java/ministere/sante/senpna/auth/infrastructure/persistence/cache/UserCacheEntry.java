package ministere.sante.senpna.auth.infrastructure.persistence.cache;

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
 * Instantané JSON-sérialisable d'un {@link User} — pont entre le cache
 * applicatif (Redis) et le modèle de domaine.
 *
 * <p>
 * Contient le hash du mot de passe et les compteurs anti-brute-force
 * ({@code tentativesEchecConnexion}, {@code verrouilleJusqua}) : rien de
 * plus sensible que ce qui est déjà en base (Redis est une infrastructure
 * de confiance au même titre que la base de données), mais c'est
 * précisément pour cela que le TTL est court
 * ({@code app.cache.user-ttl}, 5 min par défaut) et que
 * {@code UserManagementRepositoryAdapter.save(...)} évince/repeuple
 * systématiquement la clé — un verrouillage de compte ou un changement de
 * mot de passe ne doit jamais rester masqué par une entrée de cache
 * périmée.
 * </p>
 */
public record UserCacheEntry(
        UUID id,
        String nom,
        String prenom,
        String email,
        String telephone,
        String hashedPassword,
        boolean actif,
        Set<UUID> roleIds,
        int tentativesEchecConnexion,
        Instant verrouilleJusqua,
        Instant createdAt,
        Instant updatedAt) {

    public static UserCacheEntry from(User user) {
        return new UserCacheEntry(
                user.getId().getValue(),
                user.getNom().getValue(),
                user.getPrenom().getValue(),
                user.getEmail().value(),
                user.getTelephone() != null ? user.getTelephone().value() : null,
                user.getHashedPassword().value(),
                user.isActif(),
                user.getRoleIds(),
                user.getTentativesEchecConnexion(),
                user.getVerrouilleJusqua(),
                user.getCreatedAt(),
                user.getUpdatedAt());
    }

    public User toDomain() {
        return User.builder()
            .id(UserId.of(id))
            .nom(Nom.of(nom))
            .prenom(Prenom.of(prenom))
            .email(Email.of(email))
            .telephone(telephone != null ? Phone.of(telephone) : null)
            .hashedPassword(HashedPassword.of(hashedPassword))
            .actif(actif)
            .roleIds(roleIds)
            .tentativesEchecConnexion(tentativesEchecConnexion)
            .verrouilleJusqua(verrouilleJusqua)
            .createdAt(createdAt)
            .updatedAt(updatedAt)
            .build();
    }
}
