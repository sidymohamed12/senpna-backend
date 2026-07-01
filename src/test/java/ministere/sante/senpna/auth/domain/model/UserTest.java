package ministere.sante.senpna.auth.domain.model;

import ministere.sante.senpna.auth.fixtures.UserFixtures;
import ministere.sante.senpna.shared.domain.model.User;
import ministere.sante.senpna.shared.domain.valueobject.Email;
import ministere.sante.senpna.shared.domain.valueobject.HashedPassword;
import ministere.sante.senpna.shared.domain.valueobject.Nom;
import ministere.sante.senpna.shared.domain.valueobject.Prenom;
import ministere.sante.senpna.shared.domain.valueobject.UserId;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DisplayName("User — agrégat domaine")
class UserTest {

    // ══════════════════════════════════════════════════════════════════════
    // reconstruct
    // ══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("reconstruct()")
    class Reconstruct {

        @Test
        @DisplayName("crée un utilisateur avec tous les champs corrects")
        void reconstruct_champsCorrects() {
            User user = UserFixtures.actif();

            assertThat(user.getId().getValue()).isEqualTo(UserFixtures.USER_ID);
            assertThat(user.getNom().getValue()).isEqualTo(UserFixtures.NOM);
            assertThat(user.getPrenom().getValue()).isEqualTo(UserFixtures.PRENOM);
            assertThat(user.getEmail().value()).isEqualTo(UserFixtures.EMAIL);
            assertThat(user.getHashedPassword().value()).isEqualTo(UserFixtures.PASSWORD_HASH);
            assertThat(user.isActif()).isTrue();
            assertThat(user.getTentativesEchecConnexion()).isZero();
            assertThat(user.getVerrouilleJusqua()).isNull();
            assertThat(user.getTelephone()).isNull();
        }

        @Test
        @DisplayName("lève NullPointerException si nom est null")
        void reconstruct_nomNull_leve_npe() {
            // Utilisation de noms valides (≥ 2 chars) pour que seul le null sur `nom`
            // déclenche l'exception, et non la validation de Name
            assertThatNullPointerException()
                    .isThrownBy(() -> User.reconstruct(
                            UserId.of(UUID.randomUUID()),
                            null, // nom null
                            Prenom.of("Bi"), // valide (≥ 2 chars)
                            Email.of("a@b.sn"),
                            null,
                            HashedPassword.of("hash1"),
                            true, Set.of(), 0, null, Instant.now(), Instant.now()))
                    .withMessageContaining("nom");
        }

        @Test
        @DisplayName("lève NullPointerException si prenom est null")
        void reconstruct_prenomNull_leve_npe() {
            assertThatNullPointerException()
                    .isThrownBy(() -> User.reconstruct(
                            UserId.of(UUID.randomUUID()),
                            Nom.of("Ba"), // valide (≥ 2 chars)
                            null, // prenom null
                            Email.of("a@b.sn"),
                            null,
                            HashedPassword.of("hash1"),
                            true, Set.of(), 0, null, Instant.now(), Instant.now()))
                    .withMessageContaining("prénom");
        }

        @Test
        @DisplayName("lève NullPointerException si email est null")
        void reconstruct_emailNull_leve_npe() {
            assertThatNullPointerException()
                    .isThrownBy(() -> User.reconstruct(
                            UserId.of(UUID.randomUUID()),
                            Nom.of("Ba"),
                            Prenom.of("Bi"),
                            null, // email null
                            null,
                            HashedPassword.of("hash1"),
                            true, Set.of(), 0, null, Instant.now(), Instant.now()))
                    .withMessageContaining("email");
        }

        @Test
        @DisplayName("lève NullPointerException si hashedPassword est null")
        void reconstruct_passwordNull_leve_npe() {
            assertThatNullPointerException()
                    .isThrownBy(() -> User.reconstruct(
                            UserId.of(UUID.randomUUID()),
                            Nom.of("Ba"),
                            Prenom.of("Bi"),
                            Email.of("a@b.sn"),
                            null,
                            null, // password null
                            true, Set.of(), 0, null, Instant.now(), Instant.now()))
                    .withMessageContaining("mot de passe");
        }

        @Test
        @DisplayName("roleIds est une copie défensive — modifications externes n'affectent pas l'agrégat")
        void reconstruct_roleIds_copieDefensive() {
            Set<UUID> roleIds = new java.util.HashSet<>(Set.of(UUID.randomUUID()));
            User user = User.reconstruct(
                    UserId.of(UUID.randomUUID()),
                    Nom.of("Ba"), // valide
                    Prenom.of("Bi"), // valide
                    Email.of("a@b.sn"),
                    null,
                    HashedPassword.of("hash1"),
                    true, roleIds, 0, null, Instant.now(), Instant.now());

            roleIds.add(UUID.randomUUID()); // mutation externe

            assertThat(user.getRoleIds()).hasSize(1); // non affecté
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // changerMotDePasse
    // ══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("changerMotDePasse()")
    class ChangerMotDePasse {

        @Test
        @DisplayName("remplace le hash par le nouveau")
        void changerMotDePasse_remplace_hash() {
            User user = UserFixtures.actif();
            HashedPassword nouveauHash = HashedPassword.of("$2a$12$newhashXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");

            user.changerMotDePasse(nouveauHash);

            assertThat(user.getHashedPassword()).isEqualTo(nouveauHash);
        }

        @Test
        @DisplayName("remet les échecs de connexion à zéro")
        void changerMotDePasse_reset_echecs() {
            User user = UserFixtures.avecEchecs(3);

            user.changerMotDePasse(HashedPassword.of("$2a$12$newhashXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"));

            assertThat(user.getTentativesEchecConnexion()).isZero();
        }

        @Test
        @DisplayName("supprime le verrouillage existant")
        void changerMotDePasse_supprime_verrouillage() {
            User user = UserFixtures.verrouille();
            assertThat(user.estVerrouille()).isTrue();

            user.changerMotDePasse(HashedPassword.of("$2a$12$newhashXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX"));

            assertThat(user.estVerrouille()).isFalse();
            assertThat(user.getVerrouilleJusqua()).isNull();
        }

        @Test
        @DisplayName("lève NullPointerException si le nouveau hash est null")
        void changerMotDePasse_null_leve_npe() {
            User user = UserFixtures.actif();
            assertThatNullPointerException()
                    .isThrownBy(() -> user.changerMotDePasse(null));
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // enregistrerEchecConnexion / estVerrouille
    // ══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("enregistrerEchecConnexion() & estVerrouille()")
    class EchecEtVerrouillage {

        @Test
        @DisplayName("incrémente le compteur à chaque échec")
        void echec_incremente_compteur() {
            User user = UserFixtures.actif();

            user.enregistrerEchecConnexion(5, Instant.now().plusSeconds(900));
            user.enregistrerEchecConnexion(5, Instant.now().plusSeconds(900));

            assertThat(user.getTentativesEchecConnexion()).isEqualTo(2);
        }

        @Test
        @DisplayName("verrouille le compte dès que le seuil est atteint")
        void echec_verrouille_au_seuil() {
            User user = UserFixtures.actif();
            Instant lockUntil = Instant.now().plusSeconds(900);

            for (int i = 0; i < 5; i++) {
                user.enregistrerEchecConnexion(5, lockUntil);
            }

            assertThat(user.estVerrouille()).isTrue();
            assertThat(user.getVerrouilleJusqua()).isEqualTo(lockUntil);
        }

        @Test
        @DisplayName("ne verrouille pas si le seuil n'est pas atteint")
        void echec_pas_verrouillage_avant_seuil() {
            User user = UserFixtures.actif();

            for (int i = 0; i < 4; i++) {
                user.enregistrerEchecConnexion(5, Instant.now().plusSeconds(900));
            }

            assertThat(user.estVerrouille()).isFalse();
            assertThat(user.getVerrouilleJusqua()).isNull();
        }

        @Test
        @DisplayName("estVerrouille() retourne false si verrouilleJusqua est null")
        void estVerrouille_false_si_null() {
            User user = UserFixtures.actif();
            assertThat(user.estVerrouille()).isFalse();
        }

        @Test
        @DisplayName("estVerrouille() retourne false si verrouilleJusqua est dans le passé")
        void estVerrouille_false_si_passe() {
            User user = User.reconstruct(
                    UserId.of(UserFixtures.USER_ID), Nom.of(UserFixtures.NOM), Prenom.of(UserFixtures.PRENOM),
                    Email.of(UserFixtures.EMAIL), null, HashedPassword.of(UserFixtures.PASSWORD_HASH),
                    true, Set.of(), 5, Instant.now().minusSeconds(1),
                    Instant.now(), Instant.now());

            assertThat(user.estVerrouille()).isFalse();
        }

        @Test
        @DisplayName("estVerrouille() retourne true si verrouilleJusqua est dans le futur")
        void estVerrouille_true_si_futur() {
            User user = UserFixtures.verrouille();
            assertThat(user.estVerrouille()).isTrue();
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // reinitialiserEchecsConnexion
    // ══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("reinitialiserEchecsConnexion()")
    class ReinitialiserEchecs {

        @Test
        @DisplayName("remet le compteur à zéro et supprime le verrouillage")
        void reinitialiser_reset_tout() {
            User user = UserFixtures.verrouille();
            assertThat(user.getTentativesEchecConnexion()).isEqualTo(5);

            user.reinitialiserEchecsConnexion();

            assertThat(user.getTentativesEchecConnexion()).isZero();
            assertThat(user.getVerrouilleJusqua()).isNull();
            assertThat(user.estVerrouille()).isFalse();
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // ajouterRole / retirerRole / possedeAuMoinsUnRole
    // ══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Gestion des rôles")
    class GestionRoles {

        @Test
        @DisplayName("ajouterRole() ajoute l'identifiant dans la collection")
        void ajouterRole_ajoute() {
            User user = UserFixtures.sansRole();
            UUID newRole = UUID.randomUUID();

            user.ajouterRole(newRole);

            assertThat(user.getRoleIds()).containsExactly(newRole);
        }

        @Test
        @DisplayName("ajouterRole() avec null lève NullPointerException")
        void ajouterRole_null_leve_npe() {
            User user = UserFixtures.actif();
            assertThatNullPointerException().isThrownBy(() -> user.ajouterRole(null));
        }

        @Test
        @DisplayName("retirerRole() supprime l'identifiant de la collection")
        void retirerRole_supprime() {
            User user = UserFixtures.actif();

            user.retirerRole(UserFixtures.ROLE_GESTIONNAIRE_PNA_ID);

            assertThat(user.getRoleIds()).doesNotContain(UserFixtures.ROLE_GESTIONNAIRE_PNA_ID);
        }

        @Test
        @DisplayName("retirerRole() sur un rôle absent est silencieux")
        void retirerRole_absent_silencieux() {
            User user = UserFixtures.actif();
            assertThatNoException().isThrownBy(() -> user.retirerRole(UUID.randomUUID()));
        }

        @Test
        @DisplayName("possedeAuMoinsUnRole() retourne true si rôles présents")
        void possedeAuMoinsUnRole_true() {
            assertThat(UserFixtures.actif().possedeAuMoinsUnRole()).isTrue();
        }

        @Test
        @DisplayName("possedeAuMoinsUnRole() retourne false si aucun rôle")
        void possedeAuMoinsUnRole_false() {
            assertThat(UserFixtures.sansRole().possedeAuMoinsUnRole()).isFalse();
        }

        @Test
        @DisplayName("getRoleIds() retourne une vue non modifiable")
        void getRoleIds_nonModifiable() {
            User user = UserFixtures.actif();
            Set<UUID> ids = user.getRoleIds();

            assertThatThrownBy(() -> ids.add(UUID.randomUUID()))
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // renommer
    // ══════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("renommer()")
    class Renommer {

        @Test
        @DisplayName("met à jour nom et prénom")
        void renommer_met_a_jour() {
            User user = UserFixtures.actif();

            user.renommer(Nom.of("Sow"), Prenom.of("Fatou"));

            assertThat(user.getNom().getValue()).isEqualTo("Sow");
            assertThat(user.getPrenom().getValue()).isEqualTo("Fatou");
        }

        @Test
        @DisplayName("lève NullPointerException si nom null")
        void renommer_nomNull_leve_npe() {
            User user = UserFixtures.actif();
            assertThatNullPointerException()
                    .isThrownBy(() -> user.renommer(null, Prenom.of("Fatou")));
        }

        @Test
        @DisplayName("lève NullPointerException si prénom null")
        void renommer_prenomNull_leve_npe() {
            User user = UserFixtures.actif();
            assertThatNullPointerException()
                    .isThrownBy(() -> user.renommer(Nom.of("Sow"), null));
        }
    }
}
