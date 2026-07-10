package ministere.sante.senpna.medicament.domain.command;

import ministere.sante.senpna.medicament.domain.valueobject.TemperatureConservation;
import ministere.sante.senpna.medicament.domain.valueobject.VoieAdministration;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class MedicamentCommands {

        private MedicamentCommands() {
        }

        public record CreateMedicamentCommand(String code, String nomCommercial, String dci, String dosage,
                        UUID formeId, UUID familleId, VoieAdministration voieAdministration,
                        TemperatureConservation temperatureConservation, String programmeSante,
                        Integer delaiApprovisionnementJours, boolean necessiteOrdonnance, String fabricant,
                        Integer stockMinimum, Integer stockMaximum) {
        }

        public record UpdateMedicamentCommand(UUID medicamentId, String nomCommercial, String dci, String dosage,
                        UUID formeId, UUID familleId, VoieAdministration voieAdministration,
                        TemperatureConservation temperatureConservation, String programmeSante,
                        Integer delaiApprovisionnementJours, boolean necessiteOrdonnance, String fabricant,
                        Integer stockMinimum, Integer stockMaximum) {
        }

        public record ArchiveMedicamentCommand(UUID medicamentId) {
        }

        public record DesarchiveMedicamentCommand(UUID medicamentId) {
        }

        public record GetMedicamentQuery(UUID medicamentId) {
        }

        public record ListMedicamentsQuery(String recherche, UUID familleId, UUID formeId, Boolean actif,
                        Integer page, Integer size, String sortBy, String sortDirection) {
        }

        public record MedicamentDetail(UUID id, String code, String nomCommercial, String dci, String dosage,
                        UUID formeId, String formeLibelle, UUID familleId, String familleLibelle,
                        VoieAdministration voieAdministration, TemperatureConservation temperatureConservation,
                        String programmeSante, Integer delaiApprovisionnementJours, boolean necessiteOrdonnance,
                        String fabricant, Integer stockMinimum, Integer stockMaximum, boolean actif,
                        Instant createdAt, Instant updatedAt) {
        }

        public record MedicamentPage(List<MedicamentDetail> content, int page, int size, long totalElements,
                        int totalPages) {
        }

}
