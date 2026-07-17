package ministere.sante.senpna.actualite.application.usecase;

import ministere.sante.senpna.actualite.application.service.ActualiteCommandMapper;
import ministere.sante.senpna.actualite.application.service.ActualiteDetailAssembler;
import ministere.sante.senpna.actualite.domain.command.ActualiteCommands.ActualiteDetail;
import ministere.sante.senpna.actualite.domain.command.ActualiteCommands.ActualitePage;
import ministere.sante.senpna.actualite.domain.command.ActualiteCommands.CreateActualiteCommand;
import ministere.sante.senpna.actualite.domain.command.ActualiteCommands.DesactiverActualiteCommand;
import ministere.sante.senpna.actualite.domain.command.ActualiteCommands.GetActualiteQuery;
import ministere.sante.senpna.actualite.domain.command.ActualiteCommands.ListActualitesQuery;
import ministere.sante.senpna.actualite.domain.command.ActualiteCommands.PublierActualiteCommand;
import ministere.sante.senpna.actualite.domain.command.ActualiteCommands.RemettreEnBrouillonActualiteCommand;
import ministere.sante.senpna.actualite.domain.command.ActualiteCommands.UpdateActualiteCommand;
import ministere.sante.senpna.actualite.domain.exception.ActualiteIntrouvableException;
import ministere.sante.senpna.actualite.domain.exception.CategorieActualiteInvalideException;
import ministere.sante.senpna.actualite.domain.model.Actualite;
import ministere.sante.senpna.actualite.domain.port.out.ActualiteRepositoryPort;
import ministere.sante.senpna.actualite.domain.valueobject.ActualiteId;
import ministere.sante.senpna.actualite.domain.valueobject.CategorieActualite;
import ministere.sante.senpna.actualite.domain.valueobject.StatutActualite;
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
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("Actualite Use Cases — create / update / publier / desactiver / get / list")
class ActualiteUseCasesTest {

        private static final UUID ACTUALITE_ID = UUID.randomUUID();
        private static final UUID AUTEUR_ID = UUID.randomUUID();

        private final ActualiteCommandMapper commandMapper = new ActualiteCommandMapper();
        private final ActualiteDetailAssembler assembler = new ActualiteDetailAssembler();

        @ExtendWith(MockitoExtension.class)
        @Nested
        @DisplayName("creer")
        class Creer {

                @Mock
                private ActualiteRepositoryPort actualiteRepositoryPort;

                @Mock
                private UserManagementRepositoryPort userManagementRepositoryPort;

                @Test
                @DisplayName("crée l'actualité en BROUILLON avec l'auteur résolu")
                void creeQuandAuteurExiste() {
                        CreateActualiteUseCaseImpl useCase = new CreateActualiteUseCaseImpl(actualiteRepositoryPort,
                                        userManagementRepositoryPort, commandMapper, assembler);

                        User auteur = User.creer(new User.CreationCommand(Nom.of("Diop"), Prenom.of("Awa"), Email.of("awa.diop@sante.sn"), null,
                                        HashedPassword.of(
                                                        "$2a$12$abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzab"),
                                        java.util.Set.of()));
                        when(userManagementRepositoryPort.findById(UserId.of(AUTEUR_ID)))
                                        .thenReturn(Optional.of(auteur));
                        when(actualiteRepositoryPort.save(any(Actualite.class))).thenAnswer(inv -> inv.getArgument(0));

                        ActualiteDetail result = useCase.creer(new CreateActualiteCommand(AUTEUR_ID, "PROJET",
                                        "Nouveau centre de santé", "Ouverture prochaine", List.of(), List.of("sante")));

                        assertThat(result.titre()).isEqualTo("Nouveau centre de santé");
                        assertThat(result.categorie()).isEqualTo("PROJET");
                        assertThat(result.statut()).isEqualTo("BROUILLON");
                        assertThat(result.auteurNom()).isEqualTo("Awa Diop");
                }

                @Test
                @DisplayName("lève UserNotFoundException quand l'auteur n'existe pas")
                void leveExceptionQuandAuteurInexistant() {
                        CreateActualiteUseCaseImpl useCase = new CreateActualiteUseCaseImpl(actualiteRepositoryPort,
                                        userManagementRepositoryPort, commandMapper, assembler);
                        when(userManagementRepositoryPort.findById(UserId.of(AUTEUR_ID))).thenReturn(Optional.empty());

                        var createActualiteCommand = new CreateActualiteCommand(AUTEUR_ID, "PROJET", "Titre", null,
                                        List.of(), List.of());
                        assertThatThrownBy(() -> useCase.creer(createActualiteCommand))
                                        .isInstanceOf(UserNotFoundException.class);

                        verify(actualiteRepositoryPort, never()).save(any());
                }

                @Test
                @DisplayName("lève CategorieActualiteInvalideException pour une catégorie inconnue")
                void leveExceptionQuandCategorieInvalide() {
                        CreateActualiteUseCaseImpl useCase = new CreateActualiteUseCaseImpl(actualiteRepositoryPort,
                                        userManagementRepositoryPort, commandMapper, assembler);
                        User auteur = User.creer(new User.CreationCommand(Nom.of("Diop"), Prenom.of("Awa"), Email.of("awa.diop@sante.sn"), null,
                                        HashedPassword.of(
                                                        "$2a$12$abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzab"),
                                        java.util.Set.of()));
                        when(userManagementRepositoryPort.findById(UserId.of(AUTEUR_ID)))
                                        .thenReturn(Optional.of(auteur));

                        var createActualiteCommand = new CreateActualiteCommand(AUTEUR_ID, "INEXISTANTE", "Titre", null,
                                        List.of(), List.of());
                        assertThatThrownBy(() -> useCase.creer(createActualiteCommand))
                                        .isInstanceOf(CategorieActualiteInvalideException.class);
                }
        }

        @ExtendWith(MockitoExtension.class)
        @Nested
        @DisplayName("modifier")
        class Modifier {

                @Mock
                private ActualiteRepositoryPort actualiteRepositoryPort;

                @Test
                @DisplayName("modifie l'actualité quand elle existe")
                void modifieQuandExiste() {
                        UpdateActualiteUseCaseImpl useCase = new UpdateActualiteUseCaseImpl(actualiteRepositoryPort,
                                        commandMapper, assembler);
                        Actualite existante = actualiteExistante();
                        when(actualiteRepositoryPort.findById(ActualiteId.of(ACTUALITE_ID)))
                                        .thenReturn(Optional.of(existante));
                        when(actualiteRepositoryPort.save(any(Actualite.class))).thenAnswer(inv -> inv.getArgument(0));

                        ActualiteDetail result = useCase.modifier(new UpdateActualiteCommand(ACTUALITE_ID,
                                        "PARTENARIAT",
                                        "Titre modifié", "Description modifiée", List.of(), List.of("nouveau")));

                        assertThat(result.titre()).isEqualTo("Titre modifié");
                        assertThat(result.categorie()).isEqualTo("PARTENARIAT");
                }

                @Test
                @DisplayName("lève ActualiteIntrouvableException quand l'actualité n'existe pas")
                void leveIntrouvableQuandInexistante() {
                        UpdateActualiteUseCaseImpl useCase = new UpdateActualiteUseCaseImpl(actualiteRepositoryPort,
                                        commandMapper, assembler);
                        when(actualiteRepositoryPort.findById(ActualiteId.of(ACTUALITE_ID)))
                                        .thenReturn(Optional.empty());

                        var updateActualiteCommand = new UpdateActualiteCommand(ACTUALITE_ID, "PROJET", "Titre", null,
                                        List.of(), List.of());
                        assertThatThrownBy(() -> useCase.modifier(updateActualiteCommand))
                                        .isInstanceOf(ActualiteIntrouvableException.class);
                }
        }

        @ExtendWith(MockitoExtension.class)
        @Nested
        @DisplayName("publier / desactiver")
        class PublierDesactiver {

                @Mock
                private ActualiteRepositoryPort actualiteRepositoryPort;

                @Test
                @DisplayName("publie une actualité en brouillon")
                void publieActualiteBrouillon() {
                        PublierActualiteUseCaseImpl useCase = new PublierActualiteUseCaseImpl(actualiteRepositoryPort,
                                        assembler);
                        Actualite existante = actualiteExistante();
                        when(actualiteRepositoryPort.findById(ActualiteId.of(ACTUALITE_ID)))
                                        .thenReturn(Optional.of(existante));
                        when(actualiteRepositoryPort.save(any(Actualite.class))).thenAnswer(inv -> inv.getArgument(0));

                        ActualiteDetail result = useCase.publier(new PublierActualiteCommand(ACTUALITE_ID));

                        assertThat(result.statut()).isEqualTo("PUBLIE");
                }

                @Test
                @DisplayName("désactive une actualité publiée")
                void desactiveActualitePubliee() {
                        DesactiverActualiteUseCaseImpl useCase = new DesactiverActualiteUseCaseImpl(
                                        actualiteRepositoryPort,
                                        assembler);
                        Actualite existante = actualiteExistante();
                        existante.publier();
                        when(actualiteRepositoryPort.findById(ActualiteId.of(ACTUALITE_ID)))
                                        .thenReturn(Optional.of(existante));
                        when(actualiteRepositoryPort.save(any(Actualite.class))).thenAnswer(inv -> inv.getArgument(0));

                        ActualiteDetail result = useCase.desactiver(new DesactiverActualiteCommand(ACTUALITE_ID));

                        assertThat(result.statut()).isEqualTo("DESACTIVE");
                }

                @Test
                @DisplayName("lève ActualiteIntrouvableException quand l'actualité n'existe pas")
                void leveIntrouvableQuandInexistante() {
                        PublierActualiteUseCaseImpl useCase = new PublierActualiteUseCaseImpl(actualiteRepositoryPort,
                                        assembler);
                        when(actualiteRepositoryPort.findById(ActualiteId.of(ACTUALITE_ID)))
                                        .thenReturn(Optional.empty());

                        var publierActualiteCommand = new PublierActualiteCommand(ACTUALITE_ID);
                        assertThatThrownBy(() -> useCase.publier(publierActualiteCommand))
                                        .isInstanceOf(ActualiteIntrouvableException.class);
                }
        }

        @ExtendWith(MockitoExtension.class)
        @Nested
        @DisplayName("obtenir")
        class Obtenir {

                @Mock
                private ActualiteRepositoryPort actualiteRepositoryPort;

                @Test
                @DisplayName("retourne l'actualité quand elle existe")
                void retourneQuandExiste() {
                        GetActualiteUseCaseImpl useCase = new GetActualiteUseCaseImpl(actualiteRepositoryPort,
                                        assembler);
                        Actualite existante = actualiteExistante();
                        when(actualiteRepositoryPort.findById(ActualiteId.of(ACTUALITE_ID)))
                                        .thenReturn(Optional.of(existante));

                        ActualiteDetail result = useCase.obtenir(new GetActualiteQuery(ACTUALITE_ID));

                        assertThat(result.id()).isEqualTo(ACTUALITE_ID);
                }

                @Test
                @DisplayName("lève ActualiteIntrouvableException quand elle n'existe pas")
                void leveIntrouvableQuandInexistante() {
                        GetActualiteUseCaseImpl useCase = new GetActualiteUseCaseImpl(actualiteRepositoryPort,
                                        assembler);
                        when(actualiteRepositoryPort.findById(ActualiteId.of(ACTUALITE_ID)))
                                        .thenReturn(Optional.empty());

                        var getActualiteQuery = new GetActualiteQuery(ACTUALITE_ID);
                        assertThatThrownBy(() -> useCase.obtenir(getActualiteQuery))
                                        .isInstanceOf(ActualiteIntrouvableException.class);
                }
        }

        @ExtendWith(MockitoExtension.class)
        @Nested
        @DisplayName("lister")
        class Lister {

                @Mock
                private ActualiteRepositoryPort actualiteRepositoryPort;

                @Test
                @DisplayName("délègue la recherche paginée au repository")
                void delegueRecherchePaginee() {
                        ListActualitesUseCaseImpl useCase = new ListActualitesUseCaseImpl(actualiteRepositoryPort,
                                        commandMapper,
                                        assembler);
                        Actualite existante = actualiteExistante();
                        when(actualiteRepositoryPort.search(any(), any()))
                                        .thenReturn(PageResult.of(List.of(existante), 0, 20, 1));

                        ActualitePage result = useCase
                                        .lister(new ListActualitesQuery("centre", "PROJET", "BROUILLON", 0, 20,
                                                        "createdAt", "DESC"));

                        assertThat(result.content()).hasSize(1);
                        assertThat(result.totalElements()).isEqualTo(1);
                }
        }

        @ExtendWith(MockitoExtension.class)
        @Nested
        @DisplayName("obtenirPublique")
        class ObtenirPublique {

                @Mock
                private ActualiteRepositoryPort actualiteRepositoryPort;

                @Test
                @DisplayName("retourne l'actualité publiée quand elle existe")
                void retourneQuandExistePubliee() {
                        GetActualitePubliqueUseCaseImpl useCase = new GetActualitePubliqueUseCaseImpl(
                                        actualiteRepositoryPort, assembler);
                        Actualite existante = actualiteExistante();
                        existante.publier();
                        when(actualiteRepositoryPort.findById(ActualiteId.of(ACTUALITE_ID)))
                                        .thenReturn(Optional.of(existante));

                        ActualiteDetail result = useCase.obtenirPublique(new GetActualiteQuery(ACTUALITE_ID));

                        assertThat(result.id()).isEqualTo(ACTUALITE_ID);
                        assertThat(result.statut()).isEqualTo("PUBLIE");
                }

                @Test
                @DisplayName("lève ActualiteIntrouvableException quand elle existe mais n'est pas publiée")
                void leveIntrouvableQuandNonPubliee() {
                        GetActualitePubliqueUseCaseImpl useCase = new GetActualitePubliqueUseCaseImpl(
                                        actualiteRepositoryPort, assembler);
                        Actualite existante = actualiteExistante();
                        when(actualiteRepositoryPort.findById(ActualiteId.of(ACTUALITE_ID)))
                                        .thenReturn(Optional.of(existante));

                        var getActualiteQuery = new GetActualiteQuery(ACTUALITE_ID);
                        assertThatThrownBy(() -> useCase.obtenirPublique(getActualiteQuery))
                                        .isInstanceOf(ActualiteIntrouvableException.class);
                }

                @Test
                @DisplayName("lève ActualiteIntrouvableException quand elle n'existe pas")
                void leveIntrouvableQuandInexistante() {
                        GetActualitePubliqueUseCaseImpl useCase = new GetActualitePubliqueUseCaseImpl(
                                        actualiteRepositoryPort, assembler);
                        when(actualiteRepositoryPort.findById(ActualiteId.of(ACTUALITE_ID)))
                                        .thenReturn(Optional.empty());

                        var getActualiteQuery = new GetActualiteQuery(ACTUALITE_ID);
                        assertThatThrownBy(() -> useCase.obtenirPublique(getActualiteQuery))
                                        .isInstanceOf(ActualiteIntrouvableException.class);
                }
        }

        @ExtendWith(MockitoExtension.class)
        @Nested
        @DisplayName("remettreEnBrouillon")
        class RemettreEnBrouillon {

                @Mock
                private ActualiteRepositoryPort actualiteRepositoryPort;

                @Test
                @DisplayName("remet en brouillon une actualité publiée")
                void remetEnBrouillonActualitePubliee() {
                        RemettreEnBrouillonActualiteUseCaseImpl useCase = new RemettreEnBrouillonActualiteUseCaseImpl(
                                        actualiteRepositoryPort, assembler);
                        Actualite existante = actualiteExistante();
                        existante.publier();
                        when(actualiteRepositoryPort.findById(ActualiteId.of(ACTUALITE_ID)))
                                        .thenReturn(Optional.of(existante));
                        when(actualiteRepositoryPort.save(any(Actualite.class))).thenAnswer(inv -> inv.getArgument(0));

                        ActualiteDetail result = useCase
                                        .remettreEnBrouillon(new RemettreEnBrouillonActualiteCommand(ACTUALITE_ID));

                        assertThat(result.statut()).isEqualTo("BROUILLON");
                }

                @Test
                @DisplayName("lève ActualiteIntrouvableException quand l'actualité n'existe pas")
                void leveIntrouvableQuandInexistante() {
                        RemettreEnBrouillonActualiteUseCaseImpl useCase = new RemettreEnBrouillonActualiteUseCaseImpl(
                                        actualiteRepositoryPort, assembler);
                        when(actualiteRepositoryPort.findById(ActualiteId.of(ACTUALITE_ID)))
                                        .thenReturn(Optional.empty());

                        var command = new RemettreEnBrouillonActualiteCommand(ACTUALITE_ID);
                        assertThatThrownBy(() -> useCase.remettreEnBrouillon(command))
                                        .isInstanceOf(ActualiteIntrouvableException.class);
                }
        }

        private Actualite actualiteExistante() {
                Instant maintenant = Instant.now();
                return Actualite.builder()
                    .id(ActualiteId.of(ACTUALITE_ID))
                    .categorie(CategorieActualite.PROJET)
                    .titre("Titre")
                    .description("Description")
                    .medias(List.of())
                    .auteurId(AUTEUR_ID)
                    .auteurNom("Awa Diop")
                    .tags(List.of())
                    .statut(StatutActualite.BROUILLON)
                    .createdAt(maintenant)
                    .updatedAt(maintenant)
                    .build();
        }
}
