package ministere.sante.senpna.auth.infrastructure.persistence.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

@DisplayName("RoleJpaEntity — equals()/hashCode()")
class RoleJpaEntityTest {

    UUID id = UUID.randomUUID();

    @Nested
    @DisplayName("equals()")
    class Equals {

        @Test
        @DisplayName("Même référence")
        void memeReference_true() {
            RoleJpaEntity role = new RoleJpaEntity(id, "ADMIN_PNA", "Administrateur PNA");

            RoleJpaEntity sameReference = role;

            assertThat(role).isSameAs(sameReference);
        }

        @Test
        @DisplayName("comparé à null → false")
        void comparaisonNull_false() {
            RoleJpaEntity role = new RoleJpaEntity(id, "ADMIN_PNA", "Administrateur PNA");

            assertThat(role).isNotNull();
        }

        @Test
        @DisplayName("comparé à un objet d'une autre classe → false")
        void autreClasse_false() {
            RoleJpaEntity role = new RoleJpaEntity(id, "ADMIN_PNA", "Administrateur PNA");

            Object autre = "pas un role";

            assertNotEquals(role, autre);
        }

        @Test
        @DisplayName("identifiants différents → false")
        void identifiantsDifferents_false() {
            RoleJpaEntity role1 = new RoleJpaEntity(id, "ADMIN_PNA", "Administrateur PNA");
            RoleJpaEntity role2 = new RoleJpaEntity(UUID.randomUUID(), "ADMIN_PNA", "Administrateur PNA");

            assertThat(role1).isNotEqualTo(role2);
        }

        @Test
        @DisplayName("même id, code null d'un côté seulement → false")
        void codeNulUnCote_false() {
            RoleJpaEntity role1 = new RoleJpaEntity(id, null, "Administrateur PNA");
            RoleJpaEntity role2 = new RoleJpaEntity(id, "ADMIN_PNA", "Administrateur PNA");

            assertThat(role1).isNotEqualTo(role2);
            assertThat(role2).isNotEqualTo(role1);
        }

        @Test
        @DisplayName("même id, codes différents → false")
        void codesDifferents_false() {
            RoleJpaEntity role1 = new RoleJpaEntity(id, "ADMIN_PNA", "Administrateur PNA");
            RoleJpaEntity role2 = new RoleJpaEntity(id, "GESTIONNAIRE_PNA", "Administrateur PNA");

            assertThat(role1).isNotEqualTo(role2);
        }

        @Test
        @DisplayName("même id, même code, nom null d'un côté seulement → false")
        void nomNulUnCote_false() {
            RoleJpaEntity role1 = new RoleJpaEntity(id, "ADMIN_PNA", null);
            RoleJpaEntity role2 = new RoleJpaEntity(id, "ADMIN_PNA", "Administrateur PNA");

            assertThat(role1).isNotEqualTo(role2);
            assertThat(role2).isNotEqualTo(role1);
        }

        @Test
        @DisplayName("même id, même code, noms différents → false")
        void nomsDifferents_false() {
            RoleJpaEntity role1 = new RoleJpaEntity(id, "ADMIN_PNA", "Administrateur PNA");
            RoleJpaEntity role2 = new RoleJpaEntity(id, "ADMIN_PNA", "Autre nom");

            assertThat(role1).isNotEqualTo(role2);
        }

        @Test
        @DisplayName("même id, même code, même nom → true")
        void toutIdentique_true() {
            RoleJpaEntity role1 = new RoleJpaEntity(id, "ADMIN_PNA", "Administrateur PNA");
            RoleJpaEntity role2 = new RoleJpaEntity(id, "ADMIN_PNA", "Administrateur PNA");

            assertThat(role1).isEqualTo(role2);
        }

        @Test
        @DisplayName("même id, code et nom tous deux null → true")
        void codeEtNomNuls_true() {
            RoleJpaEntity role1 = new RoleJpaEntity(id, null, null);
            RoleJpaEntity role2 = new RoleJpaEntity(id, null, null);

            assertThat(role1).isEqualTo(role2);
        }
    }

    @Nested
    @DisplayName("hashCode()")
    class HashCode {

        @Test
        @DisplayName("cohérent pour deux instances égales")
        void coherentPourInstancesEgales() {
            RoleJpaEntity role1 = new RoleJpaEntity(id, "ADMIN_PNA", "Administrateur PNA");
            RoleJpaEntity role2 = new RoleJpaEntity(id, "ADMIN_PNA", "Administrateur PNA");

            assertThat(role1.hashCode()).hasSameHashCodeAs(role2.hashCode());
        }

        @Test
        @DisplayName("ne lève pas d'exception quand code et nom sont null")
        void codeEtNomNuls_pasException() {
            RoleJpaEntity role = new RoleJpaEntity(id, null, null);

            assertThatCode(role::hashCode).doesNotThrowAnyException();
        }
    }
}
