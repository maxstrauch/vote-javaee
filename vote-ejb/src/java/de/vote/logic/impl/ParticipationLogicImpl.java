package de.vote.logic.impl;

import de.vote.logic.ParticipationLogic;
import de.vote.logic.to.ItemOptionTO;
import de.vote.logic.to.ItemTO;
import de.vote.logic.to.PollTO;
import de.vote.persistence.PollAccess;
import de.vote.persistence.entities.Item;
import de.vote.persistence.entities.ItemOption;
import de.vote.persistence.entities.Poll;
import de.vote.persistence.entities.Token;
import java.util.Date;
import javax.ejb.Stateless;
import javax.inject.Inject;

@Stateless
public class ParticipationLogicImpl implements ParticipationLogic {

    @Inject
    private PollAccess pollAccess;

    // No role annotation, because this method is public
    @Override
    public PollTO getPollByToken(String token) throws IllegalArgumentException, 
            IllegalStateException {
        // Get the poll
        Poll poll = pollAccess.getPollByToken(token);
        
        // Prepared polls and finished polls are "unkown" to the web interface
        // but they might not be found, because PREPARED have not tokens and
        // FINISHED polls have invalid tokens only, so this if could be
        // omitted
        if (poll.getPollState() == Poll.PollState.PREPARED ||
                poll.getPollState() == Poll.PollState.FINISHED) {
            throw new IllegalArgumentException("No poll for token found");
        }
        
        // If STARTED
        if (poll.getPollState() == Poll.PollState.STARTED) {
            // Check if the poll should be in state RUNNING
            Date now = new Date(System.currentTimeMillis());
            if (poll.getStartDate().before(now)) {
                poll.setPollState(Poll.PollState.RUNNING);
                pollAccess.store(poll);
            } else {
                // If not throw an exception
                throw new IllegalStateException(String.valueOf(poll.getStartDate().getTime()));
            }
        }
        
        // Not set to finished, but finished
        if (poll.getEndDate().before(new Date(System.currentTimeMillis()))) {
            poll.setPollState(Poll.PollState.FINISHED);
            pollAccess.store(poll);
            return null;
        }
        
        // Return the poll
        PollTO to = TOConverter.convert(poll); // Flat copy is sufficient
        to.setOrganizersAsString(poll.getOrganizersAsString());
        poll.getParticipants().iterator(); // Fill the list
        to.setParticipantsCount(poll.getParticipants().size());
        return to;
    }

    // No role annotation, because this method is public
    @Override
    public boolean participate(PollTO pollTO, String token) 
            throws IllegalArgumentException, IllegalStateException {
        // Get the poll
        Poll poll = pollAccess.getPollByToken(token);
        
        if (poll.getPollState() != Poll.PollState.RUNNING) {
            throw new IllegalArgumentException("No Poll found for the token");
        }
        
        // Not set to finished, but finished
        if (poll.getEndDate().before(new Date(System.currentTimeMillis()))) {
            poll.setPollState(Poll.PollState.FINISHED);
            pollAccess.store(poll);
            return false;
        }
        
        // Check if participation is valid
        if (!pollTO.isValidVote()) {
            throw new IllegalStateException("Given participation is invalid");
        }

        // Merge the vote into the poll object
        for (ItemTO itemTO : pollTO.getItems()) {
            Item item = poll.findItemById(itemTO.getId());
            
            // If the item is not exisiting: rollback (should never happen)
            if (item == null) {
                throw new IllegalStateException("Invalid Item id");
            }
            
            // Void votes are more important than option selections
            if (itemTO.isVotedVoid()) {
                item.addVoidVote();
                continue;
            }
            
            // If not set to void, check options
            for (ItemOptionTO optionTO : itemTO.getOptions()) {
                ItemOption option = item.findOptionById(optionTO.getId());
                
                // If the option is not existing: rollback (should never happen)
                if (option == null) {
                    throw new IllegalStateException("Invalid ItemOption id");
                }
                
                if (optionTO.isSelected()) {
                    option.addVote();
                }
            }            
        }
        
        // Invalidate the token now
        Token theToken = poll.getTokenByToken(token);
        if (theToken == null) {
            // This should really not happen, because the Poll was fetched
            // by the token
            throw new IllegalArgumentException("Token not found!");
        }

        // Invalidate the token ...
        theToken.setInvalid(true); // ... by setting it to invalid
        // ... and setting also the participant to has voted if the
        // tracking is enabled
        if (theToken.getParticipant() != null) {
            theToken.getParticipant().setHasVoted(true);
            theToken.setParticipant(null);
        }
        
        // Check if the poll is finished
        boolean allTokensInvalid = true;
        for (Token t : poll.getTokens()) {
            allTokensInvalid &= t.isInvalid();
        }
        
        if (allTokensInvalid) {
            poll.setPollState(Poll.PollState.FINISHED);
        }
        
        // Commit the changes and write them into the database
        pollAccess.store(poll);
        return true;
    }
    
}
