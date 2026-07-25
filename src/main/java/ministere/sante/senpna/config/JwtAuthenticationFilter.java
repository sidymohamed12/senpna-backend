package ministere.sante.senpna.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import ministere.sante.senpna.auth.infrastructure.security.AuthUserPrincipal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Authentifie la requête à partir d'un JWT Bearer, en s'appuyant sur
 * {@link JwtTokenService} (jwt-toolkit) au lieu de l'ancien {@code JwtService}
 * maison.
 * <p>
 * Un seul appel à {@link JwtTokenService#parse(String)} suffit désormais :
 * signature, expiration <strong>et</strong> révocation (via
 * {@code RedisTokenRevocationPort}, branché automatiquement par
 * l'auto-configuration) sont vérifiées en un point unique — plus besoin
 * d'un appel séparé à {@code TokenRevocationPort.estInvalide(token)} en
 * amont, comme c'était le cas avant migration.
*/

import com.sidymohamed12.jwt.core.claims.JwtClaims;
import com.sidymohamed12.jwt.core.exception.JwtValidationException;
import com.sidymohamed12.jwt.core.token.JwtTokenService;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String MDC_USER_ID = "userId";

    private final JwtTokenService jwtTokenService;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtTokenService jwtTokenService, UserDetailsService userDetailsService) {
        this.jwtTokenService = jwtTokenService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(BEARER_PREFIX.length());

        try {
            // Signature + expiration + révocation, en un seul appel.
            JwtClaims claims = jwtTokenService.parse(token);
            String username = claims.subject();

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                // ── Affectation organisationnelle portée par le JWT ───────
                // (cf. AuthTokenFactory) : évite une requête DB par requête
                // pour le scoping entrepôt du module stock.
                if (userDetails instanceof AuthUserPrincipal principal) {
                    claims.getUUID("entrepotId").ifPresent(principal::setEntrepotId);
                    claims.getUUID("structureSanitaireId").ifPresent(principal::setStructureSanitaireId);
                    claims.getUUID("fournisseurId").ifPresent(principal::setFournisseurId);
                }

                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());

                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);

                MDC.put(MDC_USER_ID, username);
            }
        } catch (JwtValidationException e) {
            log.debug("[JWT] Token invalide : {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}