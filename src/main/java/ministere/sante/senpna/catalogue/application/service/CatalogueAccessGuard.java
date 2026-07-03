package ministere.sante.senpna.catalogue.application.service;

import ministere.sante.senpna.catalogue.domain.exception.CatalogueAccesRefuseException;
import ministere.sante.senpna.shared.domain.exception.UserNotFoundException;
import ministere.sante.senpna.shared.domain.exception.ValidationException;
import ministere.sante.senpna.shared.domain.model.User;
import ministere.sante.senpna.shared.domain.port.out.EntrepotQueryPort;
import ministere.sante.senpna.shared.domain.port.out.RoleCachePort;
import ministere.sante.senpna.shared.domain.port.out.StructureSanitaireQueryPort;
import ministere.sante.senpna.shared.domain.port.out.UserAffectationRepositoryPort;
import ministere.sante.senpna.shared.domain.port.out.UserManagementRepositoryPort;
import ministere.sante.senpna.shared.domain.projection.EntrepotProjection;
import ministere.sante.senpna.shared.domain.projection.RoleProjection;
import ministere.sante.senpna.shared.domain.projection.StructureSanitaireProjection;
import ministere.sante.senpna.shared.domain.projection.UserAffectationView;
import ministere.sante.senpna.shared.domain.valueobject.RolesEntrepot;
import ministere.sante.senpna.shared.domain.valueobject.RolesNationaux;
import ministere.sante.senpna.shared.domain.valueobject.UserId;
import ministere.sante.senpna.shared.infrastructure.security.CurrentUser;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Fait respecter les trois règles de visibilité des catalogues (cf. doc.
 * produit « Catalogue ») :
 *
 * <ul>
 * <li><strong>Catalogue PNA</strong> — visible uniquement par les acteurs
 * PNA et PRA (jamais une structure sanitaire).</li>
 * <li><strong>Catalogue inter-PRA</strong> — visible par tous les acteurs
 * PNA et PRA, quelle que soit leur région (nécessaire pour arbitrer un
 * transfert inter-PRA — cf. doc. flows §CAS 3).</li>
 * <li><strong>Catalogue régional</strong> — visible uniquement par les
 * structures sanitaires de la région concernée. Une structure sanitaire
 * ne voit jamais que le catalogue de <em>sa</em> région : la région
 * demandée est ignorée et systématiquement remplacée par celle de son
 * affectation (même principe que
 * {@code EntrepotScopeGuard.entrepotIdPourLecture}
 * pour le module {@code stock}). Un acteur PRA se voit, par le même
 * mécanisme, toujours ramené au catalogue de sa propre région (c'est,
 * après tout, son propre stock). Seul un acteur national (PNA) peut cibler
 * explicitement n'importe quelle région, à des fins de supervision.</li>
 * </ul>
 *
 * <p>
 * <strong>Zéro dépendance vers une autre feature :</strong> ce guard ne
 * s'appuie que sur des ports {@code shared}
 * ({@link UserManagementRepositoryPort},
 * {@link RoleCachePort}, {@link UserAffectationRepositoryPort},
 * {@link EntrepotQueryPort}, {@link StructureSanitaireQueryPort}), jamais
 * sur le domaine ou l'application des modules {@code organisation} ou
 * {@code utilisateurs}. L'algorithme de résolution de région (priorité au
 * rôle national, puis résolution via l'entrepôt ou la structure sanitaire
 * d'affectation) reprend celui de
 * {@code organisation.application.service.RegionScopeResolver}, réécrit
 * ici contre des projections en lecture seule plutôt que contre les
 * agrégats de domaine d'{@code organisation}.
 * </p>
 */
@Component
public class CatalogueAccessGuard {

    private final UserManagementRepositoryPort userManagementRepositoryPort;
    private final RoleCachePort roleCachePort;
    private final UserAffectationRepositoryPort userAffectationRepositoryPort;
    private final EntrepotQueryPort entrepotQueryPort;
    private final StructureSanitaireQueryPort structureSanitaireQueryPort;

    public CatalogueAccessGuard(UserManagementRepositoryPort userManagementRepositoryPort,
            RoleCachePort roleCachePort, UserAffectationRepositoryPort userAffectationRepositoryPort,
            EntrepotQueryPort entrepotQueryPort, StructureSanitaireQueryPort structureSanitaireQueryPort) {
        this.userManagementRepositoryPort = userManagementRepositoryPort;
        this.roleCachePort = roleCachePort;
        this.userAffectationRepositoryPort = userAffectationRepositoryPort;
        this.entrepotQueryPort = entrepotQueryPort;
        this.structureSanitaireQueryPort = structureSanitaireQueryPort;
    }

    /**
     * À invoquer pour le catalogue PNA et le catalogue inter-PRA.
     *
     * @throws CatalogueAccesRefuseException si l'acteur courant n'a ni
     *                                       rôle national (PNA) ni rôle
     *                                       régional (PRA) — typiquement
     *                                       un {@code GESTIONNAIRE_STRUCTURE}.
     */
    public void verifierActeurPnaOuPra() {
        Set<String> roles = rolesCourants();
        boolean autorise = RolesNationaux.contientRoleNational(roles)
                || roles.stream().anyMatch(RolesEntrepot.ROLES_PRA::contains);
        if (!autorise) {
            throw new CatalogueAccesRefuseException();
        }
    }

    /**
     * Résout la région dont le catalogue doit être consulté :
     * <ul>
     * <li>acteur national (PNA) : la région demandée, qui doit être
     * fournie ;</li>
     * <li>tout autre acteur (structure sanitaire ou PRA) : sa propre
     * région, quelle que soit la région demandée.</li>
     * </ul>
     *
     * @throws ValidationException           si un acteur national ne
     *                                       fournit aucune région
     * @throws CatalogueAccesRefuseException si un acteur non national
     *                                       n'est rattaché à aucune
     *                                       région (aucune affectation)
     */
    public UUID resoudreRegionPourCatalogueRegional(UUID regionIdDemandee) {
        UUID acteurId = currentUser().getUserId();

        if (estActeurNational(acteurId)) {
            if (regionIdDemandee == null) {
                throw new ValidationException(
                        "La région est obligatoire pour consulter un catalogue régional en tant qu'acteur national",
                        "REGION_REQUIRED");
            }
            return regionIdDemandee;
        }

        return resoudreRegionActeur(acteurId).orElseThrow(CatalogueAccesRefuseException::new);
    }

    // ── Résolution de la région d'affectation d'un acteur ──────────────────

    private boolean estActeurNational(UUID acteurId) {
        User acteur = userManagementRepositoryPort.findById(UserId.of(acteurId))
                .orElseThrow(UserNotFoundException::new);

        Set<String> codesActeur = roleCachePort.findAllById(acteur.getRoleIds()).stream()
                .map(RoleProjection::code)
                .collect(Collectors.toUnmodifiableSet());

        return RolesNationaux.contientRoleNational(codesActeur);
    }

    private Optional<UUID> resoudreRegionActeur(UUID acteurId) {
        Optional<UserAffectationView> affectation = userAffectationRepositoryPort.findAffectation(acteurId);
        if (affectation.isEmpty()) {
            return Optional.empty();
        }

        UserAffectationView vue = affectation.get();

        if (vue.entrepotId() != null) {
            return entrepotQueryPort.findById(vue.entrepotId()).map(EntrepotProjection::regionId);
        }

        if (vue.structureSanitaireId() != null) {
            return structureSanitaireQueryPort.findById(vue.structureSanitaireId())
                    .map(StructureSanitaireProjection::regionId);
        }

        return Optional.empty();
    }

    // ── Sécurité ─────────────────────────────────────────────────────────

    private Set<String> rolesCourants() {
        return authorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(autorite -> autorite.startsWith("ROLE_") ? autorite.substring(5) : autorite)
                .collect(Collectors.toUnmodifiableSet());
    }

    private Set<? extends GrantedAuthority> authorities() {
        return authentication().getAuthorities().stream().collect(Collectors.toUnmodifiableSet());
    }

    private CurrentUser currentUser() {
        return (CurrentUser) authentication().getPrincipal();
    }

    private Authentication authentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }
}
