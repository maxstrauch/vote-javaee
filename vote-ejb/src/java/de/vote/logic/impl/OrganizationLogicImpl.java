package de.vote.logic.impl;

import de.vote.logic.EntityOrder;
import de.vote.logic.OrganizationLogic;
import de.vote.logic.to.ParticipantSetTO;
import de.vote.logic.to.PollResultTO;
import de.vote.logic.to.PollResultTO.PollItemResultTO;
import de.vote.logic.to.PollResultTO.PollItemResultTO.PollItemOptionResultTO;
import de.vote.logic.to.PollTO;
import de.vote.logic.to.PollTO.ValidationResult;
import de.vote.logic.to.PrincipalTO;
import de.vote.persistence.PollAccess;
import de.vote.persistence.PrincipalAccess;
import de.vote.persistence.entities.Item;
import de.vote.persistence.entities.ItemOption;
import de.vote.persistence.entities.Participant;
import de.vote.persistence.entities.Poll;
import de.vote.persistence.entities.Poll.PollState;
import de.vote.persistence.entities.Principal;
import de.vote.persistence.entities.Token;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Schedule;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;

@Stateless
public class OrganizationLogicImpl implements OrganizationLogic {

    @Inject
    private PollAccess pollAccess;
    
    @Inject
    private PrincipalAccess principalAccess;
    
    @Inject
    private TOConverter converter;
    
    @Inject
    private MailDispatcher mailer;

    @RolesAllowed(value = {"ORGANIZER", "ADMINISTRATOR"})
    @Override
    public List<PollTO> getPolls(int uid, PollType type, int start, int count, 
            EntityOrder order) {
        List<Poll> polls = pollAccess.getAllByType(type, uid, start, count, order);
        
        // When closed polls are requested, make sure that the poll state
        // property is set correctly; the SQL stmt generated for the
        // db access lists also polls that are finished by date and not
        // set to the state FINISHED. So those need to be updated.
        if (type == PollType.CLOSED) {
            for (Poll poll : polls) {
                if (poll.getPollState() == Poll.PollState.FINISHED) {
                    continue; // Is already finished
                }
                
                // Adjust the state
                poll.setPollState(PollState.FINISHED);
                pollAccess.store(poll);
            }
        }
        
        // Return transfer list
        return TOConverter.convert(polls);
    }

    @RolesAllowed(value = {"ORGANIZER", "ADMINISTRATOR"})
    @Override
    public int getTotalPollCount(int uid, PollType type) {
        return pollAccess.getTotalCountByType(type, uid);
    }

    @RolesAllowed(value = {"ORGANIZER", "ADMINISTRATOR"})
    @Override
    public List<PrincipalTO> getAllOrganizers() {
        return TOConverter.convert(principalAccess.getAll(0, -1, EntityOrder.USERNAME));
    }

    @RolesAllowed(value = {"ORGANIZER", "ADMINISTRATOR"})
    @Override
    public List<ParticipantSetTO> getParticipantSets(int uid) {
        // Get all polls
        List<Poll> allPolls = pollAccess.getAllByType(
                PollType.ALL, uid, 0, -1, EntityOrder.TITLE);
        
        // Generate participant sets
        List<ParticipantSetTO> participantSets = new ArrayList<>();
        for (Poll poll : allPolls) {
            ParticipantSetTO set = new ParticipantSetTO(poll.getId());
            set.setTitle(poll.getTitle());
            set.setParticipants(TOConverter.convert(poll.getParticipants()));
            participantSets.add(set);
        }
        
        return participantSets;
    }

    @RolesAllowed(value = {"ORGANIZER", "ADMINISTRATOR"})
    @Override
    public int persistPoll(PollTO pollTo) throws IllegalArgumentException, 
            IllegalAccessException {
        if (pollTo == null) {
            throw new IllegalArgumentException("No poll given to persist");
        }
        
        if (pollTo.isValid() != ValidationResult.VALID) {
            throw new IllegalArgumentException("Poll not valid!");
        }
        
        
        // Get a poll object
        Poll poll;
        if (pollTo.getId() < 0) {
            // A new poll needs to be persisted
            poll = new Poll();
            
            // Requirement 1.2
            if (pollAccess.isPollTitleExisiting(pollTo.getTitle())) {
                throw new IllegalArgumentException("A poll with this title "
                        + "'" + pollTo.getTitle() + "' is already existing.");
            } 
        } else {
            poll = pollAccess.find(pollTo.getId());
        }
        
        if (poll.getPollState() == Poll.PollState.FINISHED/* ||
                poll.getPollState() == Poll.PollState.STARTED*/) {
            throw new IllegalAccessException("Poll is finished and "
                    + "cannot be modified"); 
        }
        
        // Fill back the entity class by the TO. This implementation
        // takes also care of the state of the poll and fills only fields
        // of the entity class which are allowed in the current poll state.
        converter.fillByTO(poll, pollTo, 0);
        
        pollAccess.store(poll); // Stores the (new) poll object
        return poll.getId();
    }

    @RolesAllowed(value = {"ORGANIZER", "ADMINISTRATOR"})
    @Override
    public void publishPoll(int pid, Locale eMailLocale) 
            throws IllegalArgumentException, IllegalStateException {
        // Get the poll
        Poll poll = pollAccess.find(pid);
        
        if (poll.getPollState() != PollState.PREPARED) {
            throw new IllegalArgumentException("Poll not in PREPARED!");
        }
        
        // Generate tokens and send mails
        String failedMails = "";
        for (Participant participant : poll.getParticipants()) {
            // Participant hasn't voted yet
            participant.setHasVoted(false);
            
            // Generate token
            String newTokenStr = UUID.randomUUID().toString();
            Token token = new Token();
            poll.getTokens().add(token);
            token.setToken(newTokenStr);
            token.setInvalid(false);
            
            // If tracking is enabled, store a reference to the participant
            if (poll.isParticipationTracking()) {
                token.setParticipant(participant);
            }

            // Send the token mail
            DateFormat df = DateFormat.getDateTimeInstance(DateFormat.SHORT, 
                DateFormat.SHORT, eMailLocale);
            Principal by = poll.getOrganizers().iterator().next();
            if (!mailer.sendMail(
                    participant, 
                    by, 
                    "Mail.newVote", 
                    eMailLocale, 
                    poll.getOrganizersAsString(),
                    poll.getTitle(),
                    df.format(poll.getStartDate()),
                    df.format(poll.getEndDate()),
                    poll.getParticipants().size(),
                    MailDispatcher.getAppURL(),
                    newTokenStr
            )) {
                failedMails += ", " + participant.getEmail();
            }
        }
        
        // Update state
        poll.setPollState(PollState.STARTED);
        
        // Persist!
        pollAccess.store(poll);
        
        if (failedMails.length() > 1) {
            failedMails = failedMails.substring(2);
            throw new IllegalStateException(failedMails);
        }
    }

    @RolesAllowed(value = {"ORGANIZER", "ADMINISTRATOR"})
    @Override
    public PollTO getPoll(int pid) {
        try {
            return pollAccess.find(pid).getTO(true);
        } catch (Exception ex) {
            return null;
        }
    }

    @RolesAllowed(value = {"ORGANIZER", "ADMINISTRATOR"})
    @Override
    public void cancelPoll(int pid) throws IllegalArgumentException {
        Poll p = pollAccess.find(pid);
        
        // Set FINISHED
        p.setPollState(PollState.FINISHED);
        for (Item item : p.getItems()) {
            item.setVoidTally(0);
            for (ItemOption itemOption : item.getOptions()) {
                itemOption.setTally(0);
            }
        }
        
        // Remove all participants and tokens
        EntityManager em = pollAccess.getEntityManager();
        for (Participant participant : p.getParticipants()) {
            em.remove(participant);
        }
        
        for (Token token : p.getTokens()) {
            em.remove(token);
        }
        
        p.setParticipants(new HashSet<Participant>());
        p.setTokens(new HashSet<Token>());
        
        // Store the result
        pollAccess.store(p);
    }

    // This method is only called by system users to show a private preview
    // of the results
    @RolesAllowed(value = {"ORGANIZER", "ADMINISTRATOR"})
    @Override
    public PollResultTO getResults(int pid) throws IllegalArgumentException, 
            IllegalArgumentException {
        return getResults(pollAccess.find(pid));
    }
    
    /**
     * Internal method to create a {@link PollResultTO} object with the results
     * of a poll.
     * 
     * @param poll The poll to create the results TO from.
     * @return The TO containing the results of the poll.
     * @throws IllegalStateException Is thrown if the poll is not finished and
     * result viewing is forbidden.
     */
    private PollResultTO getResults(Poll poll) throws IllegalStateException {
        if (poll.getPollState() != PollState.FINISHED) {
            throw new IllegalArgumentException("Poll not found");
        }
        
        if (poll.getParticipations() < 3) {
            throw new IllegalStateException("Too less participants");
        }

        // Fill the result object
        PollResultTO result = new PollResultTO();
        result.setId(poll.getResultsId());
        result.setTitle(poll.getTitle());
        result.setDescription(poll.getDescription());
        result.setStartDate(poll.getStartDate());
        result.setEndDate(poll.getEndDate());
        // Total # of participants which could vote
        result.setParticipants(poll.getParticipants().size());
        // Actual number of votes given
        int votedParticipants = 0;
        for (Participant participant : poll.getParticipants()) {
            if (participant.isHasVoted()) {
                votedParticipants++;
            }
        }
        result.setParticipations(votedParticipants);
        
        for (Item item : poll.getItems()) {
            PollItemResultTO itemResult = result.new PollItemResultTO();
            itemResult.setTitle(item.getTitle());
            itemResult.setVoidTally(item.getVoidTally());
            result.getItems().add(itemResult);
            
            for (ItemOption itemOption : item.getOptions()) {
                PollItemOptionResultTO itemOptionResult = itemResult.new PollItemOptionResultTO();
                itemOptionResult.setShortName(itemOption.getShortName());
                itemOptionResult.setDescription(itemOption.getDescription());
                itemOptionResult.setTally(itemOption.getTally());
                itemResult.getOptions().add(itemOptionResult);
            }
            
        }
        
        return result;
    }

    // No roles: this method is called by the public
    @Override
    public PollResultTO getResultsByResultsId(String resultsId) throws 
            IllegalArgumentException, IllegalStateException {
        return getResults(pollAccess.getPollByResultsId(resultsId));
    }
    
    @RolesAllowed(value = {"ORGANIZER", "ADMINISTRATOR"})
    @Override
    public String togglePublishPollResults(int pid) 
            throws IllegalArgumentException, IllegalStateException {
        Poll poll = pollAccess.find(pid);
        
        if (poll.getPollState() != PollState.FINISHED) {
            throw new IllegalArgumentException("Poll not found");
        }
        
        if (poll.getParticipations() < 3) {
            throw new IllegalStateException("Too less participants");
        }
        
        String id;
        if (poll.getResultsId() == null) {
            poll.setResultsId(id = UUID.randomUUID().toString());
        } else {
            poll.setResultsId(id = null);
        }
        
        pollAccess.store(poll);
        
        return id;
    }
    
    @RolesAllowed(value = {"ORGANIZER", "ADMINISTRATOR"})
    @Override
    public void sendPollResults(int pid, Locale eMailLocale) 
            throws IllegalArgumentException, IllegalStateException {
        Poll poll = pollAccess.find(pid);
        
        // Check states
        if (poll.getPollState() != PollState.FINISHED 
                || poll.getResultsId() == null 
                || poll.getResultsId().length() < 2) {
            throw new IllegalArgumentException("Poll results are not published");
        }
        
        // Get the result URL
        String resultUrl = MailDispatcher.getAppURL() + "results.xhtml?id=" 
                + poll.getResultsId();
        
        // Send mail to each participant
        String failedMails = "";
        Principal by = null;
        if (!poll.getOrganizers().isEmpty()) {
            by = poll.getOrganizers().iterator().next();
        }
        
        for (Participant participant : poll.getParticipants()) {
            if (!mailer.sendMail(
                    participant, 
                    by, 
                    "Mail.pollResults", 
                    eMailLocale, 
                    poll.getTitle(),
                    resultUrl,
                    poll.getOrganizersAsString()
            )) {
                failedMails += ", " + participant.getEmail();
            }
        }
        
        if (failedMails.length() > 1) {
            failedMails = failedMails.substring(2);
            throw new IllegalStateException(failedMails);
        }
    }
    
    /**
     * This methods performs the mail poll reminder. It is executed every
     * hour and looks for polls with active reminer wich was "fired" in the last
     * hour. It works for polls with participation tracking enabled and for 
     * those without.
     */
    @Schedule(hour = "*", minute = "0", second = "0") // Every hour
    public void pollReminderPerformed() {
        // Get all open polls
        List<Poll> openPolls = pollAccess.getAllByType(PollType.PUBLISHED, 
                -1, 0, -1, EntityOrder.TITLE);
        
        for (Poll poll : openPolls) {
            // Check if activated
            if (poll.getReminderBeforeEnd() == null || poll.getReminderBeforeEnd() < 1) {
                continue; // No reminder configured
            }
            
            // Check if already send
            if (poll.isReminderSend()) {
                continue;
            }
            
            // Calculate reminder date and current date
            long reminderTime = poll.getEndDate().getTime();
            reminderTime -= PollTO.ONE_DAY * poll.getReminderBeforeEnd();
            Date reminder = new Date(reminderTime);
            Date now = new Date(System.currentTimeMillis());

            if (now.after(reminder)) {
                // It might be a good idea to store the locale in the poll
                Locale eMailLocale = Locale.GERMAN;
                DateFormat df = DateFormat.getDateTimeInstance(DateFormat.SHORT, 
                        DateFormat.SHORT, eMailLocale);
                Principal by = null;
                if (!poll.getOrganizers().isEmpty()) {
                    by = poll.getOrganizers().iterator().next();
                }

                // It's time to remind
                for (Participant participant : poll.getParticipants()) {
                    if (!participant.isHasVoted()) {
                        // Send mail
                        mailer.sendMail(
                            participant, 
                            by, 
                            // Distinguish between polls with participation tracking on
                            // (mail to participants which has not voted yet) and 
                            // off (mail to all participants)
                            poll.isParticipationTracking() ? 
                                    "Mail.voteReminder" : "Mail.voteReminderGeneral",
                            eMailLocale, 
                            poll.getOrganizersAsString(),
                            poll.getTitle(),
                            df.format(poll.getEndDate())
                        );
                    }
                }
                
                // Update reminder state
                poll.setReminderSend(true);
                pollAccess.store(poll);
            }
        }
    }
    
}
