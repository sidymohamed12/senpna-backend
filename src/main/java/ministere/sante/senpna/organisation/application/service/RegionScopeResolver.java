package ministere.sante.senpna.organisation.application.service;

import ministere.sante.senpna.organisation.domain.exception.AccesRegionRefuseException;
import ministere.sante.senpna.organisation.domain.model.Entrepot;
import ministere.sante.senpna.organisation.domain.model.StructureSanitaire;
import ministere.sante.senpna.organisation.domain.port.out.EntrepotRepositoryPort;
import ministere.sante.senpna.organisation.domain.port.out.StructureSanitaireRepositoryPort;
import ministere.sante.senpna.organisation.domain.port.out.UserAffectationRepositoryPort;
import ministere.sante.senpna.organisation.domain.port.out.UserAffectationView;
import ministere.sante.senpna.organisation.domain.valueobject.EntrepotId;
import ministere.sante.senpna.organisation.domain.valueobject.RegionId;
import ministere.sante.senpna.organisation.domain.valueobject.StructureSanitaireId;
import ministere.sante.senpna.shared.domain.exception.UserNotFoundException;
import ministere.sante.senpna.shared.domain.model.User;
import ministere.sante.senpna.shared.domain.port.out.RoleCachePort;
import ministere.sante.senpna.shared.domain.port.out.UserManagementRepositoryPort;
import ministere.sante.senpna.shared.domain.projection.RoleProjection;
import ministere.sante.senpna.shared.domain.valueobject.RolesNationaux;
import ministere.sante.senpna.shared.domain.valueobject.UserId;

import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Fait respecter la règle « seuls les utilisateurs d'une région (ou les
 * rôles nationaux PNA) peuvent gérer les entrepôts de cette région » —
 * évite qu'un utilisateur rattaché à une PRA modifie la PRA d'une autre
 * région.
 *
 * <h3>Priorité au rôle, pas à l'affectation</h3>
 * <p>
 * Un acteur possédant un rôle national (cf. {@link RolesNationaux}) a
 * <strong>toujours</strong> accès à toutes les régions, quelle que soit
 * son affectation organisationnelle (même si, par exemple, un
 * {@code ADMIN_PNA} se retrouve affecté à une PRA précise). Le rôle prime
 * sur l'affectation : celle-ci n'indique où l'acteur travaille au
 * quotidien, pas l'étendue de son pouvoir.
 * </p>
 *
 * <h3>Résolution de la région (acteurs non nationaux uniquement)</h3>
 * <ul>
 * <li>Affecté à un entrepôt PRA → région de cet entrepôt.</li>
 * <li>Affecté à un entrepôt PNA centrale (sans région) → aucune
 * restriction.</li>
 * <li>Affecté à une structure sanitaire → région de cette structure.</li>
 * <li>Non affecté → aucune restriction.</li>
 * </ul>
 */
@Component
public class RegionScopeResolver {

    private final UserManagementRepositoryPort userManagementRepositoryPort;
    private final RoleCachePort roleCachePort;
    private final UserAffectationRepositoryPort userAffectationRepositoryPort;
    private final EntrepotRepositoryPort entrepotRepositoryPort;
    private final StructureSanitaireRepositoryPort structureSanitaireRepositoryPort;

    public RegionScopeResolver(UserManagementRepositoryPort userManagementRepositoryPort,
            RoleCachePort roleCachePort, UserAffectationRepositoryPort userAffectationRepositoryPort,
            EntrepotRepositoryPort entrepotRepositoryPort,
            StructureSanitaireRepositoryPort structureSanitaireRepositoryPort) {
        this.userManagementRepositoryPort = userManagementRepositoryPort;
        this.roleCachePort = roleCachePort;
        this.userAffectationRepositoryPort = userAffectationRepositoryPort;
        this.entrepotRepositoryPort = entrepotRepositoryPort;
        this.structureSanitaireRepositoryPort = structureSanitaireRepositoryPort;
    }

    /**
     * @return {@code true} si l'acteur possède au moins un rôle national
     *         (cf. {@link RolesNationaux}) — accès illimité à toutes les
     *         régions.
     */
    public boolean estActeurNational(UUID acteurId) {
        User acteur = userManagementRepositoryPort.findById(UserId.of(acteurId))
                .orElseThrow(UserNotFoundException::new);

        Set<String> codesActeur = roleCachePort.findAllById(acteur.getRoleIds()).stream()
                .map(RoleProjection::code)
                .collect(Collectors.toUnmodifiableSet());

        return RolesNationaux.contientRoleNational(codesActeur);
    }

    /**
     * Résout la région de rattachement de l'acteur — vide si l'acteur a
     * une portée nationale (non affecté, affecté à la PNA centrale, ou
     * possède un rôle national).
     */
    public Optional<RegionId> resoudreRegionActeur(UUID acteurId) {
        if (estActeurNational(acteurId)) {
            return Optional.empty();
        }

        Optional<UserAffectationView> affectation = userAffectationRepositoryPort.findAffectation(acteurId);
        if (affectation.isEmpty()) {
            return Optional.empty();
        }

        UserAffectationView vue = affectation.get();

        if (vue.entrepotId() != null) {
            return entrepotRepositoryPort.findById(EntrepotId.of(vue.entrepotId()))
                    .map(Entrepot::getRegionId);
        }

        if (vue.structureSanitaireId() != null) {
            return structureSanitaireRepositoryPort.findById(StructureSanitaireId.of(vue.structureSanitaireId()))
                    .map(StructureSanitaire::getRegionId);
        }

        return Optional.empty();
    }

    /**
     * Vérifie que l'acteur est autorisé à gérer un entrepôt de la région
     * donnée. Un acteur ayant un rôle national passe toujours, quelle que
     * soit son affectation.
     *
     * @throws AccesRegionRefuseException si l'acteur n'a pas de rôle
     *                                     national et est rattaché à une
     *                                     région différente de la région
     *                                     ciblée
     */
    public void verifierAccesRegion(UUID acteurId, RegionId regionCible) {
        if (estActeurNational(acteurId)) {
            return;
        }

        Optional<RegionId> regionActeur = resoudreRegionActeur(acteurId);
        if (regionActeur.isPresent() && !regionActeur.get().equals(regionCible)) {
            throw new AccesRegionRefuseException();
        }
    }
}
