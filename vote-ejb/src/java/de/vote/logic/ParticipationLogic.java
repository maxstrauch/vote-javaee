package de.vote.logic;

import de.vote.logic.to.PollTO;
import javax.ejb.Remote;

/**
 * Facade for public poll participation. The two methods provided by this 
 * interface are used by public visitors to view a poll and participate.
 * 
 * @author Daniel Vivas Estevao
 * @author maximilianstrauch
 */
@Remote
public interface ParticipationLogic {
    
    /**
     * Tries to retrieve a poll by a given token from the database. Thereby
     * only valid tokens, which have not been used, are considered.
     * 
     * @param token The token of a poll.
     * @return Return the {@link PollTO} object. Or <code>null</code> if the poll
     * cannot be found.
     * @throws IllegalArgumentException Is thrown if no {@link PollTO}
     * object can be found.
     * @throws IllegalStateException Is thrown if the poll is STARTED but not 
     * RUNNING! The message of the exception is the start date as unix time.
     */
    public PollTO getPollByToken(String token) 
            throws IllegalArgumentException, IllegalStateException;
    
    /**
     * Adds a vote to the given poll.
     * 
     * @param pollTO The poll object, retrieved through 
     * {@link ParticipateLogic#getPollByToken(java.lang.String)} and filled
     * with the vote result. 
     * @param token The token to invalidate.
     * @return <code>true</code> iff the vote was saved and otherwise <code>false</code>.
     * @throws IllegalArgumentException Is thrown if no poll is existing for
     * the poll TO (e.g. it might have ben closed during the voting time of the
     * participant).
     * @throws IllegalStateException  Is thrown if the vote is invalid.
     */
    public boolean participate(PollTO pollTO, String token) 
            throws IllegalArgumentException, IllegalStateException;
    
}
