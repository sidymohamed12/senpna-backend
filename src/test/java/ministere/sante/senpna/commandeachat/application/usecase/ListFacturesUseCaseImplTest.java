package ministere.sante.senpna.commandeachat.application.usecase;

import ministere.sante.senpna.commandeachat.application.service.FactureDetailAssembler;
import ministere.sante.senpna.commandeachat.domain.command.FactureCommands.FactureDetail;
import ministere.sante.senpna.commandeachat.domain.command.FactureCommands.FacturePage;
import ministere.sante.senpna.commandeachat.domain.command.FactureCommands.ListFacturesQuery;
import ministere.sante.senpna.commandeachat.domain.model.Facture;
import ministere.sante.senpna.commandeachat.domain.port.out.FactureRepositoryPort;
import ministere.sante.senpna.commandeachat.domain.valueobject.CommandeAchatId;
import ministere.sante.senpna.commandeachat.domain.valueobject.StatutFacture;
import ministere.sante.senpna.fournisseur.domain.valueobject.FournisseurId;
import ministere.sante.senpna.shared.domain.valueobject.PageResult;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ListFacturesUseCaseImpl — vue PNA, toutes les factures reçues")
class ListFacturesUseCaseImplTest {

    @Mock
    FactureRepositoryPort factureRepositoryPort;
    @Mock
    FactureDetailAssembler factureDetailAssembler;

    ListFacturesUseCaseImpl sut;

    @BeforeEach
    void setUp() {
        sut = new ListFacturesUseCaseImpl(factureRepositoryPort, factureDetailAssembler);
    }

    @Test
    @DisplayName("transmet le filtre de statut au port, assemble chaque résultat")
    void transmetFiltreStatut() {
        Facture facture = Facture.soumettre(new Facture.SoumissionCommand(CommandeAchatId.generate(), FournisseurId.generate(), "FAC-1",
                BigDecimal.TEN, LocalDate.now(), null, null));
        when(factureRepositoryPort.findAll(eq(StatutFacture.SOUMISE), any()))
                .thenReturn(PageResult.of(List.of(facture), 0, 20, 1));
        FactureDetail detail = new FactureDetail(facture.getId().getValue(), facture.getCommandeAchatId().getValue(),
                facture.getFournisseurId().getValue(), "FAC-1", BigDecimal.TEN, LocalDate.now(), null, null,
                StatutFacture.SOUMISE, null, null, null);
        when(factureDetailAssembler.assembler(facture)).thenReturn(detail);

        FacturePage result = sut.lister(new ListFacturesQuery(StatutFacture.SOUMISE, 0, 20));

        verify(factureRepositoryPort).findAll(eq(StatutFacture.SOUMISE), any());
        assertThat(result.content()).containsExactly(detail);
    }
}
