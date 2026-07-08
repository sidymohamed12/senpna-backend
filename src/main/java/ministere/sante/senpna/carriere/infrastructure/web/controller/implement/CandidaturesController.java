package ministere.sante.senpna.carriere.infrastructure.web.controller.implement;

import ministere.sante.senpna.carriere.application.facade.CandidatureFacade;
import ministere.sante.senpna.carriere.domain.command.CandidatureCommands.CandidatureDetail;
import ministere.sante.senpna.carriere.domain.command.CandidatureCommands.CandidaturePage;
import ministere.sante.senpna.carriere.domain.command.CandidatureCommands.GetCandidatureQuery;
import ministere.sante.senpna.carriere.domain.command.CandidatureCommands.ListCandidaturesQuery;
import ministere.sante.senpna.carriere.domain.command.CandidatureCommands.SoumettreCandidatureCommand;
import ministere.sante.senpna.carriere.infrastructure.web.controller.ICandidaturesController;
import ministere.sante.senpna.carriere.infrastructure.web.dto.request.SoumettreCandidatureRequest;
import ministere.sante.senpna.carriere.infrastructure.web.dto.response.CandidatureResponse;
import ministere.sante.senpna.shared.infrastructure.web.response.RestResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
public class CandidaturesController implements ICandidaturesController {

        private final CandidatureFacade candidatureFacade;

        public CandidaturesController(CandidatureFacade candidatureFacade) {
                this.candidatureFacade = candidatureFacade;
        }

        @Override
        public ResponseEntity<Map<String, Object>> soumettre(SoumettreCandidatureRequest request) {
                CandidatureDetail result = candidatureFacade.soumettreCandidature(new SoumettreCandidatureCommand(
                                request.opportuniteId(), request.civilite(), request.nomComplet(), request.email(),
                                request.telephone(), request.cvUrl(), request.lettreMotivationUrl(),
                                request.messageComplementaire(), request.consentementRgpd()));

                return ResponseEntity.status(HttpStatus.CREATED).body(
                                RestResponse.response(HttpStatus.CREATED, toResponse(result), "CANDIDATURE_CREATED",
                                                "Candidature soumise avec succès"));
        }

        @Override
        @PreAuthorize("hasAnyRole('ADMIN_PNA','GESTIONNAIRE_PNA')")
        public ResponseEntity<Map<String, Object>> obtenir(UUID id) {
                CandidatureDetail result = candidatureFacade.obtenirCandidature(new GetCandidatureQuery(id));
                return ResponseEntity.ok(RestResponse.response(HttpStatus.OK, toResponse(result),
                                "CANDIDATURE_FOUND", "Candidature récupérée"));
        }

        @Override
        @PreAuthorize("hasAnyRole('ADMIN_PNA','GESTIONNAIRE_PNA')")
        public ResponseEntity<Map<String, Object>> lister(UUID opportuniteId, String q, Integer page, Integer size,
                        String sortBy, String sortDirection) {

                CandidaturePage result = candidatureFacade.listerCandidatures(
                                new ListCandidaturesQuery(opportuniteId, q, page, size, sortBy, sortDirection));

                return ResponseEntity.ok(RestResponse.responsePaginate(
                                HttpStatus.OK,
                                result.content().stream().map(this::toResponse).toList(),
                                "CANDIDATURES_LISTED",
                                "Liste des candidatures récupérée",
                                result.page(),
                                result.totalPages(),
                                result.totalElements(),
                                result.page() == 0,
                                result.page() >= result.totalPages() - 1));
        }

        // ── Helpers ──────────────────────────────────────────────────────────

        private CandidatureResponse toResponse(CandidatureDetail detail) {
                return new CandidatureResponse(detail.id(), detail.opportuniteId(), detail.civilite(),
                                detail.nomComplet(), detail.email(), detail.telephone(), detail.cvUrl(),
                                detail.lettreMotivationUrl(), detail.messageComplementaire(),
                                detail.consentementRgpd(), detail.dateCandidature());
        }
}
