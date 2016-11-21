package de.vote.persistence.entities;

import javax.persistence.Entity;
import javax.persistence.OneToOne;

/**
 * The entity for a token.
 * 
 * @author Daniel Vivas Estevao
 * @author maximilianstrauch
 */
@Entity
public class Token extends AbstractEntity<Void> {
    
    @OneToOne
    private Participant participant;
    
    private String token;
    
    private boolean invalid;
    
    public Token() { }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public boolean isInvalid() {
        return invalid;
    }

    public void setInvalid(boolean invalid) {
        this.invalid = invalid;
    }

    public Participant getParticipant() {
        return participant;
    }

    public void setParticipant(Participant participant) {
        this.participant = participant;
    }

    @Override
    public Void getTO(boolean deepCopy) {
        return null; // No transfer object provided
    }

}
