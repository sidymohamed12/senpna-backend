package ministere.sante.senpna.appeloffre.application.usecase;

import ministere.sante.senpna.appeloffre.application.service.AppelOffreDetailAssembler;
import ministere.sante.senpna.appeloffre.domain.command.AppelOffreCommands.GetAppelOffreQuery;
import ministere.sante.senpna.appeloffre.domain.exception.AppelOffreIntrouvableException;
import ministere.sante.senpna.appeloffre.domain.model.AppelOffre;
import ministere.sante.senpna.appeloffre.domain.model.LigneAppelOffre;
import ministere.sante.senpna.appeloffre.domain.port.out.AppelOffreRepositoryPort;
import ministere.sante.senpna.medicament.domain.valueobject.MedicamentId;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetAppelOffreUseCaseImpl — consultation PNA, tous statuts")
class GetAppelOffreUseCaseImplTest {

    @Mock
    AppelOffreRepositoryPort appelOffreRepositoryPort;
    @Mock
    AppelOffreDetailAssembler appelOffreDetailAssembler;

    GetAppelOffreUseCaseImpl sut;

    @BeforeEach
    void setUp() {
        sut = new GetAppelOffreUseCaseImpl(appelOffreRepositoryPort, appelOffreDetailAssembler);
    }

    @Test
    @DisplayName("appel d'offres introuvable → AppelOffreIntrouvableException")
    void introuvable_leveException() {
        UUID id = UUID.randomUUID();
        when(appelOffreRepositoryPort
                .findById(ministere.sante.senpna.appeloffre.domain.valueobject.AppelOffreId.of(id)))
                .thenReturn(Optional.empty());

        var query = new GetAppelOffreQuery(id);
        assertThatThrownBy(() -> sut.obtenir(query)).isInstanceOf(AppelOffreIntrouvableException.class);
    }

    @Test
    @DisplayName("appel d'offres trouvé (même en BROUILLON — vue PNA sans restriction) → assemblé")
    void trouve_assemble() {
        LigneAppelOffre ligne = LigneAppelOffre.creer(MedicamentId.generate(), "Med", BigDecimal.TEN, "u");
        AppelOffre appelOffre = AppelOffre.creer("AO-1", "Objet", LocalDate.now().plusDays(10), List.of(ligne));
        when(appelOffreRepositoryPort.findById(appelOffre.getId())).thenReturn(Optional.of(appelOffre));

        sut.obtenir(new GetAppelOffreQuery(appelOffre.getId().getValue()));

        verify(appelOffreDetailAssembler).assembler(appelOffre);
    }
}
