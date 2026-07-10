package ministere.sante.senpna.organisation.application.usecase;

import ministere.sante.senpna.organisation.application.service.StructureSanitaireDetailAssembler;
import ministere.sante.senpna.organisation.application.usecase.affectation.AssignStructureToPraUseCaseImpl;
import ministere.sante.senpna.organisation.application.usecase.affectation.AssignStructureToRegionUseCaseImpl;
import ministere.sante.senpna.organisation.application.usecase.affectation.RejectAdhesionUseCaseImpl;
import ministere.sante.senpna.organisation.application.usecase.affectation.ValidateAdhesionUseCaseImpl;
import ministere.sante.senpna.organisation.application.usecase.strcuture.ActivateStructureSanitaireUseCaseImpl;
import ministere.sante.senpna.organisation.application.usecase.strcuture.CreateStructureSanitaireUseCaseImpl;
import ministere.sante.senpna.organisation.application.usecase.strcuture.DeactivateStructureSanitaireUseCaseImpl;
import ministere.sante.senpna.organisation.application.usecase.strcuture.GetStructureSanitaireUseCaseImpl;
import ministere.sante.senpna.organisation.application.usecase.strcuture.ListStructuresSanitairesUseCaseImpl;
import ministere.sante.senpna.organisation.application.usecase.strcuture.UpdateStructureSanitaireUseCaseImpl;
import ministere.sante.senpna.organisation.domain.command.OrganisationCommands.ActivateStructureSanitaireCommand;
import ministere.sante.senpna.organisation.domain.command.OrganisationCommands.AssignStructureToPraCommand;
import ministere.sante.senpna.organisation.domain.command.OrganisationCommands.AssignStructureToRegionCommand;
import ministere.sante.senpna.organisation.domain.command.OrganisationCommands.CreateStructureSanitaireCommand;
import ministere.sante.senpna.organisation.domain.command.OrganisationCommands.DeactivateStructureSanitaireCommand;
import ministere.sante.senpna.organisation.domain.command.OrganisationCommands.GetStructureSanitaireQuery;
import ministere.sante.senpna.organisation.domain.command.OrganisationCommands.ListStructuresSanitairesQuery;
import ministere.sante.senpna.organisation.domain.command.OrganisationCommands.RejectAdhesionCommand;
import ministere.sante.senpna.organisation.domain.command.OrganisationCommands.StructureSanitaireDetail;
import ministere.sante.senpna.organisation.domain.command.OrganisationCommands.StructureSanitairePage;
import ministere.sante.senpna.organisation.domain.command.OrganisationCommands.UpdateStructureSanitaireCommand;
import ministere.sante.senpna.organisation.domain.command.OrganisationCommands.ValidateAdhesionCommand;
import ministere.sante.senpna.organisation.domain.exception.CodeStructureSanitaireDejaUtiliseException;
import ministere.sante.senpna.organisation.domain.exception.DemandeAdhesionDejaTraiteeException;
import ministere.sante.senpna.organisation.domain.exception.EntrepotInactifException;
import ministere.sante.senpna.organisation.domain.exception.EntrepotIntrouvableException;
import ministere.sante.senpna.organisation.domain.exception.RegionInactiveException;
import ministere.sante.senpna.organisation.domain.exception.RegionIntrouvableException;
import ministere.sante.senpna.organisation.domain.exception.StructureSanitaireIntrouvableException;
import ministere.sante.senpna.organisation.domain.exception.StructureSanitaireNonValideeException;
import ministere.sante.senpna.organisation.domain.exception.TypeEntrepotInvalideException;
import ministere.sante.senpna.organisation.domain.model.Entrepot;
import ministere.sante.senpna.organisation.domain.model.Region;
import ministere.sante.senpna.organisation.domain.model.StructureSanitaire;
import ministere.sante.senpna.organisation.domain.port.out.EntrepotRepositoryPort;
import ministere.sante.senpna.organisation.domain.port.out.RegionRepositoryPort;
import ministere.sante.senpna.organisation.domain.port.out.StructureSanitaireRepositoryPort;
import ministere.sante.senpna.organisation.domain.valueobject.EntrepotId;
import ministere.sante.senpna.organisation.domain.valueobject.RegionId;
import ministere.sante.senpna.organisation.domain.valueobject.StatutAdhesion;
import ministere.sante.senpna.organisation.domain.valueobject.StructureSanitaireId;
import ministere.sante.senpna.organisation.domain.valueobject.TypeEntrepot;
import ministere.sante.senpna.organisation.domain.valueobject.TypeStructureSanitaire;
import ministere.sante.senpna.shared.domain.events.AdhesionValideeEvent;
import ministere.sante.senpna.shared.domain.exception.ValidationException;
import ministere.sante.senpna.shared.domain.port.out.EventPublisherPort;
import ministere.sante.senpna.shared.domain.valueobject.PageResult;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@DisplayName("StructureSanitaire Use Cases")
class StructureSanitaireUseCasesTest {

    private static final UUID STRUCTURE_ID = UUID.randomUUID();
    private static final UUID REGION_ID = UUID.randomUUID();
    private static final UUID PRA_ID = UUID.randomUUID();
    private static final StructureSanitaireDetail DETAIL_FICTIF = new StructureSanitaireDetail(
            STRUCTURE_ID, "HOP-THIES", "Hôpital de Thiès", TypeStructureSanitaire.HOPITAL, REGION_ID, "Thiès",
            null, null, "Thiès", null, null, null, "Ndiaye", "Fatou", StatutAdhesion.EN_ATTENTE_VALIDATION, null,
            false, Instant.now(), Instant.now());

    private Region regionActive() {
        return Region.creer("THIES", "Thiès");
    }

    private StructureSanitaire structureEnAttente() {
        return StructureSanitaire.reconstruct(StructureSanitaireId.of(STRUCTURE_ID), "HOP-THIES", "Hôpital de Thiès",
                TypeStructureSanitaire.HOPITAL, RegionId.of(REGION_ID), null, "Thiès", null, null, null, "Ndiaye",
                "Fatou", StatutAdhesion.EN_ATTENTE_VALIDATION, null, false, Instant.now(), Instant.now());
    }

    private StructureSanitaire structureValidee() {
        return StructureSanitaire.reconstruct(StructureSanitaireId.of(STRUCTURE_ID), "HOP-THIES", "Hôpital de Thiès",
                TypeStructureSanitaire.HOPITAL, RegionId.of(REGION_ID), null, "Thiès", null, null, null, "Ndiaye",
                "Fatou", StatutAdhesion.VALIDEE, null, true, Instant.now(), Instant.now());
    }

    private Entrepot praActive() {
        return Entrepot.reconstruct(EntrepotId.of(PRA_ID), "PRA-THIES", "PRA Thiès", TypeEntrepot.PRA,
                RegionId.of(REGION_ID), null, null, null, true, Instant.now(), Instant.now());
    }

    // ══════════════════════════════════════════════════════════════════════
    // CreateStructureSanitaireUseCaseImpl
    // ══════════════════════════════════════════════════════════════════════

    @Nested
    @ExtendWith(MockitoExtension.class)
    @DisplayName("CreateStructureSanitaireUseCaseImpl")
    class CreateStructureSanitaireTest {

        @Mock
        StructureSanitaireRepositoryPort structureSanitaireRepositoryPort;
        @Mock
        RegionRepositoryPort regionRepositoryPort;
        @Mock
        StructureSanitaireDetailAssembler structureSanitaireDetailAssembler;
        @InjectMocks
        CreateStructureSanitaireUseCaseImpl sut;

        private CreateStructureSanitaireCommand commandeValide() {
            return new CreateStructureSanitaireCommand("hop-thies", "Hôpital de Thiès", TypeStructureSanitaire.HOPITAL,
                    REGION_ID, "Thiès", null, null, "hopital@sante.gouv.sn", "Ndiaye", "Fatou");
        }

        @Test
        @DisplayName("crée la demande d'adhésion quand la région existe et est active")
        void creer_succes_creeLaDemande() {
            when(regionRepositoryPort.findById(any())).thenReturn(Optional.of(regionActive()));
            when(structureSanitaireRepositoryPort.existsByCode(any())).thenReturn(false);
            when(structureSanitaireRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(structureSanitaireDetailAssembler.assembler(any())).thenReturn(DETAIL_FICTIF);

            StructureSanitaireDetail result = sut.creer(commandeValide());

            assertThat(result).isEqualTo(DETAIL_FICTIF);
        }

        @Test
        @DisplayName("regionId null → ValidationException")
        void creer_sansRegion_leveException() {
            CreateStructureSanitaireCommand command = new CreateStructureSanitaireCommand("HOP-X", "Hôpital X",
                    TypeStructureSanitaire.HOPITAL, null, null, null, null, null, "Ndiaye", "Fatou");

            assertThatThrownBy(() -> sut.creer(command)).isInstanceOf(ValidationException.class);

            verifyNoInteractions(regionRepositoryPort, structureSanitaireRepositoryPort);
        }

        @Test
        @DisplayName("région introuvable → RegionIntrouvableException")
        void creer_regionIntrouvable_leveException() {
            when(regionRepositoryPort.findById(any())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> sut.creer(commandeValide())).isInstanceOf(RegionIntrouvableException.class);
        }

        @Test
        @DisplayName("région inactive → RegionInactiveException")
        void creer_regionInactive_leveException() {
            Region regionInactive = regionActive();
            regionInactive.desactiver();
            when(regionRepositoryPort.findById(any())).thenReturn(Optional.of(regionInactive));

            assertThatThrownBy(() -> sut.creer(commandeValide())).isInstanceOf(RegionInactiveException.class);
        }

        @Test
        @DisplayName("code déjà utilisé → CodeStructureSanitaireDejaUtiliseException")
        void creer_codeDejaUtilise_leveException() {
            when(regionRepositoryPort.findById(any())).thenReturn(Optional.of(regionActive()));
            when(structureSanitaireRepositoryPort.existsByCode(any())).thenReturn(true);

            assertThatThrownBy(() -> sut.creer(commandeValide()))
                    .isInstanceOf(CodeStructureSanitaireDejaUtiliseException.class);

            verify(structureSanitaireRepositoryPort, never()).save(any());
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // UpdateStructureSanitaireUseCaseImpl
    // ══════════════════════════════════════════════════════════════════════

    @Nested
    @ExtendWith(MockitoExtension.class)
    @DisplayName("UpdateStructureSanitaireUseCaseImpl")
    class UpdateStructureSanitaireTest {

        @Mock
        StructureSanitaireRepositoryPort structureSanitaireRepositoryPort;
        @Mock
        StructureSanitaireDetailAssembler structureSanitaireDetailAssembler;
        @InjectMocks
        UpdateStructureSanitaireUseCaseImpl sut;

        @Test
        @DisplayName("modifie les informations générales, y compris le responsable")
        void modifier_succes_metAJourInformations() {
            when(structureSanitaireRepositoryPort.findById(any())).thenReturn(Optional.of(structureEnAttente()));
            when(structureSanitaireRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(structureSanitaireDetailAssembler.assembler(any())).thenReturn(DETAIL_FICTIF);

            ArgumentCaptor<StructureSanitaire> captor = ArgumentCaptor.forClass(StructureSanitaire.class);

            sut.modifier(new UpdateStructureSanitaireCommand(STRUCTURE_ID, "Hôpital Régional", "Thiès Nord", null,
                    null, null, "Diop", "Awa"));

            verify(structureSanitaireRepositoryPort).save(captor.capture());
            assertThat(captor.getValue().getResponsableNom()).isEqualTo("Diop");
            assertThat(captor.getValue().getResponsablePrenom()).isEqualTo("Awa");
        }

        @Test
        @DisplayName("structure introuvable → StructureSanitaireIntrouvableException")
        void modifier_introuvable_leveException() {
            when(structureSanitaireRepositoryPort.findById(any())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> sut.modifier(
                    new UpdateStructureSanitaireCommand(STRUCTURE_ID, "Nom", null, null, null, null, null, null)))
                    .isInstanceOf(StructureSanitaireIntrouvableException.class);
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // ValidateAdhesionUseCaseImpl
    // ══════════════════════════════════════════════════════════════════════

    @Nested
    @ExtendWith(MockitoExtension.class)
    @DisplayName("ValidateAdhesionUseCaseImpl")
    class ValidateAdhesionTest {

        @Mock
        StructureSanitaireRepositoryPort structureSanitaireRepositoryPort;
        @Mock
        EventPublisherPort eventPublisherPort;
        @Mock
        StructureSanitaireDetailAssembler structureSanitaireDetailAssembler;
        @InjectMocks
        ValidateAdhesionUseCaseImpl sut;

        @Test
        @DisplayName("valide l'adhésion et publie l'AdhesionValideeEvent")
        void valider_succes_publieEvent() {
            when(structureSanitaireRepositoryPort.findById(any())).thenReturn(Optional.of(structureEnAttente()));
            when(structureSanitaireRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(structureSanitaireDetailAssembler.assembler(any())).thenReturn(DETAIL_FICTIF);

            ArgumentCaptor<StructureSanitaire> captor = ArgumentCaptor.forClass(StructureSanitaire.class);

            sut.valider(new ValidateAdhesionCommand(STRUCTURE_ID));

            verify(eventPublisherPort).publishAndClear(captor.capture());
            assertThat(captor.getValue().getStatutAdhesion()).isEqualTo(StatutAdhesion.VALIDEE);
            assertThat(captor.getValue().getDomainEvents()).hasSize(1);
            assertThat(captor.getValue().getDomainEvents().get(0)).isInstanceOf(AdhesionValideeEvent.class);
        }

        @Test
        @DisplayName("adhésion déjà validée → DemandeAdhesionDejaTraiteeException, aucune publication")
        void valider_dejaTraitee_leveException() {
            when(structureSanitaireRepositoryPort.findById(any())).thenReturn(Optional.of(structureValidee()));

            assertThatThrownBy(() -> sut.valider(new ValidateAdhesionCommand(STRUCTURE_ID)))
                    .isInstanceOf(DemandeAdhesionDejaTraiteeException.class);

            verifyNoInteractions(eventPublisherPort);
            verify(structureSanitaireRepositoryPort, never()).save(any());
        }

        @Test
        @DisplayName("structure introuvable → StructureSanitaireIntrouvableException")
        void valider_introuvable_leveException() {
            when(structureSanitaireRepositoryPort.findById(any())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> sut.valider(new ValidateAdhesionCommand(STRUCTURE_ID)))
                    .isInstanceOf(StructureSanitaireIntrouvableException.class);
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // RejectAdhesionUseCaseImpl
    // ══════════════════════════════════════════════════════════════════════

    @Nested
    @ExtendWith(MockitoExtension.class)
    @DisplayName("RejectAdhesionUseCaseImpl")
    class RejectAdhesionTest {

        @Mock
        StructureSanitaireRepositoryPort structureSanitaireRepositoryPort;
        @Mock
        StructureSanitaireDetailAssembler structureSanitaireDetailAssembler;
        @InjectMocks
        RejectAdhesionUseCaseImpl sut;

        @Test
        @DisplayName("rejette l'adhésion avec le motif fourni")
        void rejeter_succes_rejetteAvecMotif() {
            when(structureSanitaireRepositoryPort.findById(any())).thenReturn(Optional.of(structureEnAttente()));
            when(structureSanitaireRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(structureSanitaireDetailAssembler.assembler(any())).thenReturn(DETAIL_FICTIF);

            ArgumentCaptor<StructureSanitaire> captor = ArgumentCaptor.forClass(StructureSanitaire.class);

            sut.rejeter(new RejectAdhesionCommand(STRUCTURE_ID, "Documents manquants"));

            verify(structureSanitaireRepositoryPort).save(captor.capture());
            assertThat(captor.getValue().getStatutAdhesion()).isEqualTo(StatutAdhesion.REJETEE);
            assertThat(captor.getValue().getMotifRejet()).isEqualTo("Documents manquants");
        }

        @Test
        @DisplayName("adhésion déjà traitée → DemandeAdhesionDejaTraiteeException")
        void rejeter_dejaTraitee_leveException() {
            when(structureSanitaireRepositoryPort.findById(any())).thenReturn(Optional.of(structureValidee()));

            assertThatThrownBy(() -> sut.rejeter(new RejectAdhesionCommand(STRUCTURE_ID, "motif")))
                    .isInstanceOf(DemandeAdhesionDejaTraiteeException.class);
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // ActivateStructureSanitaireUseCaseImpl /
    // DeactivateStructureSanitaireUseCaseImpl
    // ══════════════════════════════════════════════════════════════════════

    @Nested
    @ExtendWith(MockitoExtension.class)
    @DisplayName("ActivateStructureSanitaireUseCaseImpl")
    class ActivateStructureSanitaireTest {

        @Mock
        StructureSanitaireRepositoryPort structureSanitaireRepositoryPort;
        @Mock
        StructureSanitaireDetailAssembler structureSanitaireDetailAssembler;
        @InjectMocks
        ActivateStructureSanitaireUseCaseImpl sut;

        @Test
        @DisplayName("adhésion non validée → StructureSanitaireNonValideeException")
        void activer_adhesionNonValidee_leveException() {
            when(structureSanitaireRepositoryPort.findById(any())).thenReturn(Optional.of(structureEnAttente()));

            assertThatThrownBy(() -> sut.activer(new ActivateStructureSanitaireCommand(STRUCTURE_ID)))
                    .isInstanceOf(StructureSanitaireNonValideeException.class);
        }

        @Test
        @DisplayName("adhésion validée → activation réussie")
        void activer_adhesionValidee_succes() {
            StructureSanitaire structure = structureValidee();
            structure.desactiver();
            when(structureSanitaireRepositoryPort.findById(any())).thenReturn(Optional.of(structure));
            when(structureSanitaireRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(structureSanitaireDetailAssembler.assembler(any())).thenReturn(DETAIL_FICTIF);

            StructureSanitaireDetail result = sut.activer(new ActivateStructureSanitaireCommand(STRUCTURE_ID));

            assertThat(result).isEqualTo(DETAIL_FICTIF);
        }
    }

    @Nested
    @ExtendWith(MockitoExtension.class)
    @DisplayName("DeactivateStructureSanitaireUseCaseImpl")
    class DeactivateStructureSanitaireTest {

        @Mock
        StructureSanitaireRepositoryPort structureSanitaireRepositoryPort;
        @Mock
        StructureSanitaireDetailAssembler structureSanitaireDetailAssembler;
        @InjectMocks
        DeactivateStructureSanitaireUseCaseImpl sut;

        @Test
        @DisplayName("désactive une structure active")
        void desactiver_succes_desactive() {
            when(structureSanitaireRepositoryPort.findById(any())).thenReturn(Optional.of(structureValidee()));
            when(structureSanitaireRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(structureSanitaireDetailAssembler.assembler(any())).thenReturn(DETAIL_FICTIF);

            ArgumentCaptor<StructureSanitaire> captor = ArgumentCaptor.forClass(StructureSanitaire.class);

            sut.desactiver(new DeactivateStructureSanitaireCommand(STRUCTURE_ID));

            verify(structureSanitaireRepositoryPort).save(captor.capture());
            assertThat(captor.getValue().isActif()).isFalse();
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // AssignStructureToRegionUseCaseImpl / AssignStructureToPraUseCaseImpl
    // ══════════════════════════════════════════════════════════════════════

    @Nested
    @ExtendWith(MockitoExtension.class)
    @DisplayName("AssignStructureToRegionUseCaseImpl")
    class AssignStructureToRegionTest {

        @Mock
        StructureSanitaireRepositoryPort structureSanitaireRepositoryPort;
        @Mock
        RegionRepositoryPort regionRepositoryPort;
        @Mock
        StructureSanitaireDetailAssembler structureSanitaireDetailAssembler;
        @InjectMocks
        AssignStructureToRegionUseCaseImpl sut;

        @Test
        @DisplayName("affecte la structure à une région active")
        void affecter_succes_affecteRegion() {
            when(structureSanitaireRepositoryPort.findById(any())).thenReturn(Optional.of(structureEnAttente()));
            Region nouvelleRegion = Region.creer("DAKAR", "Dakar");
            when(regionRepositoryPort.findById(any())).thenReturn(Optional.of(nouvelleRegion));
            when(structureSanitaireRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(structureSanitaireDetailAssembler.assembler(any())).thenReturn(DETAIL_FICTIF);

            ArgumentCaptor<StructureSanitaire> captor = ArgumentCaptor.forClass(StructureSanitaire.class);

            sut.affecter(new AssignStructureToRegionCommand(STRUCTURE_ID, UUID.randomUUID()));

            verify(structureSanitaireRepositoryPort).save(captor.capture());
            assertThat(captor.getValue().getRegionId()).isEqualTo(nouvelleRegion.getId());
        }

        @Test
        @DisplayName("région inactive → RegionInactiveException")
        void affecter_regionInactive_leveException() {
            when(structureSanitaireRepositoryPort.findById(any())).thenReturn(Optional.of(structureEnAttente()));
            Region regionInactive = Region.creer("DAKAR", "Dakar");
            regionInactive.desactiver();
            when(regionRepositoryPort.findById(any())).thenReturn(Optional.of(regionInactive));

            assertThatThrownBy(() -> sut.affecter(
                    new AssignStructureToRegionCommand(STRUCTURE_ID, UUID.randomUUID())))
                    .isInstanceOf(RegionInactiveException.class);
        }
    }

    @Nested
    @ExtendWith(MockitoExtension.class)
    @DisplayName("AssignStructureToPraUseCaseImpl")
    class AssignStructureToPraTest {

        @Mock
        StructureSanitaireRepositoryPort structureSanitaireRepositoryPort;
        @Mock
        EntrepotRepositoryPort entrepotRepositoryPort;
        @Mock
        StructureSanitaireDetailAssembler structureSanitaireDetailAssembler;
        @InjectMocks
        AssignStructureToPraUseCaseImpl sut;

        @Test
        @DisplayName("affecte la structure à une PRA active")
        void affecter_succes_affectePra() {
            when(structureSanitaireRepositoryPort.findById(any())).thenReturn(Optional.of(structureEnAttente()));
            when(entrepotRepositoryPort.findById(any())).thenReturn(Optional.of(praActive()));
            when(structureSanitaireRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(structureSanitaireDetailAssembler.assembler(any())).thenReturn(DETAIL_FICTIF);

            ArgumentCaptor<StructureSanitaire> captor = ArgumentCaptor.forClass(StructureSanitaire.class);

            sut.affecter(new AssignStructureToPraCommand(STRUCTURE_ID, PRA_ID));

            verify(structureSanitaireRepositoryPort).save(captor.capture());
            assertThat(captor.getValue().getPraId()).isEqualTo(EntrepotId.of(PRA_ID));
        }

        @Test
        @DisplayName("entrepôt introuvable → EntrepotIntrouvableException")
        void affecter_entrepotIntrouvable_leveException() {
            when(structureSanitaireRepositoryPort.findById(any())).thenReturn(Optional.of(structureEnAttente()));
            when(entrepotRepositoryPort.findById(any())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> sut.affecter(new AssignStructureToPraCommand(STRUCTURE_ID, PRA_ID)))
                    .isInstanceOf(EntrepotIntrouvableException.class);
        }

        @Test
        @DisplayName("entrepôt de type PNA_CENTRAL → TypeEntrepotInvalideException")
        void affecter_typeInvalide_leveException() {
            when(structureSanitaireRepositoryPort.findById(any())).thenReturn(Optional.of(structureEnAttente()));
            Entrepot pnaCentral = Entrepot.reconstruct(EntrepotId.of(PRA_ID), "PNA-CENTRAL", "PNA Centrale",
                    TypeEntrepot.PNA_CENTRAL, null, null, null, null, true, Instant.now(), Instant.now());
            when(entrepotRepositoryPort.findById(any())).thenReturn(Optional.of(pnaCentral));

            assertThatThrownBy(() -> sut.affecter(new AssignStructureToPraCommand(STRUCTURE_ID, PRA_ID)))
                    .isInstanceOf(TypeEntrepotInvalideException.class);
        }

        @Test
        @DisplayName("PRA inactive → EntrepotInactifException")
        void affecter_praInactive_leveException() {
            when(structureSanitaireRepositoryPort.findById(any())).thenReturn(Optional.of(structureEnAttente()));
            Entrepot praInactive = Entrepot.reconstruct(EntrepotId.of(PRA_ID), "PRA-THIES", "PRA Thiès",
                    TypeEntrepot.PRA, RegionId.of(REGION_ID), null, null, null, false, Instant.now(), Instant.now());
            when(entrepotRepositoryPort.findById(any())).thenReturn(Optional.of(praInactive));

            assertThatThrownBy(() -> sut.affecter(new AssignStructureToPraCommand(STRUCTURE_ID, PRA_ID)))
                    .isInstanceOf(EntrepotInactifException.class);
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // GetStructureSanitaireUseCaseImpl / ListStructuresSanitairesUseCaseImpl
    // ══════════════════════════════════════════════════════════════════════

    @Nested
    @ExtendWith(MockitoExtension.class)
    @DisplayName("GetStructureSanitaireUseCaseImpl")
    class GetStructureSanitaireTest {

        @Mock
        StructureSanitaireRepositoryPort structureSanitaireRepositoryPort;
        @Mock
        StructureSanitaireDetailAssembler structureSanitaireDetailAssembler;
        @InjectMocks
        GetStructureSanitaireUseCaseImpl sut;

        @Test
        @DisplayName("structure trouvée → retourne le détail assemblé")
        void obtenir_succes_retourneDetail() {
            StructureSanitaire structure = structureEnAttente();
            when(structureSanitaireRepositoryPort.findById(any())).thenReturn(Optional.of(structure));
            when(structureSanitaireDetailAssembler.assembler(structure)).thenReturn(DETAIL_FICTIF);

            StructureSanitaireDetail result = sut.obtenir(new GetStructureSanitaireQuery(STRUCTURE_ID));

            assertThat(result).isEqualTo(DETAIL_FICTIF);
        }

        @Test
        @DisplayName("structure introuvable → StructureSanitaireIntrouvableException")
        void obtenir_introuvable_leveException() {
            when(structureSanitaireRepositoryPort.findById(any())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> sut.obtenir(new GetStructureSanitaireQuery(STRUCTURE_ID)))
                    .isInstanceOf(StructureSanitaireIntrouvableException.class);
        }
    }

    @Nested
    @ExtendWith(MockitoExtension.class)
    @DisplayName("ListStructuresSanitairesUseCaseImpl")
    class ListStructuresSanitairesTest {

        @Mock
        StructureSanitaireRepositoryPort structureSanitaireRepositoryPort;
        @Mock
        StructureSanitaireDetailAssembler structureSanitaireDetailAssembler;
        @InjectMocks
        ListStructuresSanitairesUseCaseImpl sut;

        @Test
        @DisplayName("construit les critères et la pagination, puis mappe le résultat")
        void lister_succes_mappePage() {
            StructureSanitaire structure = structureEnAttente();
            when(structureSanitaireRepositoryPort.search(any(), any()))
                    .thenReturn(PageResult.of(List.of(structure), 0, 20, 1));
            when(structureSanitaireDetailAssembler.assembler(structure)).thenReturn(DETAIL_FICTIF);

            StructureSanitairePage result = sut.lister(new ListStructuresSanitairesQuery(null, null, null, null,
                    StatutAdhesion.EN_ATTENTE_VALIDATION, null, 0, 20, "nom", "ASC"));

            assertThat(result.content()).containsExactly(DETAIL_FICTIF);
        }

        @Test
        @DisplayName("aucun résultat → page vide")
        void lister_aucunResultat_pageVide() {
            when(structureSanitaireRepositoryPort.search(any(), any()))
                    .thenReturn(PageResult.of(List.of(), 0, 20, 0));

            StructureSanitairePage result = sut.lister(
                    new ListStructuresSanitairesQuery(null, null, null, null, null, null, null, null, null, null));

            assertThat(result.content()).isEmpty();
        }
    }
}
