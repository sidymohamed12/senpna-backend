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

    private Fournisseur(FournisseurId id, String nom, String adresse, String telephone, String email,
            String contactPrincipal, boolean actif, Instant createdAt, Instant updatedAt) {
        super(id, createdAt, updatedAt);
        this.nom = validerNom(nom);
        this.adresse = adresse;
        this.telephone = telephone;
        this.email = email;
        this.contactPrincipal = contactPrincipal;
        this.actif = actif;
    }

    public static Fournisseur reconstruct(FournisseurId id, String nom, String adresse, String telephone,
            String email, String contactPrincipal, boolean actif, Instant createdAt, Instant updatedAt) {
        return new Fournisseur(id, nom, adresse, telephone, email, contactPrincipal, actif, createdAt, updatedAt);
    }

    public static Fournisseur creer(String nom, String adresse, String telephone, String email,
            String contactPrincipal) {
        Instant maintenant = Instant.now();
        return new Fournisseur(FournisseurId.generate(), nom, adresse, telephone, email, contactPrincipal, true,
                maintenant, maintenant);
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
