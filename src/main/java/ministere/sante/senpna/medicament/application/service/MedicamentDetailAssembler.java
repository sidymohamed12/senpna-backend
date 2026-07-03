package ministere.sante.senpna.medicament.application.service;

import ministere.sante.senpna.medicament.domain.command.MedicamentCommands.MedicamentDetail;
import ministere.sante.senpna.medicament.domain.model.Medicament;
import ministere.sante.senpna.medicament.domain.port.out.FamilleRepositoryPort;
import ministere.sante.senpna.medicament.domain.port.out.FormeRepositoryPort;

import org.springframework.stereotype.Component;

/**
 * Assemble la représentation de sortie {@link MedicamentDetail} — résout au
 * passage les libellés de la famille thérapeutique et de la forme
 * pharmaceutique rattachées, pour éviter à chaque client de la vue
 * (mobile, web) de devoir enchaîner des appels {@code GET /familles/{id}}
 * et {@code GET /formes/{id}}. Même intention que
 * {@code EntrepotDetailAssembler} pour {@code regionNom} — ici sans cache
 * dédié, Famille/Forme étant interrogées via leur port directement
 * (référentiels de faible volumétrie).
 */
@Component
public class MedicamentDetailAssembler {

        private final FamilleRepositoryPort familleRepositoryPort;
        private final FormeRepositoryPort formeRepositoryPort;

        public MedicamentDetailAssembler(FamilleRepositoryPort familleRepositoryPort,
                        FormeRepositoryPort formeRepositoryPort) {
                this.familleRepositoryPort = familleRepositoryPort;
                this.formeRepositoryPort = formeRepositoryPort;
        }

        public MedicamentDetail assembler(Medicament medicament) {
                String familleLibelle = familleRepositoryPort.findById(medicament.getFamilleId())
                                .map(f -> f.getLibelle())
                                .orElse(null);
                String formeLibelle = formeRepositoryPort.findById(medicament.getFormeId())
                                .map(f -> f.getLibelle())
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
