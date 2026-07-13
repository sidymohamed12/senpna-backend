package ministere.sante.senpna.medicament.application.facade;

import ministere.sante.senpna.medicament.domain.command.ConditionnementCommands.ArchiveConditionnementCommand;
import ministere.sante.senpna.medicament.domain.command.ConditionnementCommands.ConditionnementDetail;
import ministere.sante.senpna.medicament.domain.command.ConditionnementCommands.ConditionnementPage;
import ministere.sante.senpna.medicament.domain.command.ConditionnementCommands.CreateConditionnementCommand;
import ministere.sante.senpna.medicament.domain.command.ConditionnementCommands.DesarchiveConditionnementCommand;
import ministere.sante.senpna.medicament.domain.command.ConditionnementCommands.GetConditionnementQuery;
import ministere.sante.senpna.medicament.domain.command.ConditionnementCommands.ListConditionnementsQuery;
import ministere.sante.senpna.medicament.domain.command.ConditionnementCommands.UpdateConditionnementCommand;
import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.ArchiveMedicamentCommand;
import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.CreateMedicamentCommand;
import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.DesarchiveMedicamentCommand;
import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.GetMedicamentQuery;
import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.ListMedicamentsQuery;
import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.MedicamentDetail;
import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.MedicamentPage;
import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.UpdateMedicamentCommand;
import ministere.sante.senpna.medicament.domain.port.in.conditionnement.ArchiveConditionnementUseCase;
import ministere.sante.senpna.medicament.domain.port.in.conditionnement.CreateConditionnementUseCase;
import ministere.sante.senpna.medicament.domain.port.in.conditionnement.DesarchiveConditionnementUseCase;
import ministere.sante.senpna.medicament.domain.port.in.conditionnement.GetConditionnementUseCase;
import ministere.sante.senpna.medicament.domain.port.in.conditionnement.ListConditionnementsUseCase;
import ministere.sante.senpna.medicament.domain.port.in.conditionnement.UpdateConditionnementUseCase;
import ministere.sante.senpna.medicament.domain.port.in.medicament.ArchiveMedicamentUseCase;
import ministere.sante.senpna.medicament.domain.port.in.medicament.CreateMedicamentUseCase;
import ministere.sante.senpna.medicament.domain.port.in.medicament.DesarchiveMedicamentUseCase;
import ministere.sante.senpna.medicament.domain.port.in.medicament.GetMedicamentUseCase;
import ministere.sante.senpna.medicament.domain.port.in.medicament.ListMedicamentsUseCase;
import ministere.sante.senpna.medicament.domain.port.in.medicament.UpdateMedicamentUseCase;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("MedicamentFacade / ConditionnementFacade — délégation aux use cases")
class MedicamentAndConditionnementFacadesTest {

        @Nested
        @DisplayName("MedicamentFacade")
        class MedicamentFacadeTests {

                @Mock
                CreateMedicamentUseCase createMedicamentUseCase;
                @Mock
                UpdateMedicamentUseCase updateMedicamentUseCase;
                @Mock
                ArchiveMedicamentUseCase archiveMedicamentUseCase;
                @Mock
                DesarchiveMedicamentUseCase desarchiveMedicamentUseCase;
                @Mock
                GetMedicamentUseCase getMedicamentUseCase;
                @Mock
                ListMedicamentsUseCase listMedicamentsUseCase;

                @Test
                @DisplayName("chaque méthode délègue au use case correspondant")
                void chaqueMethodeDelegue() {
                        MedicamentFacade sut = new MedicamentFacade(createMedicamentUseCase, updateMedicamentUseCase,
                                        archiveMedicamentUseCase, desarchiveMedicamentUseCase, getMedicamentUseCase,
                                        listMedicamentsUseCase);
                        UUID id = UUID.randomUUID();
                        MedicamentDetail detail = new MedicamentDetail(id, "C", "N", "D", "10mg", id, null, id, null,
                                        null,
                                        null, null, null, false, null, null, null, true, null, null);
                        MedicamentPage page = new MedicamentPage(List.of(), 0, 20, 0, 0);

                        when(createMedicamentUseCase.creer(any())).thenReturn(detail);
                        when(updateMedicamentUseCase.modifier(any())).thenReturn(detail);
                        when(archiveMedicamentUseCase.archiver(any())).thenReturn(detail);
                        when(desarchiveMedicamentUseCase.desarchiver(any())).thenReturn(detail);
                        when(getMedicamentUseCase.obtenir(any())).thenReturn(detail);
                        when(listMedicamentsUseCase.lister(any())).thenReturn(page);

                        assertThat(sut.creerMedicament(
                                        new CreateMedicamentCommand("C", "N", "D", "10mg", id, id, null, null,
                                                        null, null, false, null, null, null)))
                                        .isSameAs(detail);
                        assertThat(sut.modifierMedicament(
                                        new UpdateMedicamentCommand(id, "N", "D", "10mg", id, id, null,
                                                        null, null, null, false, null, null, null)))
                                        .isSameAs(detail);
                        assertThat(sut.archiverMedicament(new ArchiveMedicamentCommand(id))).isSameAs(detail);
                        assertThat(sut.desarchiverMedicament(new DesarchiveMedicamentCommand(id))).isSameAs(detail);
                        assertThat(sut.obtenirMedicament(new GetMedicamentQuery(id))).isSameAs(detail);
                        assertThat(sut.listerMedicaments(
                                        new ListMedicamentsQuery(null, null, null, null, 0, 20, null, null)))
                                        .isSameAs(page);
                }
        }

        @Nested
        @DisplayName("ConditionnementFacade")
        class ConditionnementFacadeTests {

                @Mock
                CreateConditionnementUseCase createConditionnementUseCase;
                @Mock
                UpdateConditionnementUseCase updateConditionnementUseCase;
                @Mock
                ArchiveConditionnementUseCase archiveConditionnementUseCase;
                @Mock
                DesarchiveConditionnementUseCase desarchiveConditionnementUseCase;
                @Mock
                GetConditionnementUseCase getConditionnementUseCase;
                @Mock
                ListConditionnementsUseCase listConditionnementsUseCase;

                @Test
                @DisplayName("chaque méthode délègue au use case correspondant")
                void chaqueMethodeDelegue() {
                        ConditionnementFacade sut = new ConditionnementFacade(createConditionnementUseCase,
                                        updateConditionnementUseCase, archiveConditionnementUseCase,
                                        desarchiveConditionnementUseCase,
                                        getConditionnementUseCase, listConditionnementsUseCase);
                        UUID id = UUID.randomUUID();
                        ConditionnementDetail detail = new ConditionnementDetail(id, id, "N", 1, BigDecimal.ONE, true,
                                        BigDecimal.ONE, BigDecimal.ONE, true, null, null);
                        ConditionnementPage page = new ConditionnementPage(List.of(), 0, 20, 0, 0);

                        when(createConditionnementUseCase.creer(any())).thenReturn(detail);
                        when(updateConditionnementUseCase.modifier(any())).thenReturn(detail);
                        when(archiveConditionnementUseCase.archiver(any())).thenReturn(detail);
                        when(desarchiveConditionnementUseCase.desarchiver(any())).thenReturn(detail);
                        when(getConditionnementUseCase.obtenir(any())).thenReturn(detail);
                        when(listConditionnementsUseCase.lister(any())).thenReturn(page);

                        assertThat(sut.creerConditionnement(
                                        new CreateConditionnementCommand(id, "N", 1, BigDecimal.ONE, true,
                                                        BigDecimal.ONE, BigDecimal.ONE)))
                                        .isSameAs(detail);
                        assertThat(sut.modifierConditionnement(
                                        new UpdateConditionnementCommand(id, "N", 1, BigDecimal.ONE,
                                                        true, BigDecimal.ONE, BigDecimal.ONE)))
                                        .isSameAs(detail);
                        assertThat(sut.archiverConditionnement(new ArchiveConditionnementCommand(id))).isSameAs(detail);
                        assertThat(sut.desarchiverConditionnement(new DesarchiveConditionnementCommand(id)))
                                        .isSameAs(detail);
                        assertThat(sut.obtenirConditionnement(new GetConditionnementQuery(id))).isSameAs(detail);
                        assertThat(sut.listerConditionnements(
                                        new ListConditionnementsQuery(id, null, null, null, null, null)))
                                        .isSameAs(page);
                }
        }
}
