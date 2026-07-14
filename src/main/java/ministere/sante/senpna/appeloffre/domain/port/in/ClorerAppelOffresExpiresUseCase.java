package ministere.sante.senpna.appeloffre.domain.port.in;

/**
 * Clôture automatique des appels d'offres publiés dont la date de
 * clôture est dépassée — déclenchée par
 * {@code AppelOffreClotureScheduler}.
 */
public interface ClorerAppelOffresExpiresUseCase {
    int clorerExpires();
}
