package ministere.sante.senpna.appeloffre.infrastructure.persistence.adapter;

import ministere.sante.senpna.appeloffre.domain.model.OffreFournisseur;
import ministere.sante.senpna.appeloffre.domain.port.out.OffreFournisseurRepositoryPort;
import ministere.sante.senpna.appeloffre.domain.valueobject.AppelOffreId;
import ministere.sante.senpna.appeloffre.domain.valueobject.OffreFournisseurId;
import ministere.sante.senpna.appeloffre.domain.valueobject.StatutOffre;
import ministere.sante.senpna.appeloffre.infrastructure.persistence.entity.LigneOffreJpaEntity;
import ministere.sante.senpna.appeloffre.infrastructure.persistence.entity.OffreFournisseurJpaEntity;
import ministere.sante.senpna.appeloffre.infrastructure.persistence.mapper.OffreFournisseurMapper;
import ministere.sante.senpna.appeloffre.infrastructure.persistence.repository.LigneOffreJpaRepository;
import ministere.sante.senpna.appeloffre.infrastructure.persistence.repository.OffreFournisseurJpaRepository;
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
public class OffreFournisseurRepositoryAdapter implements OffreFournisseurRepositoryPort {

    private final OffreFournisseurJpaRepository offreFournisseurJpaRepository;
    private final LigneOffreJpaRepository ligneOffreJpaRepository;
    private final OffreFournisseurMapper offreFournisseurMapper;

    public OffreFournisseurRepositoryAdapter(OffreFournisseurJpaRepository offreFournisseurJpaRepository,
            LigneOffreJpaRepository ligneOffreJpaRepository, OffreFournisseurMapper offreFournisseurMapper) {
        this.offreFournisseurJpaRepository = offreFournisseurJpaRepository;
        this.ligneOffreJpaRepository = ligneOffreJpaRepository;
        this.offreFournisseurMapper = offreFournisseurMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<OffreFournisseur> findById(OffreFournisseurId id) {
        return offreFournisseurJpaRepository.findById(id.getValue()).map(this::toDomainAvecLignes);
    }

    @Override
    public boolean existsByAppelOffreIdAndFournisseurIdAndStatut(AppelOffreId appelOffreId,
            FournisseurId fournisseurId, StatutOffre statut) {
        return offreFournisseurJpaRepository.existsByAppelOffreIdAndFournisseurIdAndStatut(
                appelOffreId.getValue(), fournisseurId.getValue(), statut);
    }

    @Override
    @Transactional
    public OffreFournisseur save(OffreFournisseur offre) {
        OffreFournisseurJpaEntity saved = offreFournisseurJpaRepository
                .save(offreFournisseurMapper.toEntity(offre));

        ligneOffreJpaRepository.deleteByOffreId(saved.getId());
        List<LigneOffreJpaEntity> lignes = offreFournisseurMapper.toEntityLignes(offre);
        ligneOffreJpaRepository.saveAll(lignes);

        return offreFournisseurMapper.toDomain(saved, lignes);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<OffreFournisseur> findByAppelOffreId(AppelOffreId appelOffreId, PageRequest pageRequest) {
        Pageable pageable = toPageable(pageRequest);
        Page<OffreFournisseurJpaEntity> page = offreFournisseurJpaRepository
                .findByAppelOffreId(appelOffreId.getValue(), pageable);
        return toPageResult(page);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<OffreFournisseur> findByFournisseurId(FournisseurId fournisseurId, StatutOffre statut,
            PageRequest pageRequest) {
        Pageable pageable = toPageable(pageRequest);
        Page<OffreFournisseurJpaEntity> page = statut != null
                ? offreFournisseurJpaRepository.findByFournisseurIdAndStatut(fournisseurId.getValue(), statut,
                        pageable)
                : offreFournisseurJpaRepository.findByFournisseurId(fournisseurId.getValue(), pageable);
        return toPageResult(page);
    }

    private Pageable toPageable(PageRequest pageRequest) {
        Sort.Direction direction = pageRequest.direction() == PageRequest.SortDirection.ASC
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;
        return org.springframework.data.domain.PageRequest.of(pageRequest.page(), pageRequest.size(),
                Sort.by(direction, "createdAt"));
    }

    private PageResult<OffreFournisseur> toPageResult(Page<OffreFournisseurJpaEntity> page) {
        List<OffreFournisseur> content = page.getContent().stream().map(this::toDomainAvecLignes).toList();
        return PageResult.of(content, page.getNumber(), page.getSize(), page.getTotalElements());
    }

    private OffreFournisseur toDomainAvecLignes(OffreFournisseurJpaEntity entity) {
        List<LigneOffreJpaEntity> lignes = ligneOffreJpaRepository.findByOffreId(entity.getId());
        return offreFournisseurMapper.toDomain(entity, lignes);
    }
}
