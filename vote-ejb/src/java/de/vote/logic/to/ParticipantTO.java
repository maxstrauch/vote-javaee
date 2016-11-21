package de.vote.logic.to;

import java.util.Objects;

/**
 * TO for {@link Participant}.
 * 
 * @author Daniel Vivas Estevao
 * @author maximilianstrauch
 */
public class ParticipantTO extends AbstractTO {
    
    private String email;
    
    private boolean participated;

    public ParticipantTO() {
        super(-1);
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
        // Dirty hack to get a valid id
        setId(42 * email.length() + Objects.hashCode(email));
    }

    public boolean isParticipated() {
        return participated;
    }

    public void setParticipated(boolean participated) {
        this.participated = participated;
    }
    
}
