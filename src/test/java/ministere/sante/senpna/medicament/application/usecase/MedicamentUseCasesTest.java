package ministere.sante.senpna.medicament.application.usecase;

import ministere.sante.senpna.medicament.application.service.MedicamentDetailAssembler;
import ministere.sante.senpna.medicament.application.usecase.medicament.ArchiveMedicamentUseCaseImpl;
import ministere.sante.senpna.medicament.application.usecase.medicament.CreateMedicamentUseCaseImpl;
import ministere.sante.senpna.medicament.application.usecase.medicament.UpdateMedicamentUseCaseImpl;
import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.ArchiveMedicamentCommand;
import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.CreateMedicamentCommand;
import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.MedicamentDetail;
import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.UpdateMedicamentCommand;
import ministere.sante.senpna.medicament.domain.exception.famille.FamilleInactiveException;
import ministere.sante.senpna.medicament.domain.exception.famille.FamilleIntrouvableException;
import ministere.sante.senpna.medicament.domain.exception.forme.FormeInactiveException;
import ministere.sante.senpna.medicament.domain.exception.forme.FormeIntrouvableException;
import ministere.sante.senpna.medicament.domain.exception.medicament.CodeMedicamentDejaUtiliseException;
import ministere.sante.senpna.medicament.domain.exception.medicament.MedicamentIntrouvableException;
import ministere.sante.senpna.medicament.domain.model.Famille;
import ministere.sante.senpna.medicament.domain.model.Forme;
import ministere.sante.senpna.medicament.domain.model.Medicament;
import ministere.sante.senpna.medicament.domain.port.out.FamilleRepositoryPort;
import ministere.sante.senpna.medicament.domain.port.out.FormeRepositoryPort;
import ministere.sante.senpna.medicament.domain.port.out.MedicamentRepositoryPort;
import ministere.sante.senpna.medicament.domain.valueobject.FamilleId;
import ministere.sante.senpna.medicament.domain.valueobject.FormeId;
import ministere.sante.senpna.medicament.domain.valueobject.MedicamentId;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
import static org.mockito.Mockito.when;

@DisplayName("Medicament Use Cases — create / update / archive / desarchive")
class MedicamentUseCasesTest {

        private static final UUID MEDICAMENT_ID = UUID.randomUUID();
        private static final UUID FORME_ID = UUID.randomUUID();
        private static final UUID FAMILLE_ID = UUID.randomUUID();

        private static Famille familleActive() {
                return Famille.reconstruct(FamilleId.of(FAMILLE_ID), "ANTALGIQUE", "Antalgique", null, true,
                                Instant.now(), Instant.now());
        }

        private static Forme formeActive() {
                return Forme.reconstruct(FormeId.of(FORME_ID), "COMPRIME", "Comprimé", null, true, Instant.now(),
                                Instant.now());
        }

        /**
         * Assembleur simplifié — pas d'appels réseau/DB, se contente de projeter
         * l'agrégat.
         */
        private static MedicamentDetailAssembler assemblerSimplifie() {
                return new MedicamentDetailAssembler(null, null) {
                        @Override
                        public MedicamentDetail assembler(Medicament medicament) {
                                return new MedicamentDetail(medicament.getId().getValue(), medicament.getCode(),
                                                medicament.getNomCommercial(), medicament.getDci(),
                                                medicament.getDosage(),
                                                medicament.getFormeId().getValue(), "Comprimé",
                                                medicament.getFamilleId().getValue(),
                                                "Antalgique", medicament.getVoieAdministration(),
                                                medicament.getTemperatureConservation(),
                                                medicament.getProgrammeSante(),
                                                medicament.getDelaiApprovisionnementJours(),
                                                medicament.isNecessiteOrdonnance(), medicament.getFabricant(),
                                                medicament.getStockMinimum(), medicament.getStockMaximum(),
                                                medicament.isActif(),
                                                medicament.getCreatedAt(), medicament.getUpdatedAt());
                        }
                };
        }

        @ExtendWith(MockitoExtension.class)
        @Nested
        @DisplayName("creer")
        class Creer {

                @Mock
                private MedicamentRepositoryPort medicamentRepositoryPort;
                @Mock
                private FamilleRepositoryPort familleRepositoryPort;
                @Mock
                private FormeRepositoryPort formeRepositoryPort;

                private final MedicamentDetailAssembler assembler = assemblerSimplifie();

                private CreateMedicamentCommand commandeValide() {
                        return new CreateMedicamentCommand("PARA500", "Doliprane", "Paracétamol", "500 mg", FORME_ID,
                                        FAMILLE_ID, null, null, null, 30, false, "Sanofi", 1000, 10000);
                }

                @Test
                @DisplayName("crée le médicament quand le code est disponible et famille/forme actives")
                void creeQuandValide() {
                        CreateMedicamentUseCaseImpl useCase = new CreateMedicamentUseCaseImpl(medicamentRepositoryPort,
                                        familleRepositoryPort, formeRepositoryPort, assembler);
                        when(medicamentRepositoryPort.existsByCodeIgnoreCase("PARA500")).thenReturn(false);
                        when(familleRepositoryPort.findById(FamilleId.of(FAMILLE_ID)))
                                        .thenReturn(Optional.of(familleActive()));
                        when(formeRepositoryPort.findById(FormeId.of(FORME_ID))).thenReturn(Optional.of(formeActive()));
                        when(medicamentRepositoryPort.save(any(Medicament.class)))
                                        .thenAnswer(inv -> inv.getArgument(0));

                        MedicamentDetail result = useCase.creer(commandeValide());

                        assertThat(result.code()).isEqualTo("PARA500");
                        assertThat(result.actif()).isTrue();
                }

                @Test
                @DisplayName("rejette la création quand le code est déjà utilisé")
                void rejetteCodeDejaUtilise() {
                        CreateMedicamentUseCaseImpl useCase = new CreateMedicamentUseCaseImpl(medicamentRepositoryPort,
                                        familleRepositoryPort, formeRepositoryPort, assembler);
                        when(medicamentRepositoryPort.existsByCodeIgnoreCase("PARA500")).thenReturn(true);

                        var commandeValide = commandeValide();
                        assertThatThrownBy(() -> useCase.creer(commandeValide))
                                        .isInstanceOf(CodeMedicamentDejaUtiliseException.class);

                        verify(medicamentRepositoryPort, never()).save(any());
                }

                @Test
                @DisplayName("lève FamilleIntrouvableException quand la famille n'existe pas")
                void leveFamilleIntrouvable() {
                        CreateMedicamentUseCaseImpl useCase = new CreateMedicamentUseCaseImpl(medicamentRepositoryPort,
                                        familleRepositoryPort, formeRepositoryPort, assembler);
                        when(medicamentRepositoryPort.existsByCodeIgnoreCase("PARA500")).thenReturn(false);
                        when(familleRepositoryPort.findById(FamilleId.of(FAMILLE_ID))).thenReturn(Optional.empty());

                        var commandeValide = commandeValide();
                        assertThatThrownBy(() -> useCase.creer(commandeValide))
                                        .isInstanceOf(FamilleIntrouvableException.class);
                }

                @Test
                @DisplayName("lève FamilleInactiveException quand la famille est archivée")
                void leveFamilleInactive() {
                        CreateMedicamentUseCaseImpl useCase = new CreateMedicamentUseCaseImpl(medicamentRepositoryPort,
                                        familleRepositoryPort, formeRepositoryPort, assembler);
                        Famille familleArchivee = Famille.reconstruct(FamilleId.of(FAMILLE_ID), "ANTALGIQUE",
                                        "Antalgique",
                                        null, false, Instant.now(), Instant.now());
                        when(medicamentRepositoryPort.existsByCodeIgnoreCase("PARA500")).thenReturn(false);
                        when(familleRepositoryPort.findById(FamilleId.of(FAMILLE_ID)))
                                        .thenReturn(Optional.of(familleArchivee));

                        var commandeValide = commandeValide();
                        assertThatThrownBy(() -> useCase.creer(commandeValide))
                                        .isInstanceOf(FamilleInactiveException.class);

                        verify(medicamentRepositoryPort, never()).save(any());
                }

                @Test
                @DisplayName("lève FormeIntrouvableException quand la forme n'existe pas")
                void leveFormeIntrouvable() {
                        CreateMedicamentUseCaseImpl useCase = new CreateMedicamentUseCaseImpl(medicamentRepositoryPort,
                                        familleRepositoryPort, formeRepositoryPort, assembler);
                        when(medicamentRepositoryPort.existsByCodeIgnoreCase("PARA500")).thenReturn(false);
                        when(familleRepositoryPort.findById(FamilleId.of(FAMILLE_ID)))
                                        .thenReturn(Optional.of(familleActive()));
                        when(formeRepositoryPort.findById(FormeId.of(FORME_ID))).thenReturn(Optional.empty());

                        var commandeValide = commandeValide();
                        assertThatThrownBy(() -> useCase.creer(commandeValide))
                                        .isInstanceOf(FormeIntrouvableException.class);
                }

                @Test
                @DisplayName("lève FormeInactiveException quand la forme est archivée")
                void leveFormeInactive() {
                        CreateMedicamentUseCaseImpl useCase = new CreateMedicamentUseCaseImpl(medicamentRepositoryPort,
                                        familleRepositoryPort, formeRepositoryPort, assembler);
                        Forme formeArchivee = Forme.reconstruct(FormeId.of(FORME_ID), "COMPRIME", "Comprimé", null,
                                        false,
                                        Instant.now(), Instant.now());
                        when(medicamentRepositoryPort.existsByCodeIgnoreCase("PARA500")).thenReturn(false);
                        when(familleRepositoryPort.findById(FamilleId.of(FAMILLE_ID)))
                                        .thenReturn(Optional.of(familleActive()));
                        when(formeRepositoryPort.findById(FormeId.of(FORME_ID))).thenReturn(Optional.of(formeArchivee));

                        var commandeValide = commandeValide();
                        assertThatThrownBy(() -> useCase.creer(commandeValide))
                                        .isInstanceOf(FormeInactiveException.class);
                }
        }

        @ExtendWith(MockitoExtension.class)
        @Nested
        @DisplayName("modifier")
        class Modifier {

                @Mock
                private MedicamentRepositoryPort medicamentRepositoryPort;
                @Mock
                private FamilleRepositoryPort familleRepositoryPort;
                @Mock
                private FormeRepositoryPort formeRepositoryPort;

                private final MedicamentDetailAssembler assembler = assemblerSimplifie();

                @Test
                @DisplayName("lève MedicamentIntrouvableException quand le médicament n'existe pas")
                void leveIntrouvableQuandInexistant() {
                        UpdateMedicamentUseCaseImpl useCase = new UpdateMedicamentUseCaseImpl(medicamentRepositoryPort,
                                        familleRepositoryPort, formeRepositoryPort, assembler);
                        when(medicamentRepositoryPort.findById(MedicamentId.of(MEDICAMENT_ID)))
                                        .thenReturn(Optional.empty());

                        var updateMedicamentCommand = new UpdateMedicamentCommand(MEDICAMENT_ID, "Nom", "DCI", "10mg", FORME_ID, FAMILLE_ID, null, null, null, null, false, null, null, null);
                        assertThatThrownBy(() -> useCase.modifier(updateMedicamentCommand))
                                        .isInstanceOf(MedicamentIntrouvableException.class);
                }

                @Test
                @DisplayName("modifie le médicament quand famille et forme sont actives")
                void modifieQuandValide() {
                        UpdateMedicamentUseCaseImpl useCase = new UpdateMedicamentUseCaseImpl(medicamentRepositoryPort,
                                        familleRepositoryPort, formeRepositoryPort, assembler);
                        Medicament existant = Medicament.builder()
                            .id(MedicamentId.of(MEDICAMENT_ID))
                            .code("PARA500")
                            .nomCommercial("Doliprane")
                            .dci("Paracétamol")
                            .dosage("500 mg")
                            .formeId(FormeId.of(FORME_ID))
                            .familleId(FamilleId.of(FAMILLE_ID))
                            .voieAdministration(null)
                            .temperatureConservation(null)
                            .programmeSante(null)
                            .delaiApprovisionnementJours(30)
                            .necessiteOrdonnance(false)
                            .fabricant(null)
                            .stockMinimum(1000)
                            .stockMaximum(10000)
                            .actif(true)
                            .createdAt(Instant.now())
                            .updatedAt(Instant.now())
                            .build();
                        when(medicamentRepositoryPort.findById(MedicamentId.of(MEDICAMENT_ID)))
                                        .thenReturn(Optional.of(existant));
                        when(familleRepositoryPort.findById(FamilleId.of(FAMILLE_ID)))
                                        .thenReturn(Optional.of(familleActive()));
                        when(formeRepositoryPort.findById(FormeId.of(FORME_ID))).thenReturn(Optional.of(formeActive()));
                        when(medicamentRepositoryPort.save(any(Medicament.class)))
                                        .thenAnswer(inv -> inv.getArgument(0));

                        MedicamentDetail result = useCase
                                        .modifier(new UpdateMedicamentCommand(MEDICAMENT_ID, "Efferalgan",
                                                        "Paracétamol", "1000 mg", FORME_ID, FAMILLE_ID, null, null,
                                                        null, 15, true, "UPSA", 500, 5000));

                        assertThat(result.nomCommercial()).isEqualTo("Efferalgan");
                }
        }

        @ExtendWith(MockitoExtension.class)
        @Nested
        @DisplayName("archiver / desarchiver")
        class ArchiverDesarchiver {

                @Mock
                private MedicamentRepositoryPort medicamentRepositoryPort;

                private final MedicamentDetailAssembler assembler = assemblerSimplifie();

                @Test
                @DisplayName("archive un médicament actif")
                void archiveMedicamentActif() {
                        ArchiveMedicamentUseCaseImpl useCase = new ArchiveMedicamentUseCaseImpl(
                                        medicamentRepositoryPort,
                                        assembler);
                        Medicament actif = Medicament.builder()
                            .id(MedicamentId.of(MEDICAMENT_ID))
                            .code("PARA500")
                            .nomCommercial("Doliprane")
                            .dci("Paracétamol")
                            .dosage("500 mg")
                            .formeId(FormeId.of(FORME_ID))
                            .familleId(FamilleId.of(FAMILLE_ID))
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
                        when(medicamentRepositoryPort.findById(MedicamentId.of(MEDICAMENT_ID)))
                                        .thenReturn(Optional.of(actif));
                        when(medicamentRepositoryPort.save(any(Medicament.class)))
                                        .thenAnswer(inv -> inv.getArgument(0));

                        MedicamentDetail result = useCase.archiver(new ArchiveMedicamentCommand(MEDICAMENT_ID));

                        assertThat(result.actif()).isFalse();
                }
        }
}
