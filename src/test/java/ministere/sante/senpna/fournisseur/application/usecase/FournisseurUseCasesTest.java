package ministere.sante.senpna.fournisseur.application.usecase;

import ministere.sante.senpna.fournisseur.application.service.FournisseurDetailAssembler;
import ministere.sante.senpna.fournisseur.domain.command.FournisseurCommands.ActivateFournisseurCommand;
import ministere.sante.senpna.fournisseur.domain.command.FournisseurCommands.CreateFournisseurCommand;
import ministere.sante.senpna.fournisseur.domain.command.FournisseurCommands.DeactivateFournisseurCommand;
import ministere.sante.senpna.fournisseur.domain.command.FournisseurCommands.FournisseurDetail;
import ministere.sante.senpna.fournisseur.domain.command.FournisseurCommands.FournisseurPage;
import ministere.sante.senpna.fournisseur.domain.command.FournisseurCommands.GetFournisseurQuery;
import ministere.sante.senpna.fournisseur.domain.command.FournisseurCommands.ListFournisseursQuery;
import ministere.sante.senpna.fournisseur.domain.command.FournisseurCommands.UpdateFournisseurCommand;
import ministere.sante.senpna.fournisseur.domain.exception.FournisseurIntrouvableException;
import ministere.sante.senpna.fournisseur.domain.exception.NomFournisseurDejaUtiliseException;
import ministere.sante.senpna.fournisseur.domain.model.Fournisseur;
import ministere.sante.senpna.fournisseur.domain.port.out.FournisseurRepositoryPort;
import ministere.sante.senpna.fournisseur.domain.valueobject.FournisseurId;
import ministere.sante.senpna.shared.domain.port.out.FournisseurCachePort;
import ministere.sante.senpna.shared.domain.valueobject.PageResult;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("Fournisseur Use Cases — create / update / activate / deactivate / get / list")
class FournisseurUseCasesTest {

        private static final UUID FOURNISSEUR_ID = UUID.randomUUID();

        @ExtendWith(MockitoExtension.class)
        @Nested
        @DisplayName("creer")
        class Creer {

                @Mock
                private FournisseurRepositoryPort fournisseurRepositoryPort;

                @Mock
                private FournisseurCachePort fournisseurCachePort;

                private final FournisseurDetailAssembler assembler = new FournisseurDetailAssembler();

                @Test
                @DisplayName("crée le fournisseur quand le nom est disponible")
                void creeQuandNomDisponible() {
                        CreateFournisseurUseCaseImpl useCase = new CreateFournisseurUseCaseImpl(
                                        fournisseurRepositoryPort, fournisseurCachePort,
                                        assembler);
                        when(fournisseurRepositoryPort.existsByNomIgnoreCase("Laboratoire A")).thenReturn(false);
                        when(fournisseurRepositoryPort.save(any(Fournisseur.class)))
                                        .thenAnswer(inv -> inv.getArgument(0));

                        FournisseurDetail result = useCase.creer(
                                        new CreateFournisseurCommand("Laboratoire A", "Dakar", "+221771234567",
                                                        "contact@labo-a.sn",
                                                        "M. Diop"));

                        assertThat(result.nom()).isEqualTo("Laboratoire A");
                        assertThat(result.actif()).isTrue();
                }

                @Test
                @DisplayName("rejette la création quand le nom est déjà utilisé (insensible à la casse)")
                void rejetteNomDejaUtilise() {
                        CreateFournisseurUseCaseImpl useCase = new CreateFournisseurUseCaseImpl(
                                        fournisseurRepositoryPort, fournisseurCachePort,
                                        assembler);
                        when(fournisseurRepositoryPort.existsByNomIgnoreCase("laboratoire a")).thenReturn(true);

                        assertThatThrownBy(() -> useCase.creer(
                                        new CreateFournisseurCommand("laboratoire a", null, null, null, null)))
                                        .isInstanceOf(NomFournisseurDejaUtiliseException.class);

                        verify(fournisseurRepositoryPort, never()).save(any());
                }
        }

        @ExtendWith(MockitoExtension.class)
        @Nested
        @DisplayName("modifier")
        class Modifier {

                @Mock
                private FournisseurRepositoryPort fournisseurRepositoryPort;

                @Mock
                private FournisseurCachePort fournisseurCachePort;

                private final FournisseurDetailAssembler assembler = new FournisseurDetailAssembler();

                @Test
                @DisplayName("modifie le fournisseur quand il existe et que le nom reste disponible")
                void modifieQuandValide() {
                        UpdateFournisseurUseCaseImpl useCase = new UpdateFournisseurUseCaseImpl(
                                        fournisseurRepositoryPort, fournisseurCachePort,
                                        assembler);
                        Fournisseur existant = Fournisseur.reconstruct(FournisseurId.of(FOURNISSEUR_ID),
                                        "Laboratoire A",
                                        "Dakar", "+221771234567", "contact@labo-a.sn", "M. Diop", true,
                                        java.time.Instant.now(), java.time.Instant.now());
                        when(fournisseurRepositoryPort.findById(FournisseurId.of(FOURNISSEUR_ID)))
                                        .thenReturn(Optional.of(existant));
                        when(fournisseurRepositoryPort.existsByNomIgnoreCaseAndIdNot(eq("Laboratoire B"), any()))
                                        .thenReturn(false);
                        when(fournisseurRepositoryPort.save(any(Fournisseur.class)))
                                        .thenAnswer(inv -> inv.getArgument(0));

                        FournisseurDetail result = useCase.modifier(new UpdateFournisseurCommand(FOURNISSEUR_ID,
                                        "Laboratoire B", "Thiès", "+221781234567", "new@labo-b.sn", "Mme Fall"));

                        assertThat(result.nom()).isEqualTo("Laboratoire B");
                        assertThat(result.adresse()).isEqualTo("Thiès");
                }

                @Test
                @DisplayName("lève FournisseurIntrouvableException quand le fournisseur n'existe pas")
                void leveIntrouvableQuandInexistant() {
                        UpdateFournisseurUseCaseImpl useCase = new UpdateFournisseurUseCaseImpl(
                                        fournisseurRepositoryPort, fournisseurCachePort,
                                        assembler);
                        when(fournisseurRepositoryPort.findById(FournisseurId.of(FOURNISSEUR_ID)))
                                        .thenReturn(Optional.empty());

                        assertThatThrownBy(() -> useCase.modifier(
                                        new UpdateFournisseurCommand(FOURNISSEUR_ID, "Laboratoire B", null, null, null,
                                                        null)))
                                        .isInstanceOf(FournisseurIntrouvableException.class);
                }

                @Test
                @DisplayName("lève NomFournisseurDejaUtiliseException quand le nouveau nom est pris par un autre fournisseur")
                void leveConflitQuandNomPrisParAutrui() {
                        UpdateFournisseurUseCaseImpl useCase = new UpdateFournisseurUseCaseImpl(
                                        fournisseurRepositoryPort, fournisseurCachePort,
                                        assembler);
                        Fournisseur existant = Fournisseur.reconstruct(FournisseurId.of(FOURNISSEUR_ID),
                                        "Laboratoire A",
                                        null, null, null, null, true, java.time.Instant.now(), java.time.Instant.now());
                        when(fournisseurRepositoryPort.findById(FournisseurId.of(FOURNISSEUR_ID)))
                                        .thenReturn(Optional.of(existant));
                        when(fournisseurRepositoryPort.existsByNomIgnoreCaseAndIdNot(eq("Laboratoire B"), any()))
                                        .thenReturn(true);

                        assertThatThrownBy(() -> useCase.modifier(
                                        new UpdateFournisseurCommand(FOURNISSEUR_ID, "Laboratoire B", null, null, null,
                                                        null)))
                                        .isInstanceOf(NomFournisseurDejaUtiliseException.class);

                        verify(fournisseurRepositoryPort, never()).save(any());
                }
        }

        @ExtendWith(MockitoExtension.class)
        @Nested
        @DisplayName("obtenir")
        class Obtenir {

                @Mock
                private FournisseurRepositoryPort fournisseurRepositoryPort;

                private final FournisseurDetailAssembler assembler = new FournisseurDetailAssembler();

                @Test
                @DisplayName("retourne le fournisseur quand il existe")
                void retourneQuandExiste() {
                        GetFournisseurUseCaseImpl useCase = new GetFournisseurUseCaseImpl(fournisseurRepositoryPort,
                                        assembler);
                        Fournisseur existant = Fournisseur.reconstruct(FournisseurId.of(FOURNISSEUR_ID),
                                        "Laboratoire A",
                                        null, null, null, null, true, java.time.Instant.now(), java.time.Instant.now());
                        when(fournisseurRepositoryPort.findById(FournisseurId.of(FOURNISSEUR_ID)))
                                        .thenReturn(Optional.of(existant));

                        FournisseurDetail result = useCase.obtenir(new GetFournisseurQuery(FOURNISSEUR_ID));

                        assertThat(result.id()).isEqualTo(FOURNISSEUR_ID);
                }

                @Test
                @DisplayName("lève FournisseurIntrouvableException quand il n'existe pas")
                void leveIntrouvableQuandInexistant() {
                        GetFournisseurUseCaseImpl useCase = new GetFournisseurUseCaseImpl(fournisseurRepositoryPort,
                                        assembler);
                        when(fournisseurRepositoryPort.findById(FournisseurId.of(FOURNISSEUR_ID)))
                                        .thenReturn(Optional.empty());

                        assertThatThrownBy(() -> useCase.obtenir(new GetFournisseurQuery(FOURNISSEUR_ID)))
                                        .isInstanceOf(FournisseurIntrouvableException.class);
                }
        }

        @ExtendWith(MockitoExtension.class)
        @Nested
        @DisplayName("lister")
        class Lister {

                @Mock
                private FournisseurRepositoryPort fournisseurRepositoryPort;

                private final FournisseurDetailAssembler assembler = new FournisseurDetailAssembler();

                @Test
                @DisplayName("délègue la recherche paginée au repository")
                void delegueRecherchePaginee() {
                        ListFournisseursUseCaseImpl useCase = new ListFournisseursUseCaseImpl(fournisseurRepositoryPort,
                                        assembler);
                        Fournisseur existant = Fournisseur.reconstruct(FournisseurId.of(FOURNISSEUR_ID),
                                        "Laboratoire A",
                                        null, null, null, null, true, java.time.Instant.now(), java.time.Instant.now());
                        when(fournisseurRepositoryPort.search(any(), any()))
                                        .thenReturn(PageResult.of(List.of(existant), 0, 20, 1));

                        FournisseurPage result = useCase.lister(
                                        new ListFournisseursQuery("labo", true, 0, 20, "createdAt", "DESC"));

                        assertThat(result.content()).hasSize(1);
                        assertThat(result.totalElements()).isEqualTo(1);
                }
        }

        @ExtendWith(MockitoExtension.class)
        @Nested
        @DisplayName("activer / desactiver")
        class ActiverDesactiver {

                @Mock
                private FournisseurRepositoryPort fournisseurRepositoryPort;

                @Mock
                private FournisseurCachePort fournisseurCachePort;

                private final FournisseurDetailAssembler assembler = new FournisseurDetailAssembler();

                @Test
                @DisplayName("active un fournisseur inactif")
                void activeFournisseurInactif() {
                        ActivateFournisseurUseCaseImpl useCase = new ActivateFournisseurUseCaseImpl(
                                        fournisseurRepositoryPort, fournisseurCachePort,
                                        assembler);
                        Fournisseur inactif = Fournisseur.reconstruct(FournisseurId.of(FOURNISSEUR_ID), "Laboratoire A",
                                        null, null, null, null, false, java.time.Instant.now(),
                                        java.time.Instant.now());
                        when(fournisseurRepositoryPort.findById(FournisseurId.of(FOURNISSEUR_ID)))
                                        .thenReturn(Optional.of(inactif));
                        when(fournisseurRepositoryPort.save(any(Fournisseur.class)))
                                        .thenAnswer(inv -> inv.getArgument(0));

                        FournisseurDetail result = useCase.activer(new ActivateFournisseurCommand(FOURNISSEUR_ID));

                        assertThat(result.actif()).isTrue();
                }

                @Test
                @DisplayName("désactive un fournisseur actif")
                void desactiveFournisseurActif() {
                        DeactivateFournisseurUseCaseImpl useCase = new DeactivateFournisseurUseCaseImpl(
                                        fournisseurRepositoryPort, fournisseurCachePort, assembler);
                        Fournisseur actif = Fournisseur.reconstruct(FournisseurId.of(FOURNISSEUR_ID), "Laboratoire A",
                                        null, null, null, null, true, java.time.Instant.now(), java.time.Instant.now());
                        when(fournisseurRepositoryPort.findById(FournisseurId.of(FOURNISSEUR_ID)))
                                        .thenReturn(Optional.of(actif));
                        when(fournisseurRepositoryPort.save(any(Fournisseur.class)))
                                        .thenAnswer(inv -> inv.getArgument(0));

                        FournisseurDetail result = useCase.desactiver(new DeactivateFournisseurCommand(FOURNISSEUR_ID));

                        assertThat(result.actif()).isFalse();
                }

                @Test
                @DisplayName("lève FournisseurIntrouvableException quand le fournisseur n'existe pas")
                void leveIntrouvableQuandInexistant() {
                        ActivateFournisseurUseCaseImpl useCase = new ActivateFournisseurUseCaseImpl(
                                        fournisseurRepositoryPort, fournisseurCachePort,
                                        assembler);
                        when(fournisseurRepositoryPort.findById(FournisseurId.of(FOURNISSEUR_ID)))
                                        .thenReturn(Optional.empty());

                        assertThatThrownBy(() -> useCase.activer(new ActivateFournisseurCommand(FOURNISSEUR_ID)))
                                        .isInstanceOf(FournisseurIntrouvableException.class);
                }
        }
}
