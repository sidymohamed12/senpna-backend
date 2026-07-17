package ministere.sante.senpna.fournisseur.domain.model;

import ministere.sante.senpna.fournisseur.domain.valueobject.FournisseurId;
import ministere.sante.senpna.shared.domain.model.AggregateRoot;

import java.time.Instant;
import java.util.Objects;

public class Fournisseur extends AggregateRoot<FournisseurId> {

    private static final int NOM_MAX_LENGTH = 150;

    private String nom;
    private String adresse;
    private String telephone;
    private String email;
    private String contactPrincipal;
    private boolean actif;

    private Fournisseur(Builder builder) {
        super(builder.id, builder.createdAt, builder.updatedAt);
        this.nom = validerNom(builder.nom);
        this.adresse = builder.adresse;
        this.telephone = builder.telephone;
        this.email = builder.email;
        this.contactPrincipal = builder.contactPrincipal;
        this.actif = builder.actif;
    }

    /** Données nécessaires à la création d'un nouveau fournisseur. */
    public record CreationCommand(String nom, String adresse, String telephone, String email,
            String contactPrincipal) {
    }

    public static Fournisseur creer(CreationCommand command) {
        Instant maintenant = Instant.now();
        return builder()
                .id(FournisseurId.generate())
                .nom(command.nom())
                .adresse(command.adresse())
                .telephone(command.telephone())
                .email(command.email())
                .contactPrincipal(command.contactPrincipal())
                .actif(true)
                .createdAt(maintenant)
                .updatedAt(maintenant)
                .build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private FournisseurId id;
        private String nom;
        private String adresse;
        private String telephone;
        private String email;
        private String contactPrincipal;
        private boolean actif;
        private Instant createdAt;
        private Instant updatedAt;

        private Builder() {
        }

        public Builder id(FournisseurId id) {
            this.id = id;
            return this;
        }

        public Builder nom(String nom) {
            this.nom = nom;
            return this;
        }

        public Builder adresse(String adresse) {
            this.adresse = adresse;
            return this;
        }

        public Builder telephone(String telephone) {
            this.telephone = telephone;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder contactPrincipal(String contactPrincipal) {
            this.contactPrincipal = contactPrincipal;
            return this;
        }

        public Builder actif(boolean actif) {
            this.actif = actif;
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

        public Fournisseur build() {
            return new Fournisseur(this);
        }
    }

    // ── Comportements métier ────────────────────────────────────────────

    public void modifierInformations(String nom, String adresse, String telephone, String email,
            String contactPrincipal) {
        this.nom = validerNom(nom);
        this.adresse = adresse;
        this.telephone = telephone;
        this.email = email;
        this.contactPrincipal = contactPrincipal;
        markUpdated();
    }

    public void activer() {
        if (this.actif) {
            return;
        }
        this.actif = true;
        markUpdated();
    }

    public void desactiver() {
        if (!this.actif) {
            return;
        }
        this.actif = false;
        markUpdated();
    }

    // ── Validation ───────────────────────────────────────────────────────

    private static String validerNom(String nom) {
        Objects.requireNonNull(nom, "Le nom du fournisseur ne peut pas être null");
        String trimmed = nom.trim();
        if (trimmed.isBlank()) {
            throw new IllegalArgumentException("Le nom du fournisseur ne peut pas être vide");
        }
        if (trimmed.length() > NOM_MAX_LENGTH) {
            throw new IllegalArgumentException(
                    "Le nom du fournisseur ne peut pas dépasser " + NOM_MAX_LENGTH + " caractères");
        }
        return trimmed;
    }

    // ── Accesseurs ───────────────────────────────────────────────────────

    public String getNom() {
        return nom;
    }

    public String getAdresse() {
        return adresse;
    }

    public String getTelephone() {
        return telephone;
    }

    public String getEmail() {
        return email;
    }

    public String getContactPrincipal() {
        return contactPrincipal;
    }

    public boolean isActif() {
        return actif;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + Objects.hashCode(nom);
        result = prime * result + Objects.hashCode(adresse);
        result = prime * result + Objects.hashCode(telephone);
        result = prime * result + Objects.hashCode(email);
        result = prime * result + Objects.hashCode(contactPrincipal);
        result = prime * result + (actif ? 1231 : 1237);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        Fournisseur other = (Fournisseur) obj;
        return actif == other.actif
                && Objects.equals(nom, other.nom)
                && Objects.equals(adresse, other.adresse)
                && Objects.equals(telephone, other.telephone)
                && Objects.equals(email, other.email)
                && Objects.equals(contactPrincipal, other.contactPrincipal);
    }
}
