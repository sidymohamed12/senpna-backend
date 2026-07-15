package ministere.sante.senpna.commandeachat.application.usecase;

import ministere.sante.senpna.commandeachat.application.service.FactureDetailAssembler;
import ministere.sante.senpna.commandeachat.domain.command.FactureCommands.FactureDetail;
import ministere.sante.senpna.commandeachat.domain.command.FactureCommands.FacturePage;
import ministere.sante.senpna.commandeachat.domain.command.FactureCommands.ListMesFacturesQuery;
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
import org.mockito.ArgumentCaptor;
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
@DisplayName("ListMesFacturesUseCaseImpl — factures du fournisseur connecté")
class ListMesFacturesUseCaseImplTest {

        @Mock
        FactureRepositoryPort factureRepositoryPort;
        @Mock
        FactureDetailAssembler factureDetailAssembler;

        ListMesFacturesUseCaseImpl sut;

        @BeforeEach
        void setUp() {
                sut = new ListMesFacturesUseCaseImpl(factureRepositoryPort, factureDetailAssembler);
        }

        @Test
        @DisplayName("transmet le fournisseur et le statut au port")
        void transmetFournisseurEtStatut() {
                FournisseurId fournisseurId = FournisseurId.generate();
                Facture facture = Facture.soumettre(CommandeAchatId.generate(), fournisseurId, "FAC-1", BigDecimal.TEN,
                                LocalDate.now(), null, null);
                when(factureRepositoryPort.findByFournisseurId(any(), any(), any()))
                                .thenReturn(PageResult.of(List.of(facture), 0, 20, 1));
                FactureDetail detail = new FactureDetail(facture.getId().getValue(),
                                facture.getCommandeAchatId().getValue(),
                                fournisseurId.getValue(), "FAC-1", BigDecimal.TEN, LocalDate.now(), null, null,
                                StatutFacture.SOUMISE, null, null, null);
                when(factureDetailAssembler.assembler(facture)).thenReturn(detail);

                FacturePage result = sut
                                .lister(new ListMesFacturesQuery(fournisseurId.getValue(), StatutFacture.SOUMISE, 0,
                                                20));

                ArgumentCaptor<FournisseurId> captor = ArgumentCaptor.forClass(FournisseurId.class);
                verify(factureRepositoryPort).findByFournisseurId(captor.capture(), eq(StatutFacture.SOUMISE), any());
                assertThat(captor.getValue()).isEqualTo(fournisseurId);
                assertThat(result.content()).containsExactly(detail);
        }
}
