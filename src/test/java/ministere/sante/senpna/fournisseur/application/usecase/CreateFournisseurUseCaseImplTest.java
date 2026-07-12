package ministere.sante.senpna.fournisseur.application.usecase;

import ministere.sante.senpna.fournisseur.application.service.FournisseurDetailAssembler;
import ministere.sante.senpna.fournisseur.domain.command.FournisseurCommands.CreateFournisseurCommand;
import ministere.sante.senpna.fournisseur.domain.command.FournisseurCommands.FournisseurDetail;
import ministere.sante.senpna.fournisseur.domain.exception.NomFournisseurDejaUtiliseException;
import ministere.sante.senpna.fournisseur.domain.port.out.FournisseurRepositoryPort;
import ministere.sante.senpna.shared.domain.port.out.FournisseurCachePort;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CreateFournisseurUseCaseImpl — création d'un fournisseur")
class CreateFournisseurUseCaseImplTest {

    @Mock
    FournisseurRepositoryPort fournisseurRepositoryPort;
    @Mock
    FournisseurCachePort fournisseurCachePort;
    @Mock
    FournisseurDetailAssembler fournisseurDetailAssembler;

    CreateFournisseurUseCaseImpl sut;

    @BeforeEach
    void setUp() {
        sut = new CreateFournisseurUseCaseImpl(fournisseurRepositoryPort, fournisseurCachePort,
                fournisseurDetailAssembler);
    }

    private CreateFournisseurCommand commande() {
        return new CreateFournisseurCommand("Pharma Plus", "Dakar", "+221771234567", "c@p.sn", "Awa");
    }

    @Test
    @DisplayName("nom déjà utilisé (insensible à la casse) → NomFournisseurDejaUtiliseException")
    void nomDejaUtilise_leveException() {
        when(fournisseurRepositoryPort.existsByNomIgnoreCase("Pharma Plus")).thenReturn(true);

        var command = commande();
        assertThatThrownBy(() -> sut.creer(command))
                .isInstanceOf(NomFournisseurDejaUtiliseException.class);

        verify(fournisseurRepositoryPort, never()).save(any());
        verify(fournisseurCachePort, never()).reload();
    }

    @Test
    @DisplayName("création réussie → sauvegarde et recharge le cache immédiatement")
    void creationReussie_sauvegardeEtRechargeCache() {
        when(fournisseurRepositoryPort.existsByNomIgnoreCase(any())).thenReturn(false);
        when(fournisseurRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));
        FournisseurDetail detail = new FournisseurDetail(null, "Pharma Plus", null, null, null, null, true, null,
                null);
        when(fournisseurDetailAssembler.assembler(any())).thenReturn(detail);

        FournisseurDetail result = sut.creer(commande());

        assertThat(result).isSameAs(detail);
        verify(fournisseurCachePort).reload();
    }

    @Test
    @DisplayName("recherche l'unicité sur le nom nettoyé des espaces")
    void unicite_surNomNettoye() {
        CreateFournisseurCommand commande = new CreateFournisseurCommand("  Pharma Plus  ", null, null, null, null);
        when(fournisseurRepositoryPort.existsByNomIgnoreCase("Pharma Plus")).thenReturn(false);
        when(fournisseurRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(fournisseurDetailAssembler.assembler(any())).thenReturn(null);

        sut.creer(commande);

        verify(fournisseurRepositoryPort).existsByNomIgnoreCase("Pharma Plus");
    }
}
