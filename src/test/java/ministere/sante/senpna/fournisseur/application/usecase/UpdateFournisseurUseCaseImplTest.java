package ministere.sante.senpna.fournisseur.application.usecase;

import ministere.sante.senpna.fournisseur.application.service.FournisseurDetailAssembler;
import ministere.sante.senpna.fournisseur.domain.command.FournisseurCommands.UpdateFournisseurCommand;
import ministere.sante.senpna.fournisseur.domain.exception.FournisseurIntrouvableException;
import ministere.sante.senpna.fournisseur.domain.exception.NomFournisseurDejaUtiliseException;
import ministere.sante.senpna.fournisseur.domain.model.Fournisseur;
import ministere.sante.senpna.fournisseur.domain.port.out.FournisseurRepositoryPort;
import ministere.sante.senpna.fournisseur.domain.valueobject.FournisseurId;
import ministere.sante.senpna.shared.domain.port.out.FournisseurCachePort;

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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("UpdateFournisseurUseCaseImpl — modification d'un fournisseur")
class UpdateFournisseurUseCaseImplTest {

    @Mock
    FournisseurRepositoryPort fournisseurRepositoryPort;
    @Mock
    FournisseurCachePort fournisseurCachePort;
    @Mock
    FournisseurDetailAssembler fournisseurDetailAssembler;

    UpdateFournisseurUseCaseImpl sut;

    UUID id;

    @BeforeEach
    void setUp() {
        sut = new UpdateFournisseurUseCaseImpl(fournisseurRepositoryPort, fournisseurCachePort,
                fournisseurDetailAssembler);
        id = UUID.randomUUID();
    }

    @Test
    @DisplayName("fournisseur introuvable → FournisseurIntrouvableException")
    void introuvable_leveException() {
        when(fournisseurRepositoryPort.findById(any())).thenReturn(Optional.empty());

        var updateFournisseurCommand = new UpdateFournisseurCommand(id, "Nom", null, null, null, null);
        assertThatThrownBy(() -> sut.modifier(updateFournisseurCommand))
                .isInstanceOf(FournisseurIntrouvableException.class);
    }

    @Test
    @DisplayName("nom inchangé (insensible à la casse) → aucune vérification d'unicité")
    void nomInchange_aucuneVerificationUnicite() {
        Fournisseur fournisseur = Fournisseur.creer("Pharma Plus", null, null, null, null);
        when(fournisseurRepositoryPort.findById(FournisseurId.of(id))).thenReturn(Optional.of(fournisseur));
        when(fournisseurRepositoryPort.save(any())).thenReturn(fournisseur);

        sut.modifier(new UpdateFournisseurCommand(id, "PHARMA PLUS", "Nouvelle adresse", null, null, null));

        verify(fournisseurRepositoryPort, never()).existsByNomIgnoreCaseAndIdNot(any(), any());
        assertThat(fournisseur.getAdresse()).isEqualTo("Nouvelle adresse");
    }

    @Test
    @DisplayName("nouveau nom déjà utilisé par un autre fournisseur → NomFournisseurDejaUtiliseException")
    void nouveauNomDejaUtilise_leveException() {
        Fournisseur fournisseur = Fournisseur.creer("Ancien Nom", null, null, null, null);
        when(fournisseurRepositoryPort.findById(FournisseurId.of(id))).thenReturn(Optional.of(fournisseur));
        when(fournisseurRepositoryPort.existsByNomIgnoreCaseAndIdNot("Nouveau Nom", fournisseur.getId()))
                .thenReturn(true);

        var updateFournisseurCommand = new UpdateFournisseurCommand(id, "Nouveau Nom", null, null, null, null);
        assertThatThrownBy(() -> sut.modifier(updateFournisseurCommand))
                .isInstanceOf(NomFournisseurDejaUtiliseException.class);

        verify(fournisseurRepositoryPort, never()).save(any());
    }

    @Test
    @DisplayName("modification valide → informations mises à jour, sauvegarde et cache rechargé")
    void modificationValide_metAJourEtRechargeCache() {
        Fournisseur fournisseur = Fournisseur.creer("Ancien Nom", null, null, null, null);
        when(fournisseurRepositoryPort.findById(FournisseurId.of(id))).thenReturn(Optional.of(fournisseur));
        when(fournisseurRepositoryPort.existsByNomIgnoreCaseAndIdNot(any(), any())).thenReturn(false);
        when(fournisseurRepositoryPort.save(fournisseur)).thenReturn(fournisseur);

        sut.modifier(new UpdateFournisseurCommand(id, "Nouveau Nom", "Adresse", "Tel", "Email", "Contact"));

        assertThat(fournisseur.getNom()).isEqualTo("Nouveau Nom");
        verify(fournisseurCachePort).reload();
    }
}
