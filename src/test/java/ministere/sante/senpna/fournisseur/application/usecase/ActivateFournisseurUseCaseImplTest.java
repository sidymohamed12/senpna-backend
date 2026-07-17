package ministere.sante.senpna.fournisseur.application.usecase;

import ministere.sante.senpna.fournisseur.application.service.FournisseurDetailAssembler;
import ministere.sante.senpna.fournisseur.domain.command.FournisseurCommands.ActivateFournisseurCommand;
import ministere.sante.senpna.fournisseur.domain.exception.FournisseurIntrouvableException;
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
@DisplayName("ActivateFournisseurUseCaseImpl — activation d'un fournisseur")
class ActivateFournisseurUseCaseImplTest {

    @Mock
    FournisseurRepositoryPort fournisseurRepositoryPort;
    @Mock
    FournisseurCachePort fournisseurCachePort;
    @Mock
    FournisseurDetailAssembler fournisseurDetailAssembler;

    ActivateFournisseurUseCaseImpl sut;

    @BeforeEach
    void setUp() {
        sut = new ActivateFournisseurUseCaseImpl(fournisseurRepositoryPort, fournisseurCachePort,
                fournisseurDetailAssembler);
    }

    @Test
    @DisplayName("active le fournisseur, sauvegarde et recharge le cache")
    void active_sauvegardeEtRechargeCache() {
        UUID id = UUID.randomUUID();
        Fournisseur fournisseur = Fournisseur.creer(new Fournisseur.CreationCommand("Nom", null, null, null, null));
        fournisseur.desactiver();
        when(fournisseurRepositoryPort.findById(FournisseurId.of(id))).thenReturn(Optional.of(fournisseur));
        when(fournisseurRepositoryPort.save(fournisseur)).thenReturn(fournisseur);

        sut.activer(new ActivateFournisseurCommand(id));

        assertThat(fournisseur.isActif()).isTrue();
        verify(fournisseurCachePort).reload();
    }

    @Test
    @DisplayName("fournisseur introuvable → FournisseurIntrouvableException, cache non rechargé")
    void introuvable_leveExceptionSansRechargerCache() {
        when(fournisseurRepositoryPort.findById(any())).thenReturn(Optional.empty());

        var activateFournisseurCommand = new ActivateFournisseurCommand(UUID.randomUUID());
        assertThatThrownBy(() -> sut.activer(activateFournisseurCommand))
                .isInstanceOf(FournisseurIntrouvableException.class);

        verify(fournisseurCachePort, never()).reload();
    }
}
