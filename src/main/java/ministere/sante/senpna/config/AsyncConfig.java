package ministere.sante.senpna.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Configuration des executors asynchrones — {@code @Async} et
 * {@code @Scheduled}.
 *
 * <h3>Executors configurés</h3>
 * <table>
 * <tr>
 * <th>Bean</th>
 * <th>Usage</th>
 * <th>Threads</th>
 * </tr>
 * <tr>
 * <td>{@code taskExecutor} (défaut)</td>
 * <td>@Async général, domain events</td>
 * <td>5–20</td>
 * </tr>
 * <tr>
 * <td>{@code alerteExecutor}</td>
 * <td>Scheduler alertes péremption</td>
 * <td>2</td>
 * </tr>
 * </table>
 *
 * <h3>Pourquoi des executors séparés ?</h3>
 * <p>
 * Le scheduler d'alertes est une tâche de fond potentiellement longue
 * (scan de tous les stocks). Lui dédier un pool évite de saturer le pool
 * général utilisé par les requêtes HTTP asynchrones.
 * </p>
 *
 * <h3>MDC propagation</h3>
 * <p>
 * Le {@link org.slf4j.MDC} n'est pas automatiquement propagé aux threads
 * du pool asynchrone. Pour la traçabilité des domain events, chaque
 * {@code @EventListener} doit copier les clés MDC nécessaires.
 * </p>
 *
 * <h3>Graceful shutdown</h3>
 * <p>
 * {@code setWaitForTasksToCompleteOnShutdown(true)} avec un timeout de 30s
 * garantit que les tâches en cours terminent proprement lors d'un
 * déploiement sans coupure.
 * </p>
 */
@Configuration
@EnableAsync
@EnableScheduling
public class AsyncConfig implements AsyncConfigurer {

    private static final Logger log = LoggerFactory.getLogger(AsyncConfig.class);

    /**
     * Executor principal — utilisé par {@code @Async} sans qualificateur.
     *
     * <p>
     * Paramètres calibrés pour SenPharmaFlow (charge modérée) :
     * <ul>
     * <li>corePoolSize=5 : 5 threads toujours actifs</li>
     * <li>maxPoolSize=20 : jusqu'à 20 threads en pic</li>
     * <li>queueCapacity=100 : file d'attente avant création de nouveaux
     * threads</li>
     * </ul>
     * </p>
     */
    @Override
    @Bean(name = "taskExecutor")
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("senpna-async-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();
        return executor;
    }

    /**
     * Executor dédié aux schedulers d'alertes (péremption, rupture de stock).
     *
     * <p>
     * Pool limité à 2 threads : les tâches planifiées sont séquentielles
     * par nature (une exécution à la fois par cron). 2 threads permettent
     * un léger parallélisme en cas de retard d'exécution.
     * </p>
     */
    @Bean(name = "alerteExecutor")
    public Executor alerteExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(10);
        executor.setThreadNamePrefix("senpna-alerte-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60); // Les tâches de scan peuvent être longues
        executor.initialize();
        return executor;
    }

    /**
     * Gestionnaire d'exceptions non capturées dans les méthodes {@code @Async}.
     *
     * <p>
     * Sans ce handler, les exceptions sont silencieusement avalées.
     * Ce handler logue l'erreur avec le nom de la méthode concernée.
     * </p>
     */
    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (throwable, method, params) -> log.error("[Async] Exception non capturée dans '{}' : {}",
                method.getName(), throwable.getMessage(), throwable);
    }
}
