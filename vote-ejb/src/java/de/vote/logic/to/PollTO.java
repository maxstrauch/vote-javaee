package de.vote.logic.to;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * TO for {@class Poll}. This TO contains more (and other) attributes than the
 * entity class, because some values need to be flat (e.g. converted into strings) 
 * for the WEB tier.
 * 
 * @author Daniel Vivas Estevao
 * @author maximilianstrauch
 */
public class PollTO extends AbstractTO {
    
    /**
     * Temporal constants
     */
    public static final long 
            ONE_DAY = 1000 * 60 * 60 * 24,
            NEW_POLL_BEGIN = 1000 * 60 * 60, // 1 hour
            NEW_POLL_DURATION = ONE_DAY * 4; // 4 days
    
    private String title, description, resultsId;
    
    private Date startDate, endDate;
    
    private List<ItemTO> items;
    
    private int reminderBeforeEnd;
    
    private boolean participationTracking, reminderSend;
    
    private int participantCount;
    
    /**
     * "Flat" strings
     */
    private String state, organizersAsString;
    
    private boolean published, validResult;
    
    private List<ParticipantTO> participants; 
    
    private List<PrincipalTO> organizers;
    
    /**
     * Enum for poll validation.
     */
    public enum ValidationResult {
        VALID,
        POLL_NO_TITLE, POLL_ILLEGAL_START_END_DATE, POLL_TOO_LESS_PARTICIPANTS, 
            POLL_TOO_LESS_ITEMS, POLL_REMINDER_OUT_OF_RANGE,
        ITEM_NO_TITLE, ITEM_INVALID_M, ITEM_TOO_LESS_OPTIONS,
        OPTION_NO_SHORT_NAME;
    }
    
    public PollTO(Integer id) {
        super(id);
        startDate = new Date(System.currentTimeMillis() + NEW_POLL_BEGIN);
        endDate = new Date(startDate.getTime() + NEW_POLL_DURATION);
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

    public List<ItemTO> getItems() {
        if (items == null) {
            items = new ArrayList<>();
        }
        
        return items;
    }

    public void setItems(List<ItemTO> items) {
        this.items = items;
    }

    public List<PrincipalTO> getOrganizers() {
        if (organizers == null) {
            organizers = new ArrayList<>();
        }
        
        return organizers;
    }

    public void setOrganizers(List<PrincipalTO> organizers) {
        this.organizers = organizers;
    }

    public String getOrganizersAsString() {
        return organizersAsString;
    }

    public void setOrganizersAsString(String organizersAsString) {
        this.organizersAsString = organizersAsString;
    }
    
    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public int getReminderBeforeEnd() {
        return reminderBeforeEnd;
    }

    public void setReminderBeforeEnd(int reminderBeforeEnd) {
        this.reminderBeforeEnd = reminderBeforeEnd;
    }
    
    public boolean isParticipationTracking() {
        return participationTracking;
    }

    public void setParticipationTracking(boolean participationTracking) {
        this.participationTracking = participationTracking;
    }

    public boolean isPublished() {
        return published;
    }

    public void setPublished(boolean published) {
        this.published = published;
    }

    public List<ParticipantTO> getParticipants() {
        if (participants == null) {
            participants = new ArrayList<>();
        }
        return participants;
    }

    public void setParticipants(List<ParticipantTO> participants) {
        this.participants = participants;
    }

    public String getResultsId() {
        return resultsId;
    }

    public void setResultsId(String resultsId) {
        this.resultsId = resultsId;
    }
    
    public boolean hasPublicResultsId() {
        return resultsId != null && !resultsId.isEmpty();
    }

    public boolean isReminderSend() {
        return reminderSend;
    }

    public void setReminderSend(boolean reminderSend) {
        this.reminderSend = reminderSend;
    }

    public boolean isValidResult() {
        return validResult;
    }

    public void setValidResult(boolean validResult) {
        this.validResult = validResult;
    }
    
    public int getParticipantsCount() {
        if (participants == null || participants.size() < 1) {
            return participantCount;
        }
        
        return participants.size();
    }
    
    public void setParticipantsCount(int count) {
        participantCount = count;
    }
    
    public int getParticipationCount() {
        if (!participationTracking) {
            return 0;
        }
        
        int count = 0;
        for (ParticipantTO participantTO : participants) {
            count += participantTO.isParticipated() ? 1 : 0;
        }
        return count;
    }
   
    /**
     * Sorts the items and options by the order indicated by the index field.
     */
    public void sort() {
        for (ItemTO item : getItems()) {
            Collections.sort(item.getOptions());
        }
        Collections.sort(getItems());
    }
    
    /**
     * Tests if this poll is valid, e.g. has valid properties.
     * 
     * @return The {@link ValidationResult} which indicates the result of the
     * validation.
     */
    public ValidationResult isValid() {
        if (title == null || title.trim().isEmpty()) {
            return ValidationResult.POLL_NO_TITLE;
        }
        
        if (startDate == null || endDate == null) {
            return ValidationResult.POLL_ILLEGAL_START_END_DATE;
        }
        
        if (endDate.before(startDate) || endDate.equals(startDate)) {
            return ValidationResult.POLL_ILLEGAL_START_END_DATE;
        }
        
//        if (startDate.before(new Date())) {
//            return ValidationResult.POLL_ILLEGAL_START_END_DATE;
//        }
        
        if (participationTracking) {
            
            Date reminderDate = new Date(getEndDate().getTime() 
                    - ONE_DAY * getReminderBeforeEnd());
            
            if (!reminderDate.after(getStartDate())) {
                
                return ValidationResult.POLL_REMINDER_OUT_OF_RANGE;
            }
            
            
        }
        
        if (participants.size() < 3) {
            return ValidationResult.POLL_TOO_LESS_PARTICIPANTS;
        }
        
        if (getItems().size() < 1) {
            return ValidationResult.POLL_TOO_LESS_ITEMS;
        }
        
        boolean isValid = true;
        ValidationResult result;
        for (ItemTO itemTO : getItems()) {
            isValid &= (result = itemTO.isValid()) == ValidationResult.VALID;
            
            if (!isValid) {
                return result;
            }
        }

        return ValidationResult.VALID;
    }
    
    /**
     * Tests if the set vote for this poll is valid by invoking the
     * {ItemTO.isValidVote()} method for all items.
     * 
     * @return <code>true</code> iff all items are valid voted. Otherwise 
     * <code>false</code> is returned.
     */
    public boolean isValidVote() {
        boolean valid = true;
        for (ItemTO item : items) {
            valid &= item.isValidVote();
        }
        return valid;
    }
    
}
