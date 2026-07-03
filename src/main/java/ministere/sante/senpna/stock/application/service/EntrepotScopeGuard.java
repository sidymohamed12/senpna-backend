package ministere.sante.senpna.stock.application.service;

import ministere.sante.senpna.shared.domain.valueobject.RolesNationaux;
import ministere.sante.senpna.shared.infrastructure.security.CurrentUser;
import ministere.sante.senpna.stock.domain.exception.AucunEntrepotAffecteException;
import ministere.sante.senpna.stock.domain.exception.PorteeEntrepotInterditeException;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Fait respecter la portée organisationnelle (entrepôt) des acteurs du
 * module {@code stock}, à partir des rôles Spring Security et du claim
 * {@code entrepotId} porté par le JWT (cf. {@code AuthTokenFactory} /
 * {@code JwtAuthenticationFilter}) — aucune requête base de données
 * supplémentaire, contrairement à {@code RegionScopeResolver} (module
 * {@code organisation}) qui résout la même notion pour des opérations
 * moins fréquentes.
 *
 * <h3>Règle</h3>
 * <ul>
 * <li>Un acteur possédant un rôle national (cf. {@link RolesNationaux} —
 * les rôles {@code *_PNA}) peut <strong>consulter</strong> les données de
 * n'importe quel entrepôt (le sien ou n'importe quelle PRA), y compris en
 * filtrant explicitement, mais ne peut <strong>modifier</strong> que les
 * données de son propre entrepôt.</li>
 * <li>Un acteur régional (rôle {@code *_PRA}) ne peut ni consulter ni
 * modifier que les données de son propre entrepôt — toute tentative de
 * cibler un autre entrepôt est refusée en écriture et silencieusement
 * ramenée à son propre entrepôt en lecture (le filtre demandé est
 * ignoré).</li>
 * </ul>
 */
@Component
public class EntrepotScopeGuard {

    /**
     * @return {@code true} si l'acteur courant possède au moins un rôle
     *         national (PNA) — portée illimitée en lecture.
     */
    public boolean estActeurNational() {
        return RolesNationaux.contientRoleNational(rolesCourants());
    }

    /**
     * @return l'entrepôt d'affectation de l'acteur courant, porté par le JWT.
     * @throws AucunEntrepotAffecteException si l'acteur n'est affecté à
     *                                       aucun entrepôt.
     */
    public UUID entrepotIdCourant() {
        UUID entrepotId = currentUser().getEntrepotId();
        if (entrepotId == null) {
            throw new AucunEntrepotAffecteException();
        }
        return entrepotId;
    }

    /**
     * À invoquer pour les opérations réservées aux acteurs nationaux (ex :
     * création d'un lot — cf. règle métier « les achats fournisseurs sont
     * effectués uniquement par la PNA »).
     *
     * @throws PorteeEntrepotInterditeException si l'acteur n'a aucun rôle national.
     */
    public void verifierActeurNational() {
        if (!estActeurNational()) {
            throw new PorteeEntrepotInterditeException();
        }
    }

    /**
     * À invoquer avant toute mutation portant sur un entrepôt précis
     * (entrée/sortie de stock, réservation, définition de seuil...).
     *
     * @throws PorteeEntrepotInterditeException si l'entrepôt ciblé n'est
     *                                          pas celui de l'acteur —
     *                                          qu'il soit PNA ou PRA :
     *                                          l'écriture est toujours
     *                                          limitée à son propre
     *                                          entrepôt.
     */
    public void verifierEcritureAutorisee(UUID entrepotIdCible) {
        if (entrepotIdCible == null || !entrepotIdCible.equals(entrepotIdCourant())) {
            throw new PorteeEntrepotInterditeException();
        }
    }

    /**
     * Résout l'entrepôt à utiliser pour filtrer une lecture (liste/alerte) :
     * un acteur national conserve le filtre demandé tel quel (y compris
     * {@code null} = aucune restriction, vision globale) ; un acteur
     * régional se voit systématiquement ramené à son propre entrepôt,
     * quel que soit le filtre qu'il aurait demandé.
     */
    public UUID entrepotIdPourLecture(UUID entrepotIdDemande) {
        if (estActeurNational()) {
            return entrepotIdDemande;
        }
        return entrepotIdCourant();
    }

    /**
     * À invoquer après avoir chargé une ressource déjà rattachée à un
     * entrepôt précis (consultation unitaire d'un stock ou d'un mouvement) :
     * un acteur national peut toujours consulter ; un acteur régional ne
     * peut consulter que si l'entrepôt de la ressource est le sien.
     */
    public void verifierLectureAutorisee(UUID entrepotIdRessource) {
        if (estActeurNational()) {
            return;
        }
        if (entrepotIdRessource == null || !entrepotIdRessource.equals(entrepotIdCourant())) {
            throw new PorteeEntrepotInterditeException();
        }
    }

    private Set<String> rolesCourants() {
        return authorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(autorite -> autorite.startsWith("ROLE_") ? autorite.substring(5) : autorite)
                .collect(Collectors.toUnmodifiableSet());
    }

    private Set<? extends GrantedAuthority> authorities() {
        return authentication().getAuthorities().stream().collect(Collectors.toUnmodifiableSet());
    }

    private CurrentUser currentUser() {
        return (CurrentUser) authentication().getPrincipal();
    }

    private Authentication authentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }
}
