package ministere.sante.senpna.commandeachat.infrastructure.persistence.mapper;

import ministere.sante.senpna.commandeachat.domain.model.Facture;
import ministere.sante.senpna.commandeachat.domain.valueobject.StatutFacture;
import ministere.sante.senpna.commandeachat.infrastructure.persistence.entity.FactureJpaEntity;
import ministere.sante.senpna.fournisseur.domain.valueobject.FournisseurId;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("FactureMapper — conversion domaine ↔ entité JPA")
class FactureMapperTest {

    FactureMapper sut = new FactureMapper();

    private Facture facture() {
        return Facture.soumettre(new Facture.SoumissionCommand(ministere.sante.senpna.commandeachat.domain.valueobject.CommandeAchatId.generate(),
                FournisseurId.generate(), "FAC-2026-0001", BigDecimal.valueOf(3_500_000), LocalDate.now(),
                LocalDate.now().plusDays(30), "media-1"));
    }

    @Test
    @DisplayName("toEntity() reporte fidèlement chaque champ")
    void toEntity_reporteChaqueChamp() {
        Facture facture = facture();

        FactureJpaEntity entity = sut.toEntity(facture);

        assertThat(entity.getId()).isEqualTo(facture.getId().getValue());
        assertThat(entity.getCommandeAchatId()).isEqualTo(facture.getCommandeAchatId().getValue());
        assertThat(entity.getFournisseurId()).isEqualTo(facture.getFournisseurId().getValue());
        assertThat(entity.getNumeroFacture()).isEqualTo("FAC-2026-0001");
        assertThat(entity.getMontant()).isEqualByComparingTo("3500000");
        assertThat(entity.getStatut()).isEqualTo(StatutFacture.SOUMISE);
        assertThat(entity.getPieceJointeMediaId()).isEqualTo("media-1");
        assertThat(entity.getCreatedAt()).isEqualTo(facture.getCreatedAt());
    }

    @Test
    @DisplayName("toDomain() reconstruit fidèlement l'agrégat")
    void toDomain_reconstruitFidelement() {
        UUID factureId = UUID.randomUUID();
        UUID commandeAchatId = UUID.randomUUID();
        UUID fournisseurId = UUID.randomUUID();
        Instant createdAt = Instant.now().minusSeconds(3600);
        Instant updatedAt = Instant.now();

        FactureJpaEntity entity = FactureJpaEntity.builder()
            .id(factureId)
            .commandeAchatId(commandeAchatId)
            .fournisseurId(fournisseurId)
            .numeroFacture("FAC-2026-0099")
            .montant(BigDecimal.TEN)
            .dateEmission(LocalDate.now())
            .dateEcheance(LocalDate.now().plusDays(15))
            .pieceJointeMediaId("media-2")
            .statut(StatutFacture.VALIDEE)
            .motifRejet(null)
            .build();
        entity.setCreatedAt(createdAt);
        entity.setUpdatedAt(updatedAt);

        Facture facture = sut.toDomain(entity);

        assertThat(facture.getId().getValue()).isEqualTo(factureId);
        assertThat(facture.getCommandeAchatId().getValue()).isEqualTo(commandeAchatId);
        assertThat(facture.getFournisseurId().getValue()).isEqualTo(fournisseurId);
        assertThat(facture.getNumeroFacture()).isEqualTo("FAC-2026-0099");
        assertThat(facture.getStatut()).isEqualTo(StatutFacture.VALIDEE);
        assertThat(facture.getCreatedAt()).isEqualTo(createdAt);
        assertThat(facture.getUpdatedAt()).isEqualTo(updatedAt);
    }

    @Test
    @DisplayName("toDomain() reporte le motif de rejet quand présent")
    void toDomain_reporteMotifRejet() {
        FactureJpaEntity entity = FactureJpaEntity.builder()
            .id(UUID.randomUUID())
            .commandeAchatId(UUID.randomUUID())
            .fournisseurId(UUID.randomUUID())
            .numeroFacture("FAC-1")
            .montant(BigDecimal.TEN)
            .dateEmission(LocalDate.now())
            .dateEcheance(null)
            .pieceJointeMediaId(null)
            .statut(StatutFacture.REJETEE)
            .motifRejet("Montant incohérent")
            .build();
        entity.setCreatedAt(Instant.now());
        entity.setUpdatedAt(Instant.now());

        Facture facture = sut.toDomain(entity);

        assertThat(facture.getMotifRejet()).isEqualTo("Montant incohérent");
    }

    @Test
    @DisplayName("aller-retour toEntity() puis toDomain() préserve l'état")
    void allerRetour_preserveEtat() {
        Facture original = facture();

        Facture restauree = sut.toDomain(sut.toEntity(original));

        assertThat(restauree.getId()).isEqualTo(original.getId());
        assertThat(restauree.getNumeroFacture()).isEqualTo(original.getNumeroFacture());
        assertThat(restauree.getMontant()).isEqualByComparingTo(original.getMontant());
        assertThat(restauree.getStatut()).isEqualTo(original.getStatut());
    }
}
