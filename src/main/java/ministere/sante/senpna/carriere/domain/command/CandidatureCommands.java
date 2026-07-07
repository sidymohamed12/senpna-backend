package ministere.sante.senpna.carriere.domain.command;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class CandidatureCommands {

    private CandidatureCommands() {
    }

    /**
     * Commande de soumission d'une candidature — {@code cvUrl} et
     * {@code lettreMotivationUrl} sont les URLs publiques déjà uploadées
     * par le front vers le stockage objet via le flux Presigned URL
     * (cf. {@code POST /api/medias/presigned-url/public}, restreint aux
     * {@code MediaType} éligibles à un usage public). L'API ne reçoit et
     * ne manipule jamais les octets du fichier.
     */
    public record SoumettreCandidatureCommand(
            UUID opportuniteId,
            String civilite,
            String nomComplet,
            String email,
            String telephone,
            String cvUrl,
            String lettreMotivationUrl,
            String messageComplementaire,
            boolean consentementRgpd) {
    }

    public record GetCandidatureQuery(UUID candidatureId) {
    }

    public record ListCandidaturesQuery(
            UUID opportuniteId,
            String recherche,
            Integer page,
            Integer size,
            String sortBy,
            String sortDirection) {
    }

    public record CandidatureDetail(
            UUID id,
            UUID opportuniteId,
            String civilite,
            String nomComplet,
            String email,
            String telephone,
            String cvUrl,
            String lettreMotivationUrl,
            String messageComplementaire,
            boolean consentementRgpd,
            Instant dateCandidature) {
    }

    public record CandidaturePage(
            List<CandidatureDetail> content,
            int page,
            int size,
            long totalElements,
            int totalPages) {
    }
}
