package ministere.sante.senpna.appeloffre.application.usecase;

import ministere.sante.senpna.appeloffre.application.service.OffreDetailAssembler;
import ministere.sante.senpna.appeloffre.domain.command.OffreFournisseurCommands.RetirerOffreCommand;
import ministere.sante.senpna.appeloffre.domain.exception.AccesOffreRefuseException;
import ministere.sante.senpna.appeloffre.domain.exception.OffreFournisseurIntrouvableException;
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
@DisplayName("RetirerOffreUseCaseImpl")
class RetirerOffreUseCaseImplTest {

    @Mock
    OffreFournisseurRepositoryPort offreFournisseurRepositoryPort;
    @Mock
    OffreDetailAssembler offreDetailAssembler;

    RetirerOffreUseCaseImpl sut;

    @BeforeEach
    void setUp() {
        sut = new RetirerOffreUseCaseImpl(offreFournisseurRepositoryPort, offreDetailAssembler);
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

        var command = new RetirerOffreCommand(id, UUID.randomUUID());
        assertThatThrownBy(() -> sut.retirer(command)).isInstanceOf(OffreFournisseurIntrouvableException.class);
    }

    @Test
    @DisplayName("offre appartenant à un autre fournisseur → AccesOffreRefuseException")
    void autreFournisseur_leveException() {
        OffreFournisseur offre = offre(FournisseurId.generate());
        when(offreFournisseurRepositoryPort.findById(offre.getId())).thenReturn(Optional.of(offre));

        var command = new RetirerOffreCommand(offre.getId().getValue(), UUID.randomUUID());
        assertThatThrownBy(() -> sut.retirer(command)).isInstanceOf(AccesOffreRefuseException.class);
    }

    @Test
    @DisplayName("propriétaire retire son offre → RETIREE")
    void proprietaire_retireOffre() {
        FournisseurId fournisseurId = FournisseurId.generate();
        OffreFournisseur offre = offre(fournisseurId);
        when(offreFournisseurRepositoryPort.findById(offre.getId())).thenReturn(Optional.of(offre));
        when(offreFournisseurRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

        sut.retirer(new RetirerOffreCommand(offre.getId().getValue(), fournisseurId.getValue()));

        assertThat(offre.getStatut()).isEqualTo(StatutOffre.RETIREE);
    }
}
