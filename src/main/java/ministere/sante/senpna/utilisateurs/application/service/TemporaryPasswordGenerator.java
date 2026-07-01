package ministere.sante.senpna.utilisateurs.application.service;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class TemporaryPasswordGenerator {

    private static final String MAJUSCULES = "ABCDEFGHJKLMNPQRSTUVWXYZ"; // sans I/O — ambigus à l'oral
    private static final String MINUSCULES = "abcdefghijkmnpqrstuvwxyz"; // sans l
    private static final String CHIFFRES = "23456789"; // sans 0/1
    private static final String SPECIAUX = "!@#$%^&*-_=+";
    private static final String ALPHABET = MAJUSCULES + MINUSCULES + CHIFFRES + SPECIAUX;
    private static final int LONGUEUR = 14;

    private final SecureRandom random = new SecureRandom();

    public String generer() {
        StringBuilder motDePasse = new StringBuilder(LONGUEUR);
        // Garantit la présence d'au moins un caractère de chaque catégorie.
        motDePasse.append(tirer(MAJUSCULES));
        motDePasse.append(tirer(MINUSCULES));
        motDePasse.append(tirer(CHIFFRES));
        motDePasse.append(tirer(SPECIAUX));
        for (int i = motDePasse.length(); i < LONGUEUR; i++) {
            motDePasse.append(tirer(ALPHABET));
        }
        return melanger(motDePasse.toString());
    }

    private char tirer(String source) {
        return source.charAt(random.nextInt(source.length()));
    }

    private String melanger(String valeur) {
        char[] caracteres = valeur.toCharArray();
        for (int i = caracteres.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            char tmp = caracteres[i];
            caracteres[i] = caracteres[j];
            caracteres[j] = tmp;
        }
        return new String(caracteres);
    }
}
