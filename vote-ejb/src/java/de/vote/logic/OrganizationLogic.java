package de.vote.logic;

import de.vote.logic.to.ParticipantSetTO;
import de.vote.logic.to.ParticipantTO;
import de.vote.logic.to.PollResultTO;
import de.vote.logic.to.PollTO;
import de.vote.logic.to.PrincipalTO;
import de.vote.persistence.entities.Poll;
import java.util.List;
import java.util.Locale;
import javax.ejb.Remote;

/**
 * Facade for business methods of poll organizers.
 * 
 * @author Daniel Vivas Estevao
 * @author maximilianstrauch
 */
@Remote
public interface OrganizationLogic {
    
    /**
     * Simple PollType enum for the facade which maps to {@link Poll.PollState}.
     */
    public enum PollType {
        PUBLISHED, UNPUBLISHED, CLOSED, ALL;
    }
    
    /**
     * Requests a list of {@link PollTO} objects which apply to the given
     * type. The resulting {@link PollTO}s are only a simple object copy
     * (no deep copy). Furthermore only polls for a specific user with 
     * the ID <code>uid</code> are returned. The returned objects are only a
     * <u><b>flat copy</b></u>.
     * 
     * @param uid User id.
     * @param type The type of polls to look for.
     * @param start Start element index of the list.
     * @param count Number of polls to fetch.
     * @param order The order of the polls.
     * @return A list with {@link PollTO} objects with the given type and in
     * the requested quantity.
     */
    public List<PollTO> getPolls(int uid, PollType type, int start, 
            int count, EntityOrder order);
    
    /**
     * Returns the total number of polls in the system for the given type and
     * the principal with the ID <code>uid</code>.
     * 
     * @param uid User id.
     * @param type The type of polls to look for.
     * @return Total count of polls in the system.
     */
    public int getTotalPollCount(int uid, PollType type);

    /**
     * Returns a list of all organizers available in the system.
     * 
     * @return All organizers in the system or an empty list.
     */
    public List<PrincipalTO> getAllOrganizers();
    
    /**
     * Returns a list of {@link ParticipantSetTO}s for the user identified
     * by the given ID.
     * 
     * @param uid ID to identify the user.
     * @return All sets of {@link ParticipantTO}s used by the user with the
     * ID <code>id</code> in past polls or an empty list.
     */
    public List<ParticipantSetTO> getParticipantSets(int uid);
    
    /**
     * Persists a {@link PollTO} object.
     * 
     * @param pollTo A new {@link PollTO} to create or an exisiting poll with
     * the ID field set.
     * @return Returns the id of the created object, if any, and otherwise -1.
     * @throws IllegalArgumentException Is thrown if one of the arguments
     * is wrong.
     * @throws java.lang.IllegalAccessException Is thrown if the poll cannot be
     * modified.
     */
    public int persistPoll(PollTO pollTo) throws IllegalArgumentException, 
            IllegalAccessException;
    
    /**
     * Publishes the poll with the given id.
     * 
     * @param pid ID of the poll to publish.
     * @param eMailLocale The {@link Locale} to use for the language of the
     * e-mail which is send by the system.
     * @throws IllegalArgumentException Is thrown if one of the arguments
     * is wrong.
     * @throws IllegalStateException Is thrown iff not all participants could
     * be informed with a mail. The message contains all mails that could
     * not be informed. The poll and its changed are saved before the exception
     * is thrown.
     */
    public void publishPoll(int pid, Locale eMailLocale) 
            throws IllegalArgumentException, IllegalStateException;
    
    /**
     * Returns a {@link PollTO} object for a given poll ID iff this ID is
     * valid and maps to an existing poll. The returned {@link PollTO} is a
     * <u><b>deep copy</b></u> of the poll.
     * 
     * @param pid ID of the poll to return.
     * @return The {@link PollTO} object or <code>null</code> if there is
     * no matching poll.
     */
    public PollTO getPoll(int pid);

    /**
     * Cancels the {@link Poll} object with the given ID. During this process,
     * the poll is set to state FINISHED, the vote counters are set to zero and
     * all participants and all tokens associated with this poll are removed.
     * 
     * @param pid The ID of the {@link Poll} to cancel.
     * @throws IllegalArgumentException Is thrown iff there is no poll
     * existing for the given ID.
     */
    public void cancelPoll(int pid) throws IllegalArgumentException;
    
    /**
     * Provides the result of a finished poll identified by the ID.
     * 
     * @param pid The ID of the poll.
     * @return The object containing the result data.
     * @throws IllegalArgumentException Is thrown iff there is no poll
     * existing for the given ID.
     * @throws IllegalStateException Is thrown iff the poll has not enough
     * participants to view
     */
    public PollResultTO getResults(int pid) throws IllegalArgumentException, 
            IllegalStateException;
    
    /**
     * Tries to provide the results for the poll identified by the given
     * result ID.
     * 
     * @param resultsId Result ID of the poll.
     * @return The object containing the result data.
     * @throws IllegalArgumentException Is thrown iff there is no poll
     * existing for the given ID.
     * @throws IllegalStateException Is thrown iff the poll has not enough
     * participants to view
     */
    public PollResultTO getResultsByResultsId(String resultsId) 
            throws IllegalArgumentException, IllegalStateException;
    
    /**
     * Toggles the published state of the polls result.
     * 
     * @param pid  The ID of the poll.
     * @return Returns the ID of the results or <code>null</code> if the
     * results are unpublished.
     * @throws IllegalArgumentException Is thrown iff there is no poll
     * existing for the given ID.
     * @throws IllegalStateException Is thrown iff the poll has not enough
     * participants to view
     */
    public String togglePublishPollResults(int pid) 
            throws IllegalArgumentException, IllegalStateException;
    
    /**
     * Sends a mail with an invitation link to view the results of the polls
     * to all participants.
     * 
     * @param pid The ID of the poll
     * @param eMailLocale The {@link Locale} to use for the language of the
     * e-mail which is send by the system.
     * @throws IllegalArgumentException Is thrown iff there is no poll
     * existing for the given ID.
     * @throws IllegalStateException Is thrown iff not all participants could
     * be informed with a mail. The message contains all mails that could
     * not be informed. The poll and its changed are saved before the exception
     * is thrown.
     */
    public void sendPollResults(int pid, Locale eMailLocale) 
            throws IllegalArgumentException, IllegalStateException; 
    
}
