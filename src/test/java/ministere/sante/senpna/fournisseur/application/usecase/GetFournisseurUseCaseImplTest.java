package ministere.sante.senpna.fournisseur.application.usecase;

import ministere.sante.senpna.fournisseur.application.service.FournisseurDetailAssembler;
import ministere.sante.senpna.fournisseur.domain.command.FournisseurCommands.FournisseurDetail;
import ministere.sante.senpna.fournisseur.domain.command.FournisseurCommands.GetFournisseurQuery;
import ministere.sante.senpna.fournisseur.domain.exception.FournisseurIntrouvableException;
import ministere.sante.senpna.fournisseur.domain.model.Fournisseur;
import ministere.sante.senpna.fournisseur.domain.port.out.FournisseurRepositoryPort;
import ministere.sante.senpna.fournisseur.domain.valueobject.FournisseurId;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetFournisseurUseCaseImpl — consultation d'un fournisseur")
class GetFournisseurUseCaseImplTest {

    @Mock
    FournisseurRepositoryPort fournisseurRepositoryPort;
    @Mock
    FournisseurDetailAssembler fournisseurDetailAssembler;

    GetFournisseurUseCaseImpl sut;

    @BeforeEach
    void setUp() {
        sut = new GetFournisseurUseCaseImpl(fournisseurRepositoryPort, fournisseurDetailAssembler);
    }

    @Test
    @DisplayName("fournisseur trouvé → renvoie le détail assemblé")
    void trouve_renvoieDetail() {
        UUID id = UUID.randomUUID();
        Fournisseur fournisseur = Fournisseur.creer(new Fournisseur.CreationCommand("Nom", null, null, null, null));
        when(fournisseurRepositoryPort.findById(FournisseurId.of(id))).thenReturn(Optional.of(fournisseur));
        FournisseurDetail detail = new FournisseurDetail(id, "Nom", null, null, null, null, true, null, null);
        when(fournisseurDetailAssembler.assembler(fournisseur)).thenReturn(detail);

        assertThat(sut.obtenir(new GetFournisseurQuery(id))).isSameAs(detail);
    }

    @Test
    @DisplayName("fournisseur introuvable → FournisseurIntrouvableException")
    void introuvable_leveException() {
        UUID id = UUID.randomUUID();
        when(fournisseurRepositoryPort.findById(FournisseurId.of(id))).thenReturn(Optional.empty());

        var getFournisseurQuery = new GetFournisseurQuery(id);
        assertThatThrownBy(() -> sut.obtenir(getFournisseurQuery))
                .isInstanceOf(FournisseurIntrouvableException.class);
    }
}
