package ministere.sante.senpna.carriere.domain.port.out;

/**
 * Envoi des e-mails liés au cycle de vie d'une candidature — accusé de
 * réception au candidat et notification au contact RH de l'offre.
 *
 * <p>
 * Spécifique au module {@code carriere} (contrairement à
 * {@code AccountMailPort}, dans {@code shared} car réutilisé par plusieurs
 * features) : aucune autre feature n'a besoin de notifier une candidature,
 * ce port n'a donc pas vocation à être partagé.
 * </p>
 */
public interface CandidatureMailPort {

    /**
     * Accusé de réception envoyé au candidat juste après la soumission de
     * sa candidature.
     */
    void envoyerAccuseReceptionCandidature(String email, String nomCandidat, String titreOffre,
            String nomEntreprise);

    /**
     * Notification envoyée au contact RH de l'offre (e-mail de contact
     * explicite de l'offre, ou e-mail du compte auteur si non renseigné)
     * lors de la réception d'une nouvelle candidature.
     */
    void envoyerNotificationNouvelleCandidature(String emailDestinataireRH, String titreOffre, String nomEntreprise,
            String nomCandidat, String emailCandidat, String telephoneCandidat);
}
