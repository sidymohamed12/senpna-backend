package ministere.sante.senpna;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import ministere.sante.senpna.config.AppProperties;

/**
 * Point d'entrée de l'application SEN PharmaFlow.
 *
 * <h3>Profils Spring</h3>
 * <p>
 * L'application supporte trois profils :
 * <ul>
 * <li>{@code dev} — base locale, Redis local, logs verbeux, Swagger actif</li>
 * <li>{@code prod} — PostgreSQL distant, Redis sécurisé, logs JSON, Swagger
 * désactivé</li>
 * <li>{@code test} — H2 in-memory, cache in-memory, rate limit in-memory</li>
 * </ul>
 * Activation : {@code --spring.profiles.active=dev} ou variable d'env
 * {@code SPRING_PROFILES_ACTIVE=prod}.
 * </p>
 *
 * <h3>@EnableConfigurationProperties</h3>
 * <p>
 * Active la liaison des propriétés typées {@link AppProperties}.
 * Toutes les propriétés {@code app.*} sont validées au démarrage —
 * si une propriété obligatoire est manquante, l'application refuse
 * de démarrer avec un message d'erreur explicite.
 * </p>
 */
@SpringBootApplication
@EnableConfigurationProperties(AppProperties.class)
public class SenpnaApplication {

	public static void main(String[] args) {
		SpringApplication.run(SenpnaApplication.class, args);
	}

}
