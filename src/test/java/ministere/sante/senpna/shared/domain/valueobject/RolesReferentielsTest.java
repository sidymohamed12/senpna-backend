package ministere.sante.senpna.shared.domain.valueobject;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("RolesNationaux / RolesEntrepot — référentiels de classification des rôles")
class RolesReferentielsTest {

    @Nested
    @DisplayName("RolesNationaux")
    class NationauxTests {

        @Test
        @DisplayName("estRoleNational() vrai pour un rôle PNA")
        void estRoleNational_vraiPourRolePna() {
            assertThat(RolesNationaux.estRoleNational("ADMIN_PNA")).isTrue();
            assertThat(RolesNationaux.estRoleNational("GESTIONNAIRE_PNA")).isTrue();
        }

        @Test
        @DisplayName("estRoleNational() faux pour un rôle PRA")
        void estRoleNational_fauxPourRolePra() {
            assertThat(RolesNationaux.estRoleNational("ADMIN_PRA")).isFalse();
        }

        @Test
        @DisplayName("contientRoleNational() vrai dès qu'un seul rôle national est présent")
        void contientRoleNational_vraiDesUnSeulRole() {
            assertThat(RolesNationaux.contientRoleNational(Set.of("ADMIN_PRA", "MAGASINIER_PNA"))).isTrue();
        }

        @Test
        @DisplayName("contientRoleNational() faux si aucun rôle national")
        void contientRoleNational_fauxSiAucun() {
            assertThat(RolesNationaux.contientRoleNational(Set.of("ADMIN_PRA", "PHARMACIEN_PRA"))).isFalse();
        }

        @Test
        @DisplayName("ensemble de codes vide → faux")
        void ensembleVide_faux() {
            assertThat(RolesNationaux.contientRoleNational(Set.of())).isFalse();
        }
    }

    @Nested
    @DisplayName("RolesEntrepot")
    class EntrepotTests {

        @Test
        @DisplayName("estRolePra() vrai pour les rôles PRA, y compris ADMIN_PRA")
        void estRolePra_vraiPourRolesPra() {
            assertThat(RolesEntrepot.estRolePra("ADMIN_PRA")).isTrue();
            assertThat(RolesEntrepot.estRolePra("MAGASINIER_PRA")).isTrue();
        }

        @Test
        @DisplayName("estRolePnaAvecEntrepot() exclut volontairement ADMIN_PNA")
        void estRolePnaAvecEntrepot_excludAdminPna() {
            assertThat(RolesEntrepot.estRolePnaAvecEntrepot("ADMIN_PNA")).isFalse();
            assertThat(RolesEntrepot.estRolePnaAvecEntrepot("GESTIONNAIRE_PNA")).isTrue();
        }

        @Test
        @DisplayName("un rôle PRA n'est jamais aussi classé PNA-avec-entrepôt")
        void rolePraJamaisPnaAvecEntrepot() {
            assertThat(RolesEntrepot.estRolePnaAvecEntrepot("ADMIN_PRA")).isFalse();
        }
    }
}
