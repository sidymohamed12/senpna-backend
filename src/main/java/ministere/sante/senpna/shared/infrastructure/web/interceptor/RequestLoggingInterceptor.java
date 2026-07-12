package ministere.sante.senpna.shared.infrastructure.web.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.List;
import java.util.UUID;

/**
 * Intercepteur Spring MVC — logging des requêtes HTTP avec contexte MDC.
 *
 * <h3>Responsabilités</h3>
 * <ol>
 * <li>Génère un {@code requestId} UUID unique par requête (corrélation
 * Kibana/Loki).</li>
 * <li>Logue méthode, path et IP à l'entrée.</li>
 * <li>Logue status HTTP et durée à la sortie.</li>
 * <li>Nettoie le MDC dans {@code afterCompletion} — obligatoire sur les thread
 * pools
 * Tomcat pour éviter les fuites entre requêtes.</li>
 * </ol>
 *
 * <h3>MDC Keys injectées</h3>
 * <ul>
 * <li>{@code requestId} — UUID de corrélation</li>
 * <li>{@code method} — verbe HTTP</li>
 * <li>{@code path} — URI</li>
 * <li>{@code ip} — IP client</li>
 * </ul>
 *
 * <p>
 * Les annotations {@code @NonNull/@Nullable} de
 * {@code org.springframework.lang}
 * sont dépréciées depuis Spring 6.2 — on utilise ici les signatures natives
 * de {@link HandlerInterceptor} sans annotations de nullabilité redondantes.
 * </p>
 */
public class RequestLoggingInterceptor implements HandlerInterceptor {

    private static final String LOG_REQUEST_IN = "[→] {} {}";
    private static final String LOG_REQUEST_OUT = "[←] {} {} → {} ({}ms)";
    private static final String LOG_REQUEST_OUT_ERROR = "[←] {} {} → {} ({}ms) — {}";

    private static final List<String> TECHNICAL_PATHS = List.of(
            "/actuator",
            "/swagger-ui",
            "/v3/api-docs",
            "/favicon.ico");

    private static final String ATTR_START_TIME = "req_start_time";
    private static final String MDC_REQUEST_ID = "requestId";
    private static final String MDC_METHOD = "method";
    private static final String MDC_PATH = "path";
    private static final String MDC_IP = "ip";

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingInterceptor.class);

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler) {

        if (isTechnicalPath(request.getRequestURI())) {
            return true;
        }

        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        String requestId = UUID.randomUUID().toString();

        MDC.put(MDC_REQUEST_ID, requestId);
        MDC.put(MDC_METHOD, request.getMethod());
        MDC.put(MDC_PATH, request.getRequestURI());
        MDC.put(MDC_IP, extractIp(request));

        response.setHeader("X-Request-Id", requestId);
        request.setAttribute(ATTR_START_TIME, System.currentTimeMillis());

        log.info(LOG_REQUEST_IN, request.getMethod(), request.getRequestURI());

        return response.getStatus() < HttpServletResponse.SC_BAD_REQUEST;
    }

    @Override
    public void afterCompletion(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler,
            Exception ex) {

        if (isTechnicalPath(request.getRequestURI())) {
            return;
        }

        Long startTime = (Long) request.getAttribute(ATTR_START_TIME);
        long duration = startTime != null ? System.currentTimeMillis() - startTime : -1;
        int status = response.getStatus();

        if (ex != null) {
            log.error(LOG_REQUEST_OUT_ERROR, request.getMethod(),
                    request.getRequestURI(), status, duration, ex.getMessage());
        } else if (status >= 500) {
            log.error(LOG_REQUEST_OUT, request.getMethod(), request.getRequestURI(), status, duration);
        } else if (status >= 400) {
            log.warn(LOG_REQUEST_OUT, request.getMethod(), request.getRequestURI(), status, duration);
        } else {
            log.info(LOG_REQUEST_OUT, request.getMethod(), request.getRequestURI(), status, duration);
        }

        // Nettoyage obligatoire — threads Tomcat sont réutilisés entre requêtes
        MDC.remove(MDC_REQUEST_ID);
        MDC.remove(MDC_METHOD);
        MDC.remove(MDC_PATH);
        MDC.remove(MDC_IP);
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private boolean isTechnicalPath(String path) {
        return TECHNICAL_PATHS.stream()
                .anyMatch(path::startsWith);
    }

    private String extractIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp.trim();
        }
        return request.getRemoteAddr();
    }
}
