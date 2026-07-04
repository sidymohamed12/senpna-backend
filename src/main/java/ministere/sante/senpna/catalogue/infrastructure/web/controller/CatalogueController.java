package ministere.sante.senpna.catalogue.infrastructure.web.controller;

import ministere.sante.senpna.catalogue.application.facade.CatalogueFacade;
import ministere.sante.senpna.catalogue.domain.command.CatalogueCommands.CatalogueInterPraPage;
import ministere.sante.senpna.catalogue.domain.command.CatalogueCommands.CataloguePage;
import ministere.sante.senpna.catalogue.domain.command.CatalogueCommands.ConditionnementCatalogue;
import ministere.sante.senpna.catalogue.domain.command.CatalogueCommands.ConsulterCatalogueInterPraQuery;
import ministere.sante.senpna.catalogue.domain.command.CatalogueCommands.ConsulterCatalogueNationalQuery;
import ministere.sante.senpna.catalogue.domain.command.CatalogueCommands.ConsulterCatalogueRegionalQuery;
import ministere.sante.senpna.catalogue.domain.command.CatalogueCommands.DisponibilitePra;
import ministere.sante.senpna.catalogue.domain.command.CatalogueCommands.LigneCatalogue;
import ministere.sante.senpna.catalogue.domain.command.CatalogueCommands.LigneCatalogueInterPra;
import ministere.sante.senpna.catalogue.infrastructure.web.dto.response.ConditionnementCatalogueResponse;
import ministere.sante.senpna.catalogue.infrastructure.web.dto.response.DisponibilitePraResponse;
import ministere.sante.senpna.catalogue.infrastructure.web.dto.response.LigneCatalogueInterPraResponse;
import ministere.sante.senpna.catalogue.infrastructure.web.dto.response.LigneCatalogueResponse;
import ministere.sante.senpna.shared.infrastructure.web.response.RestResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

/**
 * Consultation des catalogues (cf. doc. produit « Catalogue ») — trois
 * vues calculées, jamais persistées, sur les lignes de stock déjà
 * existantes :
 *
 * <pre>
 * GET /api/catalogue/national?recherche=&ruptureUniquement=&page=&size=
 *     → stock de la PNA centrale, agrégé par médicament — visible par les PRA et la PNA.
 *
 * GET /api/catalogue/inter-pra?recherche=&medicamentId=&ruptureUniquement=&page=&size=
 *     → disponibilités de toutes les PRA, ventilées PRA par PRA — visible par toutes les PRA et la PNA.
 *
 * GET /api/catalogue/regional?regionId=&recherche=&ruptureUniquement=&page=&size=
 *     → stock de la PRA d'une région, agrégé par médicament — visible uniquement par les
 *       structures sanitaires de cette région (regionId n'est utilisable que par un acteur PNA ;
 *       pour toute autre personne, sa propre région est utilisée quel que soit ce paramètre).
 * </pre>
 */
@RestController
@RequestMapping("/api/catalogue")
public class CatalogueController {

        private static final String ROLES_PNA_PRA = "hasAnyRole('ADMIN_PNA','GESTIONNAIRE_PNA','PHARMACIEN_PNA',"
                        + "'MAGASINIER_PNA','ADMIN_PRA','GESTIONNAIRE_PRA','PHARMACIEN_PRA','MAGASINIER_PRA')";

        private static final String ROLES_REGIONAL = "hasAnyRole('ADMIN_PNA','GESTIONNAIRE_PNA','PHARMACIEN_PNA',"
                        + "'MAGASINIER_PNA','ADMIN_PRA','GESTIONNAIRE_PRA','PHARMACIEN_PRA','MAGASINIER_PRA',"
                        + "'GESTIONNAIRE_STRUCTURE')";

        private final CatalogueFacade catalogueFacade;

        public CatalogueController(CatalogueFacade catalogueFacade) {
                this.catalogueFacade = catalogueFacade;
        }

        @GetMapping("/national")
        @PreAuthorize(ROLES_PNA_PRA)
        public ResponseEntity<Map<String, Object>> catalogueNational(
                        @RequestParam(required = false) String recherche,
                        @RequestParam(required = false) Boolean ruptureUniquement,
                        @RequestParam(required = false, defaultValue = "0") Integer page,
                        @RequestParam(required = false, defaultValue = "20") Integer size) {

                CataloguePage result = catalogueFacade.consulterCatalogueNational(
                                new ConsulterCatalogueNationalQuery(recherche, ruptureUniquement, page, size));

                return ResponseEntity.ok(RestResponse.responsePaginate(
                                HttpStatus.OK,
                                result.content().stream().map(this::toResponse).toList(),
                                "CATALOGUE_NATIONAL_LISTED",
                                "Catalogue de la PNA récupéré",
                                result.page(),
                                result.totalPages(),
                                result.totalElements(),
                                result.page() == 0,
                                result.page() >= result.totalPages() - 1));
        }

        @GetMapping("/inter-pra")
        @PreAuthorize(ROLES_PNA_PRA)
        public ResponseEntity<Map<String, Object>> catalogueInterPra(
                        @RequestParam(required = false) String recherche,
                        @RequestParam(required = false) UUID medicamentId,
                        @RequestParam(required = false) Boolean ruptureUniquement,
                        @RequestParam(required = false, defaultValue = "0") Integer page,
                        @RequestParam(required = false, defaultValue = "20") Integer size) {

                CatalogueInterPraPage result = catalogueFacade.consulterCatalogueInterPra(
                                new ConsulterCatalogueInterPraQuery(recherche, medicamentId, ruptureUniquement, page,
                                                size));

                return ResponseEntity.ok(RestResponse.responsePaginate(
                                HttpStatus.OK,
                                result.content().stream().map(this::toResponse).toList(),
                                "CATALOGUE_INTER_PRA_LISTED",
                                "Catalogue inter-PRA récupéré",
                                result.page(),
                                result.totalPages(),
                                result.totalElements(),
                                result.page() == 0,
                                result.page() >= result.totalPages() - 1));
        }

        @GetMapping("/regional")
        @PreAuthorize(ROLES_REGIONAL)
        public ResponseEntity<Map<String, Object>> catalogueRegional(
                        @RequestParam(required = false) UUID regionId,
                        @RequestParam(required = false) String recherche,
                        @RequestParam(required = false) Boolean ruptureUniquement,
                        @RequestParam(required = false, defaultValue = "0") Integer page,
                        @RequestParam(required = false, defaultValue = "20") Integer size) {

                CataloguePage result = catalogueFacade.consulterCatalogueRegional(
                                new ConsulterCatalogueRegionalQuery(regionId, recherche, ruptureUniquement, page,
                                                size));

                return ResponseEntity.ok(RestResponse.responsePaginate(
                                HttpStatus.OK,
                                result.content().stream().map(this::toResponse).toList(),
                                "CATALOGUE_REGIONAL_LISTED",
                                "Catalogue régional récupéré",
                                result.page(),
                                result.totalPages(),
                                result.totalElements(),
                                result.page() == 0,
                                result.page() >= result.totalPages() - 1));
        }

        private LigneCatalogueResponse toResponse(LigneCatalogue ligne) {
                return new LigneCatalogueResponse(ligne.medicamentId(), ligne.code(), ligne.nomCommercial(),
                                ligne.dci(),
                                ligne.familleNom(), ligne.fabricant(), ligne.fournisseurNom(),
                                ligne.quantiteDisponibleALaVente(),
                                ligne.nombreLotsActifs(), ligne.prochaineDateExpiration(), ligne.enRupture(),
                                ligne.conditionnements().stream().map(this::toResponse).toList());
        }

        private LigneCatalogueInterPraResponse toResponse(LigneCatalogueInterPra ligne) {
                return new LigneCatalogueInterPraResponse(ligne.medicamentId(), ligne.code(), ligne.nomCommercial(),
                                ligne.dci(), ligne.familleNom(), ligne.fabricant(), ligne.quantiteTotaleReseau(),
                                ligne.conditionnements().stream().map(this::toResponse).toList(),
                                ligne.disponibilites().stream().map(this::toResponse).toList());
        }

        private DisponibilitePraResponse toResponse(DisponibilitePra disponibilite) {
                return new DisponibilitePraResponse(disponibilite.entrepotId(), disponibilite.codeEntrepot(),
                                disponibilite.nomEntrepot(), disponibilite.regionId(), disponibilite.fournisseurNom(),
                                disponibilite.quantiteDisponibleALaVente(), disponibilite.prochaineDateExpiration());
        }

        private ConditionnementCatalogueResponse toResponse(ConditionnementCatalogue conditionnement) {
                return new ConditionnementCatalogueResponse(conditionnement.id(), conditionnement.nom(),
                                conditionnement.niveau(), conditionnement.quantiteUniteBase(),
                                conditionnement.prixAchat(),
                                conditionnement.prixVente());
        }
}
