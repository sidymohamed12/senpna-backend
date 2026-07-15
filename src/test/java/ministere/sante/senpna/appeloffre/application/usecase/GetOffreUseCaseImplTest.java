package ministere.sante.senpna.appeloffre.application.usecase;

import ministere.sante.senpna.appeloffre.application.service.OffreDetailAssembler;
import ministere.sante.senpna.appeloffre.domain.command.OffreFournisseurCommands.GetOffreQuery;
import ministere.sante.senpna.appeloffre.domain.exception.AccesOffreRefuseException;
import ministere.sante.senpna.appeloffre.domain.exception.OffreFournisseurIntrouvableException;
import ministere.sante.senpna.appeloffre.domain.model.LigneOffre;
import ministere.sante.senpna.appeloffre.domain.model.OffreFournisseur;
import ministere.sante.senpna.appeloffre.domain.port.out.OffreFournisseurRepositoryPort;
import ministere.sante.senpna.appeloffre.domain.valueobject.AppelOffreId;
import ministere.sante.senpna.appeloffre.domain.valueobject.LigneAppelOffreId;
import ministere.sante.senpna.appeloffre.domain.valueobject.OffreFournisseurId;
import ministere.sante.senpna.fournisseur.domain.valueobject.FournisseurId;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetOffreUseCaseImpl — consultation d'une offre (PNA ou espace fournisseur)")
class GetOffreUseCaseImplTest {

    @Mock
    OffreFournisseurRepositoryPort offreFournisseurRepositoryPort;
    @Mock
    OffreDetailAssembler offreDetailAssembler;

    GetOffreUseCaseImpl sut;

    @BeforeEach
    void setUp() {
        sut = new GetOffreUseCaseImpl(offreFournisseurRepositoryPort, offreDetailAssembler);
    }

    private OffreFournisseur offre(FournisseurId fournisseurId) {
        LigneOffre ligne = LigneOffre.creer(LigneAppelOffreId.generate(), BigDecimal.TEN, 10);
        return OffreFournisseur.soumettre(AppelOffreId.generate(), fournisseurId, null, List.of(ligne));
    }

    @Test
    @DisplayName("offre introuvable → OffreFournisseurIntrouvableException")
    void introuvable_leveException() {
        UUID id = UUID.randomUUID();
        when(offreFournisseurRepositoryPort.findById(OffreFournisseurId.of(id))).thenReturn(Optional.empty());

        var query = new GetOffreQuery(id, null);
        assertThatThrownBy(() -> sut.obtenir(query)).isInstanceOf(OffreFournisseurIntrouvableException.class);
    }

    @Test
    @DisplayName("fournisseurId null (appel PNA) → aucune restriction de propriété")
    void fournisseurIdNull_aucuneRestriction() {
        OffreFournisseur offre = offre(FournisseurId.generate());
        when(offreFournisseurRepositoryPort.findById(offre.getId())).thenReturn(Optional.of(offre));

        sut.obtenir(new GetOffreQuery(offre.getId().getValue(), null));

        verify(offreDetailAssembler).assembler(offre);
    }

    @Test
    @DisplayName("fournisseurId renseigné et différent du propriétaire → AccesOffreRefuseException")
    void fournisseurDifferent_leveException() {
        OffreFournisseur offre = offre(FournisseurId.generate());
        when(offreFournisseurRepositoryPort.findById(offre.getId())).thenReturn(Optional.of(offre));

        var query = new GetOffreQuery(offre.getId().getValue(), UUID.randomUUID());
        assertThatThrownBy(() -> sut.obtenir(query)).isInstanceOf(AccesOffreRefuseException.class);
    }

    @Test
    @DisplayName("fournisseurId renseigné et correspondant au propriétaire → autorisé")
    void fournisseurCorrespondant_autorise() {
        FournisseurId fournisseurId = FournisseurId.generate();
        OffreFournisseur offre = offre(fournisseurId);
        when(offreFournisseurRepositoryPort.findById(offre.getId())).thenReturn(Optional.of(offre));

        sut.obtenir(new GetOffreQuery(offre.getId().getValue(), fournisseurId.getValue()));

        verify(offreDetailAssembler).assembler(offre);
    }
}
