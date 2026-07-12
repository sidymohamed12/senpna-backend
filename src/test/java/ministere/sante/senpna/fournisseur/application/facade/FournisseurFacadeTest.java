package ministere.sante.senpna.fournisseur.application.facade;

import ministere.sante.senpna.fournisseur.domain.command.FournisseurCommands.ActivateFournisseurCommand;
import ministere.sante.senpna.fournisseur.domain.command.FournisseurCommands.CreateFournisseurCommand;
import ministere.sante.senpna.fournisseur.domain.command.FournisseurCommands.DeactivateFournisseurCommand;
import ministere.sante.senpna.fournisseur.domain.command.FournisseurCommands.FournisseurDetail;
import ministere.sante.senpna.fournisseur.domain.command.FournisseurCommands.GetFournisseurQuery;
import ministere.sante.senpna.fournisseur.domain.command.FournisseurCommands.UpdateFournisseurCommand;
import ministere.sante.senpna.fournisseur.domain.port.in.ActivateFournisseurUseCase;
import ministere.sante.senpna.fournisseur.domain.port.in.CreateFournisseurUseCase;
import ministere.sante.senpna.fournisseur.domain.port.in.DeactivateFournisseurUseCase;
import ministere.sante.senpna.fournisseur.domain.port.in.GetFournisseurUseCase;
import ministere.sante.senpna.fournisseur.domain.port.in.ListFournisseursUseCase;
import ministere.sante.senpna.fournisseur.domain.port.in.UpdateFournisseurUseCase;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("FournisseurFacade — délégation aux use cases")
class FournisseurFacadeTest {

    @Mock
    CreateFournisseurUseCase createFournisseurUseCase;
    @Mock
    UpdateFournisseurUseCase updateFournisseurUseCase;
    @Mock
    GetFournisseurUseCase getFournisseurUseCase;
    @Mock
    ListFournisseursUseCase listFournisseursUseCase;
    @Mock
    ActivateFournisseurUseCase activateFournisseurUseCase;
    @Mock
    DeactivateFournisseurUseCase deactivateFournisseurUseCase;

    FournisseurFacade sut;

    @BeforeEach
    void setUp() {
        sut = new FournisseurFacade(createFournisseurUseCase, updateFournisseurUseCase, getFournisseurUseCase,
                listFournisseursUseCase, activateFournisseurUseCase, deactivateFournisseurUseCase);
    }

    @Test
    @DisplayName("chaque méthode de façade délègue au use case correspondant")
    void chaqueMethodeDelegue() {
        UUID id = UUID.randomUUID();
        FournisseurDetail detail = new FournisseurDetail(id, "N", null, null, null, null, true, null, null);

        when(createFournisseurUseCase.creer(any())).thenReturn(detail);
        when(updateFournisseurUseCase.modifier(any())).thenReturn(detail);
        when(getFournisseurUseCase.obtenir(any())).thenReturn(detail);
        when(activateFournisseurUseCase.activer(any())).thenReturn(detail);
        when(deactivateFournisseurUseCase.desactiver(any())).thenReturn(detail);

        assertThat(sut.creerFournisseur(new CreateFournisseurCommand("N", null, null, null, null)))
                .isSameAs(detail);
        assertThat(sut.modifierFournisseur(new UpdateFournisseurCommand(id, "N", null, null, null, null)))
                .isSameAs(detail);
        assertThat(sut.obtenirFournisseur(new GetFournisseurQuery(id))).isSameAs(detail);
        assertThat(sut.activerFournisseur(new ActivateFournisseurCommand(id))).isSameAs(detail);
        assertThat(sut.desactiverFournisseur(new DeactivateFournisseurCommand(id))).isSameAs(detail);
    }
}
