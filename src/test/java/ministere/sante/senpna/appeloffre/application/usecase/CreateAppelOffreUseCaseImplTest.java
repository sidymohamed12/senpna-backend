package ministere.sante.senpna.appeloffre.application.usecase;

import ministere.sante.senpna.appeloffre.application.service.AppelOffreDetailAssembler;
import ministere.sante.senpna.appeloffre.domain.command.AppelOffreCommands.AppelOffreDetail;
import ministere.sante.senpna.appeloffre.domain.command.AppelOffreCommands.CreateAppelOffreCommand;
import ministere.sante.senpna.appeloffre.domain.command.AppelOffreCommands.LigneAppelOffreInput;
import ministere.sante.senpna.appeloffre.domain.exception.ReferenceAppelOffreDejaUtiliseeException;
import ministere.sante.senpna.appeloffre.domain.model.AppelOffre;
import ministere.sante.senpna.appeloffre.domain.port.out.AppelOffreRepositoryPort;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CreateAppelOffreUseCaseImpl")
class CreateAppelOffreUseCaseImplTest {

    @Mock
    AppelOffreRepositoryPort appelOffreRepositoryPort;
    @Mock
    AppelOffreDetailAssembler appelOffreDetailAssembler;

    CreateAppelOffreUseCaseImpl sut;

    @BeforeEach
    void setUp() {
        sut = new CreateAppelOffreUseCaseImpl(appelOffreRepositoryPort, appelOffreDetailAssembler);
    }

    private CreateAppelOffreCommand commande() {
        return new CreateAppelOffreCommand("AO-2026-0001", "Achat Amoxicilline", LocalDate.now().plusDays(10),
                List.of(new LigneAppelOffreInput(UUID.randomUUID(), "Amoxicilline 500 mg", BigDecimal.TEN,
                        "Comprimé")));
    }

    @Test
    @DisplayName("référence déjà utilisée → ReferenceAppelOffreDejaUtiliseeException")
    void referenceDejaUtilisee_leveException() {
        when(appelOffreRepositoryPort.existsByReferenceIgnoreCase("AO-2026-0001")).thenReturn(true);

        var command = commande();
        assertThatThrownBy(() -> sut.creer(command))
                .isInstanceOf(ReferenceAppelOffreDejaUtiliseeException.class);

        verify(appelOffreRepositoryPort, never()).save(any());
    }

    @Test
    @DisplayName("création réussie → sauvegarde l'agrégat et retourne le détail assemblé")
    void creationReussie_sauvegardeEtAssemble() {
        when(appelOffreRepositoryPort.existsByReferenceIgnoreCase(any())).thenReturn(false);
        when(appelOffreRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));
        AppelOffreDetail detail = mockDetail();
        when(appelOffreDetailAssembler.assembler(any(AppelOffre.class))).thenReturn(detail);

        AppelOffreDetail result = sut.creer(commande());

        assertThat(result).isSameAs(detail);
        verify(appelOffreRepositoryPort).save(any());
    }

    private AppelOffreDetail mockDetail() {
        return new AppelOffreDetail(UUID.randomUUID(), "AO-2026-0001", "Objet", LocalDate.now().plusDays(10), null,
                List.of(), null, null);
    }
}
