package ministere.sante.senpna.fournisseur.application.service;

import ministere.sante.senpna.fournisseur.domain.command.FournisseurCommands.FournisseurDetail;
import ministere.sante.senpna.fournisseur.domain.model.Fournisseur;

import org.springframework.stereotype.Component;

@Component
public class FournisseurDetailAssembler {

    public FournisseurDetail assembler(Fournisseur fournisseur) {
        return new FournisseurDetail(
                fournisseur.getId().getValue(),
                fournisseur.getNom(),
                fournisseur.getAdresse(),
                fournisseur.getTelephone(),
                fournisseur.getEmail(),
                fournisseur.getContactPrincipal(),
                fournisseur.isActif(),
                fournisseur.getCreatedAt(),
                fournisseur.getUpdatedAt());
    }
}
