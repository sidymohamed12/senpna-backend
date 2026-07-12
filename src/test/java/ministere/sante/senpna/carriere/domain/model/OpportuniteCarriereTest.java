package ministere.sante.senpna.carriere.domain.model;

import ministere.sante.senpna.carriere.domain.exception.DateLimiteCandidatureInvalideException;
import ministere.sante.senpna.carriere.domain.valueobject.OpportuniteCarriereId;
import ministere.sante.senpna.carriere.domain.valueobject.StatutOpportunite;
import ministere.sante.senpna.carriere.domain.valueobject.TypeContrat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("OpportuniteCarriere — agrégat de domaine")
class OpportuniteCarriereTest {

    private static final UUID AUTEUR_ID = UUID.randomUUID();

    @Nested
    @DisplayName("creer()")
    class Creer {

        @Test
        @DisplayName("crée une opportunité en BROUILLON par défaut")
        void creer_succes_statutBrouillon() {
            OpportuniteCarriere opportunite = OpportuniteCarriere.creer("Pharmacien(ne) responsable",
                    "PNA", "Description du poste", "https://cdn.senpna.sn/fiches-de-poste/test.pdf",
                    "Dakar, Sénégal", TypeContrat.CDI, LocalDate.now().plusDays(60), LocalDate.now().plusDays(30),
                    AUTEUR_ID, "Cheikh Ba", "recrutement@senpna.sn");

            assertThat(opportunite.getStatut()).isEqualTo(StatutOpportunite.BROUILLON);
            assertThat(opportunite.getTitre()).isEqualTo("Pharmacien(ne) responsable");
            assertThat(opportunite.getAuteurId()).isEqualTo(AUTEUR_ID);
            assertThat(opportunite.getAuteurNom()).isEqualTo("Cheikh Ba");
            assertThat(opportunite.getId()).isNotNull();
        }

        @Test
        @DisplayName("champs optionnels absents (fiche de poste, date de début, e-mail de contact)")
        void creer_succes_champsOptionnelsAbsents() {
            OpportuniteCarriere opportunite = OpportuniteCarriere.creer("Magasinier(ère)", "PRA Thiès",
                    "Description minimale", null, "Thiès, Sénégal", TypeContrat.CDD, null,
                    LocalDate.now().plusDays(15), AUTEUR_ID, "Mamadou Diallo", null);

            assertThat(opportunite.getFicheDePosteUrl()).isNull();
            assertThat(opportunite.getDateDebut()).isNull();
            assertThat(opportunite.getEmailContact()).isNull();
        }

        @Test
        @DisplayName("titre vide → IllegalArgumentException")
        void creer_titreVide_leveException() {
            assertThatThrownBy(() -> creerOpportunite(builder -> builder.titre("   ")))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("nom d'entreprise vide → IllegalArgumentException")
        void creer_nomEntrepriseVide_leveException() {
            assertThatThrownBy(() -> creerOpportunite(builder -> builder.nomEntreprise("")))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("description vide → IllegalArgumentException")
        void creer_descriptionVide_leveException() {
            assertThatThrownBy(() -> creerOpportunite(builder -> builder.description(" ")))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("lieu vide → IllegalArgumentException")
        void creer_lieuVide_leveException() {
            assertThatThrownBy(() -> creerOpportunite(builder -> builder.lieu("")))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("type de contrat null → NullPointerException")
        void creer_typeContratNull_leveException() {
            assertThatThrownBy(() -> creerOpportunite(builder -> builder.typeContrat(null)))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("date limite de candidature null → NullPointerException")
        void creer_dateLimiteNulle_leveException() {
            assertThatThrownBy(() -> creerOpportunite(builder -> builder.dateLimiteCandidature(null)))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("date limite postérieure à la date de début → DateLimiteCandidatureInvalideException")
        void creer_dateLimitePostérieureADateDebut_leveException() {
            assertThatThrownBy(() -> creerOpportunite(builder -> builder
                    .dateDebut(LocalDate.now().plusDays(10))
                    .dateLimiteCandidature(LocalDate.now().plusDays(20))))
                    .isInstanceOf(DateLimiteCandidatureInvalideException.class);
        }

        @Test
        @DisplayName("date limite égale à la date de début → autorisé")
        void creer_dateLimiteEgaleADateDebut_autorise() {
            LocalDate date = LocalDate.now().plusDays(15);

            OpportuniteCarriere opportunite = creerOpportunite(
                    builder -> builder.dateDebut(date).dateLimiteCandidature(date));

            assertThat(opportunite.getDateLimiteCandidature()).isEqualTo(date);
        }

        @Test
        @DisplayName("auteur null → NullPointerException")
        void creer_auteurNull_leveException() {
            assertThatThrownBy(() -> creerOpportunite(builder -> builder.auteurId(null)))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("publier() / mettreEnCours() / cloturer() / remettreEnBrouillon()")
    class Transitions {

        @Test
        @DisplayName("publier() rend l'opportunité OUVERT")
        void publier_rendOuvert() {
            OpportuniteCarriere opportunite = opportuniteBrouillon();

            opportunite.publier();

            assertThat(opportunite.getStatut()).isEqualTo(StatutOpportunite.OUVERT);
        }

        @Test
        @DisplayName("publier() est idempotent")
        void publier_idempotent() {
            OpportuniteCarriere opportunite = opportuniteBrouillon();
            opportunite.publier();

            opportunite.publier();

            assertThat(opportunite.getStatut()).isEqualTo(StatutOpportunite.OUVERT);
        }

        @Test
        @DisplayName("publier() sur une offre dont la date limite est dépassée → DateLimiteCandidatureInvalideException")
        void publier_dateLimiteDepassee_leveException() {
            OpportuniteCarriere opportunite = opportuniteExpiree(StatutOpportunite.BROUILLON);

            assertThatThrownBy(opportunite::publier)
                    .isInstanceOf(DateLimiteCandidatureInvalideException.class);
        }

        @Test
        @DisplayName("publier() échoue même si déjà OUVERT quand la date limite est dépassée")
        void publier_dejaOuvertMaisExpiree_leveException() {
            OpportuniteCarriere opportunite = opportuniteExpiree(StatutOpportunite.OUVERT);

            assertThatThrownBy(opportunite::publier)
                    .isInstanceOf(DateLimiteCandidatureInvalideException.class);
        }

        @Test
        @DisplayName("mettreEnCours() rend l'opportunité EN_COURS")
        void mettreEnCours_rendEnCours() {
            OpportuniteCarriere opportunite = opportuniteBrouillon();
            opportunite.publier();

            opportunite.mettreEnCours();

            assertThat(opportunite.getStatut()).isEqualTo(StatutOpportunite.EN_COURS);
        }

        @Test
        @DisplayName("mettreEnCours() est idempotent")
        void mettreEnCours_idempotent() {
            OpportuniteCarriere opportunite = opportuniteBrouillon();
            opportunite.publier();
            opportunite.mettreEnCours();

            opportunite.mettreEnCours();

            assertThat(opportunite.getStatut()).isEqualTo(StatutOpportunite.EN_COURS);
        }

        @Test
        @DisplayName("cloturer() rend l'opportunité CLOTURE")
        void cloturer_rendCloture() {
            OpportuniteCarriere opportunite = opportuniteBrouillon();
            opportunite.publier();

            opportunite.cloturer();

            assertThat(opportunite.getStatut()).isEqualTo(StatutOpportunite.CLOTURE);
        }

        @Test
        @DisplayName("cloturer() est idempotent")
        void cloturer_idempotent() {
            OpportuniteCarriere opportunite = opportuniteBrouillon();
            opportunite.cloturer();

            opportunite.cloturer();

            assertThat(opportunite.getStatut()).isEqualTo(StatutOpportunite.CLOTURE);
        }

        @Test
        @DisplayName("remettreEnBrouillon() repasse une offre clôturée en brouillon")
        void remettreEnBrouillon_repasseEnBrouillon() {
            OpportuniteCarriere opportunite = opportuniteBrouillon();
            opportunite.publier();
            opportunite.cloturer();

            opportunite.remettreEnBrouillon();

            assertThat(opportunite.getStatut()).isEqualTo(StatutOpportunite.BROUILLON);
        }

        @Test
        @DisplayName("remettreEnBrouillon() est idempotent")
        void remettreEnBrouillon_idempotent() {
            OpportuniteCarriere opportunite = opportuniteBrouillon();

            opportunite.remettreEnBrouillon();

            assertThat(opportunite.getStatut()).isEqualTo(StatutOpportunite.BROUILLON);
        }
    }

    @Nested
    @DisplayName("getStatutEffectif() / estExpiree() / accepteCandidatures() / estVisiblePubliquement()")
    class StatutEffectif {

        @Test
        @DisplayName("offre OUVERT non expirée → statut effectif OUVERT, accepte les candidatures, visible")
        void ouverteNonExpiree() {
            OpportuniteCarriere opportunite = opportuniteExistante(StatutOpportunite.OUVERT,
                    LocalDate.now().plusDays(10));

            assertThat(opportunite.getStatutEffectif()).isEqualTo(StatutOpportunite.OUVERT);
            assertThat(opportunite.estExpiree()).isFalse();
            assertThat(opportunite.accepteCandidatures()).isTrue();
            assertThat(opportunite.estVisiblePubliquement()).isTrue();
        }

        @Test
        @DisplayName("offre persistée OUVERT mais expirée → statut effectif CLOTURE, n'accepte plus, non visible")
        void ouvertePersisteeMaisExpiree() {
            OpportuniteCarriere opportunite = opportuniteExistante(StatutOpportunite.OUVERT,
                    LocalDate.now().minusDays(1));

            assertThat(opportunite.getStatutEffectif()).isEqualTo(StatutOpportunite.CLOTURE);
            assertThat(opportunite.estExpiree()).isTrue();
            assertThat(opportunite.accepteCandidatures()).isFalse();
            assertThat(opportunite.estVisiblePubliquement()).isFalse();
        }

        @Test
        @DisplayName("offre EN_COURS non expirée → visible publiquement mais n'accepte plus de nouvelles candidatures")
        void enCoursNonExpiree() {
            OpportuniteCarriere opportunite = opportuniteExistante(StatutOpportunite.EN_COURS,
                    LocalDate.now().plusDays(5));

            assertThat(opportunite.getStatutEffectif()).isEqualTo(StatutOpportunite.EN_COURS);
            assertThat(opportunite.accepteCandidatures()).isFalse();
            assertThat(opportunite.estVisiblePubliquement()).isTrue();
        }

        @Test
        @DisplayName("offre EN_COURS expirée → statut effectif CLOTURE, non visible")
        void enCoursExpiree() {
            OpportuniteCarriere opportunite = opportuniteExistante(StatutOpportunite.EN_COURS,
                    LocalDate.now().minusDays(3));

            assertThat(opportunite.getStatutEffectif()).isEqualTo(StatutOpportunite.CLOTURE);
            assertThat(opportunite.estVisiblePubliquement()).isFalse();
        }

        @Test
        @DisplayName("offre BROUILLON → jamais visible publiquement, même avec une date limite future")
        void brouillonJamaisVisible() {
            OpportuniteCarriere opportunite = opportuniteExistante(StatutOpportunite.BROUILLON,
                    LocalDate.now().plusDays(30));

            assertThat(opportunite.estVisiblePubliquement()).isFalse();
            assertThat(opportunite.accepteCandidatures()).isFalse();
        }

        @Test
        @DisplayName("offre CLOTURE manuellement → non visible même avec une date limite future")
        void clotureManuelleNonVisible() {
            OpportuniteCarriere opportunite = opportuniteExistante(StatutOpportunite.CLOTURE,
                    LocalDate.now().plusDays(30));

            assertThat(opportunite.getStatutEffectif()).isEqualTo(StatutOpportunite.CLOTURE);
            assertThat(opportunite.estVisiblePubliquement()).isFalse();
            assertThat(opportunite.accepteCandidatures()).isFalse();
        }
    }

    @Nested
    @DisplayName("modifierContenu()")
    class ModifierContenu {

        @Test
        @DisplayName("modifie le titre, la description, le lieu et le type de contrat")
        void modifie_contenuMisAJour() {
            OpportuniteCarriere opportunite = opportuniteBrouillon();

            opportunite.modifierContenu("Nouveau titre", "Nouvelle entreprise", "Nouvelle description",
                    "https://cdn.senpna.sn/fiches-de-poste/nouvelle.pdf", "Ziguinchor, Sénégal", TypeContrat.STAGE,
                    LocalDate.now().plusDays(90), LocalDate.now().plusDays(45), "nouveau@senpna.sn");

            assertThat(opportunite.getTitre()).isEqualTo("Nouveau titre");
            assertThat(opportunite.getNomEntreprise()).isEqualTo("Nouvelle entreprise");
            assertThat(opportunite.getLieu()).isEqualTo("Ziguinchor, Sénégal");
            assertThat(opportunite.getTypeContrat()).isEqualTo(TypeContrat.STAGE);
            assertThat(opportunite.getEmailContact()).isEqualTo("nouveau@senpna.sn");
        }

        @Test
        @DisplayName("ne modifie pas le statut courant")
        void modifie_neChangePasLeStatut() {
            OpportuniteCarriere opportunite = opportuniteBrouillon();
            opportunite.publier();

            opportunite.modifierContenu("Titre modifié", "Entreprise", "Description", null, "Dakar, Sénégal",
                    TypeContrat.CDI, null, LocalDate.now().plusDays(20), null);

            assertThat(opportunite.getStatut()).isEqualTo(StatutOpportunite.OUVERT);
        }
    }

    // ── Fixtures ─────────────────────────────────────────────────────────

    private OpportuniteCarriere opportuniteBrouillon() {
        return creerOpportunite(builder -> {
        });
    }

    private OpportuniteCarriere opportuniteExpiree(StatutOpportunite statut) {
        return opportuniteExistante(statut, LocalDate.now().minusDays(1));
    }

    private OpportuniteCarriere opportuniteExistante(StatutOpportunite statut, LocalDate dateLimiteCandidature) {
        Instant maintenant = Instant.now();
        return OpportuniteCarriere.reconstruct(
                OpportuniteCarriereId.generate(), "Titre",
                "Entreprise", "Description", null, "Dakar, Sénégal", TypeContrat.CDI, null, dateLimiteCandidature,
                AUTEUR_ID, "Cheikh Ba", null, statut, maintenant, maintenant);
    }

    private OpportuniteCarriere creerOpportunite(java.util.function.Consumer<OpportuniteBuilder> customizer) {
        OpportuniteBuilder builder = new OpportuniteBuilder();
        customizer.accept(builder);
        return builder.build();
    }

    /**
     * Petit builder de confort pour ne faire varier qu'un seul paramètre par test.
     */
    private static final class OpportuniteBuilder {
        private String titre = "Titre valide";
        private String nomEntreprise = "Entreprise valide";
        private String description = "Description valide";
        private String ficheDePosteUrl = null;
        private String lieu = "Dakar, Sénégal";
        private TypeContrat typeContrat = TypeContrat.CDI;
        private LocalDate dateDebut = null;
        private LocalDate dateLimiteCandidature = LocalDate.now().plusDays(30);
        private UUID auteurId = AUTEUR_ID;
        private String auteurNom = "Cheikh Ba";
        private String emailContact = null;

        OpportuniteBuilder titre(String titre) {
            this.titre = titre;
            return this;
        }

        OpportuniteBuilder nomEntreprise(String nomEntreprise) {
            this.nomEntreprise = nomEntreprise;
            return this;
        }

        OpportuniteBuilder description(String description) {
            this.description = description;
            return this;
        }

        OpportuniteBuilder lieu(String lieu) {
            this.lieu = lieu;
            return this;
        }

        OpportuniteBuilder typeContrat(TypeContrat typeContrat) {
            this.typeContrat = typeContrat;
            return this;
        }

        OpportuniteBuilder dateDebut(LocalDate dateDebut) {
            this.dateDebut = dateDebut;
            return this;
        }

        OpportuniteBuilder dateLimiteCandidature(LocalDate dateLimiteCandidature) {
            this.dateLimiteCandidature = dateLimiteCandidature;
            return this;
        }

        OpportuniteBuilder auteurId(UUID auteurId) {
            this.auteurId = auteurId;
            return this;
        }

        OpportuniteCarriere build() {
            return OpportuniteCarriere.creer(titre, nomEntreprise, description, ficheDePosteUrl, lieu, typeContrat,
                    dateDebut, dateLimiteCandidature, auteurId, auteurNom, emailContact);
        }
    }
}
