package ministere.sante.senpna.carriere.infrastructure.web.dto.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/**
 * @param cvUrl               URL publique du CV, déjà uploadé par le
 *                            candidat via
 *                            {@code POST /api/medias/presigned-url/public}
 *                            avec {@code mediaType=CV} — obligatoire.
 * @param lettreMotivationUrl URL publique de la lettre de motivation,
 *                            même flux avec
 *                            {@code mediaType=LETTRE_DE_MOTIVATION} —
 *                            optionnelle. L'API ne reçoit jamais les
 *                            fichiers eux-mêmes.
 */
public record SoumettreCandidatureRequest(

                @NotNull(message = "L'opportunité ciblée est obligatoire") UUID opportuniteId,

                @NotBlank(message = "La civilité est obligatoire") String civilite,

                @NotBlank(message = "Le nom complet est obligatoire") @Size(max = 200, message = "Le nom complet ne peut pas dépasser 200 caractères") String nomComplet,

                @NotBlank(message = "L'e-mail est obligatoire") @Email(message = "L'adresse e-mail est invalide") String email,

                @NotBlank(message = "Le téléphone est obligatoire") String telephone,

                @NotBlank(message = "Le CV est obligatoire") @Size(max = 1000) String cvUrl,

                @Size(max = 1000) String lettreMotivationUrl,

                @Size(max = 2000, message = "Le message ne peut pas dépasser 2000 caractères") String messageComplementaire,

                @AssertTrue(message = "Le consentement RGPD est obligatoire pour postuler") boolean consentementRgpd) {
}
