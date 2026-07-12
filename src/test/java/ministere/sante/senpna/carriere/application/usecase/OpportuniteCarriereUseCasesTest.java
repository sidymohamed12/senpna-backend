package ministere.sante.senpna.carriere.application.usecase;

import ministere.sante.senpna.carriere.application.service.OpportuniteCarriereCommandMapper;
import ministere.sante.senpna.carriere.application.service.OpportuniteCarriereDetailAssembler;
import ministere.sante.senpna.carriere.domain.command.OpportuniteCarriereCommands.CloturerOpportuniteCommand;
import ministere.sante.senpna.carriere.domain.command.OpportuniteCarriereCommands.CreateOpportuniteCarriereCommand;
import ministere.sante.senpna.carriere.domain.command.OpportuniteCarriereCommands.GetOpportuniteCarriereQuery;
import ministere.sante.senpna.carriere.domain.command.OpportuniteCarriereCommands.ListOpportunitesCarriereQuery;
import ministere.sante.senpna.carriere.domain.command.OpportuniteCarriereCommands.MettreEnCoursOpportuniteCommand;
import ministere.sante.senpna.carriere.domain.command.OpportuniteCarriereCommands.OpportuniteCarriereDetail;
import ministere.sante.senpna.carriere.domain.command.OpportuniteCarriereCommands.OpportuniteCarrierePage;
import ministere.sante.senpna.carriere.domain.command.OpportuniteCarriereCommands.PublierOpportuniteCommand;
import ministere.sante.senpna.carriere.domain.command.OpportuniteCarriereCommands.RemettreEnBrouillonOpportuniteCommand;
import ministere.sante.senpna.carriere.domain.command.OpportuniteCarriereCommands.UpdateOpportuniteCarriereCommand;
import ministere.sante.senpna.carriere.domain.exception.DateLimiteCandidatureInvalideException;
import ministere.sante.senpna.carriere.domain.exception.OpportuniteCarriereIntrouvableException;
import ministere.sante.senpna.carriere.domain.exception.TypeContratInvalideException;
import ministere.sante.senpna.carriere.domain.model.OpportuniteCarriere;
import ministere.sante.senpna.carriere.domain.port.out.OpportuniteCarriereRepositoryPort;
import ministere.sante.senpna.carriere.domain.valueobject.OpportuniteCarriereId;
import ministere.sante.senpna.carriere.domain.valueobject.StatutOpportunite;
import ministere.sante.senpna.carriere.domain.valueobject.TypeContrat;
import ministere.sante.senpna.shared.domain.exception.UserNotFoundException;
import ministere.sante.senpna.shared.domain.model.User;
import ministere.sante.senpna.shared.domain.port.out.UserManagementRepositoryPort;
import ministere.sante.senpna.shared.domain.valueobject.Email;
import ministere.sante.senpna.shared.domain.valueobject.HashedPassword;
import ministere.sante.senpna.shared.domain.valueobject.Nom;
import ministere.sante.senpna.shared.domain.valueobject.PageResult;
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

@DisplayName("OpportuniteCarriere Use Cases — create / update / transitions / get / list")
class OpportuniteCarriereUseCasesTest {

        private static final UUID OPPORTUNITE_ID = UUID.randomUUID();
        private static final UUID AUTEUR_ID = UUID.randomUUID();

        private final OpportuniteCarriereCommandMapper commandMapper = new OpportuniteCarriereCommandMapper();
        private final OpportuniteCarriereDetailAssembler assembler = new OpportuniteCarriereDetailAssembler();

        @ExtendWith(MockitoExtension.class)
        @Nested
        @DisplayName("creer")
        class Creer {

                @Mock
                private OpportuniteCarriereRepositoryPort opportuniteCarriereRepositoryPort;

                @Mock
                private UserManagementRepositoryPort userManagementRepositoryPort;

                @Test
                @DisplayName("crée l'opportunité en BROUILLON avec l'auteur résolu")
                void creeQuandAuteurExiste() {
                        CreateOpportuniteCarriereUseCaseImpl useCase = new CreateOpportuniteCarriereUseCaseImpl(
                                        opportuniteCarriereRepositoryPort, userManagementRepositoryPort, commandMapper,
                                        assembler);
                        User auteur = auteurValide();
                        when(userManagementRepositoryPort.findById(UserId.of(AUTEUR_ID)))
                                        .thenReturn(Optional.of(auteur));
                        when(opportuniteCarriereRepositoryPort.save(any(OpportuniteCarriere.class)))
                                        .thenAnswer(inv -> inv.getArgument(0));

                        OpportuniteCarriereDetail result = useCase.creer(new CreateOpportuniteCarriereCommand(
                                        AUTEUR_ID, "Pharmacien(ne) responsable", "PNA", "Description", null,
                                        "Dakar, Sénégal", "CDI", null, LocalDate.now().plusDays(30), null));

                        assertThat(result.titre()).isEqualTo("Pharmacien(ne) responsable");
                        assertThat(result.typeContrat()).isEqualTo("CDI");
                        assertThat(result.statut()).isEqualTo("BROUILLON");
                        assertThat(result.auteurNom()).isEqualTo("Cheikh Ba");
                }

                @Test
                @DisplayName("lève UserNotFoundException quand l'auteur n'existe pas")
                void leveExceptionQuandAuteurInexistant() {
                        CreateOpportuniteCarriereUseCaseImpl useCase = new CreateOpportuniteCarriereUseCaseImpl(
                                        opportuniteCarriereRepositoryPort, userManagementRepositoryPort, commandMapper,
                                        assembler);
                        when(userManagementRepositoryPort.findById(UserId.of(AUTEUR_ID)))
                                        .thenReturn(Optional.empty());

                        var createOpportuniteCarriereCommand = new CreateOpportuniteCarriereCommand(AUTEUR_ID, "Titre", "Entreprise", "Description", null, "Dakar, Sénégal", "CDI", null, LocalDate.now().plusDays(30), null);
                        assertThatThrownBy(() -> useCase.creer(createOpportuniteCarriereCommand))
                                        .isInstanceOf(UserNotFoundException.class);

                        verify(opportuniteCarriereRepositoryPort, never()).save(any());
                }

                @Test
                @DisplayName("lève TypeContratInvalideException pour un type de contrat inconnu")
                void leveExceptionQuandTypeContratInvalide() {
                        CreateOpportuniteCarriereUseCaseImpl useCase = new CreateOpportuniteCarriereUseCaseImpl(
                                        opportuniteCarriereRepositoryPort, userManagementRepositoryPort, commandMapper,
                                        assembler);
                        when(userManagementRepositoryPort.findById(UserId.of(AUTEUR_ID)))
                                        .thenReturn(Optional.of(auteurValide()));

                        var createOpportuniteCarriereCommand = new CreateOpportuniteCarriereCommand(AUTEUR_ID, "Titre", "Entreprise", "Description", null, "Dakar, Sénégal", "INTERIM", null, LocalDate.now().plusDays(30), null);
                        assertThatThrownBy(() -> useCase.creer(createOpportuniteCarriereCommand))
                                        .isInstanceOf(TypeContratInvalideException.class);
                }
        }

        @ExtendWith(MockitoExtension.class)
        @Nested
        @DisplayName("modifier")
        class Modifier {

                @Mock
                private OpportuniteCarriereRepositoryPort opportuniteCarriereRepositoryPort;

                @Test
                @DisplayName("modifie l'opportunité quand elle existe")
                void modifieQuandExiste() {
                        UpdateOpportuniteCarriereUseCaseImpl useCase = new UpdateOpportuniteCarriereUseCaseImpl(
                                        opportuniteCarriereRepositoryPort, commandMapper, assembler);
                        OpportuniteCarriere existante = opportuniteExistante(StatutOpportunite.BROUILLON);
                        when(opportuniteCarriereRepositoryPort.findById(OpportuniteCarriereId.of(OPPORTUNITE_ID)))
                                        .thenReturn(Optional.of(existante));
                        when(opportuniteCarriereRepositoryPort.save(any(OpportuniteCarriere.class)))
                                        .thenAnswer(inv -> inv.getArgument(0));

                        OpportuniteCarriereDetail result = useCase.modifier(new UpdateOpportuniteCarriereCommand(
                                        OPPORTUNITE_ID, "Titre modifié", "Entreprise modifiée", "Description modifiée",
                                        null, "Thiès, Sénégal", "CDD", null, LocalDate.now().plusDays(15), null));

                        assertThat(result.titre()).isEqualTo("Titre modifié");
                        assertThat(result.typeContrat()).isEqualTo("CDD");
                }

                @Test
                @DisplayName("retire la fiche de poste quand ficheDePosteUrl est explicitement null")
                void retireFicheDePosteQuandNull() {
                        UpdateOpportuniteCarriereUseCaseImpl useCase = new UpdateOpportuniteCarriereUseCaseImpl(
                                        opportuniteCarriereRepositoryPort, commandMapper, assembler);
                        OpportuniteCarriere existante = OpportuniteCarriere.reconstruct(
                                        OpportuniteCarriereId.of(OPPORTUNITE_ID), "Titre", "Entreprise", "Description",
                                        "https://cdn.senpna.sn/fiches-de-poste/existante.pdf", "Dakar, Sénégal",
                                        TypeContrat.CDI, null, LocalDate.now().plusDays(30), AUTEUR_ID, "Cheikh Ba",
                                        null, StatutOpportunite.BROUILLON, Instant.now(), Instant.now());
                        when(opportuniteCarriereRepositoryPort.findById(OpportuniteCarriereId.of(OPPORTUNITE_ID)))
                                        .thenReturn(Optional.of(existante));
                        when(opportuniteCarriereRepositoryPort.save(any(OpportuniteCarriere.class)))
                                        .thenAnswer(inv -> inv.getArgument(0));

                        // Le use case ne fait que persister l'URL reçue (jamais d'upload, cf. sa
                        // Javadoc) : ficheDePosteUrl=null retire la fiche de poste, il ne la
                        // conserve pas implicitement — c'est au client de renvoyer l'URL
                        // existante s'il veut la conserver (cf. test suivant).
                        OpportuniteCarriereDetail result = useCase.modifier(new UpdateOpportuniteCarriereCommand(
                                        OPPORTUNITE_ID, "Titre modifié", "Entreprise", "Description", null,
                                        "Dakar, Sénégal", "CDI", null, LocalDate.now().plusDays(30), null));

                        assertThat(result.ficheDePosteUrl()).isNull();
                }

                @Test
                @DisplayName("conserve la fiche de poste quand le client renvoie la même URL")
                void conserveFicheDePosteQuandMemeUrlRenvoyee() {
                        UpdateOpportuniteCarriereUseCaseImpl useCase = new UpdateOpportuniteCarriereUseCaseImpl(
                                        opportuniteCarriereRepositoryPort, commandMapper, assembler);
                        String ficheDePosteUrl = "https://cdn.senpna.sn/fiches-de-poste/existante.pdf";
                        OpportuniteCarriere existante = OpportuniteCarriere.reconstruct(
                                        OpportuniteCarriereId.of(OPPORTUNITE_ID), "Titre", "Entreprise", "Description",
                                        ficheDePosteUrl, "Dakar, Sénégal", TypeContrat.CDI, null,
                                        LocalDate.now().plusDays(30), AUTEUR_ID, "Cheikh Ba", null,
                                        StatutOpportunite.BROUILLON, Instant.now(), Instant.now());
                        when(opportuniteCarriereRepositoryPort.findById(OpportuniteCarriereId.of(OPPORTUNITE_ID)))
                                        .thenReturn(Optional.of(existante));
                        when(opportuniteCarriereRepositoryPort.save(any(OpportuniteCarriere.class)))
                                        .thenAnswer(inv -> inv.getArgument(0));

                        OpportuniteCarriereDetail result = useCase.modifier(new UpdateOpportuniteCarriereCommand(
                                        OPPORTUNITE_ID, "Titre modifié", "Entreprise", "Description", ficheDePosteUrl,
                                        "Dakar, Sénégal", "CDI", null, LocalDate.now().plusDays(30), null));

                        assertThat(result.ficheDePosteUrl()).isEqualTo(ficheDePosteUrl);
                }

                @Test
                @DisplayName("lève OpportuniteCarriereIntrouvableException quand l'opportunité n'existe pas")
                void leveIntrouvableQuandInexistante() {
                        UpdateOpportuniteCarriereUseCaseImpl useCase = new UpdateOpportuniteCarriereUseCaseImpl(
                                        opportuniteCarriereRepositoryPort, commandMapper, assembler);
                        when(opportuniteCarriereRepositoryPort.findById(OpportuniteCarriereId.of(OPPORTUNITE_ID)))
                                        .thenReturn(Optional.empty());

                        var updateOpportuniteCarriereCommand = new UpdateOpportuniteCarriereCommand( OPPORTUNITE_ID, "Titre", "Entreprise", "Description", null, "Dakar, Sénégal", "CDI", null, LocalDate.now().plusDays(30), null);
                        assertThatThrownBy(() -> useCase.modifier(updateOpportuniteCarriereCommand))
                                        .isInstanceOf(OpportuniteCarriereIntrouvableException.class);
                }
        }

        @ExtendWith(MockitoExtension.class)
        @Nested
        @DisplayName("publier / mettreEnCours / cloturer / remettreEnBrouillon")
        class Transitions {

                @Mock
                private OpportuniteCarriereRepositoryPort opportuniteCarriereRepositoryPort;

                @Test
                @DisplayName("publie une opportunité en brouillon")
                void publieOpportuniteBrouillon() {
                        PublierOpportuniteUseCaseImpl useCase = new PublierOpportuniteUseCaseImpl(
                                        opportuniteCarriereRepositoryPort, assembler);
                        OpportuniteCarriere existante = opportuniteExistante(StatutOpportunite.BROUILLON);
                        when(opportuniteCarriereRepositoryPort.findById(OpportuniteCarriereId.of(OPPORTUNITE_ID)))
                                        .thenReturn(Optional.of(existante));
                        when(opportuniteCarriereRepositoryPort.save(any(OpportuniteCarriere.class)))
                                        .thenAnswer(inv -> inv.getArgument(0));

                        OpportuniteCarriereDetail result = useCase.publier(
                                        new PublierOpportuniteCommand(OPPORTUNITE_ID));

                        assertThat(result.statut()).isEqualTo("OUVERT");
                }

                @Test
                @DisplayName("publier() lève DateLimiteCandidatureInvalideException quand la date limite est dépassée")
                void publierEchoueQuandExpiree() {
                        PublierOpportuniteUseCaseImpl useCase = new PublierOpportuniteUseCaseImpl(
                                        opportuniteCarriereRepositoryPort, assembler);
                        OpportuniteCarriere existante = OpportuniteCarriere.reconstruct(
                                        OpportuniteCarriereId.of(OPPORTUNITE_ID), "Titre", "Entreprise", "Description",
                                        null, "Dakar, Sénégal", TypeContrat.CDI, null, LocalDate.now().minusDays(1),
                                        AUTEUR_ID, "Cheikh Ba", null, StatutOpportunite.BROUILLON, Instant.now(),
                                        Instant.now());
                        when(opportuniteCarriereRepositoryPort.findById(OpportuniteCarriereId.of(OPPORTUNITE_ID)))
                                        .thenReturn(Optional.of(existante));

                        var publierOpportuniteCommand = new PublierOpportuniteCommand(OPPORTUNITE_ID);
                        assertThatThrownBy(() -> useCase.publier(publierOpportuniteCommand))
                                        .isInstanceOf(DateLimiteCandidatureInvalideException.class);
                }

                @Test
                @DisplayName("publier() lève OpportuniteCarriereIntrouvableException quand l'opportunité n'existe pas")
                void publierLeveIntrouvableQuandInexistante() {
                        PublierOpportuniteUseCaseImpl useCase = new PublierOpportuniteUseCaseImpl(
                                        opportuniteCarriereRepositoryPort, assembler);
                        when(opportuniteCarriereRepositoryPort.findById(OpportuniteCarriereId.of(OPPORTUNITE_ID)))
                                        .thenReturn(Optional.empty());

                        var publierOpportuniteCommand = new PublierOpportuniteCommand(OPPORTUNITE_ID);
                        assertThatThrownBy(() -> useCase.publier(publierOpportuniteCommand))
                                        .isInstanceOf(OpportuniteCarriereIntrouvableException.class);
                }

                @Test
                @DisplayName("passe une opportunité ouverte en traitement")
                void mettreEnCoursOpportuniteOuverte() {
                        MettreEnCoursOpportuniteUseCaseImpl useCase = new MettreEnCoursOpportuniteUseCaseImpl(
                                        opportuniteCarriereRepositoryPort, assembler);
                        OpportuniteCarriere existante = opportuniteExistante(StatutOpportunite.OUVERT);
                        when(opportuniteCarriereRepositoryPort.findById(OpportuniteCarriereId.of(OPPORTUNITE_ID)))
                                        .thenReturn(Optional.of(existante));
                        when(opportuniteCarriereRepositoryPort.save(any(OpportuniteCarriere.class)))
                                        .thenAnswer(inv -> inv.getArgument(0));

                        OpportuniteCarriereDetail result = useCase.mettreEnCours(
                                        new MettreEnCoursOpportuniteCommand(OPPORTUNITE_ID));

                        assertThat(result.statut()).isEqualTo("EN_COURS");
                }

                @Test
                @DisplayName("clôture manuellement une opportunité")
                void clotureOpportunite() {
                        CloturerOpportuniteUseCaseImpl useCase = new CloturerOpportuniteUseCaseImpl(
                                        opportuniteCarriereRepositoryPort, assembler);
                        OpportuniteCarriere existante = opportuniteExistante(StatutOpportunite.EN_COURS);
                        when(opportuniteCarriereRepositoryPort.findById(OpportuniteCarriereId.of(OPPORTUNITE_ID)))
                                        .thenReturn(Optional.of(existante));
                        when(opportuniteCarriereRepositoryPort.save(any(OpportuniteCarriere.class)))
                                        .thenAnswer(inv -> inv.getArgument(0));

                        OpportuniteCarriereDetail result = useCase.cloturer(
                                        new CloturerOpportuniteCommand(OPPORTUNITE_ID));

                        assertThat(result.statut()).isEqualTo("CLOTURE");
                }

                @Test
                @DisplayName("remet une opportunité clôturée en brouillon")
                void remetEnBrouillon() {
                        RemettreEnBrouillonOpportuniteUseCaseImpl useCase = new RemettreEnBrouillonOpportuniteUseCaseImpl(
                                        opportuniteCarriereRepositoryPort, assembler);
                        OpportuniteCarriere existante = opportuniteExistante(StatutOpportunite.CLOTURE);
                        when(opportuniteCarriereRepositoryPort.findById(OpportuniteCarriereId.of(OPPORTUNITE_ID)))
                                        .thenReturn(Optional.of(existante));
                        when(opportuniteCarriereRepositoryPort.save(any(OpportuniteCarriere.class)))
                                        .thenAnswer(inv -> inv.getArgument(0));

                        OpportuniteCarriereDetail result = useCase.remettreEnBrouillon(
                                        new RemettreEnBrouillonOpportuniteCommand(OPPORTUNITE_ID));

                        assertThat(result.statut()).isEqualTo("BROUILLON");
                }
        }

        @ExtendWith(MockitoExtension.class)
        @Nested
        @DisplayName("obtenir / obtenirPublique")
        class Obtenir {

                @Mock
                private OpportuniteCarriereRepositoryPort opportuniteCarriereRepositoryPort;

                @Test
                @DisplayName("obtenir() retourne l'opportunité quelle que soit son visibilité")
                void obtenirRetourneQuandExiste() {
                        GetOpportuniteCarriereUseCaseImpl useCase = new GetOpportuniteCarriereUseCaseImpl(
                                        opportuniteCarriereRepositoryPort, assembler);
                        OpportuniteCarriere existante = opportuniteExistante(StatutOpportunite.BROUILLON);
                        when(opportuniteCarriereRepositoryPort.findById(OpportuniteCarriereId.of(OPPORTUNITE_ID)))
                                        .thenReturn(Optional.of(existante));

                        OpportuniteCarriereDetail result = useCase.obtenir(
                                        new GetOpportuniteCarriereQuery(OPPORTUNITE_ID));

                        assertThat(result.id()).isEqualTo(OPPORTUNITE_ID);
                        assertThat(result.statut()).isEqualTo("BROUILLON");
                }

                @Test
                @DisplayName("obtenir() lève OpportuniteCarriereIntrouvableException quand elle n'existe pas")
                void obtenirLeveIntrouvableQuandInexistante() {
                        GetOpportuniteCarriereUseCaseImpl useCase = new GetOpportuniteCarriereUseCaseImpl(
                                        opportuniteCarriereRepositoryPort, assembler);
                        when(opportuniteCarriereRepositoryPort.findById(OpportuniteCarriereId.of(OPPORTUNITE_ID)))
                                        .thenReturn(Optional.empty());

                        var getOpportuniteCarriereQuery = new GetOpportuniteCarriereQuery(OPPORTUNITE_ID);
                        assertThatThrownBy(() -> useCase.obtenir(getOpportuniteCarriereQuery))
                                        .isInstanceOf(OpportuniteCarriereIntrouvableException.class);
                }

                @Test
                @DisplayName("obtenirPublique() retourne l'opportunité quand elle est OUVERT")
                void obtenirPubliqueRetourneQuandOuverte() {
                        GetOpportuniteCarrierePubliqueUseCaseImpl useCase = new GetOpportuniteCarrierePubliqueUseCaseImpl(
                                        opportuniteCarriereRepositoryPort, assembler);
                        OpportuniteCarriere existante = opportuniteExistante(StatutOpportunite.OUVERT);
                        when(opportuniteCarriereRepositoryPort.findById(OpportuniteCarriereId.of(OPPORTUNITE_ID)))
                                        .thenReturn(Optional.of(existante));

                        OpportuniteCarriereDetail result = useCase.obtenirPublique(
                                        new GetOpportuniteCarriereQuery(OPPORTUNITE_ID));

                        assertThat(result.statut()).isEqualTo("OUVERT");
                }

                @Test
                @DisplayName("obtenirPublique() lève OpportuniteCarriereIntrouvableException quand elle est BROUILLON")
                void obtenirPubliqueLeveIntrouvableQuandBrouillon() {
                        GetOpportuniteCarrierePubliqueUseCaseImpl useCase = new GetOpportuniteCarrierePubliqueUseCaseImpl(
                                        opportuniteCarriereRepositoryPort, assembler);
                        OpportuniteCarriere existante = opportuniteExistante(StatutOpportunite.BROUILLON);
                        when(opportuniteCarriereRepositoryPort.findById(OpportuniteCarriereId.of(OPPORTUNITE_ID)))
                                        .thenReturn(Optional.of(existante));

                        var getOpportuniteCarriereQuery = new GetOpportuniteCarriereQuery(OPPORTUNITE_ID);
                        assertThatThrownBy(() -> useCase.obtenirPublique(getOpportuniteCarriereQuery))
                                        .isInstanceOf(OpportuniteCarriereIntrouvableException.class);
                }

                @Test
                @DisplayName("obtenirPublique() lève OpportuniteCarriereIntrouvableException quand OUVERT persistée mais expirée")
                void obtenirPubliqueLeveIntrouvableQuandExpiree() {
                        GetOpportuniteCarrierePubliqueUseCaseImpl useCase = new GetOpportuniteCarrierePubliqueUseCaseImpl(
                                        opportuniteCarriereRepositoryPort, assembler);
                        OpportuniteCarriere existante = OpportuniteCarriere.reconstruct(
                                        OpportuniteCarriereId.of(OPPORTUNITE_ID), "Titre", "Entreprise", "Description",
                                        null, "Dakar, Sénégal", TypeContrat.CDI, null, LocalDate.now().minusDays(1),
                                        AUTEUR_ID, "Cheikh Ba", null, StatutOpportunite.OUVERT, Instant.now(),
                                        Instant.now());
                        when(opportuniteCarriereRepositoryPort.findById(OpportuniteCarriereId.of(OPPORTUNITE_ID)))
                                        .thenReturn(Optional.of(existante));

                        var getOpportuniteCarriereQuery = new GetOpportuniteCarriereQuery(OPPORTUNITE_ID);
                        assertThatThrownBy(() -> useCase.obtenirPublique(getOpportuniteCarriereQuery))
                                        .isInstanceOf(OpportuniteCarriereIntrouvableException.class);
                }
        }

        @ExtendWith(MockitoExtension.class)
        @Nested
        @DisplayName("lister")
        class Lister {

                @Mock
                private OpportuniteCarriereRepositoryPort opportuniteCarriereRepositoryPort;

                @Test
                @DisplayName("délègue la recherche paginée au repository")
                void delegueRecherchePaginee() {
                        ListOpportunitesCarriereUseCaseImpl useCase = new ListOpportunitesCarriereUseCaseImpl(
                                        opportuniteCarriereRepositoryPort, commandMapper, assembler);
                        OpportuniteCarriere existante = opportuniteExistante(StatutOpportunite.OUVERT);
                        when(opportuniteCarriereRepositoryPort.search(any(), any()))
                                        .thenReturn(PageResult.of(List.of(existante), 0, 20, 1));

                        OpportuniteCarrierePage result = useCase.lister(new ListOpportunitesCarriereQuery(
                                        "pharmacien", "CDI", "OUVERT", false, 0, 20, "createdAt", "DESC"));

                        assertThat(result.content()).hasSize(1);
                        assertThat(result.totalElements()).isEqualTo(1);
                }

                @Test
                @DisplayName("lève TypeContratInvalideException pour un filtre de type de contrat invalide")
                void leveExceptionQuandFiltreTypeContratInvalide() {
                        ListOpportunitesCarriereUseCaseImpl useCase = new ListOpportunitesCarriereUseCaseImpl(
                                        opportuniteCarriereRepositoryPort, commandMapper, assembler);

                        var listOpportunitesCarriereQuery = new ListOpportunitesCarriereQuery(null, "INTERIM", null, false, 0, 20, "createdAt", "DESC");
                        assertThatThrownBy(() -> useCase.lister(listOpportunitesCarriereQuery))
                                        .isInstanceOf(TypeContratInvalideException.class);
                }
        }

        // ── Fixtures ─────────────────────────────────────────────────────────

        private User auteurValide() {
                return User.creer(Nom.of("Ba"), Prenom.of("Cheikh"), Email.of("cheikh.ba@sante.sn"), null,
                                HashedPassword.of("$2a$12$abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzab"),
                                Set.of());
        }

        private OpportuniteCarriere opportuniteExistante(StatutOpportunite statut) {
                Instant maintenant = Instant.now();
                return OpportuniteCarriere.reconstruct(OpportuniteCarriereId.of(OPPORTUNITE_ID), "Titre",
                                "Entreprise", "Description", null, "Dakar, Sénégal", TypeContrat.CDI, null,
                                LocalDate.now().plusDays(30), AUTEUR_ID, "Cheikh Ba", null, statut, maintenant,
                                maintenant);
        }
}