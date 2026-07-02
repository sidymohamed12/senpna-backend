package ministere.sante.senpna.organisation.domain.model;

import ministere.sante.senpna.organisation.domain.exception.DemandeAdhesionDejaTraiteeException;
import ministere.sante.senpna.organisation.domain.exception.StructureSanitaireNonValideeException;
import ministere.sante.senpna.organisation.domain.valueobject.EntrepotId;
import ministere.sante.senpna.organisation.domain.valueobject.RegionId;
import ministere.sante.senpna.organisation.domain.valueobject.StatutAdhesion;
import ministere.sante.senpna.organisation.domain.valueobject.TypeStructureSanitaire;
import ministere.sante.senpna.shared.domain.events.AdhesionValideeEvent;
import ministere.sante.senpna.shared.domain.events.DomainEvent;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("StructureSanitaire — agrégat de domaine")
class StructureSanitaireTest {

    private static final RegionId REGION_ID = RegionId.generate();

    private StructureSanitaire structureValide() {
        return StructureSanitaire.creer("hop-thies", "Hôpital de Thiès", TypeStructureSanitaire.HOPITAL, REGION_ID,
                "Thiès", "Route de Dakar", "+221771234567", "hopital.thies@sante.gouv.sn", "Ndiaye", "Fatou");
    }

    @Nested
    @DisplayName("creer()")
    class Creer {

        @Test
        @DisplayName("crée une demande d'adhésion EN_ATTENTE_VALIDATION, inactive, rattachée à la région")
        void creer_succes_demandeEnAttente() {
            StructureSanitaire structure = structureValide();

            assertThat(structure.getStatutAdhesion()).isEqualTo(StatutAdhesion.EN_ATTENTE_VALIDATION);
            assertThat(structure.isActif()).isFalse();
            assertThat(structure.getRegionId()).isEqualTo(REGION_ID);
            assertThat(structure.getPraId()).isNull();
            assertThat(structure.getCode()).isEqualTo("HOP-THIES");
            assertThat(structure.getResponsableNom()).isEqualTo("Ndiaye");
            assertThat(structure.getResponsablePrenom()).isEqualTo("Fatou");
        }

        @Test
        @DisplayName("région null → IllegalArgumentException (la région est obligatoire dès la création)")
        void creer_sansRegion_leveException() {
            assertThatThrownBy(() -> StructureSanitaire.creer("HOP-X", "Hôpital X", TypeStructureSanitaire.HOPITAL,
                    null, null, null, null, null, "Ndiaye", "Fatou"))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("responsableNom null → NullPointerException")
        void creer_sansResponsableNom_leveException() {
            assertThatThrownBy(() -> StructureSanitaire.creer("HOP-X", "Hôpital X", TypeStructureSanitaire.HOPITAL,
                    REGION_ID, null, null, null, null, null, "Fatou")).isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("responsablePrenom null → NullPointerException")
        void creer_sansResponsablePrenom_leveException() {
            assertThatThrownBy(() -> StructureSanitaire.creer("HOP-X", "Hôpital X", TypeStructureSanitaire.HOPITAL,
                    REGION_ID, null, null, null, null, "Ndiaye", null)).isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("validerAdhesion()")
    class ValiderAdhesion {

        @Test
        @DisplayName("passe le statut à VALIDEE et active la structure")
        void validerAdhesion_succes_devientValideeEtActive() {
            StructureSanitaire structure = structureValide();

            structure.validerAdhesion();

            assertThat(structure.getStatutAdhesion()).isEqualTo(StatutAdhesion.VALIDEE);
            assertThat(structure.isActif()).isTrue();
            assertThat(structure.getMotifRejet()).isNull();
        }

        @Test
        @DisplayName("émet un AdhesionValideeEvent avec les informations du responsable")
        void validerAdhesion_succes_emetEvent() {
            StructureSanitaire structure = structureValide();

            structure.validerAdhesion();

            List<DomainEvent> events = structure.getDomainEvents();
            assertThat(events).hasSize(1);
            assertThat(events.get(0)).isInstanceOf(AdhesionValideeEvent.class);
            AdhesionValideeEvent event = (AdhesionValideeEvent) events.get(0);
            assertThat(event.structureSanitaireId()).isEqualTo(structure.getId().getValue());
            assertThat(event.responsableNom()).isEqualTo("Ndiaye");
            assertThat(event.responsablePrenom()).isEqualTo("Fatou");
            assertThat(event.email()).isEqualTo("hopital.thies@sante.gouv.sn");
        }

        @Test
        @DisplayName("adhésion déjà validée → DemandeAdhesionDejaTraiteeException")
        void validerAdhesion_dejaValidee_leveException() {
            StructureSanitaire structure = structureValide();
            structure.validerAdhesion();

            assertThatThrownBy(structure::validerAdhesion)
                    .isInstanceOf(DemandeAdhesionDejaTraiteeException.class);
        }

        @Test
        @DisplayName("adhésion déjà rejetée → DemandeAdhesionDejaTraiteeException")
        void validerAdhesion_dejaRejetee_leveException() {
            StructureSanitaire structure = structureValide();
            structure.rejeterAdhesion("Dossier incomplet");

            assertThatThrownBy(structure::validerAdhesion)
                    .isInstanceOf(DemandeAdhesionDejaTraiteeException.class);
        }
    }

    @Nested
    @DisplayName("rejeterAdhesion()")
    class RejeterAdhesion {

        @Test
        @DisplayName("passe le statut à REJETEE, garde la structure inactive, conserve le motif")
        void rejeterAdhesion_succes_devientRejetee() {
            StructureSanitaire structure = structureValide();

            structure.rejeterAdhesion("Documents manquants");

            assertThat(structure.getStatutAdhesion()).isEqualTo(StatutAdhesion.REJETEE);
            assertThat(structure.isActif()).isFalse();
            assertThat(structure.getMotifRejet()).isEqualTo("Documents manquants");
        }

        @Test
        @DisplayName("adhésion déjà traitée → DemandeAdhesionDejaTraiteeException")
        void rejeterAdhesion_dejaTraitee_leveException() {
            StructureSanitaire structure = structureValide();
            structure.validerAdhesion();

            assertThatThrownBy(() -> structure.rejeterAdhesion("motif"))
                    .isInstanceOf(DemandeAdhesionDejaTraiteeException.class);
        }
    }

    @Nested
    @DisplayName("activer() / desactiver()")
    class ActiverDesactiver {

        @Test
        @DisplayName("activer() une structure non validée → StructureSanitaireNonValideeException")
        void activer_adhesionNonValidee_leveException() {
            StructureSanitaire structure = structureValide();

            assertThatThrownBy(structure::activer)
                    .isInstanceOf(StructureSanitaireNonValideeException.class);
        }

        @Test
        @DisplayName("activer() une structure validée mais désactivée → réactivation réussie")
        void activer_valideeEtDesactivee_succes() {
            StructureSanitaire structure = structureValide();
            structure.validerAdhesion();
            structure.desactiver();

            structure.activer();

            assertThat(structure.isActif()).isTrue();
        }

        @Test
        @DisplayName("desactiver() une structure active la rend inactive")
        void desactiver_active_devientInactive() {
            StructureSanitaire structure = structureValide();
            structure.validerAdhesion();

            structure.desactiver();

            assertThat(structure.isActif()).isFalse();
        }
    }

    @Nested
    @DisplayName("affecterRegion() / affecterPra()")
    class Affectations {

        @Test
        @DisplayName("affecterRegion() met à jour la région de rattachement")
        void affecterRegion_succes() {
            StructureSanitaire structure = structureValide();
            RegionId nouvelleRegion = RegionId.generate();

            structure.affecterRegion(nouvelleRegion);

            assertThat(structure.getRegionId()).isEqualTo(nouvelleRegion);
        }

        @Test
        @DisplayName("affecterRegion(null) → NullPointerException")
        void affecterRegion_null_leveException() {
            StructureSanitaire structure = structureValide();

            assertThatThrownBy(() -> structure.affecterRegion(null)).isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("affecterPra() met à jour la PRA de rattachement")
        void affecterPra_succes() {
            StructureSanitaire structure = structureValide();
            EntrepotId praId = EntrepotId.generate();

            structure.affecterPra(praId);

            assertThat(structure.getPraId()).isEqualTo(praId);
        }

        @Test
        @DisplayName("affecterPra(null) → NullPointerException")
        void affecterPra_null_leveException() {
            StructureSanitaire structure = structureValide();

            assertThatThrownBy(() -> structure.affecterPra(null)).isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("modifierInformations()")
    class ModifierInformations {

        @Test
        @DisplayName("met à jour les informations générales, y compris le responsable")
        void modifierInformations_succes_metAJourChamps() {
            StructureSanitaire structure = structureValide();

            structure.modifierInformations("Hôpital Régional de Thiès", "Thiès Nord", "Nouvelle adresse",
                    "+221709999999", "nouveau@sante.gouv.sn", "Diop", "Awa");

            assertThat(structure.getNom()).isEqualTo("Hôpital Régional de Thiès");
            assertThat(structure.getDistrict()).isEqualTo("Thiès Nord");
            assertThat(structure.getResponsableNom()).isEqualTo("Diop");
            assertThat(structure.getResponsablePrenom()).isEqualTo("Awa");
        }
    }
}
