package ministere.sante.senpna.carriere.domain.model;

import ministere.sante.senpna.carriere.domain.events.NouvelleCandidatureEvent;
import ministere.sante.senpna.carriere.domain.exception.ConsentementRgpdRequisException;
import ministere.sante.senpna.carriere.domain.valueobject.Civilite;
import ministere.sante.senpna.shared.domain.valueobject.Email;
import ministere.sante.senpna.shared.domain.valueobject.Phone;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Candidature — agrégat de domaine")
class CandidatureTest {

    private static final UUID OPPORTUNITE_ID = UUID.randomUUID();

    @Nested
    @DisplayName("soumettre()")
    class Soumettre {

        @Test
        @DisplayName("crée la candidature et enregistre le domain event NouvelleCandidatureEvent")
        void soumettre_succes_enregistreEvent() {
            Candidature candidature = candidatureValide();

            assertThat(candidature.getOpportuniteId()).isEqualTo(OPPORTUNITE_ID);
            assertThat(candidature.getCivilite()).isEqualTo(Civilite.MME);
            assertThat(candidature.getNomComplet()).isEqualTo("Fatou Diagne");
            assertThat(candidature.isConsentementRgpd()).isTrue();
            assertThat(candidature.getDateCandidature()).isEqualTo(candidature.getCreatedAt());

            assertThat(candidature.getDomainEvents()).hasSize(1);
            NouvelleCandidatureEvent event = (NouvelleCandidatureEvent) candidature.getDomainEvents().get(0);
            assertThat(event.candidatureId()).isEqualTo(candidature.getId().getValue());
            assertThat(event.opportuniteId()).isEqualTo(OPPORTUNITE_ID);
            assertThat(event.titreOffre()).isEqualTo("Pharmacien(ne) responsable");
            assertThat(event.nomEntreprise()).isEqualTo("PNA");
            assertThat(event.nomCandidat()).isEqualTo("Fatou Diagne");
            assertThat(event.emailCandidat()).isEqualTo("fatou.diagne@example.sn");
            assertThat(event.emailContactRH()).isEqualTo("rh@senpna.sn");
        }

        @Test
        @DisplayName("lettre de motivation et message complémentaire optionnels absents")
        void soumettre_succes_champsOptionnelsAbsents() {
            Candidature candidature = Candidature.soumettre(OPPORTUNITE_ID, Civilite.M, "Ibrahima Sarr",
                    Email.of("ibrahima.sarr@example.sn"), Phone.of("+221771112233"),
                    "https://cdn.senpna.sn/cvs/ibrahima-sarr.pdf", null, null, true, "Titre", "Entreprise",
                    "rh@senpna.sn");

            assertThat(candidature.getLettreMotivationUrl()).isNull();
            assertThat(candidature.getMessageComplementaire()).isNull();
        }

        @Test
        @DisplayName("consentement RGPD non coché → ConsentementRgpdRequisException")
        void soumettre_consentementRefuse_leveException() {
            assertThatThrownBy(() -> Candidature.soumettre(OPPORTUNITE_ID, Civilite.MME, "Fatou Diagne",
                    Email.of("fatou.diagne@example.sn"), Phone.of("+221771112233"),
                    "https://cdn.senpna.sn/cvs/fatou-diagne.pdf", null, null, false, "Titre", "Entreprise",
                    "rh@senpna.sn"))
                    .isInstanceOf(ConsentementRgpdRequisException.class);
        }

        @Test
        @DisplayName("nom complet vide → IllegalArgumentException")
        void soumettre_nomCompletVide_leveException() {
            assertThatThrownBy(() -> Candidature.soumettre(OPPORTUNITE_ID, Civilite.M, "   ",
                    Email.of("test@example.sn"), Phone.of("+221771112233"), "https://cdn.senpna.sn/cvs/test.pdf",
                    null, null, true, "Titre", "Entreprise", "rh@senpna.sn"))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("CV manquant → IllegalArgumentException")
        void soumettre_cvManquant_leveException() {
            assertThatThrownBy(() -> Candidature.soumettre(OPPORTUNITE_ID, Civilite.M, "Ibrahima Sarr",
                    Email.of("test@example.sn"), Phone.of("+221771112233"), "", null, null, true, "Titre",
                    "Entreprise", "rh@senpna.sn"))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("message complémentaire de plus de 2000 caractères → IllegalArgumentException")
        void soumettre_messageTropLong_leveException() {
            String messageTropLong = "a".repeat(2001);

            assertThatThrownBy(() -> Candidature.soumettre(OPPORTUNITE_ID, Civilite.M, "Ibrahima Sarr",
                    Email.of("test@example.sn"), Phone.of("+221771112233"), "https://cdn.senpna.sn/cvs/test.pdf",
                    null, messageTropLong, true, "Titre", "Entreprise", "rh@senpna.sn"))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("email null → NullPointerException")
        void soumettre_emailNull_leveException() {
            assertThatThrownBy(() -> Candidature.soumettre(OPPORTUNITE_ID, Civilite.M, "Ibrahima Sarr", null,
                    Phone.of("+221771112233"), "https://cdn.senpna.sn/cvs/test.pdf", null, null, true, "Titre",
                    "Entreprise", "rh@senpna.sn"))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("téléphone null → NullPointerException")
        void soumettre_telephoneNull_leveException() {
            assertThatThrownBy(() -> Candidature.soumettre(OPPORTUNITE_ID, Civilite.M, "Ibrahima Sarr",
                    Email.of("test@example.sn"), null, "https://cdn.senpna.sn/cvs/test.pdf", null, null, true,
                    "Titre", "Entreprise", "rh@senpna.sn"))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    private Candidature candidatureValide() {
        return Candidature.soumettre(OPPORTUNITE_ID, Civilite.MME, "Fatou Diagne",
                Email.of("fatou.diagne@example.sn"), Phone.of("+221771112233"),
                "https://cdn.senpna.sn/cvs/fatou-diagne.pdf",
                "https://cdn.senpna.sn/lettres-de-motivation/fatou-diagne.pdf",
                "Message de motivation.", true, "Pharmacien(ne) responsable", "PNA", "rh@senpna.sn");
    }
}
