package ministere.sante.senpna.actualite.application.service;

import ministere.sante.senpna.actualite.domain.command.ActualiteCommands.ActualiteDetail;
import ministere.sante.senpna.actualite.domain.command.ActualiteCommands.MediaDetail;
import ministere.sante.senpna.actualite.domain.model.Actualite;
import ministere.sante.senpna.actualite.domain.model.ActualiteMedia;

import org.springframework.stereotype.Component;

@Component
public class ActualiteDetailAssembler {

    public ActualiteDetail assembler(Actualite actualite) {
        return new ActualiteDetail(
                actualite.getId().getValue(),
                actualite.getCategorie().name(),
                actualite.getTitre(),
                actualite.getDescription(),
                actualite.getMedias().stream().map(this::assemblerMedia).toList(),
                actualite.getAuteurId(),
                actualite.getAuteurNom(),
                actualite.getTags(),
                actualite.getStatut().name(),
                actualite.getCreatedAt(),
                actualite.getUpdatedAt());
    }

    private MediaDetail assemblerMedia(ActualiteMedia media) {
        return new MediaDetail(media.getId(), media.getType().name(), media.getUrl(), media.getOrdre());
    }
}
