package ministere.sante.senpna.appeloffre.application.usecase;

import ministere.sante.senpna.appeloffre.application.service.AppelOffreDetailAssembler;
import ministere.sante.senpna.appeloffre.domain.command.AppelOffreCommands.PublierAppelOffreCommand;
import ministere.sante.senpna.appeloffre.domain.exception.AppelOffreIntrouvableException;
import ministere.sante.senpna.appeloffre.domain.model.AppelOffre;
import ministere.sante.senpna.appeloffre.domain.model.LigneAppelOffre;
import ministere.sante.senpna.appeloffre.domain.port.out.AppelOffreRepositoryPort;
import ministere.sante.senpna.appeloffre.domain.valueobject.AppelOffreId;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("PublierAppelOffreUseCaseImpl")
class PublierAppelOffreUseCaseImplTest {

    @Mock
    AppelOffreRepositoryPort appelOffreRepositoryPort;
    @Mock
    AppelOffreDetailAssembler appelOffreDetailAssembler;

    PublierAppelOffreUseCaseImpl sut;

    @BeforeEach
    void setUp() {
        sut = new PublierAppelOffreUseCaseImpl(appelOffreRepositoryPort, appelOffreDetailAssembler);
    }

    private AppelOffre appelOffre() {
        LigneAppelOffre ligne = LigneAppelOffre.creer(MedicamentId.generate(), "Med", BigDecimal.TEN, "u");

        return AppelOffre.creer(new AppelOffre.CreationCommand("AO-1", "Objet", LocalDate.now().plusDays(10), List.of(ligne)));
    }

    @Test
    @DisplayName("appel d'offres introuvable → AppelOffreIntrouvableException")
    void introuvable_leveException() {
        UUID id = UUID.randomUUID();
        when(appelOffreRepositoryPort.findById(AppelOffreId.of(id))).thenReturn(Optional.empty());

        PublierAppelOffreCommand command = new PublierAppelOffreCommand(id);
        assertThatThrownBy(() -> sut.publier(command)).isInstanceOf(AppelOffreIntrouvableException.class);
    }

    @Test
    @DisplayName("cas nominal → transition appliquée et sauvegardée")
    void casNominal_transitionAppliquee() {
        AppelOffre appelOffre = appelOffre();
        when(appelOffreRepositoryPort.findById(appelOffre.getId())).thenReturn(Optional.of(appelOffre));
        when(appelOffreRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

        sut.publier(new PublierAppelOffreCommand(appelOffre.getId().getValue()));

        verify(appelOffreRepositoryPort).save(appelOffre);
    }
}
