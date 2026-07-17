package ministere.sante.senpna.carriere.domain.model;

import ministere.sante.senpna.carriere.domain.exception.DateLimiteCandidatureInvalideException;
import ministere.sante.senpna.carriere.domain.valueobject.OpportuniteCarriereId;
import ministere.sante.senpna.carriere.domain.valueobject.StatutOpportunite;
import ministere.sante.senpna.carriere.domain.valueobject.TypeContrat;
import ministere.sante.senpna.shared.domain.model.AggregateRoot;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/**
 * Offre d'emploi / opportunité de carrière — agrégat racine.
 *
 * <h3>Cycle de vie</h3>
 * <p>
 * Toujours créée à l'état {@link StatutOpportunite#BROUILLON} — la
 * publication est une décision distincte de la saisie (même principe que
 * l'agrégat {@code Actualite} du module {@code actualite}). Transite
 * ensuite vers {@code OUVERT} (accepte les candidatures),
 * {@code EN_COURS} (traitement des candidatures, n'accepte plus de
 * nouvelles candidatures mais reste visible) puis {@code CLOTURE}
 * (terminale, manuelle ou automatique).
 * </p>
 *
 * <h3>Clôture automatique</h3>
 * <p>
 * Le statut persisté ({@link #statut}) peut être en retard d'au plus un
 * cycle du job planifié de clôture automatique (cf.
 * {@code CloturerOpportunitesExpireesJob}). Pour ne jamais exposer un
 * statut incohérent à l'utilisateur, {@link #getStatutEffectif()} calcule
 * le statut réel à la lecture : une opportunité {@code OUVERT} ou
 * {@code EN_COURS} dont la date limite est dépassée est toujours
 * présentée comme {@code CLOTURE}, que le job planifié soit déjà passé ou
 * non.
 * </p>
 */
public class OpportuniteCarriere extends AggregateRoot<OpportuniteCarriereId> {

    private static final int TITRE_MAX_LENGTH = 200;
    private static final int ENTREPRISE_MAX_LENGTH = 200;
    private static final int DESCRIPTION_MAX_LENGTH = 8000;
    private static final int LIEU_MAX_LENGTH = 200;
    private static final int URL_MAX_LENGTH = 1000;

    private String titre;
    private String nomEntreprise;
    private String description;
    private String ficheDePosteUrl;
    private String lieu;
    private TypeContrat typeContrat;
    private LocalDate dateDebut;
    private LocalDate dateLimiteCandidature;
    private final UUID auteurId;
    private String auteurNom;
    private String emailContact;
    private StatutOpportunite statut;

    private OpportuniteCarriere(Builder builder) {
        super(builder.id, builder.createdAt, builder.updatedAt);
        this.titre = validerTitre(builder.titre);
        this.nomEntreprise = validerNomEntreprise(builder.nomEntreprise);
        this.description = validerDescription(builder.description);
        this.ficheDePosteUrl = validerUrl(builder.ficheDePosteUrl, "fiche de poste");
        this.lieu = validerLieu(builder.lieu);
        this.typeContrat = Objects.requireNonNull(builder.typeContrat, "Le type de contrat est obligatoire");
        this.dateDebut = builder.dateDebut;
        this.dateLimiteCandidature = validerDates(builder.dateDebut, builder.dateLimiteCandidature);
        this.auteurId = Objects.requireNonNull(builder.auteurId, "L'auteur de l'offre est obligatoire");
        this.auteurNom = validerAuteurNom(builder.auteurNom);
        this.emailContact = builder.emailContact != null && !builder.emailContact.isBlank()
                ? builder.emailContact.trim()
                : null;
        this.statut = builder.statut != null ? builder.statut : StatutOpportunite.BROUILLON;
    }

    /** Données nécessaires à la création d'une nouvelle offre d'emploi. */
    public record CreationCommand(String titre, String nomEntreprise, String description, String ficheDePosteUrl,
            String lieu, TypeContrat typeContrat, LocalDate dateDebut, LocalDate dateLimiteCandidature,
            UUID auteurId, String auteurNom, String emailContact) {
    }

    public static OpportuniteCarriere creer(CreationCommand command) {
        Instant maintenant = Instant.now();
        return builder()
                .id(OpportuniteCarriereId.generate())
                .titre(command.titre())
                .nomEntreprise(command.nomEntreprise())
                .description(command.description())
                .ficheDePosteUrl(command.ficheDePosteUrl())
                .lieu(command.lieu())
                .typeContrat(command.typeContrat())
                .dateDebut(command.dateDebut())
                .dateLimiteCandidature(command.dateLimiteCandidature())
                .auteurId(command.auteurId())
                .auteurNom(command.auteurNom())
                .emailContact(command.emailContact())
                .statut(StatutOpportunite.BROUILLON)
                .createdAt(maintenant)
                .updatedAt(maintenant)
                .build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private OpportuniteCarriereId id;
        private String titre;
        private String nomEntreprise;
        private String description;
        private String ficheDePosteUrl;
        private String lieu;
        private TypeContrat typeContrat;
        private LocalDate dateDebut;
        private LocalDate dateLimiteCandidature;
        private UUID auteurId;
        private String auteurNom;
        private String emailContact;
        private StatutOpportunite statut;
        private Instant createdAt;
        private Instant updatedAt;

        private Builder() {
        }

        public Builder id(OpportuniteCarriereId id) {
            this.id = id;
            return this;
        }

        public Builder titre(String titre) {
            this.titre = titre;
            return this;
        }

        public Builder nomEntreprise(String nomEntreprise) {
            this.nomEntreprise = nomEntreprise;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder ficheDePosteUrl(String ficheDePosteUrl) {
            this.ficheDePosteUrl = ficheDePosteUrl;
            return this;
        }

        public Builder lieu(String lieu) {
            this.lieu = lieu;
            return this;
        }

        public Builder typeContrat(TypeContrat typeContrat) {
            this.typeContrat = typeContrat;
            return this;
        }

        public Builder dateDebut(LocalDate dateDebut) {
            this.dateDebut = dateDebut;
            return this;
        }

        public Builder dateLimiteCandidature(LocalDate dateLimiteCandidature) {
            this.dateLimiteCandidature = dateLimiteCandidature;
            return this;
        }

        public Builder auteurId(UUID auteurId) {
            this.auteurId = auteurId;
            return this;
        }

        public Builder auteurNom(String auteurNom) {
            this.auteurNom = auteurNom;
            return this;
        }

        public Builder emailContact(String emailContact) {
            this.emailContact = emailContact;
            return this;
        }

        public Builder statut(StatutOpportunite statut) {
            this.statut = statut;
            return this;
        }

        public Builder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder updatedAt(Instant updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public OpportuniteCarriere build() {
            return new OpportuniteCarriere(this);
        }
    }

    // ── Comportements métier ────────────────────────────────────────────

    public void modifierContenu(String titre, String nomEntreprise, String description, String ficheDePosteUrl,
            String lieu, TypeContrat typeContrat, LocalDate dateDebut, LocalDate dateLimiteCandidature,
            String emailContact) {
        this.titre = validerTitre(titre);
        this.nomEntreprise = validerNomEntreprise(nomEntreprise);
        this.description = validerDescription(description);
        this.ficheDePosteUrl = validerUrl(ficheDePosteUrl, "fiche de poste");
        this.lieu = validerLieu(lieu);
        this.typeContrat = Objects.requireNonNull(typeContrat, "Le type de contrat est obligatoire");
        this.dateDebut = dateDebut;
        this.dateLimiteCandidature = validerDates(dateDebut, dateLimiteCandidature);
        this.emailContact = emailContact != null && !emailContact.isBlank() ? emailContact.trim() : null;
        markUpdated();
    }

    public void publier() {
        if (estExpiree()) {
            throw new DateLimiteCandidatureInvalideException(
                    "Impossible de publier une offre dont la date limite de candidature est dépassée");
        }
        if (this.statut == StatutOpportunite.OUVERT) {
            return;
        }
        this.statut = StatutOpportunite.OUVERT;
        markUpdated();
    }

    public void mettreEnCours() {
        if (this.statut == StatutOpportunite.EN_COURS) {
            return;
        }
        this.statut = StatutOpportunite.EN_COURS;
        markUpdated();
    }

    public void cloturer() {
        if (this.statut == StatutOpportunite.CLOTURE) {
            return;
        }
        this.statut = StatutOpportunite.CLOTURE;
        markUpdated();
    }

    public void remettreEnBrouillon() {
        if (this.statut == StatutOpportunite.BROUILLON) {
            return;
        }
        this.statut = StatutOpportunite.BROUILLON;
        markUpdated();
    }

    public boolean estExpiree() {
        return dateLimiteCandidature != null && dateLimiteCandidature.isBefore(LocalDate.now());
    }

    /**
     * Statut réellement applicable, tenant compte de la clôture automatique
     * par dépassement de date — toujours à utiliser en lecture (assembleur,
     * règle métier d'acceptation d'une candidature) plutôt que
     * {@link #getStatut()} qui reflète l'état persisté.
     */
    public StatutOpportunite getStatutEffectif() {
        if ((statut == StatutOpportunite.OUVERT || statut == StatutOpportunite.EN_COURS) && estExpiree()) {
            return StatutOpportunite.CLOTURE;
        }
        return statut;
    }

    public boolean accepteCandidatures() {
        return getStatutEffectif() == StatutOpportunite.OUVERT;
    }

    public boolean estVisiblePubliquement() {
        StatutOpportunite effectif = getStatutEffectif();
        return effectif == StatutOpportunite.OUVERT || effectif == StatutOpportunite.EN_COURS;
    }

    // ── Validation ───────────────────────────────────────────────────────

    private static String validerTitre(String titre) {
        Objects.requireNonNull(titre, "Le titre de l'offre ne peut pas être null");
        String trimmed = titre.trim();
        if (trimmed.isBlank()) {
            throw new IllegalArgumentException("Le titre de l'offre ne peut pas être vide");
        }
        if (trimmed.length() > TITRE_MAX_LENGTH) {
            throw new IllegalArgumentException("Le titre ne peut pas dépasser " + TITRE_MAX_LENGTH + " caractères");
        }
        return trimmed;
    }

    private static String validerNomEntreprise(String nomEntreprise) {
        Objects.requireNonNull(nomEntreprise, "Le nom de l'entreprise / structure ne peut pas être null");
        String trimmed = nomEntreprise.trim();
        if (trimmed.isBlank()) {
            throw new IllegalArgumentException("Le nom de l'entreprise / structure ne peut pas être vide");
        }
        if (trimmed.length() > ENTREPRISE_MAX_LENGTH) {
            throw new IllegalArgumentException(
                    "Le nom de l'entreprise ne peut pas dépasser " + ENTREPRISE_MAX_LENGTH + " caractères");
        }
        return trimmed;
    }

    private static String validerDescription(String description) {
        Objects.requireNonNull(description, "La description du poste ne peut pas être null");
        String trimmed = description.trim();
        if (trimmed.isBlank()) {
            throw new IllegalArgumentException("La description du poste ne peut pas être vide");
        }
        if (trimmed.length() > DESCRIPTION_MAX_LENGTH) {
            throw new IllegalArgumentException(
                    "La description ne peut pas dépasser " + DESCRIPTION_MAX_LENGTH + " caractères");
        }
        return trimmed;
    }

    private static String validerLieu(String lieu) {
        Objects.requireNonNull(lieu, "Le lieu / la localisation est obligatoire");
        String trimmed = lieu.trim();
        if (trimmed.isBlank()) {
            throw new IllegalArgumentException("Le lieu / la localisation ne peut pas être vide");
        }
        if (trimmed.length() > LIEU_MAX_LENGTH) {
            throw new IllegalArgumentException("Le lieu ne peut pas dépasser " + LIEU_MAX_LENGTH + " caractères");
        }
        return trimmed;
    }

    private static String validerAuteurNom(String auteurNom) {
        Objects.requireNonNull(auteurNom, "Le nom de l'auteur est obligatoire");
        String trimmed = auteurNom.trim();
        if (trimmed.isBlank()) {
            throw new IllegalArgumentException("Le nom de l'auteur ne peut pas être vide");
        }
        return trimmed;
    }

    private static String validerUrl(String url, String libelle) {
        if (url == null || url.isBlank()) {
            return null;
        }
        String trimmed = url.trim();
        if (trimmed.length() > URL_MAX_LENGTH) {
            throw new IllegalArgumentException(
                    "L'URL de la " + libelle + " ne peut pas dépasser " + URL_MAX_LENGTH + " caractères");
        }
        return trimmed;
    }

    private static LocalDate validerDates(LocalDate dateDebut, LocalDate dateLimiteCandidature) {
        Objects.requireNonNull(dateLimiteCandidature, "La date limite de candidature est obligatoire");
        if (dateDebut != null && dateLimiteCandidature.isAfter(dateDebut)) {
            throw new DateLimiteCandidatureInvalideException(
                    "La date limite de candidature doit être antérieure ou égale à la date de début prévue");
        }
        return dateLimiteCandidature;
    }

    // ── Accesseurs ───────────────────────────────────────────────────────

    public String getTitre() {
        return titre;
    }

    public String getNomEntreprise() {
        return nomEntreprise;
    }

    public String getDescription() {
        return description;
    }

    public String getFicheDePosteUrl() {
        return ficheDePosteUrl;
    }

    public String getLieu() {
        return lieu;
    }

    public TypeContrat getTypeContrat() {
        return typeContrat;
    }

    public LocalDate getDateDebut() {
        return dateDebut;
    }

    public LocalDate getDateLimiteCandidature() {
        return dateLimiteCandidature;
    }

    public UUID getAuteurId() {
        return auteurId;
    }

    public String getAuteurNom() {
        return auteurNom;
    }

    public String getEmailContact() {
        return emailContact;
    }

    /**
     * Statut persisté brut — préférer {@link #getStatutEffectif()} pour
     * toute présentation à l'utilisateur ou décision métier.
     */
    public StatutOpportunite getStatut() {
        return statut;
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
