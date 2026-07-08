package ministere.sante.senpna.carriere.domain.model;

import ministere.sante.senpna.carriere.domain.events.NouvelleCandidatureEvent;
import ministere.sante.senpna.carriere.domain.exception.ConsentementRgpdRequisException;
import ministere.sante.senpna.carriere.domain.valueobject.CandidatureId;
import ministere.sante.senpna.carriere.domain.valueobject.Civilite;
import ministere.sante.senpna.shared.domain.model.AggregateRoot;
import ministere.sante.senpna.shared.domain.valueobject.Email;
import ministere.sante.senpna.shared.domain.valueobject.Phone;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class Candidature extends AggregateRoot<CandidatureId> {

    private static final int NOM_MAX_LENGTH = 200;
    private static final int MESSAGE_MAX_LENGTH = 2000;
    private static final int URL_MAX_LENGTH = 1000;

    private final UUID opportuniteId;
    private Civilite civilite;
    private String nomComplet;
    private Email email;
    private Phone telephone;
    private String cvUrl;
    private String lettreMotivationUrl;
    private String messageComplementaire;
    private boolean consentementRgpd;

    private Candidature(CandidatureId id, UUID opportuniteId, Civilite civilite, String nomComplet, Email email,
            Phone telephone, String cvUrl, String lettreMotivationUrl, String messageComplementaire,
            boolean consentementRgpd, Instant createdAt, Instant updatedAt) {
        super(id, createdAt, updatedAt);
        this.opportuniteId = Objects.requireNonNull(opportuniteId, "L'opportunité ciblée est obligatoire");
        this.civilite = Objects.requireNonNull(civilite, "La civilité est obligatoire");
        this.nomComplet = validerNomComplet(nomComplet);
        this.email = Objects.requireNonNull(email, "L'e-mail est obligatoire");
        this.telephone = Objects.requireNonNull(telephone, "Le téléphone est obligatoire");
        this.cvUrl = validerUrlObligatoire(cvUrl, "CV");
        this.lettreMotivationUrl = validerUrlOptionnelle(lettreMotivationUrl);
        this.messageComplementaire = validerMessage(messageComplementaire);
        if (!consentementRgpd) {
            throw new ConsentementRgpdRequisException();
        }
        this.consentementRgpd = true;
    }

    public static Candidature reconstruct(CandidatureId id, UUID opportuniteId, Civilite civilite,
            String nomComplet, Email email, Phone telephone, String cvUrl, String lettreMotivationUrl,
            String messageComplementaire, boolean consentementRgpd, Instant createdAt, Instant updatedAt) {
        return new Candidature(id, opportuniteId, civilite, nomComplet, email, telephone, cvUrl,
                lettreMotivationUrl, messageComplementaire, consentementRgpd, createdAt, updatedAt);
    }

    /**
     * Soumet une nouvelle candidature et enregistre le domain event
     * {@link NouvelleCandidatureEvent}, consommé de façon best-effort après
     * commit pour déclencher l'accusé de réception et la notification RH
     * un échec d'envoi d'e-mail ne doit jamais faire échouer la soumission de la
     * candidature elle-même.
     */
    public static Candidature soumettre(UUID opportuniteId, Civilite civilite, String nomComplet, Email email,
            Phone telephone, String cvUrl, String lettreMotivationUrl, String messageComplementaire,
            boolean consentementRgpd, String titreOffre, String nomEntreprise, String emailContactRH) {
        Instant maintenant = Instant.now();
        Candidature candidature = new Candidature(CandidatureId.generate(), opportuniteId, civilite, nomComplet,
                email, telephone, cvUrl, lettreMotivationUrl, messageComplementaire, consentementRgpd, maintenant,
                maintenant);

        candidature.registerEvent(new NouvelleCandidatureEvent(
                candidature.getId().getValue(), opportuniteId, titreOffre, nomEntreprise,
                candidature.getNomComplet(), candidature.getEmail().value(), candidature.getTelephone().value(),
                emailContactRH, maintenant));

        return candidature;
    }

    // ── Validation ───────────────────────────────────────────────────────

    private static String validerNomComplet(String nomComplet) {
        Objects.requireNonNull(nomComplet, "Le nom complet est obligatoire");
        String trimmed = nomComplet.trim();
        if (trimmed.isBlank()) {
            throw new IllegalArgumentException("Le nom complet ne peut pas être vide");
        }
        if (trimmed.length() > NOM_MAX_LENGTH) {
            throw new IllegalArgumentException("Le nom complet ne peut pas dépasser " + NOM_MAX_LENGTH
                    + " caractères");
        }
        return trimmed;
    }

    private static String validerUrlObligatoire(String url, String libelle) {
        Objects.requireNonNull(url, "Le " + libelle + " est obligatoire");
        String trimmed = url.trim();
        if (trimmed.isBlank()) {
            throw new IllegalArgumentException("Le " + libelle + " est obligatoire");
        }
        if (trimmed.length() > URL_MAX_LENGTH) {
            throw new IllegalArgumentException(
                    "L'URL du " + libelle + " ne peut pas dépasser " + URL_MAX_LENGTH + " caractères");
        }
        return trimmed;
    }

    private static String validerUrlOptionnelle(String url) {
        if (url == null || url.isBlank()) {
            return null;
        }
        String trimmed = url.trim();
        if (trimmed.length() > URL_MAX_LENGTH) {
            throw new IllegalArgumentException(
                    "L'URL de la lettre de motivation ne peut pas dépasser " + URL_MAX_LENGTH + " caractères");
        }
        return trimmed;
    }

    private static String validerMessage(String message) {
        if (message == null || message.isBlank()) {
            return null;
        }
        String trimmed = message.trim();
        if (trimmed.length() > MESSAGE_MAX_LENGTH) {
            throw new IllegalArgumentException(
                    "Le message complémentaire ne peut pas dépasser " + MESSAGE_MAX_LENGTH + " caractères");
        }
        return trimmed;
    }

    // ── Accesseurs ───────────────────────────────────────────────────────

    public UUID getOpportuniteId() {
        return opportuniteId;
    }

    public Civilite getCivilite() {
        return civilite;
    }

    public String getNomComplet() {
        return nomComplet;
    }

    public Email getEmail() {
        return email;
    }

    public Phone getTelephone() {
        return telephone;
    }

    public String getCvUrl() {
        return cvUrl;
    }

    public String getLettreMotivationUrl() {
        return lettreMotivationUrl;
    }

    public String getMessageComplementaire() {
        return messageComplementaire;
    }

    public boolean isConsentementRgpd() {
        return consentementRgpd;
    }

    public Instant getDateCandidature() {
        return getCreatedAt();
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }
}
