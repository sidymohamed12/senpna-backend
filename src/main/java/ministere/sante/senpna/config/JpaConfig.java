package ministere.sante.senpna.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Configuration JPA — active les repositories et la gestion des transactions.
 *
 * <h3>basePackages</h3>
 * <p>
 * Scanne tous les sous-packages de {@code ministere.sante.senpna} pour
 * découvrir les interfaces {@code JpaRepository}. Chaque module (medicament,
 * commande, stock…) expose son propre repository dans son package
 * {@code infrastructure/persistence}.
 * </p>
 *
 * <h3>@EnableTransactionManagement</h3>
 * <p>
 * Active le support des annotations {@code @Transactional} par proxy AOP.
 * Les use cases et adapters utilisent {@code @Transactional} pour délimiter
 * les unités de travail. Sans cette annotation, {@code @Transactional} est
 * ignorée silencieusement.
 * </p>
 *
 * <h3>Convention de nommage des colonnes</h3>
 * <p>
 * Spring Boot configure automatiquement la stratégie
 * {@code SpringPhysicalNamingStrategy} : {@code camelCase} → {@code snake_case}.
 * Ex: {@code dateExpiration} → {@code date_expiration}.
 * Aucune configuration supplémentaire n'est nécessaire.
 * </p>
 *
 * <h3>Flyway</h3>
 * <p>
 * La gestion du schéma est déléguée à Flyway (configuré dans
 * {@code application.yml}). Hibernate est en mode {@code validate}
 * (pas de DDL auto) pour éviter toute modification de schéma accidentelle
 * en production.
 * </p>
 */
@Configuration
@EnableJpaRepositories(basePackages = "ministere.sante.senpna")
@EnableTransactionManagement
public class JpaConfig {

    /*
     * Aucun bean supplémentaire n'est nécessaire ici.
     *
     * Spring Boot auto-configure :
     * - DataSource (depuis spring.datasource.*)
     * - EntityManagerFactory (depuis spring.jpa.*)
     * - PlatformTransactionManager
     * - HibernateJpaVendorAdapter
     *
     * Les propriétés JPA sont centralisées dans application-{profile}.yml :
     *
     *   spring:
     *     jpa:
     *       hibernate:
     *         ddl-auto: validate          # jamais create/update en prod
     *       show-sql: false               # true uniquement en dev/debug
     *       properties:
     *         hibernate:
     *           format_sql: true
     *           jdbc:
     *             batch_size: 25          # batch insert/update Hibernate
     *           order_inserts: true
     *           order_updates: true
     */
}
