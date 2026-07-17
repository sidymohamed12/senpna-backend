package ministere.sante.senpna.projet.application.usecase;

import ministere.sante.senpna.projet.application.service.ProjetCommandMapper;
import ministere.sante.senpna.projet.application.service.ProjetDetailAssembler;
import ministere.sante.senpna.projet.domain.command.ProjetCommands.UpdateProjetCommand;
import ministere.sante.senpna.projet.domain.exception.ProjetIntrouvableException;
import ministere.sante.senpna.projet.domain.model.Projet;
import ministere.sante.senpna.projet.domain.port.out.ProjetRepositoryPort;
import ministere.sante.senpna.projet.domain.valueobject.CategorieProjet;
import ministere.sante.senpna.projet.domain.valueobject.ProjetId;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("UpdateProjetUseCaseImpl — modification d'un projet")
class UpdateProjetUseCaseImplTest {

    @Mock
    ProjetRepositoryPort projetRepositoryPort;
    @Mock
    ProjetCommandMapper commandMapper;
    @Mock
    ProjetDetailAssembler assembler;

    UpdateProjetUseCaseImpl sut;

    @BeforeEach
    void setUp() {
        sut = new UpdateProjetUseCaseImpl(projetRepositoryPort, commandMapper, assembler);
    }

    @Test
    @DisplayName("projet introuvable → ProjetIntrouvableException")
    void introuvable_leveException() {
        UUID id = UUID.randomUUID();
        when(projetRepositoryPort.findById(ProjetId.of(id))).thenReturn(Optional.empty());

        var updateProjetCommand = new UpdateProjetCommand(id, "SANTE", "Nom", "Desc", List.of(), List.of(), null);
        assertThatThrownBy(() -> sut.modifier(updateProjetCommand))
                .isInstanceOf(ProjetIntrouvableException.class);
    }

    @Test
    @DisplayName("modification valide → contenu mis à jour et sauvegardé")
    void modificationValide_metAJourEtSauvegarde() {
        UUID id = UUID.randomUUID();
        Projet projet = Projet.creer(new Projet.CreationCommand(CategorieProjet.SOCIAL, "Ancien nom", "Ancienne desc", List.of(), List.of(),
                null));
        when(projetRepositoryPort.findById(ProjetId.of(id))).thenReturn(Optional.of(projet));
        when(commandMapper.versCategorie("SANTE")).thenReturn(CategorieProjet.SANTE);
        when(projetRepositoryPort.save(projet)).thenReturn(projet);

        sut.modifier(new UpdateProjetCommand(id, "SANTE", "Nouveau nom", "Nouvelle desc", List.of("O"), List.of("I"),
                "img"));

        assertThat(projet.getNom()).isEqualTo("Nouveau nom");
        assertThat(projet.getCategorie()).isEqualTo(CategorieProjet.SANTE);
    }
}
