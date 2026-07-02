package ministere.sante.senpna.organisation.infrastructure.web.dto.response;

import ministere.sante.senpna.organisation.domain.valueobject.StatutAdhesion;
import ministere.sante.senpna.organisation.domain.valueobject.TypeStructureSanitaire;

import java.time.Instant;
import java.util.UUID;

public record StructureSanitaireResponse(
        UUID id,
        String code,
        String nom,
        TypeStructureSanitaire type,
        UUID regionId,
        String regionNom,
        UUID praId,
        String praNom,
        String district,
        String adresse,
        String telephone,
        String email,
        String responsableNom,
        String responsablePrenom,
        StatutAdhesion statutAdhesion,
        String motifRejet,
        boolean actif,
        Instant createdAt,
        Instant updatedAt) {
}
