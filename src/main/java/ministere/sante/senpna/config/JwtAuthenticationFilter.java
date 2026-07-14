package ministere.sante.senpna.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import ministere.sante.senpna.auth.infrastructure.security.AuthUserPrincipal;
import ministere.sante.senpna.shared.domain.port.out.TokenRevocationPort;

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

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String MDC_USER_ID = "userId";

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final TokenRevocationPort tokenRevocationPort;

    public JwtAuthenticationFilter(JwtService jwtService, UserDetailsService userDetailsService,
            TokenRevocationPort tokenRevocationPort) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.tokenRevocationPort = tokenRevocationPort;
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
            // ── Vérification révocation (liste noire Redis) ──────────────
            if (tokenRevocationPort.estInvalide(token)) {
                log.debug("[JWT] Token révoqué, accès refusé.");
                filterChain.doFilter(request, response);
                return;
            }

            // ── Validation signature + expiration + UserDetails ───────────
            String username = jwtService.extractUsername(token);

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                if (jwtService.isTokenValid(token, userDetails)) {
                    // ── Affectation organisationnelle portée par le JWT ───────
                    // (cf. AuthTokenFactory) : évite une requête DB par requête
                    // pour le scoping entrepôt du module stock.
                    if (userDetails instanceof AuthUserPrincipal principal) {
                        principal.setEntrepotId(jwtService.extractEntrepotId(token));
                        principal.setStructureSanitaireId(jwtService.extractStructureSanitaireId(token));
                        principal.setFournisseurId(jwtService.extractFournisseurId(token));
                    }

                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());

                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);

                    MDC.put(MDC_USER_ID, username);
                }
            }
        } catch (Exception e) {
            log.debug("[JWT] Token invalide : {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}
