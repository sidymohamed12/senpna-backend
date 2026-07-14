package ministere.sante.senpna.appeloffre.infrastructure.persistence.adapter;

import ministere.sante.senpna.appeloffre.domain.criteria.AppelOffreSearchCriteria;
import ministere.sante.senpna.appeloffre.domain.model.AppelOffre;
import ministere.sante.senpna.appeloffre.domain.port.out.AppelOffreRepositoryPort;
import ministere.sante.senpna.appeloffre.domain.valueobject.AppelOffreId;
import ministere.sante.senpna.appeloffre.domain.valueobject.StatutAppelOffre;
import ministere.sante.senpna.appeloffre.infrastructure.persistence.entity.AppelOffreJpaEntity;
import ministere.sante.senpna.appeloffre.infrastructure.persistence.entity.LigneAppelOffreJpaEntity;
import ministere.sante.senpna.appeloffre.infrastructure.persistence.mapper.AppelOffreMapper;
import ministere.sante.senpna.appeloffre.infrastructure.persistence.repository.AppelOffreJpaRepository;
import ministere.sante.senpna.appeloffre.infrastructure.persistence.repository.LigneAppelOffreJpaRepository;
import ministere.sante.senpna.appeloffre.infrastructure.persistence.specification.AppelOffreSpecifications;
import ministere.sante.senpna.shared.domain.valueobject.PageRequest;
import ministere.sante.senpna.shared.domain.valueobject.PageResult;

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

/**
 * Recompose l'agrégat {@link AppelOffre} à partir de sa table propre et de
 * la table de ses lignes — pas de relation JPA bidirectionnelle (cf.
 * {@code LigneAppelOffreJpaEntity}). {@link #save} remplace
 * systématiquement l'ensemble des lignes : un appel d'offres n'est modifié
 * en lignes qu'à l'état {@code BROUILLON}, où le volume est faible et la
 * fréquence d'écriture négligeable — une stratégie « delete + re-insert »
 * reste donc largement suffisante et bien plus simple qu'un diff fin.
 */
@Component
public class AppelOffreRepositoryAdapter implements AppelOffreRepositoryPort {

    private static final Set<String> CHAMPS_TRI_AUTORISES = Set.of("reference", "dateCloture", "statut",
            "createdAt", "updatedAt");

    private final AppelOffreJpaRepository appelOffreJpaRepository;
    private final LigneAppelOffreJpaRepository ligneAppelOffreJpaRepository;
    private final AppelOffreMapper appelOffreMapper;

    public AppelOffreRepositoryAdapter(AppelOffreJpaRepository appelOffreJpaRepository,
            LigneAppelOffreJpaRepository ligneAppelOffreJpaRepository, AppelOffreMapper appelOffreMapper) {
        this.appelOffreJpaRepository = appelOffreJpaRepository;
        this.ligneAppelOffreJpaRepository = ligneAppelOffreJpaRepository;
        this.appelOffreMapper = appelOffreMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AppelOffre> findById(AppelOffreId id) {
        return appelOffreJpaRepository.findById(id.getValue()).map(this::toDomainAvecLignes);
    }

    @Override
    public boolean existsByReferenceIgnoreCase(String reference) {
        return appelOffreJpaRepository.existsByReferenceIgnoreCase(reference);
    }

    @Override
    @Transactional
    public AppelOffre save(AppelOffre appelOffre) {
        AppelOffreJpaEntity saved = appelOffreJpaRepository.save(appelOffreMapper.toEntity(appelOffre));

        ligneAppelOffreJpaRepository.deleteByAppelOffreId(saved.getId());
        List<LigneAppelOffreJpaEntity> lignes = appelOffreMapper.toEntityLignes(appelOffre);
        ligneAppelOffreJpaRepository.saveAll(lignes);

        return appelOffreMapper.toDomain(saved, lignes);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<AppelOffre> search(AppelOffreSearchCriteria criteria, PageRequest pageRequest) {
        Specification<AppelOffreJpaEntity> specification = AppelOffreSpecifications.combiner(criteria.recherche(),
                criteria.statut());

        Sort.Direction direction = pageRequest.direction() == PageRequest.SortDirection.ASC
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;
        String champTri = CHAMPS_TRI_AUTORISES.contains(pageRequest.sortBy()) ? pageRequest.sortBy() : "createdAt";

        Pageable pageable = org.springframework.data.domain.PageRequest.of(
                pageRequest.page(), pageRequest.size(), Sort.by(direction, champTri));

        Page<AppelOffreJpaEntity> page = appelOffreJpaRepository.findAll(specification, pageable);
        List<AppelOffre> content = page.getContent().stream().map(this::toDomainAvecLignes).toList();

        return PageResult.of(content, page.getNumber(), page.getSize(), page.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppelOffre> findPubliesAvecClotureDepassee() {
        return appelOffreJpaRepository
                .findByStatutAndDateClotureLessThan(StatutAppelOffre.PUBLIE, LocalDate.now().plusDays(1))
                .stream()
                .map(this::toDomainAvecLignes)
                .toList();
    }

    private AppelOffre toDomainAvecLignes(AppelOffreJpaEntity entity) {
        List<LigneAppelOffreJpaEntity> lignes = ligneAppelOffreJpaRepository.findByAppelOffreId(entity.getId());
        return appelOffreMapper.toDomain(entity, lignes);
    }
}
