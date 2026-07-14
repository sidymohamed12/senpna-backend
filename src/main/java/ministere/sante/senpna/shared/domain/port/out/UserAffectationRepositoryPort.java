package ministere.sante.senpna.shared.domain.port.out;

import ministere.sante.senpna.shared.domain.projection.UserAffectationView;

import java.util.Optional;
import java.util.UUID;

/**
 * Port de sortie dédié à l'affectation d'un utilisateur à une unité
 * organisationnelle (entrepôt/PRA ou structure sanitaire).
 *
 * <p>
 * Ni {@code utilisateurs} ni {@code organisation} ne possède l'ensemble
 * de cette donnée : le premier possède l'agrégat {@code User} mais pas les
 * entrepôts/structures, le second l'inverse. Ce port — défini dans
 * {@code shared} — agit uniquement sur les colonnes d'affectation
 * ({@code entrepot_id}, {@code structure_sanitaire_id}) de la table
 * {@code users}, via des commandes ciblées, sans exposer le reste de
 * l'agrégat {@code User}. Un utilisateur n'est rattaché qu'à une seule
 * unité organisationnelle à la fois.
 * </p>
 *
 * <p>
 * Utilisé par {@code utilisateurs} pour l'affectation atomique à la
 * création d'un compte, et par {@code organisation} pour
 * l'affectation/désaffectation ultérieure et la résolution de la portée
 * régionale d'un acteur.
 * </p>
 */
public interface UserAffectationRepositoryPort {

    boolean existsUtilisateur(UUID userId);

    void affecterEntrepot(UUID userId, UUID entrepotId);

    void affecterStructureSanitaire(UUID userId, UUID structureSanitaireId);

    void affecterFournisseur(UUID userId, UUID fournisseurId);

    void retirerAffectation(UUID userId);

    Optional<UserAffectationView> findAffectation(UUID userId);
}
