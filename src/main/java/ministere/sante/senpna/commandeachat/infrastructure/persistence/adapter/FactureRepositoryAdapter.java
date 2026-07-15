package ministere.sante.senpna.commandeachat.infrastructure.persistence.adapter;

import ministere.sante.senpna.commandeachat.domain.model.Facture;
import ministere.sante.senpna.commandeachat.domain.port.out.FactureRepositoryPort;
import ministere.sante.senpna.commandeachat.domain.valueobject.FactureId;
import ministere.sante.senpna.commandeachat.domain.valueobject.StatutFacture;
import ministere.sante.senpna.commandeachat.infrastructure.persistence.entity.FactureJpaEntity;
import ministere.sante.senpna.commandeachat.infrastructure.persistence.mapper.FactureMapper;
import ministere.sante.senpna.commandeachat.infrastructure.persistence.repository.FactureJpaRepository;
import ministere.sante.senpna.fournisseur.domain.valueobject.FournisseurId;
import ministere.sante.senpna.shared.domain.valueobject.PageRequest;
import ministere.sante.senpna.shared.domain.valueobject.PageResult;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Component
public class FactureRepositoryAdapter implements FactureRepositoryPort {

    private final FactureJpaRepository factureJpaRepository;
    private final FactureMapper factureMapper;

    public FactureRepositoryAdapter(FactureJpaRepository factureJpaRepository, FactureMapper factureMapper) {
        this.factureJpaRepository = factureJpaRepository;
        this.factureMapper = factureMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Facture> findById(FactureId id) {
        return factureJpaRepository.findById(id.getValue()).map(factureMapper::toDomain);
    }

    @Override
    @Transactional
    public Facture save(Facture facture) {
        FactureJpaEntity saved = factureJpaRepository.save(factureMapper.toEntity(facture));
        return factureMapper.toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<Facture> findAll(StatutFacture statut, PageRequest pageRequest) {
        Pageable pageable = toPageable(pageRequest);
        Page<FactureJpaEntity> page = statut != null
                ? factureJpaRepository.findByStatut(statut, pageable)
                : factureJpaRepository.findAll(pageable);
        return toPageResult(page);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<Facture> findByFournisseurId(FournisseurId fournisseurId, StatutFacture statut,
            PageRequest pageRequest) {
        Pageable pageable = toPageable(pageRequest);
        Page<FactureJpaEntity> page = statut != null
                ? factureJpaRepository.findByFournisseurIdAndStatut(fournisseurId.getValue(), statut, pageable)
                : factureJpaRepository.findByFournisseurId(fournisseurId.getValue(), pageable);
        return toPageResult(page);
    }

    private Pageable toPageable(PageRequest pageRequest) {
        Sort.Direction direction = pageRequest.direction() == PageRequest.SortDirection.ASC
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;
        return org.springframework.data.domain.PageRequest.of(pageRequest.page(), pageRequest.size(),
                Sort.by(direction, "createdAt"));
    }

    private PageResult<Facture> toPageResult(Page<FactureJpaEntity> page) {
        List<Facture> content = page.getContent().stream().map(factureMapper::toDomain).toList();
        return PageResult.of(content, page.getNumber(), page.getSize(), page.getTotalElements());
    }
}
