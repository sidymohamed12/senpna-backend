package ministere.sante.senpna.auth.infrastructure.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("SpringPasswordEncoderAdapter — encodage BCrypt des mots de passe")
class SpringPasswordEncoderAdapterTest {

    SpringPasswordEncoderAdapter sut;

    @BeforeEach
    void setUp() {
        sut = new SpringPasswordEncoderAdapter(new BCryptPasswordEncoder(12));
    }

    @Test
    @DisplayName("encoder() produit un hash différent du mot de passe brut")
    void encoder_produitHashDifferent() {
        String hash = sut.encoder("MotDePasse@2024");

        assertThat(hash).isNotEqualTo("MotDePasse@2024")
                .startsWith("$2a$").doesNotContain("MotDePasse");
    }

    @Test
    @DisplayName("correspond() vrai pour le mot de passe utilisé pour générer le hash")
    void correspond_vraiPourLeBonMotDePasse() {
        String hash = sut.encoder("MotDePasse@2024");

        assertThat(sut.correspond("MotDePasse@2024", hash)).isTrue();
    }

    @Test
    @DisplayName("correspond() faux pour un mot de passe incorrect")
    void correspond_fauxPourMauvaisMotDePasse() {
        String hash = sut.encoder("MotDePasse@2024");

        assertThat(sut.correspond("AutreMotDePasse", hash)).isFalse();
    }

    @Test
    @DisplayName("deux encodages successifs du même mot de passe produisent des hash différents (salage)")
    void deuxEncodages_hashDifferents() {
        String hash1 = sut.encoder("MotDePasse@2024");
        String hash2 = sut.encoder("MotDePasse@2024");

        assertThat(hash1).isNotEqualTo(hash2);
    }
}
