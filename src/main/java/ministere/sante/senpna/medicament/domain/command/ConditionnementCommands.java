package ministere.sante.senpna.medicament.domain.command;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class ConditionnementCommands {

    public record CreateConditionnementCommand(UUID medicamentId, String nom, int niveau,
            BigDecimal quantiteUniteBase, boolean estUniteBase, BigDecimal prixAchat,
            BigDecimal prixVente) {
    }

    public record UpdateConditionnementCommand(UUID conditionnementId, String nom, int niveau,
            BigDecimal quantiteUniteBase, boolean estUniteBase, BigDecimal prixAchat,
            BigDecimal prixVente) {
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
            BigDecimal quantiteUniteBase, boolean estUniteBase, BigDecimal prixAchat,
            BigDecimal prixVente, boolean actif, Instant createdAt, Instant updatedAt) {
    }

    public record ConditionnementPage(List<ConditionnementDetail> content, int page, int size,
            long totalElements, int totalPages) {
    }

}
