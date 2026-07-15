package ministere.sante.senpna.appeloffre.application.usecase;

import ministere.sante.senpna.appeloffre.application.service.OffreDetailAssembler;
import ministere.sante.senpna.appeloffre.domain.command.OffreFournisseurCommands.RetenirOffreCommand;
import ministere.sante.senpna.appeloffre.domain.exception.OffreFournisseurIntrouvableException;
import ministere.sante.senpna.appeloffre.domain.exception.TransitionStatutOffreInvalideException;
import ministere.sante.senpna.appeloffre.domain.model.LigneOffre;
import ministere.sante.senpna.appeloffre.domain.model.OffreFournisseur;
import ministere.sante.senpna.appeloffre.domain.port.out.OffreFournisseurRepositoryPort;
import ministere.sante.senpna.appeloffre.domain.valueobject.AppelOffreId;
import ministere.sante.senpna.appeloffre.domain.valueobject.LigneAppelOffreId;
import ministere.sante.senpna.appeloffre.domain.valueobject.OffreFournisseurId;
import ministere.sante.senpna.appeloffre.domain.valueobject.StatutOffre;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("RetenirOffreUseCaseImpl")
class RetenirOffreUseCaseImplTest {

    @Mock
    OffreFournisseurRepositoryPort offreFournisseurRepositoryPort;
    @Mock
    OffreDetailAssembler offreDetailAssembler;

    RetenirOffreUseCaseImpl sut;

    @BeforeEach
    void setUp() {
        sut = new RetenirOffreUseCaseImpl(offreFournisseurRepositoryPort, offreDetailAssembler);
    }

    private OffreFournisseur offreSoumise() {
        LigneOffre ligne = LigneOffre.creer(LigneAppelOffreId.generate(), BigDecimal.TEN, 10);
        return OffreFournisseur.soumettre(AppelOffreId.generate(), FournisseurId.generate(), null, List.of(ligne));
    }

    @Test
    @DisplayName("offre introuvable → OffreFournisseurIntrouvableException")
    void introuvable_leveException() {
        UUID id = UUID.randomUUID();
        when(offreFournisseurRepositoryPort.findById(OffreFournisseurId.of(id))).thenReturn(Optional.empty());

        var command = new RetenirOffreCommand(id);
        assertThatThrownBy(() -> sut.retenir(command)).isInstanceOf(OffreFournisseurIntrouvableException.class);
    }

    @Test
    @DisplayName("cas nominal → RETENUE")
    void casNominal_passeRetenue() {
        OffreFournisseur offre = offreSoumise();
        when(offreFournisseurRepositoryPort.findById(offre.getId())).thenReturn(Optional.of(offre));
        when(offreFournisseurRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

        sut.retenir(new RetenirOffreCommand(offre.getId().getValue()));

        assertThat(offre.getStatut()).isEqualTo(StatutOffre.RETENUE);
    }

    @Test
    @DisplayName("offre déjà retenue → TransitionStatutOffreInvalideException")
    void dejaRetenue_leveException() {
        OffreFournisseur offre = offreSoumise();
        offre.retenir();
        when(offreFournisseurRepositoryPort.findById(offre.getId())).thenReturn(Optional.of(offre));

        var command = new RetenirOffreCommand(offre.getId().getValue());
        assertThatThrownBy(() -> sut.retenir(command)).isInstanceOf(TransitionStatutOffreInvalideException.class);
    }
}
