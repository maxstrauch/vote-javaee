package de.vote.logic.to;

import java.util.ArrayList;
import java.util.List;

/**
 * This TO has no entity, because it is only used to provide lists of
 * participants of older polls of a principal to add them to a new poll.
 * 
 * @author Daniel Vivas Estevao
 * @author maximilianstrauch
 */
public class ParticipantSetTO extends AbstractTO {    
    
    private String title;
    
    private List<ParticipantTO> participants;
    
    public ParticipantSetTO(int id) {
        super(id);
        this.participants = new ArrayList<>();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<ParticipantTO> getParticipants() {
        return participants;
    }

    public void setParticipants(List<ParticipantTO> participants) {
        this.participants = participants;
    }
    
}
