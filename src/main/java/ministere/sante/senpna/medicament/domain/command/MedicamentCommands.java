package ministere.sante.senpna.medicament.domain.command;

import ministere.sante.senpna.medicament.domain.valueobject.TemperatureConservation;
import ministere.sante.senpna.medicament.domain.valueobject.VoieAdministration;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Commandes, requêtes et résultats du module {@code medicament} — regroupe
 * les 4 agrégats (Famille, Forme, Medicament, Conditionnement), à
 * l'instar de {@code OrganisationCommands} pour le module organisation.
 */
public final class MedicamentCommands {

        private MedicamentCommands() {
        }

        // ── Famille ─────────────────────────────────────────────────────────

        public record CreateFamilleCommand(String code, String libelle, String description) {
        }

        public record UpdateFamilleCommand(UUID familleId, String libelle, String description) {
        }

        public record ArchiveFamilleCommand(UUID familleId) {
        }

        public record DesarchiveFamilleCommand(UUID familleId) {
        }

        public record GetFamilleQuery(UUID familleId) {
        }

        public record ListFamillesQuery(String recherche, Boolean actif, Integer page, Integer size, String sortBy,
                        String sortDirection) {
        }

        public record FamilleDetail(UUID id, String code, String libelle, String description, boolean actif,
                        Instant createdAt, Instant updatedAt) {
        }

        public record FamillePage(List<FamilleDetail> content, int page, int size, long totalElements,
                        int totalPages) {
        }

        // ── Forme ───────────────────────────────────────────────────────────

        public record CreateFormeCommand(String code, String libelle, String description) {
        }

        public record UpdateFormeCommand(UUID formeId, String libelle, String description) {
        }

        public record ArchiveFormeCommand(UUID formeId) {
        }

        public record DesarchiveFormeCommand(UUID formeId) {
        }

        public record GetFormeQuery(UUID formeId) {
        }

        public record ListFormesQuery(String recherche, Boolean actif, Integer page, Integer size, String sortBy,
                        String sortDirection) {
        }

        public record FormeDetail(UUID id, String code, String libelle, String description, boolean actif,
                        Instant createdAt, Instant updatedAt) {
        }

        public record FormePage(List<FormeDetail> content, int page, int size, long totalElements, int totalPages) {
        }

        // ── Médicament ──────────────────────────────────────────────────────

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

        // ── Conditionnement ─────────────────────────────────────────────────

        public record CreateConditionnementCommand(UUID medicamentId, String nom, int niveau,
                        BigDecimal quantiteUniteBase, boolean estUniteBase) {
        }

        public record UpdateConditionnementCommand(UUID conditionnementId, String nom, int niveau,
                        BigDecimal quantiteUniteBase, boolean estUniteBase) {
        }

        public record ArchiveConditionnementCommand(UUID conditionnementId) {
        }

        public record DesarchiveConditionnementCommand(UUID conditionnementId) {
        }

        public record GetConditionnementQuery(UUID conditionnementId) {
        }

        public record ListConditionnementsQuery(UUID medicamentId, Boolean actif, Integer page, Integer size,
                        String sortBy, String sortDirection) {
        }

        public record ConditionnementDetail(UUID id, UUID medicamentId, String nom, int niveau,
                        BigDecimal quantiteUniteBase, boolean estUniteBase, boolean actif, Instant createdAt,
                        Instant updatedAt) {
        }

        public record ConditionnementPage(List<ConditionnementDetail> content, int page, int size,
                        long totalElements, int totalPages) {
        }
}
