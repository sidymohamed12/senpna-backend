package ministere.sante.senpna.carriere.application.usecase;

import ministere.sante.senpna.auth.fixtures.UserFixtures;
import ministere.sante.senpna.carriere.application.service.CandidatureCommandMapper;
import ministere.sante.senpna.carriere.application.service.CandidatureDetailAssembler;
import ministere.sante.senpna.carriere.domain.command.CandidatureCommands.SoumettreCandidatureCommand;
import ministere.sante.senpna.carriere.domain.exception.OpportuniteCarriereIntrouvableException;
import ministere.sante.senpna.carriere.domain.exception.OpportuniteFermeeException;
import ministere.sante.senpna.carriere.domain.model.OpportuniteCarriere;
import ministere.sante.senpna.carriere.domain.port.out.CandidatureRepositoryPort;
import ministere.sante.senpna.carriere.domain.port.out.OpportuniteCarriereRepositoryPort;
import ministere.sante.senpna.carriere.domain.valueobject.Civilite;
import ministere.sante.senpna.carriere.domain.valueobject.OpportuniteCarriereId;
import ministere.sante.senpna.carriere.domain.valueobject.TypeContrat;
import ministere.sante.senpna.shared.domain.exception.UserNotFoundException;
import ministere.sante.senpna.shared.domain.model.User;
import ministere.sante.senpna.shared.domain.port.out.EventPublisherPort;
import ministere.sante.senpna.shared.domain.port.out.UserManagementRepositoryPort;
import ministere.sante.senpna.shared.domain.valueobject.UserId;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("SoumettreCandidatureUseCaseImpl — soumission d'une candidature")
class SoumettreCandidatureUseCaseImplTest {

        @Mock
        OpportuniteCarriereRepositoryPort opportuniteCarriereRepositoryPort;
        @Mock
        CandidatureRepositoryPort candidatureRepositoryPort;
        @Mock
        UserManagementRepositoryPort userManagementRepositoryPort;
        @Mock
        CandidatureCommandMapper commandMapper;
        @Mock
        CandidatureDetailAssembler assembler;
        @Mock
        EventPublisherPort eventPublisherPort;

        SoumettreCandidatureUseCaseImpl sut;

        UUID opportuniteId;

        @BeforeEach
        void setUp() {
                sut = new SoumettreCandidatureUseCaseImpl(opportuniteCarriereRepositoryPort, candidatureRepositoryPort,
                                userManagementRepositoryPort, commandMapper, assembler, eventPublisherPort);
                opportuniteId = UUID.randomUUID();
        }

        private SoumettreCandidatureCommand commande() {
                return new SoumettreCandidatureCommand(opportuniteId, "M", "Ibra Ndiaye", "ibra@mail.sn",
                                "+221771234567",
                                "https://cv.pdf", null, "Motive", true);
        }

        @Test
        @DisplayName("opportunité introuvable → OpportuniteCarriereIntrouvableException")
        void opportuniteIntrouvable_leveException() {
                when(opportuniteCarriereRepositoryPort.findById(OpportuniteCarriereId.of(opportuniteId)))
                                .thenReturn(Optional.empty());

                var command = commande();
                assertThatThrownBy(() -> sut.soumettre(command))
                                .isInstanceOf(OpportuniteCarriereIntrouvableException.class);
        }

        @Test
        @DisplayName("opportunité fermée (statut effectif != OUVERT) → OpportuniteFermeeException")
        void opportuniteFermee_leveException() {
                OpportuniteCarriere opportunite = OpportuniteCarriere.creer("T", "E", "D", null, "L", TypeContrat.CDI,
                                LocalDate.now().plusMonths(2), LocalDate.now().plusMonths(1), UUID.randomUUID(),
                                "Auteur", "rh@e.sn");
                // Reste en BROUILLON — n'accepte pas les candidatures
                when(opportuniteCarriereRepositoryPort.findById(OpportuniteCarriereId.of(opportuniteId)))
                                .thenReturn(Optional.of(opportunite));

                var command = commande();
                assertThatThrownBy(() -> sut.soumettre(command)).isInstanceOf(OpportuniteFermeeException.class);

                verify(candidatureRepositoryPort, never()).save(any());
        }

        @Test
        @DisplayName("email de contact absent sur l'offre → résout l'email de l'auteur")
        void emailContactAbsent_resoutEmailAuteur() {
                OpportuniteCarriere opportunite = OpportuniteCarriere.creer("T", "E", "D", null, "L", TypeContrat.CDI,
                                LocalDate.now().plusMonths(2), LocalDate.now().plusMonths(1), UserFixtures.USER_ID,
                                "Auteur", null);
                opportunite.publier();
                when(opportuniteCarriereRepositoryPort.findById(OpportuniteCarriereId.of(opportuniteId)))
                                .thenReturn(Optional.of(opportunite));
                when(commandMapper.versCivilite("M")).thenReturn(Civilite.M);
                User auteur = UserFixtures.actif();
                when(userManagementRepositoryPort.findById(UserId.of(UserFixtures.USER_ID)))
                                .thenReturn(Optional.of(auteur));
                when(candidatureRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

                sut.soumettre(commande());

                verify(userManagementRepositoryPort).findById(UserId.of(UserFixtures.USER_ID));
        }

        @Test
        @DisplayName("email de contact absent et auteur introuvable → UserNotFoundException")
        void emailContactAbsentEtAuteurIntrouvable_leveException() {
                OpportuniteCarriere opportunite = OpportuniteCarriere.creer("T", "E", "D", null, "L", TypeContrat.CDI,
                                LocalDate.now().plusMonths(2), LocalDate.now().plusMonths(1), UUID.randomUUID(),
                                "Auteur", null);
                opportunite.publier();
                when(opportuniteCarriereRepositoryPort.findById(OpportuniteCarriereId.of(opportuniteId)))
                                .thenReturn(Optional.of(opportunite));
                when(commandMapper.versCivilite("M")).thenReturn(Civilite.M);
                when(userManagementRepositoryPort.findById(any())).thenReturn(Optional.empty());

                var command = commande();
                assertThatThrownBy(() -> sut.soumettre(command)).isInstanceOf(UserNotFoundException.class);
        }

        @Test
        @DisplayName("succès → sauvegarde la candidature et publie l'événement associé")
        void succes_sauvegardeEtPublieEvenement() {
                OpportuniteCarriere opportunite = OpportuniteCarriere.creer("T", "E", "D", null, "L", TypeContrat.CDI,
                                LocalDate.now().plusMonths(2), LocalDate.now().plusMonths(1), UUID.randomUUID(),
                                "Auteur", "rh@e.sn");
                opportunite.publier();
                when(opportuniteCarriereRepositoryPort.findById(OpportuniteCarriereId.of(opportuniteId)))
                                .thenReturn(Optional.of(opportunite));
                when(commandMapper.versCivilite("M")).thenReturn(Civilite.M);
                when(candidatureRepositoryPort.save(any())).thenAnswer(inv -> inv.getArgument(0));
                when(assembler.assembler(any())).thenReturn(null);

                sut.soumettre(commande());

                verify(eventPublisherPort).publishAndClear(any());
                verify(candidatureRepositoryPort).save(any());
        }
}
