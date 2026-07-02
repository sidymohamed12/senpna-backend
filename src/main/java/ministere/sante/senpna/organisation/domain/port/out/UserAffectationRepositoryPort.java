package ministere.sante.senpna.organisation.domain.port.out;

import java.util.Optional;
import java.util.UUID;

/**
 * Port de sortie dédié à l'affectation d'un utilisateur à une unité
 * organisationnelle (entrepôt/PRA ou structure sanitaire).
 *
 * <p>
 * Le module {@code organisation} ne possède pas l'agrégat {@code User}
 * (propriété des modules {@code auth}/{@code utilisateurs}) : il agit donc
 * uniquement sur les colonnes d'affectation ({@code entrepot_id},
 * {@code structure_sanitaire_id}) de la table {@code users}, via une
 * commande ciblée — un utilisateur ne peut être rattaché qu'à une seule
 * unité organisationnelle à la fois.
 * </p>
 */
public interface UserAffectationRepositoryPort {

    boolean existsUtilisateur(UUID userId);

    void affecterEntrepot(UUID userId, UUID entrepotId);

    void affecterStructureSanitaire(UUID userId, UUID structureSanitaireId);

    void retirerAffectation(UUID userId);

    Optional<UserAffectationView> findAffectation(UUID userId);
}
