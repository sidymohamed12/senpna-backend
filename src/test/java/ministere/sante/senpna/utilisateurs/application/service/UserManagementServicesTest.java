package ministere.sante.senpna.utilisateurs.application.service;

import ministere.sante.senpna.auth.fixtures.UserFixtures;
import ministere.sante.senpna.shared.domain.model.User;
import ministere.sante.senpna.shared.domain.port.out.RoleCachePort;
import ministere.sante.senpna.shared.domain.port.out.UserAffectationRepositoryPort;
import ministere.sante.senpna.shared.domain.projection.RoleProjection;
import ministere.sante.senpna.shared.domain.projection.UserAffectationView;
import ministere.sante.senpna.utilisateurs.domain.command.UserCommands.RoleSummary;
import ministere.sante.senpna.utilisateurs.domain.command.UserCommands.UserDetail;
import ministere.sante.senpna.utilisateurs.fixtures.RoleFixtures;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires des services applicatifs du module {@code utilisateurs} :
 * {@link UserDetailAssembler}, {@link UserRoleSummaryResolver} et
 * {@link TemporaryPasswordGenerator}.
 */
@DisplayName("Services applicatifs — UserDetailAssembler / UserRoleSummaryResolver / TemporaryPasswordGenerator")
class UserManagementServicesTest {

    // ══════════════════════════════════════════════════════════════════════
    // UserDetailAssembler
    // ══════════════════════════════════════════════════════════════════════

    @Nested
    @ExtendWith(MockitoExtension.class)
    @DisplayName("UserDetailAssembler")
    class UserDetailAssemblerTest {

        @Mock
        UserRoleSummaryResolver roleSummaryResolver;
        @Mock
        UserAffectationRepositoryPort userAffectationRepositoryPort;
        @InjectMocks
        UserDetailAssembler sut;

        @Test
        @DisplayName("assemble un UserDetail complet à partir de l'agrégat User, avec son affectation")
        void assembler_construit_userDetail_complet() {
            User user = UserFixtures.actifAvecTelephone();
            Set<RoleSummary> roles = Set.of(new RoleSummary(RoleFixtures.ROLE_GESTIONNAIRE_PNA_ID,
                    "GESTIONNAIRE_PNA", "Gestionnaire PNA"));
            when(roleSummaryResolver.resoudre(user.getRoleIds())).thenReturn(roles);
            UUID entrepotId = UUID.randomUUID();
            when(userAffectationRepositoryPort.findAffectation(user.getId().getValue()))
                    .thenReturn(Optional.of(new UserAffectationView(user.getId().getValue(), entrepotId, null)));

            UserDetail detail = sut.assembler(user);

            assertThat(detail.id()).isEqualTo(user.getId().getValue());
            assertThat(detail.nom()).isEqualTo(UserFixtures.NOM);
            assertThat(detail.prenom()).isEqualTo(UserFixtures.PRENOM);
            assertThat(detail.email()).isEqualTo(UserFixtures.EMAIL);
            assertThat(detail.telephone()).isEqualTo(UserFixtures.TELEPHONE);
            assertThat(detail.actif()).isTrue();
            assertThat(detail.roles()).isEqualTo(roles);
            assertThat(detail.entrepotId()).isEqualTo(entrepotId);
            assertThat(detail.structureSanitaireId()).isNull();
            assertThat(detail.createdAt()).isEqualTo(user.getCreatedAt());
            assertThat(detail.updatedAt()).isEqualTo(user.getUpdatedAt());
        }

        @Test
        @DisplayName("utilisateur sans téléphone ni affectation → champs correspondants à null")
        void assembler_sansTelephoneNiAffectation_champsNull() {
            User user = UserFixtures.actif();
            when(roleSummaryResolver.resoudre(any())).thenReturn(Set.of());
            when(userAffectationRepositoryPort.findAffectation(any())).thenReturn(Optional.empty());

            UserDetail detail = sut.assembler(user);

            assertThat(detail.telephone()).isNull();
            assertThat(detail.entrepotId()).isNull();
            assertThat(detail.structureSanitaireId()).isNull();
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // UserRoleSummaryResolver
    // ══════════════════════════════════════════════════════════════════════

    @Nested
    @ExtendWith(MockitoExtension.class)
    @DisplayName("UserRoleSummaryResolver")
    class UserRoleSummaryResolverTest {

        @Mock
        RoleCachePort roleCachePort;
        @InjectMocks
        UserRoleSummaryResolver sut;

        @Test
        @DisplayName("résout les identifiants de rôle en résumés (id, code, nom)")
        void resoudre_mappe_projections_en_summaries() {
            RoleProjection projection = RoleFixtures.gestionnairePna();
            when(roleCachePort.findAllById(Set.of(RoleFixtures.ROLE_GESTIONNAIRE_PNA_ID)))
                    .thenReturn(Set.of(projection));

            Set<RoleSummary> result = sut.resoudre(Set.of(RoleFixtures.ROLE_GESTIONNAIRE_PNA_ID));

            assertThat(result).containsExactly(
                    new RoleSummary(projection.id(), projection.code(), projection.nom()));
        }

        @Test
        @DisplayName("aucun identifiant → ensemble vide, sans appel au cache pour un id inconnu")
        void resoudre_ensembleVide_retourne_vide() {
            when(roleCachePort.findAllById(Set.of())).thenReturn(Set.of());

            Set<RoleSummary> result = sut.resoudre(Set.of());

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("plusieurs rôles → tous les résumés correspondants sont retournés")
        void resoudre_plusieursRoles_retourne_tousLesSummaries() {
            when(roleCachePort.findAllById(any()))
                    .thenReturn(Set.of(RoleFixtures.gestionnairePna(), RoleFixtures.pharmacienPra()));

            Set<RoleSummary> result = sut.resoudre(
                    Set.of(RoleFixtures.ROLE_GESTIONNAIRE_PNA_ID, RoleFixtures.ROLE_PHARMACIEN_PRA_ID));

            assertThat(result).hasSize(2);
            assertThat(result).extracting(RoleSummary::code)
                    .containsExactlyInAnyOrder("GESTIONNAIRE_PNA", "PHARMACIEN_PRA");
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // TemporaryPasswordGenerator
    // ══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("TemporaryPasswordGenerator")
    class TemporaryPasswordGeneratorTest {

        TemporaryPasswordGenerator sut = new TemporaryPasswordGenerator();

        @Test
        @DisplayName("génère un mot de passe de 14 caractères")
        void generer_longueurCorrecte() {
            String motDePasse = sut.generer();

            assertThat(motDePasse).hasSize(14);
        }

        @Test
        @DisplayName("contient au moins une majuscule, une minuscule, un chiffre et un caractère spécial")
        void generer_contientToutesLesCategories() {
            String motDePasse = sut.generer();

            assertThat(motDePasse)
                    .containsPattern("[ABCDEFGHJKLMNPQRSTUVWXYZ]") // majuscules sans I/O
                    .containsPattern("[abcdefghijkmnpqrstuvwxyz]") // minuscules sans l/o
                    .containsPattern("[2-9]") // chiffres sans 0/1
                    .containsPattern("[!@#$%^&*_=+\\-]");
        }

        @Test
        @DisplayName("n'utilise jamais les caractères ambigus I, O, l, o, 0, 1")
        void generer_evitCaracteresAmbigus() {
            String motDePasse = sut.generer();

            assertThat(motDePasse).doesNotContain("I", "O", "l", "o", "0", "1");
        }

        @Test
        @DisplayName("deux générations successives produisent des mots de passe différents")
        void generer_deuxAppels_produisentValeursDifferentes() {
            String premier = sut.generer();
            String second = sut.generer();

            assertThat(premier).isNotEqualTo(second);
        }
    }
}
