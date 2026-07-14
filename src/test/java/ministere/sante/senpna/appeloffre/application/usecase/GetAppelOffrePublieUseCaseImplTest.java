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

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetAppelOffrePublieUseCaseImpl — visibilité côté fournisseur")
class GetAppelOffrePublieUseCaseImplTest {

    @Mock
    AppelOffreRepositoryPort appelOffreRepositoryPort;
    @Mock
    AppelOffreDetailAssembler appelOffreDetailAssembler;

    GetAppelOffrePublieUseCaseImpl sut;

    @BeforeEach
    void setUp() {
        sut = new GetAppelOffrePublieUseCaseImpl(appelOffreRepositoryPort, appelOffreDetailAssembler);
    }

    @Test
    @DisplayName("appel d'offres en BROUILLON → AppelOffreIntrouvableException (masqué au fournisseur)")
    void brouillon_masqueAuFournisseur() {
        LigneAppelOffre ligne = LigneAppelOffre.creer(MedicamentId.generate(), "Med", BigDecimal.TEN, "u");
        AppelOffre appelOffre = AppelOffre.creer("AO-1", "Objet", LocalDate.now().plusDays(10), List.of(ligne));
        when(appelOffreRepositoryPort.findById(appelOffre.getId())).thenReturn(Optional.of(appelOffre));

        var query = new GetAppelOffreQuery(appelOffre.getId().getValue());
        assertThatThrownBy(() -> sut.obtenir(query)).isInstanceOf(AppelOffreIntrouvableException.class);
    }

    @Test
    @DisplayName("appel d'offres PUBLIE → visible")
    void publie_visible() {
        LigneAppelOffre ligne = LigneAppelOffre.creer(MedicamentId.generate(), "Med", BigDecimal.TEN, "u");
        AppelOffre appelOffre = AppelOffre.creer("AO-1", "Objet", LocalDate.now().plusDays(10), List.of(ligne));
        appelOffre.publier();
        when(appelOffreRepositoryPort.findById(appelOffre.getId())).thenReturn(Optional.of(appelOffre));

        sut.obtenir(new GetAppelOffreQuery(appelOffre.getId().getValue()));

        verify(appelOffreDetailAssembler).assembler(appelOffre);
    }
}
