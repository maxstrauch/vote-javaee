package de.vote.persistence.entities;

import de.vote.logic.to.ParticipantTO;
import javax.persistence.Entity;

/**
 * The entity for a poll participant.
 * 
 * @author Daniel Vivas Estevao
 * @author maximilianstrauch
 */
@Entity
public class Participant extends AbstractEntity<ParticipantTO> {
    
    private String email;
    
    private boolean hasVoted;
    
    public Participant() { }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isHasVoted() {
        return hasVoted;
    }

    public void setHasVoted(boolean hasVoted) {
        this.hasVoted = hasVoted;
    }

    @Override
    public ParticipantTO getTO(boolean deepCopy) {
        ParticipantTO to = new ParticipantTO();
        to.setEmail(email);
        to.setParticipated(hasVoted);
        return to;
    }
    
}
