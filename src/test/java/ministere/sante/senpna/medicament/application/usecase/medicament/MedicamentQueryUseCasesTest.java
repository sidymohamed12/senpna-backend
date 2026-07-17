package ministere.sante.senpna.medicament.application.usecase.medicament;

import ministere.sante.senpna.medicament.application.service.MedicamentDetailAssembler;
import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.DesarchiveMedicamentCommand;
import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.GetMedicamentQuery;
import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.ListMedicamentsQuery;
import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.MedicamentPage;
import ministere.sante.senpna.medicament.domain.exception.medicament.MedicamentIntrouvableException;
import ministere.sante.senpna.medicament.domain.model.Medicament;
import ministere.sante.senpna.medicament.domain.port.out.MedicamentRepositoryPort;
import ministere.sante.senpna.medicament.domain.valueobject.FamilleId;
import ministere.sante.senpna.medicament.domain.valueobject.FormeId;
import ministere.sante.senpna.medicament.domain.valueobject.MedicamentId;
import ministere.sante.senpna.medicament.domain.valueobject.VoieAdministration;
import ministere.sante.senpna.shared.domain.port.out.FamilleCachePort;
import ministere.sante.senpna.shared.domain.port.out.FormeCachePort;
import ministere.sante.senpna.shared.domain.valueobject.PageResult;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetMedicamentUseCaseImpl / ListMedicamentsUseCaseImpl / DesarchiveMedicamentUseCaseImpl")
class MedicamentQueryUseCasesTest {

    @Mock
    MedicamentRepositoryPort medicamentRepositoryPort;
    @Mock
    FamilleCachePort familleCachePort;
    @Mock
    FormeCachePort formeCachePort;

    MedicamentDetailAssembler assembler;

    @BeforeEach
    void setUp() {
        assembler = new MedicamentDetailAssembler(familleCachePort, formeCachePort);
        lenient().when(familleCachePort.findById(any())).thenReturn(Optional.empty());
        lenient().when(formeCachePort.findById(any())).thenReturn(Optional.empty());
    }

    private Medicament medicament() {
        return Medicament.creer(new Medicament.CreationCommand("MED-1", "Zolpidem", "Zolpidem", "10mg", FormeId.generate(), FamilleId.generate(),
                VoieAdministration.ORALE, null, null, null, false, "Sanofi", null, null));
    }

    @Nested
    @DisplayName("GetMedicamentUseCaseImpl")
    class Get {

        @Test
        @DisplayName("introuvable → MedicamentIntrouvableException")
        void introuvable_leveException() {
            when(medicamentRepositoryPort.findById(any())).thenReturn(Optional.empty());

            var sut = new GetMedicamentUseCaseImpl(medicamentRepositoryPort, assembler);
            var command = new GetMedicamentQuery(UUID.randomUUID());
            assertThatThrownBy(() -> sut
                    .obtenir(command))
                    .isInstanceOf(MedicamentIntrouvableException.class);
        }

        @Test
        @DisplayName("trouvé → renvoie le détail assemblé")
        void trouve_renvoieDetail() {
            Medicament m = medicament();
            when(medicamentRepositoryPort.findById(MedicamentId.of(m.getId().getValue())))
                    .thenReturn(Optional.of(m));

            assertThat(new GetMedicamentUseCaseImpl(medicamentRepositoryPort, assembler)
                    .obtenir(new GetMedicamentQuery(m.getId().getValue())).nomCommercial())
                    .isEqualTo("Zolpidem");
        }
    }

    @Nested
    @DisplayName("ListMedicamentsUseCaseImpl")
    class List_ {

        @Test
        @DisplayName("aucun résultat → page vide")
        void aucunResultat_pageVide() {
            when(medicamentRepositoryPort.search(any(), any())).thenReturn(PageResult.of(List.of(), 0, 20, 0));

            MedicamentPage page = new ListMedicamentsUseCaseImpl(medicamentRepositoryPort, assembler)
                    .lister(new ListMedicamentsQuery(null, null, null, null, 0, 20, null, null));

            assertThat(page.content()).isEmpty();
        }
    }

    @Nested
    @DisplayName("DesarchiveMedicamentUseCaseImpl")
    class Desarchive {

        @Test
        @DisplayName("réactive un médicament archivé")
        void reactiveMedicamentArchive() {
            Medicament m = medicament();
            m.archiver();
            when(medicamentRepositoryPort.findById(MedicamentId.of(m.getId().getValue())))
                    .thenReturn(Optional.of(m));
            when(medicamentRepositoryPort.save(m)).thenReturn(m);

            new DesarchiveMedicamentUseCaseImpl(medicamentRepositoryPort, assembler)
                    .desarchiver(new DesarchiveMedicamentCommand(m.getId().getValue()));

            assertThat(m.isActif()).isTrue();
        }
    }
}
