package ministere.sante.senpna.stock.infrastructure.persistence.adapter;

import ministere.sante.senpna.medicament.domain.valueobject.MedicamentId;
import ministere.sante.senpna.shared.domain.valueobject.PageRequest;
import ministere.sante.senpna.shared.domain.valueobject.PageResult;
import ministere.sante.senpna.stock.domain.criteria.AlertePeremptionCriteria;
import ministere.sante.senpna.stock.domain.criteria.LotSearchCriteria;
import ministere.sante.senpna.stock.domain.model.Lot;
import ministere.sante.senpna.stock.domain.port.out.LotRepositoryPort;
import ministere.sante.senpna.stock.domain.valueobject.LotId;
import ministere.sante.senpna.stock.domain.valueobject.StatutLot;
import ministere.sante.senpna.stock.infrastructure.persistence.entity.LotJpaEntity;
import ministere.sante.senpna.stock.infrastructure.persistence.mapper.LotMapper;
import ministere.sante.senpna.stock.infrastructure.persistence.repository.LotJpaRepository;
import ministere.sante.senpna.stock.infrastructure.persistence.specification.LotSpecifications;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Component
public class LotRepositoryAdapter implements LotRepositoryPort {

    private static final Set<String> CHAMPS_TRI_AUTORISES = Set.of(
            "numeroLot", "dateExpiration", "dateFabrication", "statut", "createdAt", "updatedAt");

    private final LotJpaRepository lotJpaRepository;
    private final LotMapper lotMapper;

    public LotRepositoryAdapter(LotJpaRepository lotJpaRepository, LotMapper lotMapper) {
        this.lotJpaRepository = lotJpaRepository;
        this.lotMapper = lotMapper;
    }

    @Override
    public Optional<Lot> findById(LotId id) {
        return lotJpaRepository.findById(id.getValue()).map(lotMapper::toDomain);
    }

    @Override
    public boolean existsByMedicamentIdAndNumeroLotIgnoreCase(MedicamentId medicamentId, String numeroLot) {
        return lotJpaRepository.existsByMedicamentIdAndNumeroLotIgnoreCase(medicamentId.getValue(), numeroLot);
    }

    @Override
    @Transactional
    public Lot save(Lot lot) {
        LotJpaEntity saved = lotJpaRepository.save(lotMapper.toEntity(lot));
        return lotMapper.toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<Lot> search(LotSearchCriteria criteria, PageRequest pageRequest) {
        Specification<LotJpaEntity> specification = LotSpecifications.combiner(
                criteria.recherche(), criteria.medicamentId(), criteria.fournisseurId(), criteria.statut(),
                criteria.entrepotId());

        Pageable pageable = versPageable(pageRequest);
        Page<LotJpaEntity> page = lotJpaRepository.findAll(specification, pageable);
        List<Lot> content = page.getContent().stream().map(lotMapper::toDomain).toList();

        return PageResult.of(content, page.getNumber(), page.getSize(), page.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Lot> findActifsNonExpiresParMedicamentTriesFefo(MedicamentId medicamentId) {
        LocalDate aujourdHui = LocalDate.now();
        return lotJpaRepository
                .findByMedicamentIdAndStatutOrderByDateExpirationAsc(medicamentId.getValue(), StatutLot.ACTIF.name())
                .stream()
                .filter(entity -> !entity.getDateExpiration().isBefore(aujourdHui))
                .map(lotMapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<Lot> findExpirantAvant(AlertePeremptionCriteria criteria, PageRequest pageRequest) {
        Specification<LotJpaEntity> specification = Specification.allOf();
        specification = specification.and(LotSpecifications.expirantAvant(criteria.dateLimite()));
        if (criteria.medicamentId() != null) {
            specification = specification.and(LotSpecifications.medicamentId(criteria.medicamentId()));
        }

        if (criteria.entrepotId() != null) {
            specification = specification.and(LotSpecifications.possedeStockDansEntrepot(criteria.entrepotId()));
        }

        Pageable pageable = versPageable(pageRequest);
        Page<LotJpaEntity> page = lotJpaRepository.findAll(specification, pageable);
        List<Lot> content = page.getContent().stream().map(lotMapper::toDomain).toList();

        return PageResult.of(content, page.getNumber(), page.getSize(), page.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Lot> findActifsExpires() {
        return lotJpaRepository.findByStatutAndDateExpirationBefore(StatutLot.ACTIF.name(), LocalDate.now())
                .stream()
                .map(lotMapper::toDomain)
                .toList();
    }

    private static Pageable versPageable(PageRequest pageRequest) {
        Sort.Direction direction = pageRequest.direction() == PageRequest.SortDirection.ASC
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;
        String champTri = CHAMPS_TRI_AUTORISES.contains(pageRequest.sortBy()) ? pageRequest.sortBy() : "createdAt";
        return org.springframework.data.domain.PageRequest.of(pageRequest.page(), pageRequest.size(),
                Sort.by(direction, champTri));
    }
}
