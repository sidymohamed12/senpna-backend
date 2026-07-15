package ministere.sante.senpna.commandeachat.infrastructure.persistence.adapter;

import ministere.sante.senpna.commandeachat.domain.criteria.CommandeAchatSearchCriteria;
import ministere.sante.senpna.commandeachat.domain.model.CommandeAchat;
import ministere.sante.senpna.commandeachat.domain.port.out.CommandeAchatRepositoryPort;
import ministere.sante.senpna.commandeachat.domain.valueobject.CommandeAchatId;
import ministere.sante.senpna.commandeachat.domain.valueobject.StatutCommandeAchat;
import ministere.sante.senpna.commandeachat.infrastructure.persistence.entity.CommandeAchatJpaEntity;
import ministere.sante.senpna.commandeachat.infrastructure.persistence.entity.LigneCommandeAchatJpaEntity;
import ministere.sante.senpna.commandeachat.infrastructure.persistence.mapper.CommandeAchatMapper;
import ministere.sante.senpna.commandeachat.infrastructure.persistence.repository.CommandeAchatJpaRepository;
import ministere.sante.senpna.commandeachat.infrastructure.persistence.repository.LigneCommandeAchatJpaRepository;
import ministere.sante.senpna.commandeachat.infrastructure.persistence.specification.CommandeAchatSpecifications;
import ministere.sante.senpna.fournisseur.domain.valueobject.FournisseurId;
import ministere.sante.senpna.shared.domain.valueobject.PageRequest;
import ministere.sante.senpna.shared.domain.valueobject.PageResult;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Recompose l'agrégat {@link CommandeAchat} à partir de sa table propre et
 * de la table de ses lignes — même stratégie que
 * {@code AppelOffreRepositoryAdapter} : pas de relation JPA
 * bidirectionnelle, remplacement intégral des lignes à chaque
 * sauvegarde. Contrairement à un appel d'offres, une commande d'achat
 * voit ses lignes évoluer à chaque étape du workflow (expédition,
 * réception) — le remplacement complet reste néanmoins acceptable ici
 * car le volume de lignes par commande est faible (quelques dizaines au
 * plus) et chaque sauvegarde se produit dans une transaction courte.
 */
@Component
public class CommandeAchatRepositoryAdapter implements CommandeAchatRepositoryPort {

    private static final Set<String> CHAMPS_TRI_AUTORISES = Set.of("reference", "statut", "createdAt", "updatedAt");

    private final CommandeAchatJpaRepository commandeAchatJpaRepository;
    private final LigneCommandeAchatJpaRepository ligneCommandeAchatJpaRepository;
    private final CommandeAchatMapper commandeAchatMapper;

    public CommandeAchatRepositoryAdapter(CommandeAchatJpaRepository commandeAchatJpaRepository,
            LigneCommandeAchatJpaRepository ligneCommandeAchatJpaRepository,
            CommandeAchatMapper commandeAchatMapper) {
        this.commandeAchatJpaRepository = commandeAchatJpaRepository;
        this.ligneCommandeAchatJpaRepository = ligneCommandeAchatJpaRepository;
        this.commandeAchatMapper = commandeAchatMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CommandeAchat> findById(CommandeAchatId id) {
        return commandeAchatJpaRepository.findById(id.getValue()).map(this::toDomainAvecLignes);
    }

    @Override
    public boolean existsByReferenceIgnoreCase(String reference) {
        return commandeAchatJpaRepository.existsByReferenceIgnoreCase(reference);
    }

    @Override
    @Transactional
    public CommandeAchat save(CommandeAchat commandeAchat) {
        CommandeAchatJpaEntity saved = commandeAchatJpaRepository.save(commandeAchatMapper.toEntity(commandeAchat));

        ligneCommandeAchatJpaRepository.deleteByCommandeAchatId(saved.getId());
        List<LigneCommandeAchatJpaEntity> lignes = commandeAchatMapper.toEntityLignes(commandeAchat);
        ligneCommandeAchatJpaRepository.saveAll(lignes);

        return commandeAchatMapper.toDomain(saved, lignes);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<CommandeAchat> search(CommandeAchatSearchCriteria criteria, PageRequest pageRequest) {
        Specification<CommandeAchatJpaEntity> specification = CommandeAchatSpecifications.combiner(
                criteria.recherche(), criteria.statut());

        Pageable pageable = toPageable(pageRequest);
        Page<CommandeAchatJpaEntity> page = commandeAchatJpaRepository.findAll(specification, pageable);

        return toPageResult(page);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<CommandeAchat> findByFournisseurId(FournisseurId fournisseurId, StatutCommandeAchat statut,
            PageRequest pageRequest) {
        Pageable pageable = toPageable(pageRequest);
        Page<CommandeAchatJpaEntity> page = statut != null
                ? commandeAchatJpaRepository.findByFournisseurIdAndStatut(fournisseurId.getValue(), statut, pageable)
                : commandeAchatJpaRepository.findByFournisseurId(fournisseurId.getValue(), pageable);
        return toPageResult(page);
    }

    private Pageable toPageable(PageRequest pageRequest) {
        Sort.Direction direction = pageRequest.direction() == PageRequest.SortDirection.ASC
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;
        String champTri = CHAMPS_TRI_AUTORISES.contains(pageRequest.sortBy()) ? pageRequest.sortBy() : "createdAt";
        return org.springframework.data.domain.PageRequest.of(pageRequest.page(), pageRequest.size(),
                Sort.by(direction, champTri));
    }

    private PageResult<CommandeAchat> toPageResult(Page<CommandeAchatJpaEntity> page) {
        List<CommandeAchat> content = page.getContent().stream().map(this::toDomainAvecLignes).toList();
        return PageResult.of(content, page.getNumber(), page.getSize(), page.getTotalElements());
    }

    private CommandeAchat toDomainAvecLignes(CommandeAchatJpaEntity entity) {
        List<LigneCommandeAchatJpaEntity> lignes = ligneCommandeAchatJpaRepository
                .findByCommandeAchatId(entity.getId());
        return commandeAchatMapper.toDomain(entity, lignes);
    }
}
