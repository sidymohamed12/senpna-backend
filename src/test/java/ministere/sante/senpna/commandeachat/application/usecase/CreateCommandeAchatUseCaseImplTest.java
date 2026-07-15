package ministere.sante.senpna.commandeachat.application.usecase;

import ministere.sante.senpna.commandeachat.application.service.CommandeAchatDetailAssembler;
import ministere.sante.senpna.commandeachat.domain.command.CommandeAchatCommands.CommandeAchatDetail;
import ministere.sante.senpna.commandeachat.domain.command.CommandeAchatCommands.CreateCommandeAchatCommand;
import ministere.sante.senpna.commandeachat.domain.command.CommandeAchatCommands.LigneCommandeAchatInput;
import ministere.sante.senpna.commandeachat.domain.exception.ReferenceCommandeAchatDejaUtiliseeException;
import ministere.sante.senpna.commandeachat.domain.model.CommandeAchat;
import ministere.sante.senpna.commandeachat.domain.port.out.CommandeAchatRepositoryPort;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CreateCommandeAchatUseCaseImpl")
class CreateCommandeAchatUseCaseImplTest {

    @Mock
    CommandeAchatRepositoryPort commandeAchatRepositoryPort;
    @Mock
    CommandeAchatDetailAssembler commandeAchatDetailAssembler;

    CreateCommandeAchatUseCaseImpl sut;

    @BeforeEach
    void setUp() {
        sut = new CreateCommandeAchatUseCaseImpl(commandeAchatRepositoryPort, commandeAchatDetailAssembler);
    }

    private CreateCommandeAchatCommand commande() {
        return new CreateCommandeAchatCommand("BC-2026-0001", UUID.randomUUID(), UUID.randomUUID(), "Commentaire",
                List.of(new LigneCommandeAchatInput(UUID.randomUUID(), UUID.randomUUID(), BigDecimal.TEN,
                        BigDecimal.valueOf(200_000))));
    }

    @Test
    @DisplayName("référence déjà utilisée → ReferenceCommandeAchatDejaUtiliseeException")
    void referenceDejaUtilisee_leveException() {
        when(commandeAchatRepositoryPort.existsByReferenceIgnoreCase("BC-2026-0001")).thenReturn(true);

        var command = commande();
        assertThatThrownBy(() -> sut.creer(command))
                .isInstanceOf(ReferenceCommandeAchatDejaUtiliseeException.class);

        verify(commandeAchatRepositoryPort, never()).save(any());
    }

    @Test
    @DisplayName("création réussie → sauvegarde l'agrégat et retourne le détail assemblé")
    void creationReussie_sauvegardeEtAssemble() {
        when(commandeAchatRepositoryPort.existsByReferenceIgnoreCase(any())).thenReturn(false);
        when(commandeAchatRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));
        CommandeAchatDetail detail = new CommandeAchatDetail(UUID.randomUUID(), "BC-2026-0001", UUID.randomUUID(),
                UUID.randomUUID(), null, List.of(), null, null, null, null, null, null, null, null);
        when(commandeAchatDetailAssembler.assembler(any(CommandeAchat.class))).thenReturn(detail);

        CommandeAchatDetail result = sut.creer(commande());

        assertThat(result).isSameAs(detail);
        verify(commandeAchatRepositoryPort).save(any());
    }
}
