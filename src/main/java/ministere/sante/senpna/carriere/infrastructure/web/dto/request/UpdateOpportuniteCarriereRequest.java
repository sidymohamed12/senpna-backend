package ministere.sante.senpna.carriere.infrastructure.web.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * @param ficheDePosteUrl URL de la fiche de poste à conserver telle
 *                        quelle, à remplacer par une nouvelle URL après
 *                        un nouvel upload, ou {@code null} pour la
 *                        retirer — cf. {@code CreateOpportuniteCarriereRequest}.
 */
public record UpdateOpportuniteCarriereRequest(

                @NotBlank(message = "Le titre est obligatoire") @Size(max = 200, message = "Le titre ne peut pas dépasser 200 caractères") String titre,

                @NotBlank(message = "Le nom de l'entreprise est obligatoire") @Size(max = 200, message = "Le nom de l'entreprise ne peut pas dépasser 200 caractères") String nomEntreprise,

                @NotBlank(message = "La description est obligatoire") @Size(max = 8000, message = "La description ne peut pas dépasser 8000 caractères") String description,

                @Size(max = 1000, message = "L'URL de la fiche de poste ne peut pas dépasser 1000 caractères") String ficheDePosteUrl,

                @NotBlank(message = "Le lieu est obligatoire") @Size(max = 200, message = "Le lieu ne peut pas dépasser 200 caractères") String lieu,

                @NotBlank(message = "Le type de contrat est obligatoire") String typeContrat,

                LocalDate dateDebut,

                @NotNull(message = "La date limite de candidature est obligatoire") LocalDate dateLimiteCandidature,

                @Email(message = "L'e-mail de contact est invalide") String emailContact) {
}
