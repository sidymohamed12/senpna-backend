package ministere.sante.senpna.organisation.application.facade;

import ministere.sante.senpna.organisation.domain.command.OrganisationCommands.*;
import ministere.sante.senpna.organisation.domain.port.in.*;
import ministere.sante.senpna.organisation.domain.valueobject.StatutAdhesion;
import ministere.sante.senpna.organisation.domain.valueobject.TypeEntrepot;
import ministere.sante.senpna.organisation.domain.valueobject.TypeStructureSanitaire;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests unitaires de {@link OrganisationFacade} — vérifie que chaque
 * méthode délègue à l'use case correspondant avec la commande/requête
 * fournie, sans logique métier propre (même esprit que
 * {@code UserManagementFacadeTest}).
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("OrganisationFacade")
class OrganisationFacadeTest {

    @Mock
    CreateRegionUseCase createRegionUseCase;
    @Mock
    GetRegionUseCase getRegionUseCase;
    @Mock
    ListRegionsUseCase listRegionsUseCase;
    @Mock
    CreatePraUseCase createPraUseCase;
    @Mock
    UpdatePraUseCase updatePraUseCase;
    @Mock
    DeactivatePraUseCase deactivatePraUseCase;
    @Mock
    ActivatePraUseCase activatePraUseCase;
    @Mock
    GetEntrepotUseCase getEntrepotUseCase;
    @Mock
    ListEntrepotsUseCase listEntrepotsUseCase;
    @Mock
    CreateStructureSanitaireUseCase createStructureSanitaireUseCase;
    @Mock
    UpdateStructureSanitaireUseCase updateStructureSanitaireUseCase;
    @Mock
    ValidateAdhesionUseCase validateAdhesionUseCase;
    @Mock
    RejectAdhesionUseCase rejectAdhesionUseCase;
    @Mock
    ActivateStructureSanitaireUseCase activateStructureSanitaireUseCase;
    @Mock
    DeactivateStructureSanitaireUseCase deactivateStructureSanitaireUseCase;
    @Mock
    AssignStructureToRegionUseCase assignStructureToRegionUseCase;
    @Mock
    AssignStructureToPraUseCase assignStructureToPraUseCase;
    @Mock
    GetStructureSanitaireUseCase getStructureSanitaireUseCase;
    @Mock
    ListStructuresSanitairesUseCase listStructuresSanitairesUseCase;
    @Mock
    AssignUserToEntrepotUseCase assignUserToEntrepotUseCase;
    @Mock
    AssignUserToStructureUseCase assignUserToStructureUseCase;
    @Mock
    UnassignUserUseCase unassignUserUseCase;

    @InjectMocks
    OrganisationFacade sut;

    private static final UUID ID = UUID.randomUUID();

    private static final RegionDetail REGION_DETAIL = new RegionDetail(ID, "DAKAR", "Dakar", true, Instant.now(),
            Instant.now());

    private static final EntrepotDetail ENTREPOT_DETAIL = new EntrepotDetail(ID, "PRA-DAKAR", "PRA Dakar",
            TypeEntrepot.PRA, ID, "Dakar", null, null, null, true, Instant.now(), Instant.now());

    private static final StructureSanitaireDetail STRUCTURE_DETAIL = new StructureSanitaireDetail(ID, "HOP-X",
            "Hôpital X", TypeStructureSanitaire.HOPITAL, ID, "Dakar", null, null, null, null, null, null, "Ndiaye",
            "Fatou", StatutAdhesion.EN_ATTENTE_VALIDATION, null, false, Instant.now(), Instant.now());

    private static final UserAffectationDetail AFFECTATION_DETAIL = new UserAffectationDetail(ID, ID, null);

    // ── Région ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("creerRegion() délègue à CreateRegionUseCase")
    void creerRegion_delegue() {
        CreateRegionCommand command = new CreateRegionCommand("DAKAR", "Dakar");
        when(createRegionUseCase.creer(command)).thenReturn(REGION_DETAIL);

        assertThat(sut.creerRegion(command)).isEqualTo(REGION_DETAIL);
        verify(createRegionUseCase).creer(command);
    }

    @Test
    @DisplayName("obtenirRegion() délègue à GetRegionUseCase")
    void obtenirRegion_delegue() {
        GetRegionQuery query = new GetRegionQuery(ID);
        when(getRegionUseCase.obtenir(query)).thenReturn(REGION_DETAIL);

        assertThat(sut.obtenirRegion(query)).isEqualTo(REGION_DETAIL);
        verify(getRegionUseCase).obtenir(query);
    }

    @Test
    @DisplayName("listerRegions() délègue à ListRegionsUseCase")
    void listerRegions_delegue() {
        when(listRegionsUseCase.lister()).thenReturn(List.of(REGION_DETAIL));

        assertThat(sut.listerRegions()).containsExactly(REGION_DETAIL);
        verify(listRegionsUseCase).lister();
    }

    // ── PRA / Entrepôt ──────────────────────────────────────────────────

    @Test
    @DisplayName("creerPra() délègue à CreatePraUseCase")
    void creerPra_delegue() {
        CreatePraCommand command = new CreatePraCommand("PRA-DAKAR", "PRA Dakar", ID, null, null);
        when(createPraUseCase.creer(command)).thenReturn(ENTREPOT_DETAIL);

        assertThat(sut.creerPra(command)).isEqualTo(ENTREPOT_DETAIL);
        verify(createPraUseCase).creer(command);
    }

    @Test
    @DisplayName("modifierPra() délègue à UpdatePraUseCase")
    void modifierPra_delegue() {
        UpdatePraCommand command = new UpdatePraCommand(ID, ID, "PRA Dakar", null, null, null);
        when(updatePraUseCase.modifier(command)).thenReturn(ENTREPOT_DETAIL);

        assertThat(sut.modifierPra(command)).isEqualTo(ENTREPOT_DETAIL);
        verify(updatePraUseCase).modifier(command);
    }

    @Test
    @DisplayName("desactiverPra() délègue à DeactivatePraUseCase")
    void desactiverPra_delegue() {
        DeactivatePraCommand command = new DeactivatePraCommand(ID, ID);
        when(deactivatePraUseCase.desactiver(command)).thenReturn(ENTREPOT_DETAIL);

        assertThat(sut.desactiverPra(command)).isEqualTo(ENTREPOT_DETAIL);
        verify(deactivatePraUseCase).desactiver(command);
    }

    @Test
    @DisplayName("activerPra() délègue à ActivatePraUseCase")
    void activerPra_delegue() {
        ActivatePraCommand command = new ActivatePraCommand(ID, ID);
        when(activatePraUseCase.activer(command)).thenReturn(ENTREPOT_DETAIL);

        assertThat(sut.activerPra(command)).isEqualTo(ENTREPOT_DETAIL);
        verify(activatePraUseCase).activer(command);
    }

    @Test
    @DisplayName("obtenirEntrepot() délègue à GetEntrepotUseCase")
    void obtenirEntrepot_delegue() {
        GetEntrepotQuery query = new GetEntrepotQuery(ID);
        when(getEntrepotUseCase.obtenir(query)).thenReturn(ENTREPOT_DETAIL);

        assertThat(sut.obtenirEntrepot(query)).isEqualTo(ENTREPOT_DETAIL);
        verify(getEntrepotUseCase).obtenir(query);
    }

    @Test
    @DisplayName("listerEntrepots() délègue à ListEntrepotsUseCase")
    void listerEntrepots_delegue() {
        ListEntrepotsQuery query = new ListEntrepotsQuery(null, null, null, null, 0, 20, null, null);
        EntrepotPage page = new EntrepotPage(List.of(ENTREPOT_DETAIL), 0, 20, 1, 1);
        when(listEntrepotsUseCase.lister(query)).thenReturn(page);

        assertThat(sut.listerEntrepots(query)).isEqualTo(page);
        verify(listEntrepotsUseCase).lister(query);
    }

    // ── Structure sanitaire ─────────────────────────────────────────────

    @Test
    @DisplayName("creerStructureSanitaire() délègue à CreateStructureSanitaireUseCase")
    void creerStructureSanitaire_delegue() {
        CreateStructureSanitaireCommand command = new CreateStructureSanitaireCommand("HOP-X", "Hôpital X",
                TypeStructureSanitaire.HOPITAL, ID, null, null, null, null, "Ndiaye", "Fatou");
        when(createStructureSanitaireUseCase.creer(command)).thenReturn(STRUCTURE_DETAIL);

        assertThat(sut.creerStructureSanitaire(command)).isEqualTo(STRUCTURE_DETAIL);
        verify(createStructureSanitaireUseCase).creer(command);
    }

    @Test
    @DisplayName("modifierStructureSanitaire() délègue à UpdateStructureSanitaireUseCase")
    void modifierStructureSanitaire_delegue() {
        UpdateStructureSanitaireCommand command = new UpdateStructureSanitaireCommand(ID, "Hôpital X", null, null,
                null, null, null, null);
        when(updateStructureSanitaireUseCase.modifier(command)).thenReturn(STRUCTURE_DETAIL);

        assertThat(sut.modifierStructureSanitaire(command)).isEqualTo(STRUCTURE_DETAIL);
        verify(updateStructureSanitaireUseCase).modifier(command);
    }

    @Test
    @DisplayName("validerAdhesion() délègue à ValidateAdhesionUseCase")
    void validerAdhesion_delegue() {
        ValidateAdhesionCommand command = new ValidateAdhesionCommand(ID);
        when(validateAdhesionUseCase.valider(command)).thenReturn(STRUCTURE_DETAIL);

        assertThat(sut.validerAdhesion(command)).isEqualTo(STRUCTURE_DETAIL);
        verify(validateAdhesionUseCase).valider(command);
    }

    @Test
    @DisplayName("rejeterAdhesion() délègue à RejectAdhesionUseCase")
    void rejeterAdhesion_delegue() {
        RejectAdhesionCommand command = new RejectAdhesionCommand(ID, "motif");
        when(rejectAdhesionUseCase.rejeter(command)).thenReturn(STRUCTURE_DETAIL);

        assertThat(sut.rejeterAdhesion(command)).isEqualTo(STRUCTURE_DETAIL);
        verify(rejectAdhesionUseCase).rejeter(command);
    }

    @Test
    @DisplayName("activerStructureSanitaire() délègue à ActivateStructureSanitaireUseCase")
    void activerStructureSanitaire_delegue() {
        ActivateStructureSanitaireCommand command = new ActivateStructureSanitaireCommand(ID);
        when(activateStructureSanitaireUseCase.activer(command)).thenReturn(STRUCTURE_DETAIL);

        assertThat(sut.activerStructureSanitaire(command)).isEqualTo(STRUCTURE_DETAIL);
        verify(activateStructureSanitaireUseCase).activer(command);
    }

    @Test
    @DisplayName("desactiverStructureSanitaire() délègue à DeactivateStructureSanitaireUseCase")
    void desactiverStructureSanitaire_delegue() {
        DeactivateStructureSanitaireCommand command = new DeactivateStructureSanitaireCommand(ID);
        when(deactivateStructureSanitaireUseCase.desactiver(command)).thenReturn(STRUCTURE_DETAIL);

        assertThat(sut.desactiverStructureSanitaire(command)).isEqualTo(STRUCTURE_DETAIL);
        verify(deactivateStructureSanitaireUseCase).desactiver(command);
    }

    @Test
    @DisplayName("affecterStructureARegion() délègue à AssignStructureToRegionUseCase")
    void affecterStructureARegion_delegue() {
        AssignStructureToRegionCommand command = new AssignStructureToRegionCommand(ID, ID);
        when(assignStructureToRegionUseCase.affecter(command)).thenReturn(STRUCTURE_DETAIL);

        assertThat(sut.affecterStructureARegion(command)).isEqualTo(STRUCTURE_DETAIL);
        verify(assignStructureToRegionUseCase).affecter(command);
    }

    @Test
    @DisplayName("affecterStructureAPra() délègue à AssignStructureToPraUseCase")
    void affecterStructureAPra_delegue() {
        AssignStructureToPraCommand command = new AssignStructureToPraCommand(ID, ID);
        when(assignStructureToPraUseCase.affecter(command)).thenReturn(STRUCTURE_DETAIL);

        assertThat(sut.affecterStructureAPra(command)).isEqualTo(STRUCTURE_DETAIL);
        verify(assignStructureToPraUseCase).affecter(command);
    }

    @Test
    @DisplayName("obtenirStructureSanitaire() délègue à GetStructureSanitaireUseCase")
    void obtenirStructureSanitaire_delegue() {
        GetStructureSanitaireQuery query = new GetStructureSanitaireQuery(ID);
        when(getStructureSanitaireUseCase.obtenir(query)).thenReturn(STRUCTURE_DETAIL);

        assertThat(sut.obtenirStructureSanitaire(query)).isEqualTo(STRUCTURE_DETAIL);
        verify(getStructureSanitaireUseCase).obtenir(query);
    }

    @Test
    @DisplayName("listerStructuresSanitaires() délègue à ListStructuresSanitairesUseCase")
    void listerStructuresSanitaires_delegue() {
        ListStructuresSanitairesQuery query = new ListStructuresSanitairesQuery(null, null, null, null, null, null,
                0, 20, null, null);
        StructureSanitairePage page = new StructureSanitairePage(List.of(STRUCTURE_DETAIL), 0, 20, 1, 1);
        when(listStructuresSanitairesUseCase.lister(query)).thenReturn(page);

        assertThat(sut.listerStructuresSanitaires(query)).isEqualTo(page);
        verify(listStructuresSanitairesUseCase).lister(query);
    }

    // ── Affectation utilisateur ─────────────────────────────────────────

    @Test
    @DisplayName("affecterUtilisateurAEntrepot() délègue à AssignUserToEntrepotUseCase")
    void affecterUtilisateurAEntrepot_delegue() {
        AssignUserToEntrepotCommand command = new AssignUserToEntrepotCommand(ID, ID);
        when(assignUserToEntrepotUseCase.affecter(command)).thenReturn(AFFECTATION_DETAIL);

        assertThat(sut.affecterUtilisateurAEntrepot(command)).isEqualTo(AFFECTATION_DETAIL);
        verify(assignUserToEntrepotUseCase).affecter(command);
    }

    @Test
    @DisplayName("affecterUtilisateurAStructure() délègue à AssignUserToStructureUseCase")
    void affecterUtilisateurAStructure_delegue() {
        AssignUserToStructureCommand command = new AssignUserToStructureCommand(ID, ID);
        when(assignUserToStructureUseCase.affecter(command)).thenReturn(AFFECTATION_DETAIL);

        assertThat(sut.affecterUtilisateurAStructure(command)).isEqualTo(AFFECTATION_DETAIL);
        verify(assignUserToStructureUseCase).affecter(command);
    }

    @Test
    @DisplayName("retirerAffectationUtilisateur() délègue à UnassignUserUseCase")
    void retirerAffectationUtilisateur_delegue() {
        UnassignUserCommand command = new UnassignUserCommand(ID);
        when(unassignUserUseCase.retirer(command)).thenReturn(AFFECTATION_DETAIL);

        assertThat(sut.retirerAffectationUtilisateur(command)).isEqualTo(AFFECTATION_DETAIL);
        verify(unassignUserUseCase).retirer(command);
    }
}
