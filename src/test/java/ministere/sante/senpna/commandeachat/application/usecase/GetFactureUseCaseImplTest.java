package ministere.sante.senpna.commandeachat.application.usecase;

import ministere.sante.senpna.commandeachat.application.service.FactureDetailAssembler;
import ministere.sante.senpna.commandeachat.domain.command.FactureCommands.GetFactureQuery;
import ministere.sante.senpna.commandeachat.domain.exception.AccesFactureRefuseException;
import ministere.sante.senpna.commandeachat.domain.exception.FactureIntrouvableException;
import ministere.sante.senpna.commandeachat.domain.model.Facture;
import ministere.sante.senpna.commandeachat.domain.port.out.FactureRepositoryPort;
import ministere.sante.senpna.commandeachat.domain.valueobject.CommandeAchatId;
import ministere.sante.senpna.commandeachat.domain.valueobject.FactureId;
import ministere.sante.senpna.fournisseur.domain.valueobject.FournisseurId;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetFactureUseCaseImpl — consultation d'une facture (PNA ou espace fournisseur)")
class GetFactureUseCaseImplTest {

    @Mock
    FactureRepositoryPort factureRepositoryPort;
    @Mock
    FactureDetailAssembler factureDetailAssembler;

    GetFactureUseCaseImpl sut;

    @BeforeEach
    void setUp() {
        sut = new GetFactureUseCaseImpl(factureRepositoryPort, factureDetailAssembler);
    }

    private Facture facture(FournisseurId fournisseurId) {
        return Facture.soumettre(CommandeAchatId.generate(), fournisseurId, "FAC-1", BigDecimal.TEN, LocalDate.now(),
                null, null);
    }

    @Test
    @DisplayName("facture introuvable → FactureIntrouvableException")
    void introuvable_leveException() {
        UUID id = UUID.randomUUID();
        when(factureRepositoryPort.findById(FactureId.of(id))).thenReturn(Optional.empty());

        var query = new GetFactureQuery(id, null);
        assertThatThrownBy(() -> sut.obtenir(query)).isInstanceOf(FactureIntrouvableException.class);
    }

    @Test
    @DisplayName("fournisseurId null (appel PNA) → aucune restriction de propriété")
    void fournisseurIdNull_aucuneRestriction() {
        Facture facture = facture(FournisseurId.generate());
        when(factureRepositoryPort.findById(facture.getId())).thenReturn(Optional.of(facture));

        sut.obtenir(new GetFactureQuery(facture.getId().getValue(), null));

        verify(factureDetailAssembler).assembler(facture);
    }

    @Test
    @DisplayName("fournisseurId renseigné et différent du propriétaire → AccesFactureRefuseException")
    void fournisseurDifferent_leveException() {
        Facture facture = facture(FournisseurId.generate());
        when(factureRepositoryPort.findById(facture.getId())).thenReturn(Optional.of(facture));

        var query = new GetFactureQuery(facture.getId().getValue(), UUID.randomUUID());
        assertThatThrownBy(() -> sut.obtenir(query)).isInstanceOf(AccesFactureRefuseException.class);
    }

    @Test
    @DisplayName("fournisseurId renseigné et correspondant → autorisé")
    void fournisseurCorrespondant_autorise() {
        FournisseurId fournisseurId = FournisseurId.generate();
        Facture facture = facture(fournisseurId);
        when(factureRepositoryPort.findById(facture.getId())).thenReturn(Optional.of(facture));

        sut.obtenir(new GetFactureQuery(facture.getId().getValue(), fournisseurId.getValue()));

        verify(factureDetailAssembler).assembler(facture);
    }
}
