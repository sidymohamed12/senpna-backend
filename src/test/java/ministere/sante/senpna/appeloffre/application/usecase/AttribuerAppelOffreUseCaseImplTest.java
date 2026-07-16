package ministere.sante.senpna.appeloffre.application.usecase;

import ministere.sante.senpna.appeloffre.application.service.AppelOffreDetailAssembler;
import ministere.sante.senpna.appeloffre.domain.command.AppelOffreCommands.AttribuerAppelOffreCommand;
import ministere.sante.senpna.appeloffre.domain.exception.AppelOffreIntrouvableException;
import ministere.sante.senpna.appeloffre.domain.exception.OffreFournisseurIntrouvableException;
import ministere.sante.senpna.appeloffre.domain.model.AppelOffre;
import ministere.sante.senpna.appeloffre.domain.model.LigneAppelOffre;
import ministere.sante.senpna.appeloffre.domain.model.LigneOffre;
import ministere.sante.senpna.appeloffre.domain.model.OffreFournisseur;
import ministere.sante.senpna.appeloffre.domain.port.out.AppelOffreRepositoryPort;
import ministere.sante.senpna.appeloffre.domain.port.out.OffreFournisseurRepositoryPort;
import ministere.sante.senpna.appeloffre.domain.valueobject.AppelOffreId;
import ministere.sante.senpna.appeloffre.domain.valueobject.LigneAppelOffreId;
import ministere.sante.senpna.appeloffre.domain.valueobject.OffreFournisseurId;
import ministere.sante.senpna.appeloffre.domain.valueobject.StatutOffre;
import ministere.sante.senpna.fournisseur.domain.valueobject.FournisseurId;
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
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
@DisplayName("AttribuerAppelOffreUseCaseImpl")
class AttribuerAppelOffreUseCaseImplTest {

    @Mock
    AppelOffreRepositoryPort appelOffreRepositoryPort;
    @Mock
    OffreFournisseurRepositoryPort offreFournisseurRepositoryPort;
    @Mock
    AppelOffreDetailAssembler appelOffreDetailAssembler;

    AttribuerAppelOffreUseCaseImpl sut;

    @BeforeEach
    void setUp() {
        sut = new AttribuerAppelOffreUseCaseImpl(appelOffreRepositoryPort, offreFournisseurRepositoryPort,
                appelOffreDetailAssembler);
    }

    private AppelOffre appelOffreCloture() {
        LigneAppelOffre ligne = LigneAppelOffre.creer(MedicamentId.generate(), "Med", BigDecimal.TEN, "u");
        AppelOffre appelOffre = AppelOffre.creer("AO-1", "Objet", LocalDate.now().plusDays(10), List.of(ligne));
        appelOffre.publier();
        appelOffre.cloturer();
        return appelOffre;
    }

    private OffreFournisseur offreSoumise() {
        LigneOffre ligne = LigneOffre.creer(LigneAppelOffreId.generate(), BigDecimal.TEN, 10);
        return OffreFournisseur.soumettre(AppelOffreId.generate(), FournisseurId.generate(), null, List.of(ligne));
    }

    @Test
    @DisplayName("appel d'offres introuvable → AppelOffreIntrouvableException")
    void appelOffreIntrouvable_leveException() {
        UUID id = UUID.randomUUID();
        when(appelOffreRepositoryPort.findById(AppelOffreId.of(id))).thenReturn(Optional.empty());

        AttribuerAppelOffreCommand command = new AttribuerAppelOffreCommand(id, List.of(), List.of());
        assertThatThrownBy(() -> sut.attribuer(command)).isInstanceOf(AppelOffreIntrouvableException.class);
    }

    @Test
    @DisplayName("offre retenue introuvable → OffreFournisseurIntrouvableException")
    void offreIntrouvable_leveException() {
        AppelOffre appelOffre = appelOffreCloture();
        when(appelOffreRepositoryPort.findById(appelOffre.getId())).thenReturn(Optional.of(appelOffre));
        UUID offreId = UUID.randomUUID();
        when(offreFournisseurRepositoryPort.findById(OffreFournisseurId.of(offreId))).thenReturn(Optional.empty());

        AttribuerAppelOffreCommand command = new AttribuerAppelOffreCommand(appelOffre.getId().getValue(),
                List.of(offreId), List.of());
        assertThatThrownBy(() -> sut.attribuer(command)).isInstanceOf(OffreFournisseurIntrouvableException.class);
    }

    @Test
    @DisplayName("cas nominal → AO attribué, offres retenues et rejetées statuées")
    void casNominal_statueSurLesOffres() {
        AppelOffre appelOffre = appelOffreCloture();
        OffreFournisseur offreRetenue = offreSoumise();
        OffreFournisseur offreRejetee = offreSoumise();

        when(appelOffreRepositoryPort.findById(appelOffre.getId())).thenReturn(Optional.of(appelOffre));
        when(appelOffreRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(offreFournisseurRepositoryPort.findById(offreRetenue.getId())).thenReturn(Optional.of(offreRetenue));
        when(offreFournisseurRepositoryPort.findById(offreRejetee.getId())).thenReturn(Optional.of(offreRejetee));

        sut.attribuer(new AttribuerAppelOffreCommand(appelOffre.getId().getValue(),
                List.of(offreRetenue.getId().getValue()), List.of(offreRejetee.getId().getValue())));

        verify(offreFournisseurRepositoryPort).save(offreRetenue);
        verify(offreFournisseurRepositoryPort).save(offreRejetee);
        org.assertj.core.api.Assertions.assertThat(offreRetenue.getStatut()).isEqualTo(StatutOffre.RETENUE);
        org.assertj.core.api.Assertions.assertThat(offreRejetee.getStatut()).isEqualTo(StatutOffre.REJETEE);
    }

    @Test
    @DisplayName("listes d'offres null → attribue l'AO sans statuer sur aucune offre")
    void listesNull_attribueSansStatuer() {
        AppelOffre appelOffre = appelOffreCloture();
        when(appelOffreRepositoryPort.findById(appelOffre.getId())).thenReturn(Optional.of(appelOffre));
        when(appelOffreRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

        sut.attribuer(new AttribuerAppelOffreCommand(appelOffre.getId().getValue(), null, null));

        verify(offreFournisseurRepositoryPort, never()).findById(any());
        verify(offreFournisseurRepositoryPort, never()).save(any());
        org.assertj.core.api.Assertions.assertThat(appelOffre.getStatut())
                .isEqualTo(ministere.sante.senpna.appeloffre.domain.valueobject.StatutAppelOffre.ATTRIBUE);
    }
}
