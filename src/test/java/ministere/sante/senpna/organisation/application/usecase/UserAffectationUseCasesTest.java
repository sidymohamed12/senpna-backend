package ministere.sante.senpna.organisation.application.usecase;

import ministere.sante.senpna.organisation.application.usecase.affectation.AssignUserToEntrepotUseCaseImpl;
import ministere.sante.senpna.organisation.application.usecase.affectation.AssignUserToStructureUseCaseImpl;
import ministere.sante.senpna.organisation.application.usecase.affectation.UnassignUserUseCaseImpl;
import ministere.sante.senpna.organisation.domain.command.OrganisationCommands.AssignUserToEntrepotCommand;
import ministere.sante.senpna.organisation.domain.command.OrganisationCommands.AssignUserToStructureCommand;
import ministere.sante.senpna.organisation.domain.command.OrganisationCommands.UnassignUserCommand;
import ministere.sante.senpna.organisation.domain.command.OrganisationCommands.UserAffectationDetail;
import ministere.sante.senpna.organisation.domain.exception.EntrepotInactifException;
import ministere.sante.senpna.organisation.domain.exception.EntrepotIntrouvableException;
import ministere.sante.senpna.organisation.domain.exception.StructureSanitaireIntrouvableException;
import ministere.sante.senpna.organisation.domain.exception.StructureSanitaireNonValideeException;
import ministere.sante.senpna.organisation.domain.model.Entrepot;
import ministere.sante.senpna.organisation.domain.model.StructureSanitaire;
import ministere.sante.senpna.organisation.domain.port.out.EntrepotRepositoryPort;
import ministere.sante.senpna.organisation.domain.port.out.StructureSanitaireRepositoryPort;
import ministere.sante.senpna.organisation.domain.valueobject.EntrepotId;
import ministere.sante.senpna.organisation.domain.valueobject.RegionId;
import ministere.sante.senpna.organisation.domain.valueobject.StatutAdhesion;
import ministere.sante.senpna.organisation.domain.valueobject.StructureSanitaireId;
import ministere.sante.senpna.organisation.domain.valueobject.TypeEntrepot;
import ministere.sante.senpna.organisation.domain.valueobject.TypeStructureSanitaire;
import ministere.sante.senpna.shared.domain.exception.UserNotFoundException;
import ministere.sante.senpna.shared.domain.port.out.UserAffectationRepositoryPort;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@DisplayName("Affectation utilisateur Use Cases — entrepôt / structure sanitaire / retrait")
class UserAffectationUseCasesTest {

    private static final UUID USER_ID = UUID.randomUUID();
    private static final UUID ENTREPOT_ID = UUID.randomUUID();
    private static final UUID STRUCTURE_ID = UUID.randomUUID();

    private Entrepot entrepotActif() {
        return Entrepot.builder()
            .id(EntrepotId.of(ENTREPOT_ID))
            .code("PRA-DAKAR")
            .nom("PRA Dakar")
            .type(TypeEntrepot.PRA)
            .regionId(RegionId.generate())
            .adresse(null)
            .telephone(null)
            .responsableUserId(null)
            .actif(true)
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();
    }

    private StructureSanitaire structureValidee() {
        return StructureSanitaire.builder()
            .id(StructureSanitaireId.of(STRUCTURE_ID))
            .code("HOP-X")
            .nom("Hôpital X")
            .type(TypeStructureSanitaire.HOPITAL)
            .regionId(RegionId.generate())
            .praId(null)
            .district(null)
            .adresse(null)
            .telephone(null)
            .email(null)
            .responsableNom("Ndiaye")
            .responsablePrenom("Fatou")
            .statutAdhesion(StatutAdhesion.VALIDEE)
            .motifRejet(null)
            .actif(true)
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();
    }

    // ══════════════════════════════════════════════════════════════════════
    // AssignUserToEntrepotUseCaseImpl
    // ══════════════════════════════════════════════════════════════════════

    @Nested
    @ExtendWith(MockitoExtension.class)
    @DisplayName("AssignUserToEntrepotUseCaseImpl")
    class AssignUserToEntrepotTest {

        @Mock
        UserAffectationRepositoryPort userAffectationRepositoryPort;
        @Mock
        EntrepotRepositoryPort entrepotRepositoryPort;
        @InjectMocks
        AssignUserToEntrepotUseCaseImpl sut;

        @Test
        @DisplayName("affecte l'utilisateur à un entrepôt actif")
        void affecter_succes_affecteEntrepot() {
            when(userAffectationRepositoryPort.existsUtilisateur(USER_ID)).thenReturn(true);
            when(entrepotRepositoryPort.findById(any())).thenReturn(Optional.of(entrepotActif()));

            UserAffectationDetail result = sut.affecter(new AssignUserToEntrepotCommand(USER_ID, ENTREPOT_ID));

            assertThat(result.userId()).isEqualTo(USER_ID);
            assertThat(result.entrepotId()).isEqualTo(ENTREPOT_ID);
            assertThat(result.structureSanitaireId()).isNull();
            verify(userAffectationRepositoryPort).affecterEntrepot(USER_ID, ENTREPOT_ID);
        }

        @Test
        @DisplayName("utilisateur introuvable → UserNotFoundException")
        void affecter_userIntrouvable_leveException() {
            when(userAffectationRepositoryPort.existsUtilisateur(USER_ID)).thenReturn(false);

            var assignUserToEntrepotCommand = new AssignUserToEntrepotCommand(USER_ID, ENTREPOT_ID);
            assertThatThrownBy(() -> sut.affecter(assignUserToEntrepotCommand))
                    .isInstanceOf(UserNotFoundException.class);

            verifyNoInteractions(entrepotRepositoryPort);
        }

        @Test
        @DisplayName("entrepôt introuvable → EntrepotIntrouvableException")
        void affecter_entrepotIntrouvable_leveException() {
            when(userAffectationRepositoryPort.existsUtilisateur(USER_ID)).thenReturn(true);
            when(entrepotRepositoryPort.findById(any())).thenReturn(Optional.empty());

            var assignUserToEntrepotCommand = new AssignUserToEntrepotCommand(USER_ID, ENTREPOT_ID);
            assertThatThrownBy(() -> sut.affecter(assignUserToEntrepotCommand))
                    .isInstanceOf(EntrepotIntrouvableException.class);

            verify(userAffectationRepositoryPort, never()).affecterEntrepot(any(), any());
        }

        @Test
        @DisplayName("entrepôt inactif → EntrepotInactifException")
        void affecter_entrepotInactif_leveException() {
            when(userAffectationRepositoryPort.existsUtilisateur(USER_ID)).thenReturn(true);
            Entrepot entrepotInactif = Entrepot.builder()
                .id(EntrepotId.of(ENTREPOT_ID))
                .code("PRA-DAKAR")
                .nom("PRA Dakar")
                .type(TypeEntrepot.PRA)
                .regionId(RegionId.generate())
                .adresse(null)
                .telephone(null)
                .responsableUserId(null)
                .actif(false)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
            when(entrepotRepositoryPort.findById(any())).thenReturn(Optional.of(entrepotInactif));

            var assignUserToEntrepotCommand = new AssignUserToEntrepotCommand(USER_ID, ENTREPOT_ID);
            assertThatThrownBy(() -> sut.affecter(assignUserToEntrepotCommand))
                    .isInstanceOf(EntrepotInactifException.class);
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // AssignUserToStructureUseCaseImpl
    // ══════════════════════════════════════════════════════════════════════

    @Nested
    @ExtendWith(MockitoExtension.class)
    @DisplayName("AssignUserToStructureUseCaseImpl")
    class AssignUserToStructureTest {

        @Mock
        UserAffectationRepositoryPort userAffectationRepositoryPort;
        @Mock
        StructureSanitaireRepositoryPort structureSanitaireRepositoryPort;
        @InjectMocks
        AssignUserToStructureUseCaseImpl sut;

        @Test
        @DisplayName("affecte l'utilisateur à une structure dont l'adhésion est validée")
        void affecter_succes_affecteStructure() {
            when(userAffectationRepositoryPort.existsUtilisateur(USER_ID)).thenReturn(true);
            when(structureSanitaireRepositoryPort.findById(any())).thenReturn(Optional.of(structureValidee()));

            UserAffectationDetail result = sut.affecter(new AssignUserToStructureCommand(USER_ID, STRUCTURE_ID));

            assertThat(result.structureSanitaireId()).isEqualTo(STRUCTURE_ID);
            assertThat(result.entrepotId()).isNull();
            verify(userAffectationRepositoryPort).affecterStructureSanitaire(USER_ID, STRUCTURE_ID);
        }

        @Test
        @DisplayName("structure introuvable → StructureSanitaireIntrouvableException")
        void affecter_structureIntrouvable_leveException() {
            when(userAffectationRepositoryPort.existsUtilisateur(USER_ID)).thenReturn(true);
            when(structureSanitaireRepositoryPort.findById(any())).thenReturn(Optional.empty());

            var assignUserToStructureCommand = new AssignUserToStructureCommand(USER_ID, STRUCTURE_ID);
            assertThatThrownBy(() -> sut.affecter(assignUserToStructureCommand))
                    .isInstanceOf(StructureSanitaireIntrouvableException.class);
        }

        @Test
        @DisplayName("adhésion non validée → StructureSanitaireNonValideeException")
        void affecter_adhesionNonValidee_leveException() {
            when(userAffectationRepositoryPort.existsUtilisateur(USER_ID)).thenReturn(true);
            StructureSanitaire structureEnAttente = StructureSanitaire.builder()
                .id(StructureSanitaireId.of(STRUCTURE_ID))
                .code("HOP-X")
                .nom("Hôpital X")
                .type(TypeStructureSanitaire.HOPITAL)
                .regionId(RegionId.generate())
                .praId(null)
                .district(null)
                .adresse(null)
                .telephone(null)
                .email(null)
                .responsableNom("Ndiaye")
                .responsablePrenom("Fatou")
                .statutAdhesion(StatutAdhesion.EN_ATTENTE_VALIDATION)
                .motifRejet(null)
                .actif(false)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
            when(structureSanitaireRepositoryPort.findById(any())).thenReturn(Optional.of(structureEnAttente));

            var assignUserToStructureCommand = new AssignUserToStructureCommand(USER_ID, STRUCTURE_ID);
            assertThatThrownBy(() -> sut.affecter(assignUserToStructureCommand))
                    .isInstanceOf(StructureSanitaireNonValideeException.class);

            verify(userAffectationRepositoryPort, never()).affecterStructureSanitaire(any(), any());
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // UnassignUserUseCaseImpl
    // ══════════════════════════════════════════════════════════════════════

    @Nested
    @ExtendWith(MockitoExtension.class)
    @DisplayName("UnassignUserUseCaseImpl")
    class UnassignUserTest {

        @Mock
        UserAffectationRepositoryPort userAffectationRepositoryPort;
        @InjectMocks
        UnassignUserUseCaseImpl sut;

        @Test
        @DisplayName("retire l'affectation de l'utilisateur")
        void retirer_succes_retireAffectation() {
            when(userAffectationRepositoryPort.existsUtilisateur(USER_ID)).thenReturn(true);

            UserAffectationDetail result = sut.retirer(new UnassignUserCommand(USER_ID));

            assertThat(result.entrepotId()).isNull();
            assertThat(result.structureSanitaireId()).isNull();
            verify(userAffectationRepositoryPort).retirerAffectation(USER_ID);
        }

        @Test
        @DisplayName("utilisateur introuvable → UserNotFoundException")
        void retirer_userIntrouvable_leveException() {
            when(userAffectationRepositoryPort.existsUtilisateur(USER_ID)).thenReturn(false);

            var unassignUserCommand = new UnassignUserCommand(USER_ID);
            assertThatThrownBy(() -> sut.retirer(unassignUserCommand))
                    .isInstanceOf(UserNotFoundException.class);

            verify(userAffectationRepositoryPort, never()).retirerAffectation(any());
        }
    }
}
