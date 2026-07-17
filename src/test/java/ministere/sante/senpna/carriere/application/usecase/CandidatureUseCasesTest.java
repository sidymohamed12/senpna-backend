package ministere.sante.senpna.carriere.application.usecase;

import ministere.sante.senpna.carriere.application.service.CandidatureCommandMapper;
import ministere.sante.senpna.carriere.application.service.CandidatureDetailAssembler;
import ministere.sante.senpna.carriere.domain.command.CandidatureCommands.CandidatureDetail;
import ministere.sante.senpna.carriere.domain.command.CandidatureCommands.CandidaturePage;
import ministere.sante.senpna.carriere.domain.command.CandidatureCommands.GetCandidatureQuery;
import ministere.sante.senpna.carriere.domain.command.CandidatureCommands.ListCandidaturesQuery;
import ministere.sante.senpna.carriere.domain.command.CandidatureCommands.SoumettreCandidatureCommand;
import ministere.sante.senpna.carriere.domain.exception.CandidatureIntrouvableException;
import ministere.sante.senpna.carriere.domain.exception.CiviliteInvalideException;
import ministere.sante.senpna.carriere.domain.exception.OpportuniteCarriereIntrouvableException;
import ministere.sante.senpna.carriere.domain.exception.OpportuniteFermeeException;
import ministere.sante.senpna.carriere.domain.model.Candidature;
import ministere.sante.senpna.carriere.domain.model.OpportuniteCarriere;
import ministere.sante.senpna.carriere.domain.port.out.CandidatureRepositoryPort;
import ministere.sante.senpna.carriere.domain.port.out.OpportuniteCarriereRepositoryPort;
import ministere.sante.senpna.carriere.domain.valueobject.CandidatureId;
import ministere.sante.senpna.carriere.domain.valueobject.Civilite;
import ministere.sante.senpna.carriere.domain.valueobject.OpportuniteCarriereId;
import ministere.sante.senpna.carriere.domain.valueobject.StatutOpportunite;
import ministere.sante.senpna.carriere.domain.valueobject.TypeContrat;
import ministere.sante.senpna.shared.domain.exception.UserNotFoundException;
import ministere.sante.senpna.shared.domain.model.User;
import ministere.sante.senpna.shared.domain.port.out.EventPublisherPort;
import ministere.sante.senpna.shared.domain.port.out.UserManagementRepositoryPort;
import ministere.sante.senpna.shared.domain.valueobject.Email;
import ministere.sante.senpna.shared.domain.valueobject.HashedPassword;
import ministere.sante.senpna.shared.domain.valueobject.Nom;
import ministere.sante.senpna.shared.domain.valueobject.PageResult;
import ministere.sante.senpna.shared.domain.valueobject.Phone;
import ministere.sante.senpna.shared.domain.valueobject.Prenom;
import ministere.sante.senpna.shared.domain.valueobject.UserId;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("Candidature Use Cases — soumettre / get / list")
class CandidatureUseCasesTest {

        private static final UUID OPPORTUNITE_ID = UUID.randomUUID();
        private static final UUID CANDIDATURE_ID = UUID.randomUUID();
        private static final UUID AUTEUR_ID = UUID.randomUUID();

        private final CandidatureCommandMapper commandMapper = new CandidatureCommandMapper();
        private final CandidatureDetailAssembler assembler = new CandidatureDetailAssembler();

        @ExtendWith(MockitoExtension.class)
        @Nested
        @DisplayName("soumettre")
        class Soumettre {

                @Mock
                private OpportuniteCarriereRepositoryPort opportuniteCarriereRepositoryPort;

                @Mock
                private CandidatureRepositoryPort candidatureRepositoryPort;

                @Mock
                private UserManagementRepositoryPort userManagementRepositoryPort;

                @Mock
                private EventPublisherPort eventPublisherPort;

                @Test
                @DisplayName("soumet la candidature quand l'offre est OUVERT, avec e-mail de contact explicite")
                void soumetQuandOffreOuverteAvecContact() {
                        SoumettreCandidatureUseCaseImpl useCase = new SoumettreCandidatureUseCaseImpl(
                                        opportuniteCarriereRepositoryPort, candidatureRepositoryPort,
                                        userManagementRepositoryPort, commandMapper, assembler, eventPublisherPort);
                        OpportuniteCarriere offre = offreExistante(StatutOpportunite.OUVERT, "rh@senpna.sn");
                        when(opportuniteCarriereRepositoryPort.findById(OpportuniteCarriereId.of(OPPORTUNITE_ID)))
                                        .thenReturn(Optional.of(offre));
                        when(candidatureRepositoryPort.save(any(Candidature.class)))
                                        .thenAnswer(inv -> inv.getArgument(0));

                        CandidatureDetail result = useCase.soumettre(commandeValide());

                        assertThat(result.nomComplet()).isEqualTo("Fatou Diagne");
                        assertThat(result.opportuniteId()).isEqualTo(OPPORTUNITE_ID);
                        verify(userManagementRepositoryPort, never()).findById(any());
                        verify(eventPublisherPort).publishAndClear(any(Candidature.class));
                }

                @Test
                @DisplayName("résout l'e-mail de contact RH via l'auteur quand l'offre n'a pas d'e-mail de contact dédié")
                void soumetQuandOffreSansContactExplicite() {
                        SoumettreCandidatureUseCaseImpl useCase = new SoumettreCandidatureUseCaseImpl(
                                        opportuniteCarriereRepositoryPort, candidatureRepositoryPort,
                                        userManagementRepositoryPort, commandMapper, assembler, eventPublisherPort);
                        OpportuniteCarriere offre = offreExistante(StatutOpportunite.OUVERT, null);
                        when(opportuniteCarriereRepositoryPort.findById(OpportuniteCarriereId.of(OPPORTUNITE_ID)))
                                        .thenReturn(Optional.of(offre));
                        when(userManagementRepositoryPort.findById(UserId.of(AUTEUR_ID)))
                                        .thenReturn(Optional.of(auteurValide()));
                        when(candidatureRepositoryPort.save(any(Candidature.class)))
                                        .thenAnswer(inv -> inv.getArgument(0));

                        useCase.soumettre(commandeValide());

                        verify(userManagementRepositoryPort).findById(UserId.of(AUTEUR_ID));
                }

                @Test
                @DisplayName("lève UserNotFoundException quand l'auteur de l'offre est introuvable et qu'aucun contact n'est défini")
                void leveUserNotFoundQuandAuteurIntrouvable() {
                        SoumettreCandidatureUseCaseImpl useCase = new SoumettreCandidatureUseCaseImpl(
                                        opportuniteCarriereRepositoryPort, candidatureRepositoryPort,
                                        userManagementRepositoryPort, commandMapper, assembler, eventPublisherPort);
                        OpportuniteCarriere offre = offreExistante(StatutOpportunite.OUVERT, null);
                        when(opportuniteCarriereRepositoryPort.findById(OpportuniteCarriereId.of(OPPORTUNITE_ID)))
                                        .thenReturn(Optional.of(offre));
                        when(userManagementRepositoryPort.findById(UserId.of(AUTEUR_ID)))
                                        .thenReturn(Optional.empty());

                        var commandeValide = commandeValide();
                        assertThatThrownBy(() -> useCase.soumettre(commandeValide))
                                        .isInstanceOf(UserNotFoundException.class);

                        verify(candidatureRepositoryPort, never()).save(any());
                }

                @Test
                @DisplayName("lève OpportuniteCarriereIntrouvableException quand l'offre n'existe pas")
                void leveIntrouvableQuandOffreInexistante() {
                        SoumettreCandidatureUseCaseImpl useCase = new SoumettreCandidatureUseCaseImpl(
                                        opportuniteCarriereRepositoryPort, candidatureRepositoryPort,
                                        userManagementRepositoryPort, commandMapper, assembler, eventPublisherPort);
                        when(opportuniteCarriereRepositoryPort.findById(OpportuniteCarriereId.of(OPPORTUNITE_ID)))
                                        .thenReturn(Optional.empty());

                        var commandeValide = commandeValide();
                        assertThatThrownBy(() -> useCase.soumettre(commandeValide))
                                        .isInstanceOf(OpportuniteCarriereIntrouvableException.class);

                        verify(candidatureRepositoryPort, never()).save(any());
                }

                @Test
                @DisplayName("lève OpportuniteFermeeException quand l'offre est EN_COURS")
                void leveOpportuniteFermeeQuandEnCours() {
                        SoumettreCandidatureUseCaseImpl useCase = new SoumettreCandidatureUseCaseImpl(
                                        opportuniteCarriereRepositoryPort, candidatureRepositoryPort,
                                        userManagementRepositoryPort, commandMapper, assembler, eventPublisherPort);
                        OpportuniteCarriere offre = offreExistante(StatutOpportunite.EN_COURS, null);
                        when(opportuniteCarriereRepositoryPort.findById(OpportuniteCarriereId.of(OPPORTUNITE_ID)))
                                        .thenReturn(Optional.of(offre));

                        var commandeValide = commandeValide();
                        assertThatThrownBy(() -> useCase.soumettre(commandeValide))
                                        .isInstanceOf(OpportuniteFermeeException.class);

                        verify(candidatureRepositoryPort, never()).save(any());
                }

                @Test
                @DisplayName("lève OpportuniteFermeeException quand l'offre est persistée OUVERT mais expirée")
                void leveOpportuniteFermeeQuandExpiree() {
                        SoumettreCandidatureUseCaseImpl useCase = new SoumettreCandidatureUseCaseImpl(
                                        opportuniteCarriereRepositoryPort, candidatureRepositoryPort,
                                        userManagementRepositoryPort, commandMapper, assembler, eventPublisherPort);
                        OpportuniteCarriere offre = OpportuniteCarriere.builder()
                            .id(OpportuniteCarriereId.of(OPPORTUNITE_ID))
                            .titre("Titre")
                            .nomEntreprise("Entreprise")
                            .description("Description")
                            .ficheDePosteUrl(null)
                            .lieu("Dakar, Sénégal")
                            .typeContrat(TypeContrat.CDI)
                            .dateDebut(null)
                            .dateLimiteCandidature(LocalDate.now().minusDays(1))
                            .auteurId(AUTEUR_ID)
                            .auteurNom("Cheikh Ba")
                            .emailContact(null)
                            .statut(StatutOpportunite.OUVERT)
                            .createdAt(Instant.now())
                            .updatedAt(Instant.now())
                            .build();
                        when(opportuniteCarriereRepositoryPort.findById(OpportuniteCarriereId.of(OPPORTUNITE_ID)))
                                        .thenReturn(Optional.of(offre));

                        var commandeValide = commandeValide();
                        assertThatThrownBy(() -> useCase.soumettre(commandeValide))
                                        .isInstanceOf(OpportuniteFermeeException.class);
                }

                @Test
                @DisplayName("lève CiviliteInvalideException pour une civilité hors énumération")
                void leveCiviliteInvalide() {
                        SoumettreCandidatureUseCaseImpl useCase = new SoumettreCandidatureUseCaseImpl(
                                        opportuniteCarriereRepositoryPort, candidatureRepositoryPort,
                                        userManagementRepositoryPort, commandMapper, assembler, eventPublisherPort);
                        OpportuniteCarriere offre = offreExistante(StatutOpportunite.OUVERT, "rh@senpna.sn");
                        when(opportuniteCarriereRepositoryPort.findById(OpportuniteCarriereId.of(OPPORTUNITE_ID)))
                                        .thenReturn(Optional.of(offre));

                        var soumettreCandidatureCommand = new SoumettreCandidatureCommand(OPPORTUNITE_ID, "DR", "Fatou Diagne", "fatou.diagne@example.sn", "+221771112233", "https://cdn.senpna.sn/cvs/fatou-diagne.pdf", null, null, true);
                        assertThatThrownBy(() -> useCase.soumettre(soumettreCandidatureCommand))
                                        .isInstanceOf(CiviliteInvalideException.class);
                }
        }

        @ExtendWith(MockitoExtension.class)
        @Nested
        @DisplayName("obtenir")
        class Obtenir {

                @Mock
                private CandidatureRepositoryPort candidatureRepositoryPort;

                @Test
                @DisplayName("retourne la candidature quand elle existe")
                void retourneQuandExiste() {
                        GetCandidatureUseCaseImpl useCase = new GetCandidatureUseCaseImpl(candidatureRepositoryPort,
                                        assembler);
                        Candidature existante = candidatureExistante();
                        when(candidatureRepositoryPort.findById(CandidatureId.of(CANDIDATURE_ID)))
                                        .thenReturn(Optional.of(existante));

                        CandidatureDetail result = useCase.obtenir(new GetCandidatureQuery(CANDIDATURE_ID));

                        assertThat(result.id()).isEqualTo(CANDIDATURE_ID);
                }

                @Test
                @DisplayName("lève CandidatureIntrouvableException quand elle n'existe pas")
                void leveIntrouvableQuandInexistante() {
                        GetCandidatureUseCaseImpl useCase = new GetCandidatureUseCaseImpl(candidatureRepositoryPort,
                                        assembler);
                        when(candidatureRepositoryPort.findById(CandidatureId.of(CANDIDATURE_ID)))
                                        .thenReturn(Optional.empty());

                        var getCandidatureQuery = new GetCandidatureQuery(CANDIDATURE_ID);
                        assertThatThrownBy(() -> useCase.obtenir(getCandidatureQuery))
                                        .isInstanceOf(CandidatureIntrouvableException.class);
                }
        }

        @ExtendWith(MockitoExtension.class)
        @Nested
        @DisplayName("lister")
        class Lister {

                @Mock
                private CandidatureRepositoryPort candidatureRepositoryPort;

                @Test
                @DisplayName("délègue la recherche paginée au repository")
                void delegueRecherchePaginee() {
                        ListCandidaturesUseCaseImpl useCase = new ListCandidaturesUseCaseImpl(
                                        candidatureRepositoryPort, assembler);
                        Candidature existante = candidatureExistante();
                        when(candidatureRepositoryPort.search(any(), any()))
                                        .thenReturn(PageResult.of(List.of(existante), 0, 20, 1));

                        CandidaturePage result = useCase.lister(
                                        new ListCandidaturesQuery(OPPORTUNITE_ID, "diagne", 0, 20, "createdAt", "DESC"));

                        assertThat(result.content()).hasSize(1);
                        assertThat(result.totalElements()).isEqualTo(1);
                }
        }

        // ── Fixtures ─────────────────────────────────────────────────────────

        private SoumettreCandidatureCommand commandeValide() {
                return new SoumettreCandidatureCommand(OPPORTUNITE_ID, "MME", "Fatou Diagne",
                                "fatou.diagne@example.sn", "+221771112233", "https://cdn.senpna.sn/cvs/fatou-diagne.pdf",
                                "https://cdn.senpna.sn/lettres-de-motivation/fatou-diagne.pdf",
                                "Message de motivation.", true);
        }

        private OpportuniteCarriere offreExistante(StatutOpportunite statut, String emailContact) {
                Instant maintenant = Instant.now();
                return OpportuniteCarriere.builder()
                    .id(OpportuniteCarriereId.of(OPPORTUNITE_ID))
                    .titre("Pharmacien(ne) responsable")
                    .nomEntreprise("PNA")
                    .description("Description")
                    .ficheDePosteUrl(null)
                    .lieu("Dakar, Sénégal")
                    .typeContrat(TypeContrat.CDI)
                    .dateDebut(null)
                    .dateLimiteCandidature(LocalDate.now().plusDays(30))
                    .auteurId(AUTEUR_ID)
                    .auteurNom("Cheikh Ba")
                    .emailContact(emailContact)
                    .statut(statut)
                    .createdAt(maintenant)
                    .updatedAt(maintenant)
                    .build();
        }

        private User auteurValide() {
                return User.creer(new User.CreationCommand(Nom.of("Ba"), Prenom.of("Cheikh"), Email.of("cheikh.ba@sante.sn"), null,
                                HashedPassword.of("$2a$12$abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzab"),
                                Set.of()));
        }

        private Candidature candidatureExistante() {
                Instant maintenant = Instant.now();
                return Candidature.builder()
                    .id(CandidatureId.of(CANDIDATURE_ID))
                    .opportuniteId(OPPORTUNITE_ID)
                    .civilite(Civilite.MME)
                    .nomComplet("Fatou Diagne")
                    .email(Email.of("fatou.diagne@example.sn"))
                    .telephone(Phone.of("+221771112233"))
                    .cvUrl("https://cdn.senpna.sn/cvs/fatou-diagne.pdf")
                    .lettreMotivationUrl(null)
                    .messageComplementaire(null)
                    .consentementRgpd(true)
                    .createdAt(maintenant)
                    .updatedAt(maintenant)
                    .build();
        }
}
