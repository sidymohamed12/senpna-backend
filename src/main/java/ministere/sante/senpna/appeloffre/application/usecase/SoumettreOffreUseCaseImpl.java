package ministere.sante.senpna.appeloffre.application.usecase;

import ministere.sante.senpna.appeloffre.application.service.OffreDetailAssembler;
import ministere.sante.senpna.appeloffre.domain.command.OffreFournisseurCommands.LigneOffreInput;
import ministere.sante.senpna.appeloffre.domain.command.OffreFournisseurCommands.OffreDetail;
import ministere.sante.senpna.appeloffre.domain.command.OffreFournisseurCommands.SoumettreOffreCommand;
import ministere.sante.senpna.appeloffre.domain.exception.AppelOffreIntrouvableException;
import ministere.sante.senpna.appeloffre.domain.exception.AppelOffreNonPublieException;
import ministere.sante.senpna.appeloffre.domain.exception.DateClotureDepasseeException;
import ministere.sante.senpna.appeloffre.domain.exception.OffreDejaSoumiseException;
import ministere.sante.senpna.appeloffre.domain.model.AppelOffre;
import ministere.sante.senpna.appeloffre.domain.model.LigneOffre;
import ministere.sante.senpna.appeloffre.domain.model.OffreFournisseur;
import ministere.sante.senpna.appeloffre.domain.port.in.SoumettreOffreUseCase;
import ministere.sante.senpna.appeloffre.domain.port.out.AppelOffreRepositoryPort;
import ministere.sante.senpna.appeloffre.domain.port.out.OffreFournisseurRepositoryPort;
import ministere.sante.senpna.appeloffre.domain.valueobject.AppelOffreId;
import ministere.sante.senpna.appeloffre.domain.valueobject.LigneAppelOffreId;
import ministere.sante.senpna.appeloffre.domain.valueobject.StatutAppelOffre;
import ministere.sante.senpna.appeloffre.domain.valueobject.StatutOffre;
import ministere.sante.senpna.fournisseur.domain.valueobject.FournisseurId;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Soumission d'une offre par un fournisseur (cf. doc. métier §b « Plusieurs
 * fournisseurs soumettent leurs offres »).
 *
 * <p>
 * Les règles d'ouverture (AO publié, date de clôture non dépassée, offre
 * pas déjà soumise par ce fournisseur) sont vérifiées ici plutôt que dans
 * {@link AppelOffre} ou {@link OffreFournisseur} : elles impliquent les
 * deux agrégats simultanément, ce qui n'est la responsabilité d'aucun des
 * deux pris isolément.
 * </p>
 */
@Service
public class SoumettreOffreUseCaseImpl implements SoumettreOffreUseCase {

    private final AppelOffreRepositoryPort appelOffreRepositoryPort;
    private final OffreFournisseurRepositoryPort offreFournisseurRepositoryPort;
    private final OffreDetailAssembler offreDetailAssembler;

    public SoumettreOffreUseCaseImpl(AppelOffreRepositoryPort appelOffreRepositoryPort,
            OffreFournisseurRepositoryPort offreFournisseurRepositoryPort,
            OffreDetailAssembler offreDetailAssembler) {
        this.appelOffreRepositoryPort = appelOffreRepositoryPort;
        this.offreFournisseurRepositoryPort = offreFournisseurRepositoryPort;
        this.offreDetailAssembler = offreDetailAssembler;
    }

    @Override
    @Transactional
    public OffreDetail soumettre(SoumettreOffreCommand command) {
        AppelOffreId appelOffreId = AppelOffreId.of(command.appelOffreId());
        FournisseurId fournisseurId = FournisseurId.of(command.fournisseurId());

        AppelOffre appelOffre = appelOffreRepositoryPort.findById(appelOffreId)
                .orElseThrow(AppelOffreIntrouvableException::new);

        if (appelOffre.getStatut() != StatutAppelOffre.PUBLIE) {
            throw new AppelOffreNonPublieException();
        }
        if (appelOffre.dateClotureDepassee()) {
            throw new DateClotureDepasseeException();
        }
        if (offreFournisseurRepositoryPort.existsByAppelOffreIdAndFournisseurIdAndStatut(appelOffreId, fournisseurId,
                StatutOffre.SOUMISE)) {
            throw new OffreDejaSoumiseException();
        }

        List<LigneOffre> lignes = command.lignes().stream().map(this::toLigne).toList();

        OffreFournisseur offre = OffreFournisseur.soumettre(appelOffreId, fournisseurId, command.commentaire(),
                lignes);

        OffreFournisseur saved = offreFournisseurRepositoryPort.save(offre);
        return offreDetailAssembler.assembler(saved);
    }

    private LigneOffre toLigne(LigneOffreInput input) {
        return LigneOffre.creer(LigneAppelOffreId.of(input.ligneAppelOffreId()), input.prixUnitaire(),
                input.delaiLivraisonJours());
    }
}
