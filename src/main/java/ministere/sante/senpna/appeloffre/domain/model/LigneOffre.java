package ministere.sante.senpna.appeloffre.domain.model;

import ministere.sante.senpna.appeloffre.domain.valueobject.LigneAppelOffreId;
import ministere.sante.senpna.appeloffre.domain.valueobject.LigneOffreId;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Ligne de prix proposée par un fournisseur, en réponse à une
 * {@link LigneAppelOffre} précise de l'appel d'offres (cf. doc. métier §b
 * « Laboratoire A / Produit / Prix »).
 */
public class LigneOffre {

    private final LigneOffreId id;
    private final LigneAppelOffreId ligneAppelOffreId;
    private final BigDecimal prixUnitaire;
    private final Integer delaiLivraisonJours;

    private LigneOffre(LigneOffreId id, LigneAppelOffreId ligneAppelOffreId, BigDecimal prixUnitaire,
            Integer delaiLivraisonJours) {
        this.id = Objects.requireNonNull(id, "L'identifiant de la ligne d'offre est obligatoire");
        this.ligneAppelOffreId = Objects.requireNonNull(ligneAppelOffreId,
                "La ligne d'appel d'offres référencée est obligatoire");
        this.prixUnitaire = validerPrix(prixUnitaire);
        this.delaiLivraisonJours = validerDelai(delaiLivraisonJours);
    }

    public static LigneOffre reconstruct(LigneOffreId id, LigneAppelOffreId ligneAppelOffreId,
            BigDecimal prixUnitaire, Integer delaiLivraisonJours) {
        return new LigneOffre(id, ligneAppelOffreId, prixUnitaire, delaiLivraisonJours);
    }

    public static LigneOffre creer(LigneAppelOffreId ligneAppelOffreId, BigDecimal prixUnitaire,
            Integer delaiLivraisonJours) {
        return new LigneOffre(LigneOffreId.generate(), ligneAppelOffreId, prixUnitaire, delaiLivraisonJours);
    }

    private static BigDecimal validerPrix(BigDecimal prix) {
        Objects.requireNonNull(prix, "Le prix unitaire proposé est obligatoire");
        if (prix.signum() <= 0) {
            throw new IllegalArgumentException("Le prix unitaire proposé doit être strictement positif");
        }
        return prix;
    }

    private static Integer validerDelai(Integer delai) {
        Objects.requireNonNull(delai, "Le délai de livraison proposé est obligatoire");
        if (delai <= 0) {
            throw new IllegalArgumentException("Le délai de livraison proposé doit être strictement positif");
        }
        return delai;
    }

    public LigneOffreId getId() {
        return id;
    }

    public LigneAppelOffreId getLigneAppelOffreId() {
        return ligneAppelOffreId;
    }

    public BigDecimal getPrixUnitaire() {
        return prixUnitaire;
    }

    public Integer getDelaiLivraisonJours() {
        return delaiLivraisonJours;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof LigneOffre other))
            return false;
        return Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
