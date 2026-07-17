package ministere.sante.senpna.commandeachat.domain.model;

import ministere.sante.senpna.commandeachat.domain.valueobject.LigneCommandeAchatId;
import ministere.sante.senpna.medicament.domain.valueobject.ConditionnementId;
import ministere.sante.senpna.medicament.domain.valueobject.MedicamentId;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Ligne d'un bon de commande d'achat fournisseur.
 *
 * <p>
 * Porte, en plus de la quantité et du prix négociés, les informations de
 * livraison renseignées par le fournisseur au moment de l'expédition
 * (numéro de lot, dates de fabrication/péremption, certificat d'analyse —
 * cf. doc. métier « Suivi des Livraisons ») ainsi que les quantités
 * effectivement réceptionnées par la PNA.
 * </p>
 */
public class LigneCommandeAchat {

    private final LigneCommandeAchatId id;
    private final MedicamentId medicamentId;
    private final ConditionnementId conditionnementId;
    private final BigDecimal quantiteCommandee;
    private final BigDecimal prixUnitaire;

    // ── Renseignées à l'expédition (cf. genererAvisExpedition) ──────────
    private String numeroLot;
    private LocalDate dateFabrication;
    private LocalDate dateExpiration;
    private String certificatAnalyseUrl;
    private BigDecimal quantiteExpediee;

    // ── Renseignées à la réception (cf. receptionner) ────────────────────
    private BigDecimal quantiteRecue;
    private BigDecimal quantiteRefusee;
    private String motifRefus;

    private LigneCommandeAchat(Builder builder) {
        this.id = Objects.requireNonNull(builder.id, "L'identifiant de la ligne est obligatoire");
        this.medicamentId = Objects.requireNonNull(builder.medicamentId, "Le médicament est obligatoire");
        this.conditionnementId = Objects.requireNonNull(builder.conditionnementId,
                "Le conditionnement est obligatoire");
        this.quantiteCommandee = validerQuantitePositive(builder.quantiteCommandee, "La quantité commandée");
        this.prixUnitaire = validerQuantitePositive(builder.prixUnitaire, "Le prix unitaire");
        this.numeroLot = builder.numeroLot;
        this.dateFabrication = builder.dateFabrication;
        this.dateExpiration = builder.dateExpiration;
        this.certificatAnalyseUrl = builder.certificatAnalyseUrl;
        this.quantiteExpediee = builder.quantiteExpediee;
        this.quantiteRecue = builder.quantiteRecue != null ? builder.quantiteRecue : BigDecimal.ZERO;
        this.quantiteRefusee = builder.quantiteRefusee != null ? builder.quantiteRefusee : BigDecimal.ZERO;
        this.motifRefus = builder.motifRefus;
    }

    /** Données nécessaires à la création d'une nouvelle ligne de commande d'achat. */
    public record CreationCommand(MedicamentId medicamentId, ConditionnementId conditionnementId,
            BigDecimal quantiteCommandee, BigDecimal prixUnitaire) {
    }

    public static LigneCommandeAchat creer(CreationCommand command) {
        return builder()
                .id(LigneCommandeAchatId.generate())
                .medicamentId(command.medicamentId())
                .conditionnementId(command.conditionnementId())
                .quantiteCommandee(command.quantiteCommandee())
                .prixUnitaire(command.prixUnitaire())
                .quantiteRecue(BigDecimal.ZERO)
                .quantiteRefusee(BigDecimal.ZERO)
                .build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private LigneCommandeAchatId id;
        private MedicamentId medicamentId;
        private ConditionnementId conditionnementId;
        private BigDecimal quantiteCommandee;
        private BigDecimal prixUnitaire;
        private String numeroLot;
        private LocalDate dateFabrication;
        private LocalDate dateExpiration;
        private String certificatAnalyseUrl;
        private BigDecimal quantiteExpediee;
        private BigDecimal quantiteRecue;
        private BigDecimal quantiteRefusee;
        private String motifRefus;

        private Builder() {
        }

        public Builder id(LigneCommandeAchatId id) {
            this.id = id;
            return this;
        }

        public Builder medicamentId(MedicamentId medicamentId) {
            this.medicamentId = medicamentId;
            return this;
        }

        public Builder conditionnementId(ConditionnementId conditionnementId) {
            this.conditionnementId = conditionnementId;
            return this;
        }

        public Builder quantiteCommandee(BigDecimal quantiteCommandee) {
            this.quantiteCommandee = quantiteCommandee;
            return this;
        }

        public Builder prixUnitaire(BigDecimal prixUnitaire) {
            this.prixUnitaire = prixUnitaire;
            return this;
        }

        public Builder numeroLot(String numeroLot) {
            this.numeroLot = numeroLot;
            return this;
        }

        public Builder dateFabrication(LocalDate dateFabrication) {
            this.dateFabrication = dateFabrication;
            return this;
        }

        public Builder dateExpiration(LocalDate dateExpiration) {
            this.dateExpiration = dateExpiration;
            return this;
        }

        public Builder certificatAnalyseUrl(String certificatAnalyseUrl) {
            this.certificatAnalyseUrl = certificatAnalyseUrl;
            return this;
        }

        public Builder quantiteExpediee(BigDecimal quantiteExpediee) {
            this.quantiteExpediee = quantiteExpediee;
            return this;
        }

        public Builder quantiteRecue(BigDecimal quantiteRecue) {
            this.quantiteRecue = quantiteRecue;
            return this;
        }

        public Builder quantiteRefusee(BigDecimal quantiteRefusee) {
            this.quantiteRefusee = quantiteRefusee;
            return this;
        }

        public Builder motifRefus(String motifRefus) {
            this.motifRefus = motifRefus;
            return this;
        }

        public LigneCommandeAchat build() {
            return new LigneCommandeAchat(this);
        }
    }

    // ── Comportements métier ────────────────────────────────────────────

    /** Renseigne les informations de livraison — appelé par l'agrégat à l'expédition. */
    void renseignerExpedition(String numeroLot, LocalDate dateFabrication, LocalDate dateExpiration,
            String certificatAnalyseUrl, BigDecimal quantiteExpediee) {
        Objects.requireNonNull(numeroLot, "Le numéro de lot est obligatoire");
        Objects.requireNonNull(dateExpiration, "La date de péremption est obligatoire");
        if (dateFabrication != null && dateFabrication.isAfter(dateExpiration)) {
            throw new IllegalArgumentException("La date de fabrication doit précéder la date de péremption");
        }
        BigDecimal qte = validerQuantitePositive(quantiteExpediee, "La quantité expédiée");
        if (qte.compareTo(quantiteCommandee) > 0) {
            throw new IllegalArgumentException("La quantité expédiée ne peut pas excéder la quantité commandée");
        }
        this.numeroLot = numeroLot;
        this.dateFabrication = dateFabrication;
        this.dateExpiration = dateExpiration;
        this.certificatAnalyseUrl = certificatAnalyseUrl;
        this.quantiteExpediee = qte;
    }

    /** Cumule une réception (totale ou partielle) — appelé par l'agrégat à la réception. */
    void receptionner(BigDecimal qteRecue, BigDecimal qteRefusee, String motifRefus) {
        BigDecimal recue = qteRecue != null ? qteRecue : BigDecimal.ZERO;
        BigDecimal refusee = qteRefusee != null ? qteRefusee : BigDecimal.ZERO;
        if (recue.signum() < 0 || refusee.signum() < 0) {
            throw new IllegalArgumentException("Les quantités réceptionnées ne peuvent pas être négatives");
        }
        BigDecimal expedieeReference = quantiteExpediee != null ? quantiteExpediee : quantiteCommandee;
        BigDecimal totalApresReception = this.quantiteRecue.add(this.quantiteRefusee).add(recue).add(refusee);
        if (totalApresReception.compareTo(expedieeReference) > 0) {
            throw new IllegalArgumentException(
                    "Les quantités réceptionnées ne peuvent pas excéder les quantités expédiées");
        }
        this.quantiteRecue = this.quantiteRecue.add(recue);
        this.quantiteRefusee = this.quantiteRefusee.add(refusee);
        if (motifRefus != null) {
            this.motifRefus = motifRefus;
        }
    }

    /** @return {@code true} si la ligne est complètement réceptionnée (reçue + refusée = expédiée). */
    boolean estCompletementReceptionnee() {
        BigDecimal expedieeReference = quantiteExpediee != null ? quantiteExpediee : quantiteCommandee;
        return quantiteRecue.add(quantiteRefusee).compareTo(expedieeReference) >= 0;
    }

    private static BigDecimal validerQuantitePositive(BigDecimal valeur, String libelle) {
        Objects.requireNonNull(valeur, libelle + " est obligatoire");
        if (valeur.signum() <= 0) {
            throw new IllegalArgumentException(libelle + " doit être strictement positive");
        }
        return valeur;
    }

    // ── Accesseurs ───────────────────────────────────────────────────────

    public LigneCommandeAchatId getId() {
        return id;
    }

    public MedicamentId getMedicamentId() {
        return medicamentId;
    }

    public ConditionnementId getConditionnementId() {
        return conditionnementId;
    }

    public BigDecimal getQuantiteCommandee() {
        return quantiteCommandee;
    }

    public BigDecimal getPrixUnitaire() {
        return prixUnitaire;
    }

    public String getNumeroLot() {
        return numeroLot;
    }

    public LocalDate getDateFabrication() {
        return dateFabrication;
    }

    public LocalDate getDateExpiration() {
        return dateExpiration;
    }

    public String getCertificatAnalyseUrl() {
        return certificatAnalyseUrl;
    }

    public BigDecimal getQuantiteExpediee() {
        return quantiteExpediee;
    }

    public BigDecimal getQuantiteRecue() {
        return quantiteRecue;
    }

    public BigDecimal getQuantiteRefusee() {
        return quantiteRefusee;
    }

    public String getMotifRefus() {
        return motifRefus;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof LigneCommandeAchat other))
            return false;
        return Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
