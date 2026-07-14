package ministere.sante.senpna.appeloffre.application.usecase;

import ministere.sante.senpna.appeloffre.application.service.AppelOffreDetailAssembler;
import ministere.sante.senpna.appeloffre.domain.command.AppelOffreCommands.AppelOffreDetail;
import ministere.sante.senpna.appeloffre.domain.command.AppelOffreCommands.AttribuerAppelOffreCommand;
import ministere.sante.senpna.appeloffre.domain.exception.AppelOffreIntrouvableException;
import ministere.sante.senpna.appeloffre.domain.exception.OffreFournisseurIntrouvableException;
import ministere.sante.senpna.appeloffre.domain.model.AppelOffre;
import ministere.sante.senpna.appeloffre.domain.model.OffreFournisseur;
import ministere.sante.senpna.appeloffre.domain.port.in.AttribuerAppelOffreUseCase;
import ministere.sante.senpna.appeloffre.domain.port.out.AppelOffreRepositoryPort;
import ministere.sante.senpna.appeloffre.domain.port.out.OffreFournisseurRepositoryPort;
import ministere.sante.senpna.appeloffre.domain.valueobject.AppelOffreId;
import ministere.sante.senpna.appeloffre.domain.valueobject.OffreFournisseurId;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Attribution d'un appel d'offres — orchestration inter-agrégats (cf. doc.
 * métier §c « Signature du contrat ») : fait passer l'appel d'offres
 * {@code CLOTURE} à {@code ATTRIBUE} et statue simultanément sur chacune
 * des offres reçues (retenue / rejetée).
 *
 * <p>
 * Vit délibérément dans la couche application, jamais dans un des deux
 * agrégats : ni {@link AppelOffre} ni {@link OffreFournisseur} ne doit
 * connaître l'autre pour rester indépendamment testable et pour éviter
 * qu'un verrou pessimiste sur l'un bloque les écritures concurrentes sur
 * l'autre (cf. doc. de {@link OffreFournisseur}).
 * </p>
 */
@Service
public class AttribuerAppelOffreUseCaseImpl implements AttribuerAppelOffreUseCase {

    private final AppelOffreRepositoryPort appelOffreRepositoryPort;
    private final OffreFournisseurRepositoryPort offreFournisseurRepositoryPort;
    private final AppelOffreDetailAssembler appelOffreDetailAssembler;

    public AttribuerAppelOffreUseCaseImpl(AppelOffreRepositoryPort appelOffreRepositoryPort,
            OffreFournisseurRepositoryPort offreFournisseurRepositoryPort,
            AppelOffreDetailAssembler appelOffreDetailAssembler) {
        this.appelOffreRepositoryPort = appelOffreRepositoryPort;
        this.offreFournisseurRepositoryPort = offreFournisseurRepositoryPort;
        this.appelOffreDetailAssembler = appelOffreDetailAssembler;
    }

    @Override
    @Transactional
    public AppelOffreDetail attribuer(AttribuerAppelOffreCommand command) {
        AppelOffre appelOffre = appelOffreRepositoryPort.findById(AppelOffreId.of(command.appelOffreId()))
                .orElseThrow(AppelOffreIntrouvableException::new);

        // La transition d'état de l'AO valide elle-même qu'on part bien de
        // CLOTURE — inutile de dupliquer cette règle ici.
        appelOffre.attribuer();

        statuerSur(command.offresRetenuesIds(), OffreFournisseur::retenir);
        statuerSur(command.offresRejeteesIds(), OffreFournisseur::rejeter);

        AppelOffre saved = appelOffreRepositoryPort.save(appelOffre);
        return appelOffreDetailAssembler.assembler(saved);
    }

    private void statuerSur(List<java.util.UUID> offreIds, java.util.function.Consumer<OffreFournisseur> decision) {
        if (offreIds == null) {
            return;
        }
        for (java.util.UUID offreId : offreIds) {
            OffreFournisseur offre = offreFournisseurRepositoryPort.findById(OffreFournisseurId.of(offreId))
                    .orElseThrow(OffreFournisseurIntrouvableException::new);
            decision.accept(offre);
            offreFournisseurRepositoryPort.save(offre);
        }
    }
}
