package ministere.sante.senpna.commandeachat.domain.model;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Avis d'expédition émis par le fournisseur (cf. doc. métier « Suivi des
 * Livraisons » et « Réception des Commandes d'Achat : génération de
 * l'avis d'expédition »). Value object immuable — une fois émis, l'avis
 * n'est jamais modifié ; toute correction transite par une nouvelle
 * commande ou un ajustement de stock une fois réceptionné.
 */
public final class AvisExpedition {

    private final LocalDate dateExpedition;
    private final String transporteur;
    private final String numeroSuivi;
    private final LocalDate dateLivraisonEstimee;

    private AvisExpedition(LocalDate dateExpedition, String transporteur, String numeroSuivi,
            LocalDate dateLivraisonEstimee) {
        this.dateExpedition = Objects.requireNonNull(dateExpedition, "La date d'expédition est obligatoire");
        this.transporteur = transporteur;
        this.numeroSuivi = numeroSuivi;
        this.dateLivraisonEstimee = dateLivraisonEstimee;
        if (dateLivraisonEstimee != null && dateLivraisonEstimee.isBefore(dateExpedition)) {
            throw new IllegalArgumentException(
                    "La date de livraison estimée ne peut pas précéder la date d'expédition");
        }
    }

    public static AvisExpedition of(LocalDate dateExpedition, String transporteur, String numeroSuivi,
            LocalDate dateLivraisonEstimee) {
        return new AvisExpedition(dateExpedition, transporteur, numeroSuivi, dateLivraisonEstimee);
    }

    public LocalDate getDateExpedition() {
        return dateExpedition;
    }

    public String getTransporteur() {
        return transporteur;
    }

    public String getNumeroSuivi() {
        return numeroSuivi;
    }

    public LocalDate getDateLivraisonEstimee() {
        return dateLivraisonEstimee;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof AvisExpedition other))
            return false;
        return Objects.equals(dateExpedition, other.dateExpedition)
                && Objects.equals(transporteur, other.transporteur)
                && Objects.equals(numeroSuivi, other.numeroSuivi)
                && Objects.equals(dateLivraisonEstimee, other.dateLivraisonEstimee);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dateExpedition, transporteur, numeroSuivi, dateLivraisonEstimee);
    }
}
