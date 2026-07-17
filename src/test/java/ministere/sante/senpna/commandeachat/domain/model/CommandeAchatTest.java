package ministere.sante.senpna.commandeachat.domain.model;

import ministere.sante.senpna.commandeachat.domain.exception.LigneCommandeAchatIntrouvableException;
import ministere.sante.senpna.commandeachat.domain.exception.TransitionStatutCommandeAchatInvalideException;
import ministere.sante.senpna.commandeachat.domain.model.CommandeAchat.InfoExpeditionLigne;
import ministere.sante.senpna.commandeachat.domain.model.CommandeAchat.InfoReceptionLigne;
import ministere.sante.senpna.commandeachat.domain.valueobject.CommandeAchatId;
import ministere.sante.senpna.commandeachat.domain.valueobject.LigneCommandeAchatId;
import ministere.sante.senpna.commandeachat.domain.valueobject.StatutCommandeAchat;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("CommandeAchat — agrégat de domaine")
class CommandeAchatTest {

    private LigneCommandeAchat ligne() {
        return LigneCommandeAchat.creer(new LigneCommandeAchat.CreationCommand(MedicamentId.generate(),
                ConditionnementId.generate(), BigDecimal.TEN,
                BigDecimal.valueOf(200_000)));
    }

    private CommandeAchat commandeEnAttente() {
        return CommandeAchat.creer(
                new CommandeAchat.CreationCommand("BC-2026-0001", FournisseurId.generate(), EntrepotId.generate(),
                        List.of(ligne()), "Commentaire"));
    }

    private CommandeAchat commandeValidee() {
        CommandeAchat commande = commandeEnAttente();
        commande.validerInterne();
        return commande;
    }

    private CommandeAchat commandeEnTransit() {
        CommandeAchat commande = commandeValidee();
        commande.confirmerDelaiLivraison(10, LocalDate.now().plusDays(10));
        return commande;
    }

    private CommandeAchat commandeExpediee() {
        CommandeAchat commande = commandeEnTransit();
        LigneCommandeAchatId ligneId = commande.getLignes().get(0).getId();
        commande.genererAvisExpedition(
                AvisExpedition.of(LocalDate.now(), "DHL", "T-1", LocalDate.now().plusDays(5)),
                List.of(new InfoExpeditionLigne(ligneId, "LOT-A001", LocalDate.now(), LocalDate.now().plusYears(2),
                        "url", BigDecimal.TEN)));
        return commande;
    }

    @Nested
    @DisplayName("creer()")
    class Creer {

        @Test
        @DisplayName("crée une commande EN_ATTENTE_VALIDATION")
        void creer_succes() {
            CommandeAchat commande = commandeEnAttente();

            assertThat(commande.getStatut()).isEqualTo(StatutCommandeAchat.EN_ATTENTE_VALIDATION);
            assertThat(commande.getLignes()).hasSize(1);
        }

        @Test
        @DisplayName("sans ligne → IllegalArgumentException")
        void sansLigne_leveException() {

            var fournisseurId = FournisseurId.generate();
            var entrepotId = EntrepotId.generate();
            var creationCommand = new CommandeAchat.CreationCommand("BC-1", fournisseurId, entrepotId, List.of(), null);
            assertThatThrownBy(() -> CommandeAchat.creer(creationCommand)).isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("référence trop longue → IllegalArgumentException")
        void referenceTropLongue_leveException() {
            String refTropLongue = "B".repeat(51);

            var fournisseurId = FournisseurId.generate();
            var entrepotId = EntrepotId.generate();
            var listLigne = List.of(ligne());
            var creationCommand = new CommandeAchat.CreationCommand(refTropLongue, fournisseurId, entrepotId, listLigne,
                    null);
            assertThatThrownBy(() -> CommandeAchat.creer(creationCommand)).isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("validerInterne() / rejeter()")
    class ValiderRejeter {

        @Test
        @DisplayName("validerInterne() depuis EN_ATTENTE_VALIDATION → VALIDEE")
        void validerInterne_passeValidee() {
            CommandeAchat commande = commandeEnAttente();

            commande.validerInterne();

            assertThat(commande.getStatut()).isEqualTo(StatutCommandeAchat.VALIDEE);
        }

        @Test
        @DisplayName("rejeter() enregistre le motif et passe REJETEE")
        void rejeter_enregistreMotif() {
            CommandeAchat commande = commandeEnAttente();

            commande.rejeter("Prix hors marché");

            assertThat(commande.getStatut()).isEqualTo(StatutCommandeAchat.REJETEE);
            assertThat(commande.getMotifRejet()).isEqualTo("Prix hors marché");
        }

        @Test
        @DisplayName("validerInterne() depuis VALIDEE → TransitionStatutCommandeAchatInvalideException")
        void validerDepuisValidee_leveException() {
            CommandeAchat commande = commandeValidee();

            assertThatThrownBy(commande::validerInterne)
                    .isInstanceOf(TransitionStatutCommandeAchatInvalideException.class);
        }
    }

    @Nested
    @DisplayName("annuler()")
    class Annuler {

        @Test
        @DisplayName("depuis EN_ATTENTE_VALIDATION → ANNULEE")
        void depuisEnAttente_passeAnnulee() {
            CommandeAchat commande = commandeEnAttente();

            commande.annuler();

            assertThat(commande.getStatut()).isEqualTo(StatutCommandeAchat.ANNULEE);
        }

        @Test
        @DisplayName("depuis VALIDEE → ANNULEE")
        void depuisValidee_passeAnnulee() {
            CommandeAchat commande = commandeValidee();

            commande.annuler();

            assertThat(commande.getStatut()).isEqualTo(StatutCommandeAchat.ANNULEE);
        }

        @Test
        @DisplayName("depuis EN_TRANSIT → ANNULEE")
        void depuisEnTransit_passeAnnulee() {
            CommandeAchat commande = commandeEnTransit();

            commande.annuler();

            assertThat(commande.getStatut()).isEqualTo(StatutCommandeAchat.ANNULEE);
        }

        @Test
        @DisplayName("depuis EXPEDIEE → TransitionStatutCommandeAchatInvalideException (règle métier : non modifiable)")
        void depuisExpediee_leveException() {
            CommandeAchat commande = commandeExpediee();

            assertThatThrownBy(commande::annuler)
                    .isInstanceOf(TransitionStatutCommandeAchatInvalideException.class);
        }
    }

    @Nested
    @DisplayName("accuserReception() — fournisseur")
    class AccuserReception {

        @Test
        @DisplayName("depuis VALIDEE → horodate l'accusé sans changer le statut")
        void depuisValidee_horodateSansChangerStatut() {
            CommandeAchat commande = commandeValidee();

            commande.accuserReception();

            assertThat(commande.getStatut()).isEqualTo(StatutCommandeAchat.VALIDEE);
            assertThat(commande.getDateAccuseReceptionFournisseur()).isNotNull();
        }

        @Test
        @DisplayName("depuis EN_ATTENTE_VALIDATION → TransitionStatutCommandeAchatInvalideException")
        void depuisEnAttente_leveException() {
            CommandeAchat commande = commandeEnAttente();

            assertThatThrownBy(commande::accuserReception)
                    .isInstanceOf(TransitionStatutCommandeAchatInvalideException.class);
        }
    }

    @Nested
    @DisplayName("confirmerDelaiLivraison() — fournisseur")
    class ConfirmerDelaiLivraison {

        @Test
        @DisplayName("depuis VALIDEE → EN_TRANSIT")
        void depuisValidee_passeEnTransit() {
            CommandeAchat commande = commandeValidee();

            commande.confirmerDelaiLivraison(15, LocalDate.now().plusDays(15));

            assertThat(commande.getStatut()).isEqualTo(StatutCommandeAchat.EN_TRANSIT);
            assertThat(commande.getDelaiLivraisonConfirmeJours()).isEqualTo(15);
        }

        @Test
        @DisplayName("délai négatif ou nul → IllegalArgumentException")
        void delaiInvalide_leveException() {
            CommandeAchat commande = commandeValidee();

            var now = LocalDate.now();
            assertThatThrownBy(() -> commande.confirmerDelaiLivraison(0, now))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("depuis EN_ATTENTE_VALIDATION → TransitionStatutCommandeAchatInvalideException")
        void depuisEnAttente_leveException() {
            CommandeAchat commande = commandeEnAttente();

            var nowPlus1 = LocalDate.now().plusDays(10);
            assertThatThrownBy(() -> commande.confirmerDelaiLivraison(10, nowPlus1))
                    .isInstanceOf(TransitionStatutCommandeAchatInvalideException.class);
        }
    }

    @Nested
    @DisplayName("genererAvisExpedition() — fournisseur")
    class GenererAvisExpedition {

        @Test
        @DisplayName("depuis EN_TRANSIT → EXPEDIEE, lot renseigné sur la ligne")
        void depuisEnTransit_passeExpediee() {
            CommandeAchat commande = commandeExpediee();

            assertThat(commande.getStatut()).isEqualTo(StatutCommandeAchat.EXPEDIEE);
            assertThat(commande.getAvisExpedition()).isNotNull();
            assertThat(commande.getLignes().get(0).getNumeroLot()).isEqualTo("LOT-A001");
        }

        @Test
        @DisplayName("ligne inexistante → LigneCommandeAchatIntrouvableException")
        void ligneInexistante_leveException() {
            CommandeAchat commande = commandeEnTransit();

            List<InfoExpeditionLigne> lignes = List.of(new InfoExpeditionLigne(LigneCommandeAchatId.generate(),
                    "LOT-A001", null, LocalDate.now().plusYears(1), null, BigDecimal.TEN));
            AvisExpedition avis = AvisExpedition.of(LocalDate.now(), null, null, null);

            assertThatThrownBy(() -> commande.genererAvisExpedition(avis, lignes))
                    .isInstanceOf(LigneCommandeAchatIntrouvableException.class);
        }

        @Test
        @DisplayName("depuis VALIDEE (sans confirmation de délai) → TransitionStatutCommandeAchatInvalideException")
        void depuisValidee_leveException() {
            CommandeAchat commande = commandeValidee();
            LigneCommandeAchatId ligneId = commande.getLignes().get(0).getId();

            List<InfoExpeditionLigne> lignes = List.of(new InfoExpeditionLigne(ligneId, "LOT-A001", null,
                    LocalDate.now().plusYears(1), null, BigDecimal.TEN));
            AvisExpedition avis = AvisExpedition.of(LocalDate.now(), null, null, null);

            assertThatThrownBy(() -> commande.genererAvisExpedition(avis, lignes))
                    .isInstanceOf(TransitionStatutCommandeAchatInvalideException.class);
        }

        @Test
        @DisplayName("liste de lignes vide → IllegalArgumentException")
        void listeVide_leveException() {
            CommandeAchat commande = commandeEnTransit();
            AvisExpedition avis = AvisExpedition.of(LocalDate.now(), null, null, null);

            assertThatThrownBy(() -> commande.genererAvisExpedition(avis, List.of()))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("receptionner() — PNA")
    class Receptionner {

        @Test
        @DisplayName("réception totale de l'unique ligne → RECEPTIONNEE")
        void receptionTotale_passeReceptionnee() {
            CommandeAchat commande = commandeExpediee();
            LigneCommandeAchatId ligneId = commande.getLignes().get(0).getId();

            commande.receptionner(List.of(new InfoReceptionLigne(ligneId, BigDecimal.TEN, BigDecimal.ZERO, null)));

            assertThat(commande.getStatut()).isEqualTo(StatutCommandeAchat.RECEPTIONNEE);
        }

        @Test
        @DisplayName("réception partielle → PARTIELLEMENT_RECEPTIONNEE")
        void receptionPartielle_passePartiellementReceptionnee() {
            CommandeAchat commande = commandeExpediee();
            LigneCommandeAchatId ligneId = commande.getLignes().get(0).getId();

            commande.receptionner(
                    List.of(new InfoReceptionLigne(ligneId, BigDecimal.valueOf(6), BigDecimal.ZERO, null)));

            assertThat(commande.getStatut()).isEqualTo(StatutCommandeAchat.PARTIELLEMENT_RECEPTIONNEE);
        }

        @Test
        @DisplayName("réception complémentaire après réception partielle → RECEPTIONNEE")
        void receptionComplementaire_completeLaCommande() {
            CommandeAchat commande = commandeExpediee();
            LigneCommandeAchatId ligneId = commande.getLignes().get(0).getId();

            commande.receptionner(
                    List.of(new InfoReceptionLigne(ligneId, BigDecimal.valueOf(6), BigDecimal.ZERO, null)));
            commande.receptionner(
                    List.of(new InfoReceptionLigne(ligneId, BigDecimal.valueOf(4), BigDecimal.ZERO, null)));

            assertThat(commande.getStatut()).isEqualTo(StatutCommandeAchat.RECEPTIONNEE);
        }

        @Test
        @DisplayName("réception avec refus partiel → complète si reçu + refusé = expédié")
        void receptionAvecRefus_completeSiRecuPlusRefuseEgalExpedie() {
            CommandeAchat commande = commandeExpediee();
            LigneCommandeAchatId ligneId = commande.getLignes().get(0).getId();

            commande.receptionner(List.of(
                    new InfoReceptionLigne(ligneId, BigDecimal.valueOf(7), BigDecimal.valueOf(3), "Lot endommagé")));

            assertThat(commande.getStatut()).isEqualTo(StatutCommandeAchat.RECEPTIONNEE);
        }

        @Test
        @DisplayName("depuis VALIDEE (non expédiée) → TransitionStatutCommandeAchatInvalideException")
        void depuisValidee_leveException() {
            CommandeAchat commande = commandeValidee();
            LigneCommandeAchatId ligneId = commande.getLignes().get(0).getId();

            List<InfoReceptionLigne> lignes = List
                    .of(new InfoReceptionLigne(ligneId, BigDecimal.TEN, BigDecimal.ZERO, null));

            assertThatThrownBy(() -> commande.receptionner(lignes))
                    .isInstanceOf(TransitionStatutCommandeAchatInvalideException.class);
        }

        @Test
        @DisplayName("liste de lignes vide → IllegalArgumentException")
        void listeVide_leveException() {
            CommandeAchat commande = commandeExpediee();

            assertThatThrownBy(() -> commande.receptionner(List.of()))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("appartientA()")
    class AppartientA {

        @Test
        @DisplayName("même fournisseur → true")
        void memeFournisseur_true() {
            FournisseurId fournisseurId = FournisseurId.generate();
            CommandeAchat commande = CommandeAchat
                    .creer(new CommandeAchat.CreationCommand("BC-1", fournisseurId, EntrepotId.generate(),
                            List.of(ligne()), null));

            assertThat(commande.appartientA(fournisseurId)).isTrue();
        }

        @Test
        @DisplayName("fournisseur différent → false")
        void fournisseurDifferent_false() {
            CommandeAchat commande = commandeEnAttente();

            assertThat(commande.appartientA(FournisseurId.generate())).isFalse();
        }
    }

    @Nested
    @DisplayName("reconstruct()")
    class Reconstruct {

        @Test
        @DisplayName("reconstruit fidèlement une commande depuis un état persisté")
        void reconstruit_etatFidele() {
            Instant maintenant = Instant.now();
            CommandeAchatId id = CommandeAchatId.generate();
            FournisseurId fournisseurId = FournisseurId.generate();
            EntrepotId entrepotId = EntrepotId.generate();

            CommandeAchat commande = CommandeAchat.builder()
                    .id(id)
                    .reference("BC-2026-0099")
                    .fournisseurId(fournisseurId)
                    .entrepotDestinationId(entrepotId)
                    .statut(StatutCommandeAchat.VALIDEE)
                    .lignes(List.of(ligne()))
                    .dateAccuseReceptionFournisseur(null)
                    .delaiLivraisonConfirmeJours(null)
                    .dateLivraisonConfirmee(null)
                    .avisExpedition(null)
                    .motifRejet(null)
                    .commentaire("Com")
                    .createdAt(maintenant)
                    .updatedAt(maintenant)
                    .build();

            assertThat(commande.getId()).isEqualTo(id);
            assertThat(commande.getFournisseurId()).isEqualTo(fournisseurId);
            assertThat(commande.getStatut()).isEqualTo(StatutCommandeAchat.VALIDEE);
        }
    }
}
