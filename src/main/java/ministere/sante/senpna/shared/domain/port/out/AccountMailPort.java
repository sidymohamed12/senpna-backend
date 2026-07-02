package ministere.sante.senpna.shared.domain.port.out;

/**
 * Envoi des e-mails liés au cycle de vie d'un compte utilisateur (ex:
 * identifiants temporaires à la création). Générique et placé dans
 * {@code shared} afin d'être réutilisable par toute feature créant des
 * comptes — {@code utilisateurs} (création manuelle) comme les créations
 * automatiques déclenchées par événement (ex: validation d'adhésion d'une
 * structure sanitaire) — sans dupliquer de configuration SMTP.
 */
public interface AccountMailPort {

    /**
     * Envoie les identifiants de connexion d'un compte nouvellement créé.
     *
     * @param email               destinataire
     * @param nom                 nom de famille du titulaire du compte
     * @param prenom              prénom du titulaire du compte
     * @param motDePasseTemporaire mot de passe à usage unique, à changer à
     *                             la première connexion
     */
    void envoyerIdentifiantsCompte(String email, String nom, String prenom, String motDePasseTemporaire);
}
