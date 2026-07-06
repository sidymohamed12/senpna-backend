package ministere.sante.senpna.projet.infrastructure.web.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreateProjetRequest(

                @NotBlank(message = "La catégorie est obligatoire") String categorie,

                @NotBlank(message = "Le nom du projet est obligatoire") @Size(max = 200, message = "Le nom ne peut pas dépasser 200 caractères") String nom,

                @Size(max = 500, message = "La description ne peut pas dépasser 500 caractères") String description,

                @Size(max = 10, message = "Un projet ne peut pas avoir plus de 10 objectifs") List<@Size(max = 200, message = "Un objectif ne peut pas dépasser 200 caractères") String> objectifs,

                @Size(max = 10, message = "Un projet ne peut pas avoir plus de 10 impacts") List<@Size(max = 200, message = "Un impact ne peut pas dépasser 200 caractères") String> impacts,

                @Size(max = 1000, message = "L'URL de l'image ne peut pas dépasser 1000 caractères") String imageUrl) {
}
