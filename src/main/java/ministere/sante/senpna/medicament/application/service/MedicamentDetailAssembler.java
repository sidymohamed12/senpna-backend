package ministere.sante.senpna.medicament.application.service;

import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.MedicamentDetail;
import ministere.sante.senpna.medicament.domain.model.Medicament;
import ministere.sante.senpna.shared.domain.port.out.FamilleCachePort;
import ministere.sante.senpna.shared.domain.port.out.FormeCachePort;
import ministere.sante.senpna.shared.domain.projection.FamilleProjection;
import ministere.sante.senpna.shared.domain.projection.FormeProjection;

import org.springframework.stereotype.Component;

/**
 * Assemble la représentation de sortie {@link MedicamentDetail} — résout au
 * passage les libellés de la famille thérapeutique et de la forme
 * pharmaceutique rattachées, pour éviter à chaque client de la vue
 * (mobile, web) de devoir enchaîner des appels {@code GET /familles/{id}}
 * et {@code GET /formes/{id}}. Résolution via {@link FormeCachePort} /
 * {@link FamilleCachePort} (cache en mémoire, chargé au démarrage —
 * référentiels quasi-statiques) plutôt que via leur port de persistance
 * direct — évite un aller-retour SQL par médicament affiché (N+1), même
 * intention que {@code EntrepotDetailAssembler} pour {@code regionNom}.
 */
@Component
public class MedicamentDetailAssembler {

        private final FamilleCachePort familleCachePort;
        private final FormeCachePort formeCachePort;

        public MedicamentDetailAssembler(FamilleCachePort familleCachePort, FormeCachePort formeCachePort) {
                this.familleCachePort = familleCachePort;
                this.formeCachePort = formeCachePort;
        }

        public MedicamentDetail assembler(Medicament medicament) {
                String familleLibelle = familleCachePort.findById(medicament.getFamilleId().getValue())
                                .map(FamilleProjection::libelle)
                                .orElse(null);
                String formeLibelle = formeCachePort.findById(medicament.getFormeId().getValue())
                                .map(FormeProjection::libelle)
                                .orElse(null);

                return new MedicamentDetail(
                                medicament.getId().getValue(),
                                medicament.getCode(),
                                medicament.getNomCommercial(),
                                medicament.getDci(),
                                medicament.getDosage(),
                                medicament.getFormeId().getValue(),
                                formeLibelle,
                                medicament.getFamilleId().getValue(),
                                familleLibelle,
                                medicament.getVoieAdministration(),
                                medicament.getTemperatureConservation(),
                                medicament.getProgrammeSante(),
                                medicament.getDelaiApprovisionnementJours(),
                                medicament.isNecessiteOrdonnance(),
                                medicament.getFabricant(),
                                medicament.getStockMinimum(),
                                medicament.getStockMaximum(),
                                medicament.isActif(),
                                medicament.getCreatedAt(),
                                medicament.getUpdatedAt());
        }
}
