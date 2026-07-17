package ministere.sante.senpna.stock.infrastructure.persistence.adapter;

import ministere.sante.senpna.fournisseur.domain.valueobject.FournisseurId;
import ministere.sante.senpna.medicament.domain.valueobject.MedicamentId;
import ministere.sante.senpna.shared.domain.valueobject.PageRequest;
import ministere.sante.senpna.shared.domain.valueobject.PageResult;
import ministere.sante.senpna.stock.domain.criteria.AlertePeremptionCriteria;
import ministere.sante.senpna.stock.domain.criteria.LotSearchCriteria;
import ministere.sante.senpna.stock.domain.model.Lot;
import ministere.sante.senpna.stock.domain.valueobject.LotId;
import ministere.sante.senpna.stock.domain.valueobject.StatutLot;
import ministere.sante.senpna.stock.infrastructure.persistence.entity.LotJpaEntity;
import ministere.sante.senpna.stock.infrastructure.persistence.mapper.LotMapper;
import ministere.sante.senpna.stock.infrastructure.persistence.repository.LotJpaRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests unitaires (Mockito) de {@link LotRepositoryAdapter} — vérifient la
 * délégation au repository Spring Data et la traduction domaine ⇆
 * persistance, sans contexte Spring (contrairement à
 * {@code StockPersistenceAdaptersTest}, taggé {@code integration}).
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("LotRepositoryAdapter — unitaire")
class LotRepositoryAdapterUnitTest {

        @Mock
        LotJpaRepository lotJpaRepository;

        @Mock
        LotMapper lotMapper;

        LotRepositoryAdapter adapter;

        UUID id = UUID.randomUUID();
        UUID medicamentId = UUID.randomUUID();
        UUID fournisseurId = UUID.randomUUID();
        Lot lot = Lot.builder()
            .id(LotId.of(id))
            .numeroLot("LOT-001")
            .medicamentId(MedicamentId.of(medicamentId))
            .fournisseurId(FournisseurId.of(fournisseurId))
            .dateFabrication(LocalDate.now().minusMonths(1))
            .dateExpiration(LocalDate.now().plusMonths(6))
            .prixAchat(BigDecimal.TEN)
            .prixVente(new BigDecimal("15"))
            .statut(StatutLot.ACTIF)
            .createdAt(java.time.Instant.now())
            .updatedAt(java.time.Instant.now())
            .build();
        LotJpaEntity entity = LotJpaEntity.builder()
            .id(id)
            .numeroLot("LOT-001")
            .medicamentId(medicamentId)
            .fournisseurId(fournisseurId)
            .dateFabrication(LocalDate.now().minusMonths(1))
            .dateExpiration(LocalDate.now().plusMonths(6))
            .prixAchat(BigDecimal.TEN)
            .prixVente(new BigDecimal("15"))
            .statut("ACTIF")
            .build();

        @BeforeEach
        void setUp() {
                adapter = new LotRepositoryAdapter(lotJpaRepository, lotMapper);
        }

        @Test
        @DisplayName("findById() délègue au repository et mappe la présence")
        void findById_present() {
                when(lotJpaRepository.findById(id)).thenReturn(Optional.of(entity));
                when(lotMapper.toDomain(entity)).thenReturn(lot);

                assertThat(adapter.findById(LotId.of(id))).contains(lot);
        }

        @Test
        @DisplayName("findById() renvoie vide si absent")
        void findById_absent() {
                when(lotJpaRepository.findById(id)).thenReturn(Optional.empty());

                assertThat(adapter.findById(LotId.of(id))).isEmpty();
        }

        @Test
        @DisplayName("existsByMedicamentIdAndNumeroLotIgnoreCase() délègue au repository")
        void existsByMedicamentIdAndNumeroLotIgnoreCase() {
                when(lotJpaRepository.existsByMedicamentIdAndNumeroLotIgnoreCase(medicamentId, "lot-001"))
                                .thenReturn(true);

                assertThat(adapter.existsByMedicamentIdAndNumeroLotIgnoreCase(MedicamentId.of(medicamentId), "lot-001"))
                                .isTrue();
        }

        @Test
        @DisplayName("save() mappe puis sauvegarde puis re-mappe vers le domaine")
        void save() {
                when(lotMapper.toEntity(lot)).thenReturn(entity);
                when(lotJpaRepository.save(entity)).thenReturn(entity);
                when(lotMapper.toDomain(entity)).thenReturn(lot);

                Lot result = adapter.save(lot);

                assertThat(result).isEqualTo(lot);
                verify(lotJpaRepository).save(entity);
        }

        @Test
        @DisplayName("search() construit la pagination et mappe le contenu")
        void search() {
                Page<LotJpaEntity> page = new PageImpl<>(List.of(entity));
                when(lotJpaRepository.findAll(
                                Mockito.<Specification<LotJpaEntity>>any(),
                                any(Pageable.class)))
                                .thenReturn(page);

                when(lotMapper.toDomain(entity)).thenReturn(lot);

                PageResult<Lot> result = adapter.search(
                                new LotSearchCriteria("lot", medicamentId, fournisseurId, StatutLot.ACTIF, null),
                                new PageRequest(0, 20, "numeroLot", PageRequest.SortDirection.ASC));

                assertThat(result.content()).containsExactly(lot);
        }

        @Test
        @DisplayName("search() retombe sur createdAt si le champ de tri n'est pas autorisé")
        void search_champTriNonAutorise() {
                Page<LotJpaEntity> page = new PageImpl<>(List.of());
                when(lotJpaRepository.findAll(
                                Mockito.<Specification<LotJpaEntity>>any(),
                                any(Pageable.class)))
                                .thenReturn(page);

                PageResult<Lot> result = adapter.search(LotSearchCriteria.vide(),
                                new PageRequest(0, 20, "champInconnu", PageRequest.SortDirection.DESC));

                assertThat(result.content()).isEmpty();
        }

        @Test
        @DisplayName("findActifsNonExpiresParMedicamentTriesFefo() exclut les lots déjà expirés")
        void findActifsNonExpiresParMedicamentTriesFefo_excludExpires() {
                LotJpaEntity actifValide = LotJpaEntity.builder()
                    .id(UUID.randomUUID())
                    .numeroLot("LOT-A")
                    .medicamentId(medicamentId)
                    .fournisseurId(fournisseurId)
                    .dateFabrication(LocalDate.now().minusMonths(2))
                    .dateExpiration(LocalDate.now().plusDays(10))
                    .prixAchat(BigDecimal.TEN)
                    .prixVente(BigDecimal.TEN)
                    .statut("ACTIF")
                    .build();
                LotJpaEntity actifExpireHier = LotJpaEntity.builder()
                    .id(UUID.randomUUID())
                    .numeroLot("LOT-B")
                    .medicamentId(medicamentId)
                    .fournisseurId(fournisseurId)
                    .dateFabrication(LocalDate.now().minusMonths(6))
                    .dateExpiration(LocalDate.now().minusDays(1))
                    .prixAchat(BigDecimal.TEN)
                    .prixVente(BigDecimal.TEN)
                    .statut("ACTIF")
                    .build();
                when(lotJpaRepository.findByMedicamentIdAndStatutOrderByDateExpirationAsc(medicamentId, "ACTIF"))
                                .thenReturn(List.of(actifExpireHier, actifValide));
                when(lotMapper.toDomain(actifValide)).thenReturn(lot);

                List<Lot> result = adapter.findActifsNonExpiresParMedicamentTriesFefo(MedicamentId.of(medicamentId));

                assertThat(result).containsExactly(lot);
        }

        @Test
        @DisplayName("findExpirantAvant() applique le filtre de date, et optionnellement médicament/entrepôt")
        void findExpirantAvant() {
                Page<LotJpaEntity> page = new PageImpl<>(List.of(entity));
                when(lotJpaRepository.findAll(
                                Mockito.<Specification<LotJpaEntity>>any(),
                                any(Pageable.class)))
                                .thenReturn(page);

                when(lotMapper.toDomain(entity)).thenReturn(lot);

                PageResult<Lot> result = adapter.findExpirantAvant(
                                new AlertePeremptionCriteria(LocalDate.now().plusDays(30), medicamentId,
                                                UUID.randomUUID()),
                                new PageRequest(0, 20, "dateExpiration", PageRequest.SortDirection.ASC));

                assertThat(result.content()).containsExactly(lot);
        }

        @Test
        @DisplayName("findExpirantAvant() sans médicament ni entrepôt → filtre uniquement la date")
        void findExpirantAvant_sansMedicamentNiEntrepot() {
                Page<LotJpaEntity> page = new PageImpl<>(List.of());
                when(lotJpaRepository.findAll(
                                Mockito.<Specification<LotJpaEntity>>any(),
                                any(Pageable.class)))
                                .thenReturn(page);

                PageResult<Lot> result = adapter.findExpirantAvant(
                                new AlertePeremptionCriteria(LocalDate.now().plusDays(30), null, null),
                                new PageRequest(0, 20, "dateExpiration", PageRequest.SortDirection.ASC));

                assertThat(result.content()).isEmpty();
        }

        @Test
        @DisplayName("findActifsExpires() mappe chaque lot actif déjà expiré")
        void findActifsExpires() {
                when(lotJpaRepository.findByStatutAndDateExpirationBefore(eq("ACTIF"), any(LocalDate.class)))
                                .thenReturn(List.of(entity));
                when(lotMapper.toDomain(entity)).thenReturn(lot);

                assertThat(adapter.findActifsExpires()).containsExactly(lot);
        }
}
