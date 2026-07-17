package ministere.sante.senpna.medicament.application.usecase;

import ministere.sante.senpna.medicament.application.service.ConditionnementDetailAssembler;
import ministere.sante.senpna.medicament.application.usecase.conditionnement.ArchiveConditionnementUseCaseImpl;
import ministere.sante.senpna.medicament.application.usecase.conditionnement.CreateConditionnementUseCaseImpl;
import ministere.sante.senpna.medicament.domain.command.ConditionnementCommands.ArchiveConditionnementCommand;
import ministere.sante.senpna.medicament.domain.command.ConditionnementCommands.ConditionnementDetail;
import ministere.sante.senpna.medicament.domain.command.ConditionnementCommands.CreateConditionnementCommand;
import ministere.sante.senpna.medicament.domain.exception.conditionnement.ConditionnementIntrouvableException;
import ministere.sante.senpna.medicament.domain.exception.conditionnement.DerniereUniteBaseException;
import ministere.sante.senpna.medicament.domain.exception.conditionnement.NiveauConditionnementDejaUtiliseException;
import ministere.sante.senpna.medicament.domain.exception.conditionnement.NomConditionnementDejaUtiliseException;
import ministere.sante.senpna.medicament.domain.exception.conditionnement.UniteBaseDejaDefinieException;
import ministere.sante.senpna.medicament.domain.exception.medicament.MedicamentIntrouvableException;
import ministere.sante.senpna.medicament.domain.model.Conditionnement;
import ministere.sante.senpna.medicament.domain.model.Medicament;
import ministere.sante.senpna.medicament.domain.port.out.ConditionnementRepositoryPort;
import ministere.sante.senpna.medicament.domain.port.out.MedicamentRepositoryPort;
import ministere.sante.senpna.medicament.domain.valueobject.ConditionnementId;
import ministere.sante.senpna.medicament.domain.valueobject.FamilleId;
import ministere.sante.senpna.medicament.domain.valueobject.FormeId;
import ministere.sante.senpna.medicament.domain.valueobject.MedicamentId;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("Conditionnement Use Cases — create / update / archive")
class ConditionnementUseCasesTest {

        private static final UUID MEDICAMENT_ID = UUID.randomUUID();
        private static final UUID CONDITIONNEMENT_ID = UUID.randomUUID();

        private static Medicament medicamentActif() {
                return Medicament.builder()
                    .id(MedicamentId.of(MEDICAMENT_ID))
                    .code("PARA500")
                    .nomCommercial("Doliprane")
                    .dci("Paracétamol")
                    .dosage("500 mg")
                    .formeId(FormeId.generate())
                    .familleId(FamilleId.generate())
                    .voieAdministration(null)
                    .temperatureConservation(null)
                    .programmeSante(null)
                    .delaiApprovisionnementJours(null)
                    .necessiteOrdonnance(false)
                    .fabricant(null)
                    .stockMinimum(null)
                    .stockMaximum(null)
                    .actif(true)
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .build();
        }

        private static final ConditionnementDetailAssembler ASSEMBLER = new ConditionnementDetailAssembler();

        @ExtendWith(MockitoExtension.class)
        @Nested
        @DisplayName("creer")
        class Creer {

                @Mock
                private ConditionnementRepositoryPort conditionnementRepositoryPort;
                @Mock
                private MedicamentRepositoryPort medicamentRepositoryPort;

                @Test
                @DisplayName("crée le conditionnement quand niveau/nom disponibles et pas d'unité de base existante")
                void creeQuandValide() {
                        CreateConditionnementUseCaseImpl useCase = new CreateConditionnementUseCaseImpl(
                                        conditionnementRepositoryPort, medicamentRepositoryPort, ASSEMBLER);
                        when(medicamentRepositoryPort.findById(MedicamentId.of(MEDICAMENT_ID)))
                                        .thenReturn(Optional.of(medicamentActif()));
                        when(conditionnementRepositoryPort.existsByMedicamentIdAndNiveau(any(),
                                        org.mockito.ArgumentMatchers.eq(1)))
                                        .thenReturn(false);
                        when(conditionnementRepositoryPort.existsByMedicamentIdAndNomIgnoreCase(any(), any()))
                                        .thenReturn(false);
                        when(conditionnementRepositoryPort.existsUniteBaseByMedicamentId(any())).thenReturn(false);
                        when(conditionnementRepositoryPort.save(any(Conditionnement.class)))
                                        .thenAnswer(inv -> inv.getArgument(0));

                        ConditionnementDetail result = useCase
                                        .creer(new CreateConditionnementCommand(MEDICAMENT_ID, "Comprimé",
                                                        1, BigDecimal.ONE, true, null, null));

                        assertThat(result.nom()).isEqualTo("Comprimé");
                        assertThat(result.estUniteBase()).isTrue();
                }

                @Test
                @DisplayName("lève MedicamentIntrouvableException quand le médicament n'existe pas")
                void leveMedicamentIntrouvable() {
                        CreateConditionnementUseCaseImpl useCase = new CreateConditionnementUseCaseImpl(
                                        conditionnementRepositoryPort, medicamentRepositoryPort, ASSEMBLER);
                        when(medicamentRepositoryPort.findById(MedicamentId.of(MEDICAMENT_ID)))
                                        .thenReturn(Optional.empty());

                        var createConditionnementCommand = new CreateConditionnementCommand(MEDICAMENT_ID, "Comprimé",
                                        1, BigDecimal.ONE, true, null, null);
                        assertThatThrownBy(() -> useCase.creer(createConditionnementCommand))
                                        .isInstanceOf(MedicamentIntrouvableException.class);
                }

                @Test
                @DisplayName("lève NiveauConditionnementDejaUtiliseException quand le niveau est déjà occupé")
                void leveNiveauDejaUtilise() {
                        CreateConditionnementUseCaseImpl useCase = new CreateConditionnementUseCaseImpl(
                                        conditionnementRepositoryPort, medicamentRepositoryPort, ASSEMBLER);
                        when(medicamentRepositoryPort.findById(MedicamentId.of(MEDICAMENT_ID)))
                                        .thenReturn(Optional.of(medicamentActif()));
                        when(conditionnementRepositoryPort.existsByMedicamentIdAndNiveau(any(),
                                        org.mockito.ArgumentMatchers.eq(1)))
                                        .thenReturn(true);

                        var createConditionnementCommand = new CreateConditionnementCommand(MEDICAMENT_ID, "Comprimé",
                                        1, BigDecimal.ONE, true, null, null);
                        assertThatThrownBy(() -> useCase.creer(createConditionnementCommand))
                                        .isInstanceOf(NiveauConditionnementDejaUtiliseException.class);

                        verify(conditionnementRepositoryPort, never()).save(any());
                }

                @Test
                @DisplayName("lève NomConditionnementDejaUtiliseException quand le nom est déjà pris pour ce médicament")
                void leveNomDejaUtilise() {
                        CreateConditionnementUseCaseImpl useCase = new CreateConditionnementUseCaseImpl(
                                        conditionnementRepositoryPort, medicamentRepositoryPort, ASSEMBLER);
                        when(medicamentRepositoryPort.findById(MedicamentId.of(MEDICAMENT_ID)))
                                        .thenReturn(Optional.of(medicamentActif()));
                        when(conditionnementRepositoryPort.existsByMedicamentIdAndNiveau(any(),
                                        org.mockito.ArgumentMatchers.eq(2)))
                                        .thenReturn(false);
                        when(conditionnementRepositoryPort.existsByMedicamentIdAndNomIgnoreCase(any(), any()))
                                        .thenReturn(true);

                        var createConditionnementCommand = new CreateConditionnementCommand(MEDICAMENT_ID, "Boîte", 2,
                                        new BigDecimal("20"), false, null, null);
                        assertThatThrownBy(() -> useCase.creer(createConditionnementCommand))
                                        .isInstanceOf(NomConditionnementDejaUtiliseException.class);
                }

                @Test
                @DisplayName("lève UniteBaseDejaDefinieException quand une unité de base existe déjà")
                void leveUniteBaseDejaDefinie() {
                        CreateConditionnementUseCaseImpl useCase = new CreateConditionnementUseCaseImpl(
                                        conditionnementRepositoryPort, medicamentRepositoryPort, ASSEMBLER);
                        when(medicamentRepositoryPort.findById(MedicamentId.of(MEDICAMENT_ID)))
                                        .thenReturn(Optional.of(medicamentActif()));
                        when(conditionnementRepositoryPort.existsByMedicamentIdAndNiveau(any(),
                                        org.mockito.ArgumentMatchers.eq(1)))
                                        .thenReturn(false);
                        when(conditionnementRepositoryPort.existsByMedicamentIdAndNomIgnoreCase(any(), any()))
                                        .thenReturn(false);
                        when(conditionnementRepositoryPort.existsUniteBaseByMedicamentId(any())).thenReturn(true);

                        var createConditionnementCommand = new CreateConditionnementCommand(MEDICAMENT_ID, "Comprimé",
                                        1, BigDecimal.ONE, true, null, null);
                        assertThatThrownBy(() -> useCase.creer(createConditionnementCommand))
                                        .isInstanceOf(UniteBaseDejaDefinieException.class);

                        verify(conditionnementRepositoryPort, never()).save(any());
                }
        }

        @ExtendWith(MockitoExtension.class)
        @Nested
        @DisplayName("archiver")
        class Archiver {

                @Mock
                private ConditionnementRepositoryPort conditionnementRepositoryPort;

                @Test
                @DisplayName("lève ConditionnementIntrouvableException quand il n'existe pas")
                void leveIntrouvableQuandInexistant() {
                        ArchiveConditionnementUseCaseImpl useCase = new ArchiveConditionnementUseCaseImpl(
                                        conditionnementRepositoryPort, ASSEMBLER);
                        when(conditionnementRepositoryPort.findById(ConditionnementId.of(CONDITIONNEMENT_ID)))
                                        .thenReturn(Optional.empty());

                        var archiveCommand = new ArchiveConditionnementCommand(CONDITIONNEMENT_ID);
                        assertThatThrownBy(
                                        () -> useCase.archiver(archiveCommand))
                                        .isInstanceOf(ConditionnementIntrouvableException.class);
                }

                @Test
                @DisplayName("lève DerniereUniteBaseException quand c'est l'unique unité de base active du médicament")
                void leveDerniereUniteBase() {
                        ArchiveConditionnementUseCaseImpl useCase = new ArchiveConditionnementUseCaseImpl(
                                        conditionnementRepositoryPort, ASSEMBLER);
                        Conditionnement uniteBase = Conditionnement.builder()
                            .id(ConditionnementId.of(CONDITIONNEMENT_ID))
                            .medicamentId(MedicamentId.of(MEDICAMENT_ID))
                            .nom("Comprimé")
                            .niveau(1)
                            .quantiteUniteBase(BigDecimal.ONE)
                            .estUniteBase(true)
                            .prixAchat(null)
                            .prixVente(null)
                            .actif(true)
                            .createdAt(Instant.now())
                            .updatedAt(Instant.now())
                            .build();
                        when(conditionnementRepositoryPort.findById(ConditionnementId.of(CONDITIONNEMENT_ID)))
                                        .thenReturn(Optional.of(uniteBase));
                        when(conditionnementRepositoryPort.estUniqueUniteBaseActive(MedicamentId.of(MEDICAMENT_ID),
                                        ConditionnementId.of(CONDITIONNEMENT_ID))).thenReturn(true);

                        var archiveCommand = new ArchiveConditionnementCommand(CONDITIONNEMENT_ID);
                        assertThatThrownBy(
                                        () -> useCase.archiver(archiveCommand))
                                        .isInstanceOf(DerniereUniteBaseException.class);

                        verify(conditionnementRepositoryPort, never()).save(any());
                }

                @Test
                @DisplayName("archive un conditionnement non-unité-de-base sans restriction")
                void archiveConditionnementNonUniteBase() {
                        ArchiveConditionnementUseCaseImpl useCase = new ArchiveConditionnementUseCaseImpl(
                                        conditionnementRepositoryPort, ASSEMBLER);
                        Conditionnement boite = Conditionnement.builder()
                            .id(ConditionnementId.of(CONDITIONNEMENT_ID))
                            .medicamentId(MedicamentId.of(MEDICAMENT_ID))
                            .nom("Boîte")
                            .niveau(2)
                            .quantiteUniteBase(new BigDecimal("20"))
                            .estUniteBase(false)
                            .prixAchat(null)
                            .prixVente(null)
                            .actif(true)
                            .createdAt(Instant.now())
                            .updatedAt(Instant.now())
                            .build();
                        when(conditionnementRepositoryPort.findById(ConditionnementId.of(CONDITIONNEMENT_ID)))
                                        .thenReturn(Optional.of(boite));
                        when(conditionnementRepositoryPort.save(any(Conditionnement.class)))
                                        .thenAnswer(inv -> inv.getArgument(0));

                        ConditionnementDetail result = useCase
                                        .archiver(new ArchiveConditionnementCommand(CONDITIONNEMENT_ID));

                        assertThat(result.actif()).isFalse();
                }
        }
}
