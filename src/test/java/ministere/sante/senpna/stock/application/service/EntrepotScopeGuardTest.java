package ministere.sante.senpna.stock.application.service;

import ministere.sante.senpna.shared.infrastructure.security.CurrentUser;
import ministere.sante.senpna.stock.domain.exception.AucunEntrepotAffecteException;
import ministere.sante.senpna.stock.domain.exception.PorteeEntrepotInterditeException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
        CurrentUser currentUser = Mockito.mock(CurrentUser.class);
        Mockito.when(currentUser.getEntrepotId()).thenReturn(entrepotId);

        List<GrantedAuthority> authorities = List.of(roleCodes).stream()
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

            assertThatThrownBy(guard::entrepotIdCourant).isInstanceOf(AucunEntrepotAffecteException.class);
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

            assertThatCode_ne_leve_rien(() -> guard.verifierEcritureAutorisee(entrepotId));
        }

        @Test
        @DisplayName("PRA ciblant un autre entrepôt → PorteeEntrepotInterditeException")
        void praSurAutreEntrepot_refuse() {
            authentifier(UUID.randomUUID(), "MAGASINIER_PRA");
            UUID autreEntrepot = UUID.randomUUID();

            assertThatThrownBy(() -> guard.verifierEcritureAutorisee(autreEntrepot))
                    .isInstanceOf(PorteeEntrepotInterditeException.class);
        }

        @Test
        @DisplayName("PNA ciblant un entrepôt autre que le sien → refusé (l'écriture reste limitée à son propre entrepôt)")
        void pnaSurAutreEntrepot_refuseAussi() {
            authentifier(UUID.randomUUID(), "MAGASINIER_PNA");
            UUID autreEntrepot = UUID.randomUUID();

            assertThatThrownBy(() -> guard.verifierEcritureAutorisee(autreEntrepot))
                    .isInstanceOf(PorteeEntrepotInterditeException.class);
        }

        @Test
        @DisplayName("entrepôt cible null → PorteeEntrepotInterditeException")
        void entrepotCibleNull_refuse() {
            authentifier(UUID.randomUUID(), "MAGASINIER_PNA");

            assertThatThrownBy(() -> guard.verifierEcritureAutorisee(null))
                    .isInstanceOf(PorteeEntrepotInterditeException.class);
        }

        private void assertThatCode_ne_leve_rien(Runnable r) {
            r.run(); // lève si le guard refuse — pas d'assertion supplémentaire nécessaire
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

            assertThat(guard.entrepotIdPourLecture(entrepotDemande)).isEqualTo(entrepotDemande);
        }

        @Test
        @DisplayName("PNA : aucun filtre demandé (null) → reste null (vision globale)")
        void pna_sansFiltre_resteNull() {
            authentifier(UUID.randomUUID(), "ADMIN_PNA");

            assertThat(guard.entrepotIdPourLecture(null)).isNull();
        }

        @Test
        @DisplayName("PRA : filtre demandé ignoré, toujours ramené à son propre entrepôt")
        void pra_toujoursRameneASonEntrepot() {
            UUID entrepotCourant = UUID.randomUUID();
            authentifier(entrepotCourant, "GESTIONNAIRE_PRA");
            UUID entrepotDemande = UUID.randomUUID();

            assertThat(guard.entrepotIdPourLecture(entrepotDemande)).isEqualTo(entrepotCourant);
        }

        @Test
        @DisplayName("PRA : aucun filtre demandé → ramené quand même à son propre entrepôt")
        void pra_sansFiltre_rameneASonEntrepot() {
            UUID entrepotCourant = UUID.randomUUID();
            authentifier(entrepotCourant, "PHARMACIEN_PRA");

            assertThat(guard.entrepotIdPourLecture(null)).isEqualTo(entrepotCourant);
        }
    }

    @Nested
    @DisplayName("verifierLectureAutorisee()")
    class VerifierLectureAutorisee {

        @Test
        @DisplayName("PNA : toujours autorisé, quel que soit l'entrepôt de la ressource")
        void pna_toujoursAutorise() {
            authentifier(UUID.randomUUID(), "ADMIN_PNA");

            assertThatCode_ne_leve_rien(() -> guard.verifierLectureAutorisee(UUID.randomUUID()));
        }

        @Test
        @DisplayName("PRA sur sa propre ressource → autorisé")
        void praSurSaRessource_autorise() {
            UUID entrepotId = UUID.randomUUID();
            authentifier(entrepotId, "MAGASINIER_PRA");

            assertThatCode_ne_leve_rien(() -> guard.verifierLectureAutorisee(entrepotId));
        }

        @Test
        @DisplayName("PRA sur la ressource d'un autre entrepôt → PorteeEntrepotInterditeException")
        void praSurRessourceAutrui_refuse() {
            authentifier(UUID.randomUUID(), "MAGASINIER_PRA");

            var randomUUID = UUID.randomUUID();
            assertThatThrownBy(() -> guard.verifierLectureAutorisee(randomUUID))
                    .isInstanceOf(PorteeEntrepotInterditeException.class);
        }

        private void assertThatCode_ne_leve_rien(Runnable r) {
            r.run();
        }
    }

    @Nested
    @DisplayName("verifierActeurNational()")
    class VerifierActeurNational {

        @Test
        @DisplayName("acteur PNA → autorisé")
        void pna_autorise() {
            authentifier(UUID.randomUUID(), "GESTIONNAIRE_PNA");
            guard.verifierActeurNational(); // ne doit pas lever
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
