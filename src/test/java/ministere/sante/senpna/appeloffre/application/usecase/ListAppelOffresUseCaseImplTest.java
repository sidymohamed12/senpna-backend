package ministere.sante.senpna.appeloffre.application.usecase;

import ministere.sante.senpna.appeloffre.application.service.AppelOffreDetailAssembler;
import ministere.sante.senpna.appeloffre.domain.command.AppelOffreCommands.AppelOffrePage;
import ministere.sante.senpna.appeloffre.domain.command.AppelOffreCommands.AppelOffreSummary;
import ministere.sante.senpna.appeloffre.domain.command.AppelOffreCommands.ListAppelOffresQuery;
import ministere.sante.senpna.appeloffre.domain.criteria.AppelOffreSearchCriteria;
import ministere.sante.senpna.appeloffre.domain.model.AppelOffre;
import ministere.sante.senpna.appeloffre.domain.model.LigneAppelOffre;
import ministere.sante.senpna.appeloffre.domain.port.out.AppelOffreRepositoryPort;
import ministere.sante.senpna.appeloffre.domain.valueobject.StatutAppelOffre;
import ministere.sante.senpna.medicament.domain.valueobject.MedicamentId;
import ministere.sante.senpna.shared.domain.valueobject.PageRequest;
import ministere.sante.senpna.shared.domain.valueobject.PageResult;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ListAppelOffresUseCaseImpl — vue PNA, tous statuts confondus")
class ListAppelOffresUseCaseImplTest {

    @Mock
    AppelOffreRepositoryPort appelOffreRepositoryPort;
    @Mock
    AppelOffreDetailAssembler appelOffreDetailAssembler;

    ListAppelOffresUseCaseImpl sut;

    @BeforeEach
    void setUp() {
        sut = new ListAppelOffresUseCaseImpl(appelOffreRepositoryPort, appelOffreDetailAssembler);
    }

    @Test
    @DisplayName("transmet la recherche et le statut demandés au critère de recherche, sans les forcer")
    void transmetCritereTelQuel() {
        LigneAppelOffre ligne = LigneAppelOffre.creer(MedicamentId.generate(), "Med", BigDecimal.TEN, "u");
        AppelOffre appelOffre = AppelOffre.creer("AO-1", "Objet", LocalDate.now().plusDays(10), List.of(ligne));
        when(appelOffreRepositoryPort.search(any(), any()))
                .thenReturn(PageResult.of(List.of(appelOffre), 0, 20, 1));
        AppelOffreSummary summary = new AppelOffreSummary(appelOffre.getId().getValue(), "AO-1", "Objet",
                LocalDate.now().plusDays(10), StatutAppelOffre.BROUILLON, 1, appelOffre.getCreatedAt());
        when(appelOffreDetailAssembler.assemblerResume(appelOffre)).thenReturn(summary);

        AppelOffrePage result = sut.lister(
                new ListAppelOffresQuery("Amox", StatutAppelOffre.BROUILLON, 0, 20, "reference", "ASC"));

        ArgumentCaptor<AppelOffreSearchCriteria> criteriaCaptor = ArgumentCaptor
                .forClass(AppelOffreSearchCriteria.class);
        verify(appelOffreRepositoryPort).search(criteriaCaptor.capture(), any(PageRequest.class));
        assertThat(criteriaCaptor.getValue().recherche()).isEqualTo("Amox");
        assertThat(criteriaCaptor.getValue().statut()).isEqualTo(StatutAppelOffre.BROUILLON);
        assertThat(result.content()).containsExactly(summary);
        assertThat(result.totalElements()).isEqualTo(1);
    }
}
