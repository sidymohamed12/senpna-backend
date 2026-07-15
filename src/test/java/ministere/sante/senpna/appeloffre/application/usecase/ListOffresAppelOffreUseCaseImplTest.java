package ministere.sante.senpna.appeloffre.application.usecase;

import ministere.sante.senpna.appeloffre.application.service.OffreDetailAssembler;
import ministere.sante.senpna.appeloffre.domain.command.OffreFournisseurCommands.ListOffresAppelOffreQuery;
import ministere.sante.senpna.appeloffre.domain.command.OffreFournisseurCommands.OffreDetail;
import ministere.sante.senpna.appeloffre.domain.command.OffreFournisseurCommands.OffrePage;
import ministere.sante.senpna.appeloffre.domain.model.LigneOffre;
import ministere.sante.senpna.appeloffre.domain.model.OffreFournisseur;
import ministere.sante.senpna.appeloffre.domain.port.out.OffreFournisseurRepositoryPort;
import ministere.sante.senpna.appeloffre.domain.valueobject.AppelOffreId;
import ministere.sante.senpna.appeloffre.domain.valueobject.LigneAppelOffreId;
import ministere.sante.senpna.appeloffre.domain.valueobject.StatutOffre;
import ministere.sante.senpna.fournisseur.domain.valueobject.FournisseurId;
import ministere.sante.senpna.shared.domain.valueobject.PageResult;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ListOffresAppelOffreUseCaseImpl — analyse PNA des offres reçues pour un AO")
class ListOffresAppelOffreUseCaseImplTest {

    @Mock
    OffreFournisseurRepositoryPort offreFournisseurRepositoryPort;
    @Mock
    OffreDetailAssembler offreDetailAssembler;

    ListOffresAppelOffreUseCaseImpl sut;

    @BeforeEach
    void setUp() {
        sut = new ListOffresAppelOffreUseCaseImpl(offreFournisseurRepositoryPort, offreDetailAssembler);
    }

    @Test
    @DisplayName("transmet l'identifiant de l'AO au port, assemble chaque résultat")
    void transmetIdentifiantAO() {
        AppelOffreId appelOffreId = AppelOffreId.generate();
        LigneOffre ligne = LigneOffre.creer(LigneAppelOffreId.generate(), BigDecimal.TEN, 10);
        OffreFournisseur offre = OffreFournisseur.soumettre(appelOffreId, FournisseurId.generate(), null,
                List.of(ligne));
        when(offreFournisseurRepositoryPort.findByAppelOffreId(any(), any()))
                .thenReturn(PageResult.of(List.of(offre), 0, 20, 1));
        OffreDetail detail = new OffreDetail(offre.getId().getValue(), appelOffreId.getValue(), UUID.randomUUID(),
                null, StatutOffre.SOUMISE, List.of(), null, null);
        when(offreDetailAssembler.assembler(offre)).thenReturn(detail);

        OffrePage result = sut.lister(new ListOffresAppelOffreQuery(appelOffreId.getValue(), 0, 20));

        ArgumentCaptor<AppelOffreId> captor = ArgumentCaptor.forClass(AppelOffreId.class);
        verify(offreFournisseurRepositoryPort).findByAppelOffreId(captor.capture(), any());
        assertThat(captor.getValue()).isEqualTo(appelOffreId);
        assertThat(result.content()).containsExactly(detail);
    }
}
