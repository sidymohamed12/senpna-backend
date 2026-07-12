package ministere.sante.senpna.stock.application.service;

import ministere.sante.senpna.shared.infrastructure.security.CurrentUser;
import ministere.sante.senpna.stock.domain.exception.AucunEntrepotAffecteException;
import ministere.sante.senpna.stock.domain.exception.PorteeEntrepotInterditeException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("EntrepotScopeGuard")
class EntrepotScopeGuardTest {

    private final EntrepotScopeGuard guard = new EntrepotScopeGuard();

    @BeforeEach
    void clearContextBefore() {
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    private void authentifier(UUID entrepotId, String... roleCodes) {
        CurrentUser currentUser = mock(CurrentUser.class);
        when(currentUser.getEntrepotId()).thenReturn(entrepotId);

        List<GrantedAuthority> authorities = List.of(roleCodes)
                .stream()
                .map(code -> (GrantedAuthority) new SimpleGrantedAuthority("ROLE_" + code))
                .toList();

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(currentUser, null, authorities));
    }

    @Nested
    @DisplayName("estActeurNational()")
    class EstActeurNational {

        @Test
        @DisplayName("acteur avec un rôle *_PNA → true")
        void avecRolePna_retourneTrue() {
            authentifier(UUID.randomUUID(), "MAGASINIER_PNA");

            assertThat(guard.estActeurNational()).isTrue();
        }

        @Test
        @DisplayName("acteur multi-rôles incluant un rôle PNA → true")
        void multiRolesAvecPna_retourneTrue() {
            authentifier(UUID.randomUUID(), "GESTIONNAIRE_PRA", "ADMIN_PNA");

            assertThat(guard.estActeurNational()).isTrue();
        }

        @Test
        @DisplayName("acteur avec uniquement des rôles *_PRA → false")
        void avecRolePraUniquement_retourneFalse() {
            authentifier(UUID.randomUUID(), "GESTIONNAIRE_PRA");

            assertThat(guard.estActeurNational()).isFalse();
        }
    }

    @Nested
    @DisplayName("entrepotIdCourant()")
    class EntrepotIdCourant {

        @Test
        @DisplayName("acteur affecté → retourne l'entrepôt du JWT")
        void affecte_retourneEntrepot() {
            UUID entrepotId = UUID.randomUUID();
            authentifier(entrepotId, "GESTIONNAIRE_PRA");

            assertThat(guard.entrepotIdCourant()).isEqualTo(entrepotId);
        }

        @Test
        @DisplayName("acteur non affecté (entrepotId null) → AucunEntrepotAffecteException")
        void nonAffecte_leveException() {
            authentifier(null, "GESTIONNAIRE_PRA");

            assertThatThrownBy(guard::entrepotIdCourant)
                    .isInstanceOf(AucunEntrepotAffecteException.class);
        }
    }

    @Nested
    @DisplayName("verifierEcritureAutorisee()")
    class VerifierEcritureAutorisee {

        @Test
        @DisplayName("PRA ciblant son propre entrepôt → autorisé")
        void praSurSonEntrepot_autorise() {
            UUID entrepotId = UUID.randomUUID();
            authentifier(entrepotId, "MAGASINIER_PRA");

            assertThatCode(() -> guard.verifierEcritureAutorisee(entrepotId))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("PRA ciblant un autre entrepôt → PorteeEntrepotInterditeException")
        void praSurAutreEntrepot_refuse() {
            authentifier(UUID.randomUUID(), "MAGASINIER_PRA");

            var entrepotIdAutre = UUID.randomUUID();
            assertThatThrownBy(() -> guard.verifierEcritureAutorisee(entrepotIdAutre))
                    .isInstanceOf(PorteeEntrepotInterditeException.class);
        }

        @Test
        @DisplayName("PNA ciblant un entrepôt autre que le sien → refusé")
        void pnaSurAutreEntrepot_refuseAussi() {
            authentifier(UUID.randomUUID(), "MAGASINIER_PNA");

            var entrepotIdAutre = UUID.randomUUID();
            assertThatThrownBy(() -> guard.verifierEcritureAutorisee(entrepotIdAutre))
                    .isInstanceOf(PorteeEntrepotInterditeException.class);
        }

        @Test
        @DisplayName("entrepôt cible null → PorteeEntrepotInterditeException")
        void entrepotCibleNull_refuse() {
            authentifier(UUID.randomUUID(), "MAGASINIER_PNA");

            assertThatThrownBy(() -> guard.verifierEcritureAutorisee(null))
                    .isInstanceOf(PorteeEntrepotInterditeException.class);
        }
    }

    @Nested
    @DisplayName("entrepotIdPourLecture()")
    class EntrepotIdPourLecture {

        @Test
        @DisplayName("PNA : filtre demandé conservé tel quel")
        void pna_conserveLeFiltreDemande() {
            authentifier(UUID.randomUUID(), "ADMIN_PNA");
            UUID entrepotDemande = UUID.randomUUID();

            assertThat(guard.entrepotIdPourLecture(entrepotDemande))
                    .isEqualTo(entrepotDemande);
        }

        @Test
        @DisplayName("PNA : aucun filtre demandé → null")
        void pna_sansFiltre_resteNull() {
            authentifier(UUID.randomUUID(), "ADMIN_PNA");

            assertThat(guard.entrepotIdPourLecture(null))
                    .isNull();
        }

        @Test
        @DisplayName("PRA : filtre demandé ignoré")
        void pra_toujoursRameneASonEntrepot() {
            UUID entrepotCourant = UUID.randomUUID();
            authentifier(entrepotCourant, "GESTIONNAIRE_PRA");

            assertThat(guard.entrepotIdPourLecture(UUID.randomUUID()))
                    .isEqualTo(entrepotCourant);
        }

        @Test
        @DisplayName("PRA : aucun filtre demandé → propre entrepôt")
        void pra_sansFiltre_rameneASonEntrepot() {
            UUID entrepotCourant = UUID.randomUUID();
            authentifier(entrepotCourant, "PHARMACIEN_PRA");

            assertThat(guard.entrepotIdPourLecture(null))
                    .isEqualTo(entrepotCourant);
        }
    }

    @Nested
    @DisplayName("verifierLectureAutorisee()")
    class VerifierLectureAutorisee {

        @Test
        @DisplayName("PNA : toujours autorisé")
        void pna_toujoursAutorise() {
            authentifier(UUID.randomUUID(), "ADMIN_PNA");

            assertThatCode(() -> guard.verifierLectureAutorisee(UUID.randomUUID()))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("PRA sur sa propre ressource → autorisé")
        void praSurSaRessource_autorise() {
            UUID entrepotId = UUID.randomUUID();
            authentifier(entrepotId, "MAGASINIER_PRA");

            assertThatCode(() -> guard.verifierLectureAutorisee(entrepotId))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("PRA sur la ressource d'un autre entrepôt → refusé")
        void praSurRessourceAutrui_refuse() {
            authentifier(UUID.randomUUID(), "MAGASINIER_PRA");

            var entrepotIdAutre = UUID.randomUUID();
            assertThatThrownBy(() -> guard.verifierLectureAutorisee(entrepotIdAutre))
                    .isInstanceOf(PorteeEntrepotInterditeException.class);
        }
    }

    @Nested
    @DisplayName("verifierActeurNational()")
    class VerifierActeurNational {

        @Test
        @DisplayName("acteur PNA → autorisé")
        void pna_autorise() {
            authentifier(UUID.randomUUID(), "GESTIONNAIRE_PNA");

            assertThatCode(guard::verifierActeurNational)
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("acteur PRA → PorteeEntrepotInterditeException")
        void pra_refuse() {
            authentifier(UUID.randomUUID(), "GESTIONNAIRE_PRA");

            assertThatThrownBy(guard::verifierActeurNational)
                    .isInstanceOf(PorteeEntrepotInterditeException.class);
        }
    }
}