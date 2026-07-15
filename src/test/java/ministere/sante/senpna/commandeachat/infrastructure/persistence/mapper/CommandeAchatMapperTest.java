package ministere.sante.senpna.commandeachat.infrastructure.persistence.mapper;

import ministere.sante.senpna.commandeachat.domain.model.AvisExpedition;
import ministere.sante.senpna.commandeachat.domain.model.CommandeAchat;
import ministere.sante.senpna.commandeachat.domain.model.LigneCommandeAchat;
import ministere.sante.senpna.commandeachat.domain.valueobject.StatutCommandeAchat;
import ministere.sante.senpna.commandeachat.infrastructure.persistence.entity.CommandeAchatJpaEntity;
import ministere.sante.senpna.commandeachat.infrastructure.persistence.entity.LigneCommandeAchatJpaEntity;
import ministere.sante.senpna.fournisseur.domain.valueobject.FournisseurId;
import ministere.sante.senpna.medicament.domain.valueobject.ConditionnementId;
import ministere.sante.senpna.medicament.domain.valueobject.MedicamentId;
import ministere.sante.senpna.organisation.domain.valueobject.EntrepotId;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("CommandeAchatMapper — conversion domaine ↔ entité JPA")
class CommandeAchatMapperTest {

    CommandeAchatMapper sut = new CommandeAchatMapper();

    private LigneCommandeAchat ligne() {
        return LigneCommandeAchat.creer(MedicamentId.generate(), ConditionnementId.generate(), BigDecimal.TEN,
                BigDecimal.valueOf(200_000));
    }

    private CommandeAchat commande() {
        return CommandeAchat.creer("BC-2026-0001", FournisseurId.generate(), EntrepotId.generate(),
                List.of(ligne()), "Commentaire");
    }

    @Nested
    @DisplayName("toEntity() / toEntityLignes()")
    class ToEntity {

        @Test
        @DisplayName("toEntity() reporte fidèlement chaque champ, hors lignes et avis d'expédition")
        void toEntity_reporteChaqueChamp() {
            CommandeAchat commande = commande();

            CommandeAchatJpaEntity entity = sut.toEntity(commande);

            assertThat(entity.getId()).isEqualTo(commande.getId().getValue());
            assertThat(entity.getReference()).isEqualTo("BC-2026-0001");
            assertThat(entity.getFournisseurId()).isEqualTo(commande.getFournisseurId().getValue());
            assertThat(entity.getEntrepotDestinationId()).isEqualTo(commande.getEntrepotDestinationId().getValue());
            assertThat(entity.getStatut()).isEqualTo(StatutCommandeAchat.EN_ATTENTE_VALIDATION);
            assertThat(entity.getCommentaire()).isEqualTo("Commentaire");
            assertThat(entity.getAvisDateExpedition()).isNull();
        }

        @Test
        @DisplayName("toEntity() aplatit l'avis d'expédition sur les colonnes avis_*")
        void toEntity_aplatitAvisExpedition() {
            CommandeAchat commande = commande();
            commande.validerInterne();
            commande.confirmerDelaiLivraison(10, LocalDate.now().plusDays(10));
            commande.genererAvisExpedition(
                    AvisExpedition.of(LocalDate.now(), "DHL", "T-1", LocalDate.now().plusDays(5)),
                    List.of(new CommandeAchat.InfoExpeditionLigne(commande.getLignes().get(0).getId(), "LOT-1", null,
                            LocalDate.now().plusYears(1), null, BigDecimal.TEN)));

            CommandeAchatJpaEntity entity = sut.toEntity(commande);

            assertThat(entity.getAvisDateExpedition()).isEqualTo(commande.getAvisExpedition().getDateExpedition());
            assertThat(entity.getAvisTransporteur()).isEqualTo("DHL");
            assertThat(entity.getAvisNumeroSuivi()).isEqualTo("T-1");
        }

        @Test
        @DisplayName("toEntityLignes() produit une entité par ligne, rattachée au parent")
        void toEntityLignes_rattacheAuParent() {
            CommandeAchat commande = commande();

            List<LigneCommandeAchatJpaEntity> lignes = sut.toEntityLignes(commande);

            assertThat(lignes).hasSize(1);
            LigneCommandeAchatJpaEntity ligneEntity = lignes.get(0);
            assertThat(ligneEntity.getCommandeAchatId()).isEqualTo(commande.getId().getValue());
            assertThat(ligneEntity.getQuantiteCommandee()).isEqualByComparingTo("10");
            assertThat(ligneEntity.getPrixUnitaire()).isEqualByComparingTo("200000");
            assertThat(ligneEntity.getQuantiteRecue()).isEqualByComparingTo("0");
        }
    }

    @Nested
    @DisplayName("toDomain()")
    class ToDomain {

        @Test
        @DisplayName("sans avis d'expédition (colonnes avis_* nulles) → getAvisExpedition() renvoie null")
        void toDomain_sansAvisExpedition_null() {
            UUID commandeId = UUID.randomUUID();
            CommandeAchatJpaEntity entity = new CommandeAchatJpaEntity(commandeId, "BC-1", UUID.randomUUID(),
                    UUID.randomUUID(), StatutCommandeAchat.EN_ATTENTE_VALIDATION, null, null, null, null, null,
                    null, null, null, null);
            entity.setCreatedAt(Instant.now());
            entity.setUpdatedAt(Instant.now());

            LigneCommandeAchatJpaEntity ligneEntity = new LigneCommandeAchatJpaEntity(UUID.randomUUID(), commandeId,
                    UUID.randomUUID(), UUID.randomUUID(), BigDecimal.TEN, BigDecimal.TEN, null, null, null, null,
                    null, BigDecimal.ZERO, BigDecimal.ZERO, null);

            CommandeAchat commande = sut.toDomain(entity, List.of(ligneEntity));

            assertThat(commande.getAvisExpedition()).isNull();
            assertThat(commande.getLignes()).hasSize(1);
        }

        @Test
        @DisplayName("avec avis d'expédition (avis_date_expedition renseignée) → reconstruit l'AvisExpedition")
        void toDomain_avecAvisExpedition_reconstruit() {
            UUID commandeId = UUID.randomUUID();
            LocalDate dateExpedition = LocalDate.now();
            CommandeAchatJpaEntity entity = new CommandeAchatJpaEntity(commandeId, "BC-1", UUID.randomUUID(),
                    UUID.randomUUID(), StatutCommandeAchat.EXPEDIEE, null, 10, LocalDate.now().plusDays(10),
                    dateExpedition, "DHL", "T-1", LocalDate.now().plusDays(5), null, null);
            entity.setCreatedAt(Instant.now());
            entity.setUpdatedAt(Instant.now());

            LigneCommandeAchatJpaEntity ligneEntity = new LigneCommandeAchatJpaEntity(UUID.randomUUID(), commandeId,
                    UUID.randomUUID(), UUID.randomUUID(), BigDecimal.TEN, BigDecimal.TEN, "LOT-1", null,
                    LocalDate.now().plusYears(1), null, BigDecimal.TEN, BigDecimal.ZERO, BigDecimal.ZERO, null);

            CommandeAchat commande = sut.toDomain(entity, List.of(ligneEntity));

            assertThat(commande.getAvisExpedition()).isNotNull();
            assertThat(commande.getAvisExpedition().getDateExpedition()).isEqualTo(dateExpedition);
            assertThat(commande.getAvisExpedition().getTransporteur()).isEqualTo("DHL");
        }

        @Test
        @DisplayName("reconstruit fidèlement l'identité et le statut")
        void toDomain_reconstruitIdentiteEtStatut() {
            UUID commandeId = UUID.randomUUID();
            UUID fournisseurId = UUID.randomUUID();
            CommandeAchatJpaEntity entity = new CommandeAchatJpaEntity(commandeId, "BC-2026-0099", fournisseurId,
                    UUID.randomUUID(), StatutCommandeAchat.VALIDEE, null, null, null, null, null, null, null, null,
                    null);
            entity.setCreatedAt(Instant.now());
            entity.setUpdatedAt(Instant.now());

            LigneCommandeAchatJpaEntity ligneEntity = new LigneCommandeAchatJpaEntity(UUID.randomUUID(), commandeId,
                    UUID.randomUUID(), UUID.randomUUID(), BigDecimal.TEN, BigDecimal.TEN, null, null, null, null,
                    null, BigDecimal.ZERO, BigDecimal.ZERO, null);

            CommandeAchat commande = sut.toDomain(entity, List.of(ligneEntity));

            assertThat(commande.getId().getValue()).isEqualTo(commandeId);
            assertThat(commande.getFournisseurId().getValue()).isEqualTo(fournisseurId);
            assertThat(commande.getReference()).isEqualTo("BC-2026-0099");
            assertThat(commande.getStatut()).isEqualTo(StatutCommandeAchat.VALIDEE);
        }
    }

    @Test
    @DisplayName("aller-retour toEntity()/toEntityLignes() puis toDomain() préserve l'état")
    void allerRetour_preserveEtat() {
        CommandeAchat original = commande();

        CommandeAchat restaure = sut.toDomain(sut.toEntity(original), sut.toEntityLignes(original));

        assertThat(restaure.getId()).isEqualTo(original.getId());
        assertThat(restaure.getReference()).isEqualTo(original.getReference());
        assertThat(restaure.getStatut()).isEqualTo(original.getStatut());
        assertThat(restaure.getLignes()).hasSize(1);
        assertThat(restaure.getLignes().get(0).getQuantiteCommandee())
                .isEqualByComparingTo(original.getLignes().get(0).getQuantiteCommandee());
    }
}