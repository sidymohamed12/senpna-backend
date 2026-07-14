package ministere.sante.senpna.appeloffre.domain.model;

import ministere.sante.senpna.appeloffre.domain.valueobject.LigneAppelOffreId;
import ministere.sante.senpna.medicament.domain.valueobject.MedicamentId;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Ligne d'un appel d'offres — un besoin exprimé par la PNA pour un
 * médicament donné (cf. doc. métier §b « Lancement d'un appel d'offres »).
 *
 * <p>
 * Entité interne à l'agrégat {@link AppelOffre} : sa cohérence (quantité
 * positive, médicament renseigné) est garantie par son constructeur, mais
 * son cycle de vie (ajout/suppression) est entièrement piloté par
 * l'agrégat racine, seul point d'entrée autorisé en écriture.
 * </p>
 */
public class LigneAppelOffre {

    private final LigneAppelOffreId id;
    private final MedicamentId medicamentId;
    private final String designation;
    private final BigDecimal quantiteEstimee;
    private final String uniteBase;

    private LigneAppelOffre(LigneAppelOffreId id, MedicamentId medicamentId, String designation,
            BigDecimal quantiteEstimee, String uniteBase) {
        this.id = Objects.requireNonNull(id, "L'identifiant de la ligne est obligatoire");
        this.medicamentId = Objects.requireNonNull(medicamentId, "Le médicament est obligatoire");
        this.designation = validerDesignation(designation);
        this.quantiteEstimee = validerQuantite(quantiteEstimee);
        this.uniteBase = Objects.requireNonNull(uniteBase, "L'unité de base est obligatoire");
    }

    public static LigneAppelOffre reconstruct(LigneAppelOffreId id, MedicamentId medicamentId, String designation,
            BigDecimal quantiteEstimee, String uniteBase) {
        return new LigneAppelOffre(id, medicamentId, designation, quantiteEstimee, uniteBase);
    }

    public static LigneAppelOffre creer(MedicamentId medicamentId, String designation, BigDecimal quantiteEstimee,
            String uniteBase) {
        return new LigneAppelOffre(LigneAppelOffreId.generate(), medicamentId, designation, quantiteEstimee,
                uniteBase);
    }

    private static String validerDesignation(String designation) {
        Objects.requireNonNull(designation, "La désignation ne peut pas être null");
        String trimmed = designation.trim();
        if (trimmed.isBlank()) {
            throw new IllegalArgumentException("La désignation de la ligne ne peut pas être vide");
        }
        return trimmed;
    }

    private static BigDecimal validerQuantite(BigDecimal quantite) {
        Objects.requireNonNull(quantite, "La quantité estimée est obligatoire");
        if (quantite.signum() <= 0) {
            throw new IllegalArgumentException("La quantité estimée doit être strictement positive");
        }
        return quantite;
    }

    public LigneAppelOffreId getId() {
        return id;
    }

    public MedicamentId getMedicamentId() {
        return medicamentId;
    }

    public String getDesignation() {
        return designation;
    }

    public BigDecimal getQuantiteEstimee() {
        return quantiteEstimee;
    }

    public String getUniteBase() {
        return uniteBase;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof LigneAppelOffre other))
            return false;
        return Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
