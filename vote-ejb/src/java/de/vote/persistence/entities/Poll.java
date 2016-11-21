package de.vote.persistence.entities;

import de.vote.logic.impl.TOConverter;
import de.vote.logic.to.PollTO;
import javax.persistence.Entity;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * The entity for a poll.
 * 
 * @author Daniel Vivas Estevao
 * @author maximilianstrauch
 */
@Entity
public class Poll extends AbstractEntity<PollTO> {

    @ManyToMany(cascade = CascadeType.PERSIST)
    private Collection<Principal> organizers;

    @Enumerated(EnumType.STRING)
    private PollState pollState;
    
    private String title, description;

    @Temporal(TemporalType.TIMESTAMP)
    private Date startDate, endDate;
    
    private boolean participationTracking, reminderSend;
    
    private Integer reminderBeforeEnd;
    
    private String resultsId;

    @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.REMOVE})
    private Set<Token> tokens;
    
    @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.REMOVE})
    private Set<Participant> participants;
    
    @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.REMOVE})
    private Set<Item> items;
    
    public enum PollState {
        PREPARED,
        STARTED,
        RUNNING,
        FINISHED;
    }

    public Poll() {
        pollState = PollState.PREPARED;
    }
    
    public Collection<Principal> getOrganizers() {
        if (organizers == null) {
            setOrganizers(new HashSet<Principal>());
        }
        return organizers;
    }

    public void setOrganizers(Collection<Principal> param) {
        this.organizers = param;
    }

    public PollState getPollState() {
        return pollState;
    }

    public void setPollState(PollState pollState) {
        this.pollState = pollState;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public boolean isParticipationTracking() {
        return participationTracking;
    }

    public void setParticipationTracking(boolean participationTracking) {
        this.participationTracking = participationTracking;
    }

    public Set<Token> getTokens() {
        if (tokens == null) {
            setTokens(new HashSet<Token>());
        }
        return tokens;
    }

    public void setTokens(Set<Token> tokens) {
        this.tokens = tokens;
    }

    public Set<Participant> getParticipants() {
        if (participants == null) {
            setParticipants(new HashSet<Participant>());
        }
        return participants;
    }

    public void setParticipants(Set<Participant> participants) {
        this.participants = participants;
    }

    public Set<Item> getItems() {
        if (items == null) {
            setItems(new HashSet<Item>());
        }
        return items;
    }

    public void setItems(Set<Item> items) {
        this.items = items;
    }

    public Integer getReminderBeforeEnd() {
        return reminderBeforeEnd;
    }

    public void setReminderBeforeEnd(Integer reminderBeforeEnd) {
        this.reminderBeforeEnd = reminderBeforeEnd;
    }

    public boolean isReminderSend() {
        return reminderSend;
    }

    public void setReminderSend(boolean reminderSend) {
        this.reminderSend = reminderSend;
    }

    public String getResultsId() {
        return resultsId;
    }

    public void setResultsId(String resultsId) {
        this.resultsId = resultsId;
    }
    
    /**
     * Creates a comma separated string of all organizers of this poll.
     * 
     * @return All organizers of this poll
     */
    public String getOrganizersAsString() {
        if (organizers == null) {
            return null;
        }
        StringBuilder builder = new StringBuilder();
        
        Iterator<Principal> organs = organizers.iterator();
        while (organs.hasNext()) {
            builder.append(organs.next().getUsername());
            if (organs.hasNext()) {
                builder.append(", ");
            }
        }
        
        return builder.toString();
    }
    
    /**
     * Finds an item of this poll by a given id.
     * 
     * @param id The id of the item to find.
     * @return Returns the item with the given id if it is an item of this poll.
     * Otherwise <code>null</code> is returned.
     */
    public Item findItemById(Integer id) {
        for (Item item : getItems()) {
            if (Objects.equals(item.getId(), id)) {
                return item;
            }
        }
        return null;
    }
    
    /**
     * Finds an participant of this poll by its mail address.
     * 
     * @param email The mail address of the partipant.
     * @return Returns the participant object or <code>null</code> if this poll
     * has no participant with the given mail address.
     */
    public Participant findParticipantByEMail(String email) {
        for (Participant participant : getParticipants()) {
            if (Objects.equals(participant.getEmail(), email)) {
                return participant;
            }
        }
        return null;
    }
    
    /**
     * Counts the total number of participations of this poll by counting the
     * invalidates tokens.
     * 
     * @return Number of participations.
     */
    public int getParticipations() {
        int participations = 0;
        
        for (Token token : getTokens()) {
            if (token.isInvalid()) {
                participations++;
            }
        }
        return participations;
    }
    
    /**
     * Finds a {@link Token} for a given token.
     * 
     * @param tokenStr The token {@link String} to find.
     * @return The found {@link Token} object or <code>null</code> if the token
     * is not avialable.
     */
    public Token getTokenByToken(String tokenStr) {
        if (tokenStr == null) {
            return null;
        }
        
        for (Token token : tokens) {
            if (tokenStr.equals(token.getToken())) {
                return token;
            }
        }
        
        return null;
    }

    @Override
    public PollTO getTO(boolean deepCopy) {
        PollTO to = new PollTO(id);
        to.setTitle(title);
        to.setDescription(description);
        to.setStartDate(startDate);
        to.setEndDate(endDate);
        to.setPublished(pollState != PollState.PREPARED);
        to.setState("PollState" + pollState.name());
        if (reminderBeforeEnd == null) {
            to.setReminderBeforeEnd(0);
        } else {
            to.setReminderBeforeEnd(reminderBeforeEnd);
        }
        to.setResultsId(resultsId);
        to.setReminderSend(reminderSend);
        to.setParticipationTracking(participationTracking);
        if (pollState == PollState.FINISHED) {
            if (resultsId != null) {
                // Condition was checked before
                to.setValidResult(true);
            } else {
                // At least 3 participations are needed to view the result
                to.setValidResult(getParticipations() >= 3);
            }
        } else {
            to.setValidResult(false);
        }
        
        // The items and options should be embedded every time ...
        to.setItems(TOConverter.convertDeep(items));
        
        // Create a deep TO
        if (deepCopy) {
            to.setOrganizers(TOConverter.convert(organizers));
            to.setOrganizersAsString(getOrganizersAsString());
            to.setParticipants(TOConverter.convert(participants));
        }
        return to;
    }

}