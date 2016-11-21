package de.vote.persistence;

import de.vote.persistence.entities.Participant;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;

/**
 * Implementation of {@link AbstractAccess} to access {@link Participant} entites.
 * 
 * @author Daniel Vivas Estevao
 * @author maximilianstrauch
 */
@Stateless
@LocalBean
public class ParticipantAccess extends AbstractAccess<Participant> {

    public ParticipantAccess() {
        super(Participant.class);
    }
    
}
