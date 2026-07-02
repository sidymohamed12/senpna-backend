package ministere.sante.senpna.utilisateurs.application.service;

import ministere.sante.senpna.shared.domain.port.out.EntrepotQueryPort;
import ministere.sante.senpna.shared.domain.port.out.UserAffectationRepositoryPort;
import ministere.sante.senpna.shared.domain.projection.EntrepotProjection;
import ministere.sante.senpna.shared.domain.projection.UserAffectationView;
import ministere.sante.senpna.shared.domain.valueobject.RolesEntrepot;
import ministere.sante.senpna.utilisateurs.domain.exception.ActeurNonAffecteException;
import ministere.sante.senpna.utilisateurs.domain.exception.ConflitTypeEntrepotException;
import ministere.sante.senpna.utilisateurs.domain.exception.EntrepotInactifException;
import ministere.sante.senpna.utilisateurs.domain.exception.EntrepotIntrouvableException;
import ministere.sante.senpna.utilisateurs.domain.exception.EntrepotNonApplicableException;
import ministere.sante.senpna.utilisateurs.domain.exception.EntrepotRequisException;
import ministere.sante.senpna.utilisateurs.domain.exception.TypeEntrepotIncompatibleException;

import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.UUID;

/**
 * Résout l'entrepôt à affecter à un compte au moment de sa création, à
 * partir des codes de rôle demandés et de l'acteur créateur — cœur de la
 * création atomique « compte + affectation » (cf. doc. métier §2).
 *
 * <h3>Règles</h3>
 * <ul>
 * <li>Un rôle {@code ..PRA} (cf. {@link RolesEntrepot#ROLES_PRA}) exige un
 * entrepôt de type {@code PRA}.</li>
 * <li>Un rôle PNA non-administrateur (cf.
 * {@link RolesEntrepot#ROLES_PNA_AVEC_ENTREPOT}) exige un entrepôt de type
 * {@code PNA_CENTRAL}. {@code ADMIN_PNA} n'exige rien.</li>
 * <li>Si l'acteur créateur a un rôle <strong>national</strong> : il doit
 * fournir explicitement l'{@code entrepotId} (choisi parmi les entrepôts
 * du type requis).</li>
 * <li>Si l'acteur créateur est <strong>régional</strong> (ex:
 * {@code ADMIN_PRA}) : l'entrepôt fourni par le client est ignoré — le
 * nouveau compte est automatiquement affecté au <strong>même
 * entrepôt que l'acteur</strong>, ce qui empêche tout rattachement à une
 * autre région.</li>
 * </ul>
 */
@Component
public class EntrepotAffectationResolver {

    private final UserHierarchyGuard userHierarchyGuard;
    private final EntrepotQueryPort entrepotQueryPort;
    private final UserAffectationRepositoryPort userAffectationRepositoryPort;

    public EntrepotAffectationResolver(UserHierarchyGuard userHierarchyGuard, EntrepotQueryPort entrepotQueryPort,
            UserAffectationRepositoryPort userAffectationRepositoryPort) {
        this.userHierarchyGuard = userHierarchyGuard;
        this.entrepotQueryPort = entrepotQueryPort;
        this.userAffectationRepositoryPort = userAffectationRepositoryPort;
    }

    /**
     * @param acteurId       créateur du compte
     * @param codesRoles     codes des rôles demandés pour le nouveau compte
     * @param entrepotIdFourni entrepôt indiqué par le client — pris en
     *                          compte uniquement si l'acteur est national
     * @return l'identifiant de l'entrepôt à affecter, ou {@code null} si
     *         aucun n'est requis pour ces rôles (ex: {@code ADMIN_PNA} seul)
     */
    public UUID resoudre(UUID acteurId, Set<String> codesRoles, UUID entrepotIdFourni) {
        String typeRequis = determinerTypeRequis(codesRoles);

        if (typeRequis == null) {
            if (entrepotIdFourni != null) {
                throw new EntrepotNonApplicableException();
            }
            return null;
        }

        if (userHierarchyGuard.estActeurNational(acteurId)) {
            if (entrepotIdFourni == null) {
                throw new EntrepotRequisException();
            }
            EntrepotProjection entrepot = entrepotQueryPort.findById(entrepotIdFourni)
                    .orElseThrow(EntrepotIntrouvableException::new);
            verifierEntrepot(entrepot, typeRequis);
            return entrepot.id();
        }

        // Acteur régional : entrepôt toujours résolu depuis sa propre
        // affectation — celui fourni par le client, s'il y en a un, est
        // ignoré afin qu'un ADMIN_PRA ne puisse jamais rattacher un compte
        // à l'entrepôt d'une autre région.
        UUID entrepotActeurId = userAffectationRepositoryPort.findAffectation(acteurId)
                .map(UserAffectationView::entrepotId)
                .orElseThrow(ActeurNonAffecteException::new);

        EntrepotProjection entrepot = entrepotQueryPort.findById(entrepotActeurId)
                .orElseThrow(EntrepotIntrouvableException::new);
        verifierEntrepot(entrepot, typeRequis);
        return entrepot.id();
    }

    /**
     * @return le type d'entrepôt exigé par l'ensemble des rôles demandés,
     *         ou {@code null} si aucun d'eux n'en exige.
     * @throws ConflitTypeEntrepotException si les rôles demandés exigent
     *                                       des types différents
     */
    private String determinerTypeRequis(Set<String> codesRoles) {
        String typeRequis = null;
        for (String code : codesRoles) {
            String typePourCeRole = null;
            if (RolesEntrepot.estRolePra(code)) {
                typePourCeRole = RolesEntrepot.TYPE_PRA;
            } else if (RolesEntrepot.estRolePnaAvecEntrepot(code)) {
                typePourCeRole = RolesEntrepot.TYPE_PNA_CENTRAL;
            }
            if (typePourCeRole != null) {
                if (typeRequis != null && !typeRequis.equals(typePourCeRole)) {
                    throw new ConflitTypeEntrepotException();
                }
                typeRequis = typePourCeRole;
            }
        }
        return typeRequis;
    }

    private void verifierEntrepot(EntrepotProjection entrepot, String typeRequis) {
        if (!entrepot.actif()) {
            throw new EntrepotInactifException();
        }
        if (!entrepot.type().equals(typeRequis)) {
            throw new TypeEntrepotIncompatibleException(typeRequis);
        }
    }
}
