package ministere.sante.senpna.actualite.domain.valueobject;

/**
 * Type d'un média rattaché à une actualité.
 *
 * <p>
 * {@code IMAGE} et {@code VIDEO} recouvrent aussi bien un fichier uploadé
 * (via {@code MediaType.ACTUALITE}, cf. module {@code media}) qu'une
 * vidéo hébergée à l'externe (YouTube, Vimeo) — dans les deux cas seule
 * l'URL finale est persistée ici, la feature {@code actualite} n'a pas à
 * savoir d'où elle provient.
 * </p>
 */
public enum TypeMedia {
    IMAGE,
    VIDEO
}
