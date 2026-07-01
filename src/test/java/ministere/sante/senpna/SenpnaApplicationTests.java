package ministere.sante.senpna;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Tag("integration")
class SenpnaApplicationTests {

	@Test
	@DisplayName("Le contexte Spring démarre sans erreur")
	void contextLoads() {
		// Intentionnellement vide — si le contexte fail, le test fail.
	}

}
