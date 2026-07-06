package ministere.sante.senpna.media.infrastructure.config;

import ministere.sante.senpna.media.domain.port.out.StoragePort;
import ministere.sante.senpna.media.infrastructure.stockage.MockStorageAdapter;
import ministere.sante.senpna.media.infrastructure.stockage.R2StorageAdapter;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.validation.annotation.Validated;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.net.URI;

/**
 * Configuration du stockage objet — sélection par profil Spring.
 *
 * <h3>Mise à jour — support Presigned URL</h3>
 * <p>
 * Les adaptateurs MinIO et R2 reçoivent maintenant les credentials
 * directement pour pouvoir instancier leur propre {@code S3Presigner},
 * nécessaire à la génération des Presigned URLs.
 * </p>
 *
 * <pre>
 * Profil "prod" → R2StorageAdapter     → Presigned URL vers R2 Cloudflare
 * Profil "test" → MockStorageAdapter   → Presigned URL simulée en mémoire
 * </pre>
 */
public final class StorageConfig {

        private StorageConfig() {
        }

        // ══════════════════════════════════════════════════════════════════════
        // Profil "prod" et "dev" — Cloudflare R2
        // ══════════════════════════════════════════════════════════════════════

        @Configuration
        @Profile({ "prod", "dev" })
        @EnableConfigurationProperties(StorageConfig.R2Properties.class)
        static class R2StorageConfig {

                @Bean
                public StoragePort r2Storage(R2Properties props) {
                        S3Client client = S3Client.builder()
                                        .endpointOverride(URI.create(
                                                        "https://" + props.accountId() + ".r2.cloudflarestorage.com"))
                                        .credentialsProvider(StaticCredentialsProvider.create(
                                                        AwsBasicCredentials.create(props.accessKey(),
                                                                        props.secretKey())))
                                        .region(Region.of("auto"))
                                        .build();

                        // Les credentials sont passés pour permettre l'instanciation
                        // du S3Presigner interne à R2StorageAdapter
                        return new R2StorageAdapter(
                                        client,
                                        props.bucket(),
                                        props.publicUrl(),
                                        props.accountId(),
                                        props.accessKey(),
                                        props.secretKey());
                }
        }

        // ══════════════════════════════════════════════════════════════════════
        // Profil "test"
        // ══════════════════════════════════════════════════════════════════════

        @Configuration
        @Profile("test")
        static class MockStorageConfig {

                @Bean
                public StoragePort mockStorage() {
                        return new MockStorageAdapter();
                }
        }

        // ══════════════════════════════════════════════════════════════════════
        // Properties
        // ══════════════════════════════════════════════════════════════════════

        @Validated
        @ConfigurationProperties(prefix = "storage.r2")
        public record R2Properties(
                        @NotBlank(message = "storage.r2.account-id est obligatoire") String accountId,
                        @NotBlank(message = "storage.r2.access-key est obligatoire") String accessKey,
                        @NotBlank(message = "storage.r2.secret-key est obligatoire") String secretKey,
                        @NotBlank(message = "storage.r2.public-url est obligatoire") String publicUrl,
                        @NotBlank(message = "storage.r2.bucket est obligatoire") String bucket) {
        }
}
