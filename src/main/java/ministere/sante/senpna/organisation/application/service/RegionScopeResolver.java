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

import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/**
 * Détermine la région de rattachement de l'utilisateur agissant et fait
 * respecter la règle « seuls les utilisateurs d'une région (ou les rôles
 * nationaux PNA) peuvent gérer les entrepôts de cette région » — évite
 * qu'un utilisateur affecté à une PRA modifie une PRA d'une autre région.
 *
 * <h3>Résolution de la région de l'acteur</h3>
 * <ul>
 * <li>Affecté à un entrepôt PRA → région de cet entrepôt.</li>
 * <li>Affecté à un entrepôt PNA centrale (sans région) → aucune
 * restriction (portée nationale).</li>
 * <li>Affecté à une structure sanitaire → région de cette structure.</li>
 * <li>Non affecté (ex: administrateur non encore rattaché) → aucune
 * restriction.</li>
 * </ul>
 */
@Component
public class RegionScopeResolver {

    private final UserAffectationRepositoryPort userAffectationRepositoryPort;
    private final EntrepotRepositoryPort entrepotRepositoryPort;
    private final StructureSanitaireRepositoryPort structureSanitaireRepositoryPort;

    public RegionScopeResolver(UserAffectationRepositoryPort userAffectationRepositoryPort,
            EntrepotRepositoryPort entrepotRepositoryPort,
            StructureSanitaireRepositoryPort structureSanitaireRepositoryPort) {
        this.userAffectationRepositoryPort = userAffectationRepositoryPort;
        this.entrepotRepositoryPort = entrepotRepositoryPort;
        this.structureSanitaireRepositoryPort = structureSanitaireRepositoryPort;
    }

    /**
     * Résout la région de rattachement de l'acteur — vide si l'acteur a
     * une portée nationale (non affecté, ou affecté à la PNA centrale).
     */
    public Optional<RegionId> resoudreRegionActeur(UUID acteurId) {
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
     * donnée.
     *
     * @throws AccesRegionRefuseException si l'acteur est rattaché à une
     *                                    région différente de la région
     *                                    ciblée
     */
    public void verifierAccesRegion(UUID acteurId, RegionId regionCible) {
        Optional<RegionId> regionActeur = resoudreRegionActeur(acteurId);
        if (regionActeur.isPresent() && !regionActeur.get().equals(regionCible)) {
            throw new AccesRegionRefuseException();
        }
    }
}
