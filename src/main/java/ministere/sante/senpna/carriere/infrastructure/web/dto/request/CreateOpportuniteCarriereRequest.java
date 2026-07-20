package ministere.sante.senpna.carriere.infrastructure.web.dto.request;

import ministere.sante.senpna.shared.infrastructure.validation.NoHtml;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * @param ficheDePosteUrl URL publique optionnelle, déjà uploadée par le
 *                        front vers le stockage objet via
 *                        {@code POST /api/medias/presigned-url} avec
 *                        {@code mediaType=FICHE_DE_POSTE} — l'API ne
 *                        reçoit jamais le fichier lui-même.
 */
public record CreateOpportuniteCarriereRequest(

                @NotBlank(message = "Le titre est obligatoire") @Size(max = 200, message = "Le titre ne peut pas dépasser 200 caractères") @NoHtml String titre,

                @NotBlank(message = "Le nom de l'entreprise est obligatoire") @Size(max = 200, message = "Le nom de l'entreprise ne peut pas dépasser 200 caractères") @NoHtml String nomEntreprise,

                @NotBlank(message = "La description est obligatoire") @Size(max = 8000, message = "La description ne peut pas dépasser 8000 caractères") @NoHtml String description,

                @Size(max = 1000, message = "L'URL de la fiche de poste ne peut pas dépasser 1000 caractères") String ficheDePosteUrl,

                @NotBlank(message = "Le lieu est obligatoire") @Size(max = 200, message = "Le lieu ne peut pas dépasser 200 caractères") @NoHtml String lieu,

                @NotBlank(message = "Le type de contrat est obligatoire") String typeContrat,

                LocalDate dateDebut,

                @NotNull(message = "La date limite de candidature est obligatoire") LocalDate dateLimiteCandidature,

                @Email(message = "L'e-mail de contact est invalide") String emailContact) {
}
