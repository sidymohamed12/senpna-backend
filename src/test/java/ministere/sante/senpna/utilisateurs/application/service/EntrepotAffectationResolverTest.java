package ministere.sante.senpna.utilisateurs.application.service;

import ministere.sante.senpna.shared.domain.port.out.EntrepotQueryPort;
import ministere.sante.senpna.shared.domain.port.out.UserAffectationRepositoryPort;
import ministere.sante.senpna.shared.domain.projection.EntrepotProjection;
import ministere.sante.senpna.shared.domain.projection.UserAffectationView;
import ministere.sante.senpna.utilisateurs.domain.exception.ActeurNonAffecteException;
import ministere.sante.senpna.utilisateurs.domain.exception.ConflitTypeEntrepotException;
import ministere.sante.senpna.utilisateurs.domain.exception.EntrepotInactifException;
import ministere.sante.senpna.utilisateurs.domain.exception.EntrepotIntrouvableException;
import ministere.sante.senpna.utilisateurs.domain.exception.EntrepotNonApplicableException;
import ministere.sante.senpna.utilisateurs.domain.exception.EntrepotRequisException;
import ministere.sante.senpna.utilisateurs.domain.exception.TypeEntrepotIncompatibleException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("EntrepotAffectationResolver — résolution de l'entrepôt à la création d'un compte")
class EntrepotAffectationResolverTest {

    @Mock
    UserHierarchyGuard userHierarchyGuard;
    @Mock
    EntrepotQueryPort entrepotQueryPort;
    @Mock
    UserAffectationRepositoryPort userAffectationRepositoryPort;

    EntrepotAffectationResolver sut;

    private static final UUID ACTEUR_ID = UUID.randomUUID();
    private static final UUID ENTREPOT_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        sut = new EntrepotAffectationResolver(userHierarchyGuard, entrepotQueryPort, userAffectationRepositoryPort);
    }

    @Test
    @DisplayName("aucun rôle nécessitant un entrepôt (ex: ADMIN_PNA seul) → retourne null")
    void resoudre_aucunTypeRequis_retourneNull() {
        UUID resultat = sut.resoudre(ACTEUR_ID, Set.of("ADMIN_PNA"), null);

        assertThat(resultat).isNull();
    }

    @Test
    @DisplayName("aucun type requis mais entrepotId fourni → EntrepotNonApplicableException")
    void resoudre_aucunTypeRequisMaisEntrepotFourni_leveException() {
        var setOf = Set.of("ADMIN_PNA");
        assertThatThrownBy(() -> sut.resoudre(ACTEUR_ID, setOf, ENTREPOT_ID))
                .isInstanceOf(EntrepotNonApplicableException.class);
    }

    @Test
    @DisplayName("rôles PRA et PNA mélangés → ConflitTypeEntrepotException")
    void resoudre_typesConflictuels_leveException() {
        var setOf = Set.of("GESTIONNAIRE_PRA", "GESTIONNAIRE_PNA");
        assertThatThrownBy(
                () -> sut.resoudre(ACTEUR_ID, setOf, ENTREPOT_ID))
                .isInstanceOf(ConflitTypeEntrepotException.class);
    }

    @Test
    @DisplayName("acteur national + entrepotId fourni et cohérent → retourne l'entrepôt fourni")
    void resoudre_acteurNational_entrepotFourni_retourneEntrepot() {
        when(userHierarchyGuard.estActeurNational(ACTEUR_ID)).thenReturn(true);
        when(entrepotQueryPort.findById(ENTREPOT_ID)).thenReturn(
                Optional.of(new EntrepotProjection(ENTREPOT_ID, "PRA-THIES", "PRA Thiès", "PRA", UUID.randomUUID(),
                        true)));

        UUID resultat = sut.resoudre(ACTEUR_ID, Set.of("GESTIONNAIRE_PRA"), ENTREPOT_ID);

        assertThat(resultat).isEqualTo(ENTREPOT_ID);
    }

    @Test
    @DisplayName("acteur national sans entrepotId fourni → EntrepotRequisException")
    void resoudre_acteurNational_sansEntrepot_leveException() {
        when(userHierarchyGuard.estActeurNational(ACTEUR_ID)).thenReturn(true);

        var setOf = Set.of("GESTIONNAIRE_PRA");
        assertThatThrownBy(() -> sut.resoudre(ACTEUR_ID, setOf, null))
                .isInstanceOf(EntrepotRequisException.class);
    }

    @Test
    @DisplayName("acteur national + entrepot introuvable → EntrepotIntrouvableException")
    void resoudre_acteurNational_entrepotIntrouvable_leveException() {
        when(userHierarchyGuard.estActeurNational(ACTEUR_ID)).thenReturn(true);
        when(entrepotQueryPort.findById(ENTREPOT_ID)).thenReturn(Optional.empty());

        var setOf = Set.of("GESTIONNAIRE_PRA");
        assertThatThrownBy(() -> sut.resoudre(ACTEUR_ID, setOf, ENTREPOT_ID))
                .isInstanceOf(EntrepotIntrouvableException.class);
    }

    @Test
    @DisplayName("acteur national + entrepot inactif → EntrepotInactifException")
    void resoudre_acteurNational_entrepotInactif_leveException() {
        when(userHierarchyGuard.estActeurNational(ACTEUR_ID)).thenReturn(true);
        when(entrepotQueryPort.findById(ENTREPOT_ID)).thenReturn(
                Optional.of(new EntrepotProjection(ENTREPOT_ID, "PRA-THIES", "PRA Thiès", "PRA", UUID.randomUUID(),
                        false)));

        var setOf = Set.of("GESTIONNAIRE_PRA");
        assertThatThrownBy(() -> sut.resoudre(ACTEUR_ID, setOf, ENTREPOT_ID))
                .isInstanceOf(EntrepotInactifException.class);
    }

    @Test
    @DisplayName("acteur national + type d'entrepôt incompatible (PNA_CENTRAL fourni pour rôle PRA) → TypeEntrepotIncompatibleException")
    void resoudre_acteurNational_typeIncompatible_leveException() {
        when(userHierarchyGuard.estActeurNational(ACTEUR_ID)).thenReturn(true);
        when(entrepotQueryPort.findById(ENTREPOT_ID)).thenReturn(
                Optional.of(new EntrepotProjection(ENTREPOT_ID, "PNA-CENTRAL", "PNA Centrale", "PNA_CENTRAL", null,
                        true)));

        var setOf = Set.of("GESTIONNAIRE_PRA");
        assertThatThrownBy(() -> sut.resoudre(ACTEUR_ID, setOf, ENTREPOT_ID))
                .isInstanceOf(TypeEntrepotIncompatibleException.class);
    }

    @Test
    @DisplayName("acteur régional (ADMIN_PRA) → ignore l'entrepôt fourni, utilise le sien propre")
    void resoudre_acteurRegional_utiliseSonPropreEntrepot() {
        UUID entrepotActeur = UUID.randomUUID();
        UUID entrepotFourniParErreur = UUID.randomUUID();
        when(userHierarchyGuard.estActeurNational(ACTEUR_ID)).thenReturn(false);
        when(userAffectationRepositoryPort.findAffectation(ACTEUR_ID))
                .thenReturn(Optional.of(new UserAffectationView(ACTEUR_ID, entrepotActeur, null, null)));
        when(entrepotQueryPort.findById(entrepotActeur)).thenReturn(
                Optional.of(new EntrepotProjection(entrepotActeur, "PRA-THIES", "PRA Thiès", "PRA",
                        UUID.randomUUID(), true)));

        UUID resultat = sut.resoudre(ACTEUR_ID, Set.of("GESTIONNAIRE_PRA"), entrepotFourniParErreur);

        assertThat(resultat).isEqualTo(entrepotActeur);
    }

    @Test
    @DisplayName("acteur régional non affecté → ActeurNonAffecteException")
    void resoudre_acteurRegionalNonAffecte_leveException() {
        when(userHierarchyGuard.estActeurNational(ACTEUR_ID)).thenReturn(false);
        when(userAffectationRepositoryPort.findAffectation(ACTEUR_ID)).thenReturn(Optional.empty());

        var setOf = Set.of("GESTIONNAIRE_PRA");
        assertThatThrownBy(() -> sut.resoudre(ACTEUR_ID, setOf, null))
                .isInstanceOf(ActeurNonAffecteException.class);
    }
}
