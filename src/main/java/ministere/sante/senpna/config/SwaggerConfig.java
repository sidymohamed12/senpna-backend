package ministere.sante.senpna.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.List;

@Configuration
@Profile("dev")
public class SwaggerConfig {

        private static final String SECURITY_SCHEME_NAME = "BearerAuth";

        /**
         * Bean OpenAPI — métadonnées, schéma de sécurité JWT, serveurs.
         *
         * <p>
         * Le schéma {@code BearerAuth} est appliqué globalement via
         * {@link SecurityRequirement} : pas besoin d'annoter chaque endpoint
         * avec {@code @SecurityRequirement} dans les contrôleurs.
         * </p>
         */
        @Bean
        public OpenAPI senPharmaFlowOpenApi() {
                return new OpenAPI()
                                .info(apiInfo())
                                .servers(List.of(
                                                new Server()
                                                                .url("http://localhost:8080")
                                                                .description("Serveur de développement local")))
                                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                                .components(new Components()
                                                .addSecuritySchemes(SECURITY_SCHEME_NAME, jwtSecurityScheme()));
        }

        // ── Métadonnées ───────────────────────────────────────────────────────

        private Info apiInfo() {
                return new Info()
                                .title("SEN PharmaFlow API")
                                .description("""
                                                API REST de gestion de la chaîne pharmaceutique publique du Sénégal.

                                                **Acteurs** : PNA (Pharmacie Nationale), PRA (Pharmacies Régionales),
                                                Hôpitaux, Centres de Santé, Districts Sanitaires.

                                                **Authentification** : JWT Bearer — obtenez un token via `POST /api/auth/login`,
                                                puis cliquez sur **Authorize** et collez le token.
                                                """)
                                .version("1.0.0")
                                .contact(new Contact()
                                                .name("Développeur - Sidy Mohamed Saizonou")
                                                .email("mohamedsaizonou86@gmail.com"))
                                .license(new License()
                                                .name("Usage interne — Pharmacie Nationale d'Approvisionnement du Sénégal")
                                                .url("https://www.sen-pna.sn/"));
        }

        // ── Schéma de sécurité JWT ────────────────────────────────────────────

        private SecurityScheme jwtSecurityScheme() {
                return new SecurityScheme()
                                .name(SECURITY_SCHEME_NAME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Token JWT obtenu via POST /api/auth/login. Format : Bearer <token>");
        }
}
