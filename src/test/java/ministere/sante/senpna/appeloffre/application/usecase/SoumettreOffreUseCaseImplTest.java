package ministere.sante.senpna.appeloffre.application.usecase;

import ministere.sante.senpna.appeloffre.application.service.OffreDetailAssembler;
import ministere.sante.senpna.appeloffre.domain.command.OffreFournisseurCommands.LigneOffreInput;
import ministere.sante.senpna.appeloffre.domain.command.OffreFournisseurCommands.SoumettreOffreCommand;
import ministere.sante.senpna.appeloffre.domain.exception.AppelOffreIntrouvableException;
import ministere.sante.senpna.appeloffre.domain.exception.AppelOffreNonPublieException;
import ministere.sante.senpna.appeloffre.domain.exception.DateClotureDepasseeException;
import ministere.sante.senpna.appeloffre.domain.exception.OffreDejaSoumiseException;
import ministere.sante.senpna.appeloffre.domain.model.AppelOffre;
import ministere.sante.senpna.appeloffre.domain.model.LigneAppelOffre;
import ministere.sante.senpna.appeloffre.domain.port.out.AppelOffreRepositoryPort;
import ministere.sante.senpna.appeloffre.domain.port.out.OffreFournisseurRepositoryPort;
import ministere.sante.senpna.appeloffre.domain.valueobject.AppelOffreId;
import ministere.sante.senpna.appeloffre.domain.valueobject.StatutOffre;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("SoumettreOffreUseCaseImpl")
class SoumettreOffreUseCaseImplTest {

    @Mock
    AppelOffreRepositoryPort appelOffreRepositoryPort;
    @Mock
    OffreFournisseurRepositoryPort offreFournisseurRepositoryPort;
    @Mock
    OffreDetailAssembler offreDetailAssembler;

    SoumettreOffreUseCaseImpl sut;

    @BeforeEach
    void setUp() {
        sut = new SoumettreOffreUseCaseImpl(appelOffreRepositoryPort, offreFournisseurRepositoryPort,
                offreDetailAssembler);
    }

    private AppelOffre appelOffrePublie(LocalDate dateCloture) {
        LigneAppelOffre ligne = LigneAppelOffre.creer(MedicamentId.generate(), "Med", BigDecimal.TEN, "u");
        AppelOffre appelOffre = AppelOffre.creer("AO-1", "Objet", dateCloture, List.of(ligne));
        appelOffre.publier();
        return appelOffre;
    }

    private SoumettreOffreCommand commande(UUID appelOffreId, UUID ligneAppelOffreId) {
        return new SoumettreOffreCommand(appelOffreId, UUID.randomUUID(), "Commentaire",
                List.of(new LigneOffreInput(ligneAppelOffreId, BigDecimal.valueOf(2000), 15)));
    }

    @Test
    @DisplayName("appel d'offres introuvable → AppelOffreIntrouvableException")
    void appelOffreIntrouvable_leveException() {
        UUID id = UUID.randomUUID();
        when(appelOffreRepositoryPort.findById(AppelOffreId.of(id))).thenReturn(Optional.empty());

        var command = commande(id, UUID.randomUUID());
        assertThatThrownBy(() -> sut.soumettre(command)).isInstanceOf(AppelOffreIntrouvableException.class);
    }

    @Test
    @DisplayName("appel d'offres non publié (BROUILLON) → AppelOffreNonPublieException")
    void nonPublie_leveException() {
        LigneAppelOffre ligne = LigneAppelOffre.creer(MedicamentId.generate(), "Med", BigDecimal.TEN, "u");
        AppelOffre appelOffre = AppelOffre.creer("AO-1", "Objet", LocalDate.now().plusDays(10), List.of(ligne));
        when(appelOffreRepositoryPort.findById(appelOffre.getId())).thenReturn(Optional.of(appelOffre));

        var command = commande(appelOffre.getId().getValue(), ligne.getId().getValue());
        assertThatThrownBy(() -> sut.soumettre(command)).isInstanceOf(AppelOffreNonPublieException.class);
    }

    @Test
    @DisplayName("date de clôture dépassée → DateClotureDepasseeException")
    void dateClotureDepassee_leveException() {
        // Publié avec une date de clôture qui, au moment du test, est déjà dépassée :
        // on construit via reconstruct() pour contourner la validation "future" de creer().
        LigneAppelOffre ligne = LigneAppelOffre.creer(MedicamentId.generate(), "Med", BigDecimal.TEN, "u");
        AppelOffre appelOffre = AppelOffre.reconstruct(AppelOffreId.generate(), "AO-1", "Objet",
                LocalDate.now().minusDays(1), ministere.sante.senpna.appeloffre.domain.valueobject.StatutAppelOffre.PUBLIE,
                List.of(ligne), java.time.Instant.now(), java.time.Instant.now());
        when(appelOffreRepositoryPort.findById(appelOffre.getId())).thenReturn(Optional.of(appelOffre));

        var command = commande(appelOffre.getId().getValue(), ligne.getId().getValue());
        assertThatThrownBy(() -> sut.soumettre(command)).isInstanceOf(DateClotureDepasseeException.class);
    }

    @Test
    @DisplayName("offre déjà soumise par ce fournisseur → OffreDejaSoumiseException")
    void offreDejaSoumise_leveException() {
        AppelOffre appelOffre = appelOffrePublie(LocalDate.now().plusDays(10));
        when(appelOffreRepositoryPort.findById(appelOffre.getId())).thenReturn(Optional.of(appelOffre));
        when(offreFournisseurRepositoryPort.existsByAppelOffreIdAndFournisseurIdAndStatut(any(), any(),
                org.mockito.ArgumentMatchers.eq(StatutOffre.SOUMISE))).thenReturn(true);

        var command = commande(appelOffre.getId().getValue(), appelOffre.getLignes().get(0).getId().getValue());
        assertThatThrownBy(() -> sut.soumettre(command)).isInstanceOf(OffreDejaSoumiseException.class);

        verify(offreFournisseurRepositoryPort, never()).save(any());
    }

    @Test
    @DisplayName("cas nominal → offre sauvegardée")
    void casNominal_offreSauvegardee() {
        AppelOffre appelOffre = appelOffrePublie(LocalDate.now().plusDays(10));
        when(appelOffreRepositoryPort.findById(appelOffre.getId())).thenReturn(Optional.of(appelOffre));
        when(offreFournisseurRepositoryPort.existsByAppelOffreIdAndFournisseurIdAndStatut(any(), any(), any()))
                .thenReturn(false);
        when(offreFournisseurRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var command = commande(appelOffre.getId().getValue(), appelOffre.getLignes().get(0).getId().getValue());
        sut.soumettre(command);

        verify(offreFournisseurRepositoryPort).save(any());
    }
}
