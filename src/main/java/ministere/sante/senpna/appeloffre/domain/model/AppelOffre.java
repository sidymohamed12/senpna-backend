package ministere.sante.senpna.appeloffre.domain.model;

import ministere.sante.senpna.appeloffre.domain.exception.TransitionStatutAppelOffreInvalideException;
import ministere.sante.senpna.appeloffre.domain.valueobject.AppelOffreId;
import ministere.sante.senpna.appeloffre.domain.valueobject.StatutAppelOffre;
import ministere.sante.senpna.config.AppClock;
import ministere.sante.senpna.shared.domain.model.AggregateRoot;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Appel d'offres — mise en concurrence des fournisseurs par la PNA pour un
 * besoin en médicaments (cf. doc. métier §b, flows CAS 1).
 *
 * <p>
 * Racine d'agrégat englobant ses {@link LigneAppelOffre} : les lignes ne
 * sont jamais manipulées en dehors de cet agrégat, garantissant qu'un
 * appel d'offres publié possède toujours au moins une ligne cohérente.
 * </p>
 *
 * <h3>Invariants</h3>
 * <ul>
 * <li>un appel d'offres doit posséder au moins une ligne pour être
 * publié ;</li>
 * <li>la date de clôture doit être future à la publication ;</li>
 * <li>seul un appel d'offres {@code BROUILLON} peut être publié, seul un
 * appel d'offres {@code PUBLIE} peut être clôturé, seul un appel
 * d'offres {@code CLOTURE} peut être attribué ;</li>
 * <li>un appel d'offres {@code ATTRIBUE} ou {@code ANNULE} est dans un
 * état terminal — plus aucune transition n'est permise.</li>
 * </ul>
 */
public class AppelOffre extends AggregateRoot<AppelOffreId> {

    private static final int REFERENCE_MAX_LENGTH = 50;
    private static final int OBJET_MAX_LENGTH = 255;

    private String reference;
    private String objet;
    private LocalDate dateCloture;
    private StatutAppelOffre statut;
    private final List<LigneAppelOffre> lignes;

    private AppelOffre(Builder builder) {
        super(builder.id, builder.createdAt, builder.updatedAt);
        this.reference = validerReference(builder.reference);
        this.objet = validerObjet(builder.objet);
        this.dateCloture = Objects.requireNonNull(builder.dateCloture, "La date de clôture est obligatoire");
        this.statut = Objects.requireNonNull(builder.statut, "Le statut est obligatoire");
        this.lignes = new ArrayList<>(Objects.requireNonNull(builder.lignes, "Les lignes ne peuvent pas être null"));
    }

    /** Données nécessaires à la création d'un nouvel appel d'offres. */
    public record CreationCommand(String reference, String objet, LocalDate dateCloture,
            List<LigneAppelOffre> lignes) {
    }

    public static AppelOffre creer(CreationCommand command) {
        if (command.lignes() == null || command.lignes().isEmpty()) {
            throw new IllegalArgumentException("Un appel d'offres doit contenir au moins une ligne");
        }
        if (!command.dateCloture().isAfter(LocalDate.now(AppClock.APP_ZONE))) {
            throw new IllegalArgumentException("La date de clôture doit être future");
        }
        Instant maintenant = Instant.now();
        return builder()
                .id(AppelOffreId.generate())
                .reference(command.reference())
                .objet(command.objet())
                .dateCloture(command.dateCloture())
                .statut(StatutAppelOffre.BROUILLON)
                .lignes(command.lignes())
                .createdAt(maintenant)
                .updatedAt(maintenant)
                .build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private AppelOffreId id;
        private String reference;
        private String objet;
        private LocalDate dateCloture;
        private StatutAppelOffre statut;
        private List<LigneAppelOffre> lignes;
        private Instant createdAt;
        private Instant updatedAt;

        private Builder() {
        }

        public Builder id(AppelOffreId id) {
            this.id = id;
            return this;
        }

        public Builder reference(String reference) {
            this.reference = reference;
            return this;
        }

        public Builder objet(String objet) {
            this.objet = objet;
            return this;
        }

        public Builder dateCloture(LocalDate dateCloture) {
            this.dateCloture = dateCloture;
            return this;
        }

        public Builder statut(StatutAppelOffre statut) {
            this.statut = statut;
            return this;
        }

        public Builder lignes(List<LigneAppelOffre> lignes) {
            this.lignes = lignes;
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

        public AppelOffre build() {
            return new AppelOffre(this);
        }
    }

    // ── Comportements métier ────────────────────────────────────────────

    public void publier() {
        exigerStatut(StatutAppelOffre.BROUILLON, "publier");
        if (lignes.isEmpty()) {
            throw new IllegalStateException("Un appel d'offres sans ligne ne peut pas être publié");
        }
        if (!dateCloture.isAfter(LocalDate.now(AppClock.APP_ZONE))) {
            throw new IllegalStateException("La date de clôture doit être future pour publier l'appel d'offres");
        }
        this.statut = StatutAppelOffre.PUBLIE;
        markUpdated();
    }

    /**
     * Clôture l'appel d'offres — soit manuellement par la PNA, soit
     * automatiquement (scheduler) une fois {@code dateCloture} dépassée.
     * Plus aucune offre ne peut être soumise ou retirée après clôture.
     */
    public void cloturer() {
        exigerStatut(StatutAppelOffre.PUBLIE, "clôturer");
        this.statut = StatutAppelOffre.CLOTURE;
        markUpdated();
    }

    public void attribuer() {
        exigerStatut(StatutAppelOffre.CLOTURE, "attribuer");
        this.statut = StatutAppelOffre.ATTRIBUE;
        markUpdated();
    }

    public void annuler() {
        if (statut == StatutAppelOffre.ATTRIBUE || statut == StatutAppelOffre.ANNULE) {
            throw new TransitionStatutAppelOffreInvalideException(statut, "annuler");
        }
        this.statut = StatutAppelOffre.ANNULE;
        markUpdated();
    }

    public boolean estOuvertALaSoumission() {
        return statut == StatutAppelOffre.PUBLIE
                && dateCloture.isAfter(LocalDate.now(AppClock.APP_ZONE).minusDays(1));
    }

    public boolean dateClotureDepassee() {
        return !dateCloture.isAfter(LocalDate.now(AppClock.APP_ZONE));
    }

    private void exigerStatut(StatutAppelOffre attendu, String action) {
        if (this.statut != attendu) {
            throw new TransitionStatutAppelOffreInvalideException(this.statut, action);
        }
    }

    // ── Accesseurs ───────────────────────────────────────────────────────

    public String getReference() {
        return reference;
    }

    public String getObjet() {
        return objet;
    }

    public LocalDate getDateCloture() {
        return dateCloture;
    }

    public StatutAppelOffre getStatut() {
        return statut;
    }

    public List<LigneAppelOffre> getLignes() {
        return Collections.unmodifiableList(lignes);
    }

    private static String validerReference(String reference) {
        Objects.requireNonNull(reference, "La référence ne peut pas être null");
        String trimmed = reference.trim();
        if (trimmed.isBlank()) {
            throw new IllegalArgumentException("La référence ne peut pas être vide");
        }
        if (trimmed.length() > REFERENCE_MAX_LENGTH) {
            throw new IllegalArgumentException(
                    "La référence ne peut pas dépasser " + REFERENCE_MAX_LENGTH + " caractères");
        }
        return trimmed;
    }

    private static String validerObjet(String objet) {
        Objects.requireNonNull(objet, "L'objet ne peut pas être null");
        String trimmed = objet.trim();
        if (trimmed.isBlank()) {
            throw new IllegalArgumentException("L'objet ne peut pas être vide");
        }
        if (trimmed.length() > OBJET_MAX_LENGTH) {
            throw new IllegalArgumentException("L'objet ne peut pas dépasser " + OBJET_MAX_LENGTH + " caractères");
        }
        return trimmed;
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
