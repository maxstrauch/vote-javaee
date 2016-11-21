package de.vote.web;

import de.vote.LocaleBean;
import de.vote.logic.ParticipationLogic;
import de.vote.logic.to.ItemOptionTO;
import de.vote.logic.to.ItemTO;
import de.vote.logic.to.PollTO;
import static de.vote.web.ExceptionUtil.getRootAsObject;
import de.vote.web.filter.JumpToIndexFilter;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

/**
 * This backing bean manages the complete frontend actions of the Vote! system:
 * participants who vote. It also provieds a preview feature for unpublished
 * polls.
 * 
 * @author Daniel Vivas Estevao
 * @author maximilianstrauch
 */
@SessionScoped
@Named
public class ParticipateBean implements Serializable {

    @EJB
    private ParticipationLogic participateLogic;
    
    @Inject
    private LocaleBean localeBean;
    
    private String token;
    
    private PollTO pollTO;
    
    private boolean hasParticipated = false, preview = false;
    
    /**
     * This map holds error messages related to specific items of a poll
     * in order to display them at the right position.
     */
    private final Map<String, String> message = new HashMap<>();;
    
    // Message management
    
    public void addMessage(ItemTO item, String msg) {
        message.put("poll" + item.getId(), msg);
    }
    
    public String getMessage(ItemTO item) {
        String key = "poll" + item.getId();
        
        if (message.containsKey(key)) {
            return (String) message.remove(key);
        }
        
        return "";
    }
    
    public boolean hasMessage(ItemTO item) {
        return message.containsKey("poll" + item.getId());
    }
    
    public boolean hasMessages() {
        return !message.isEmpty();
    }
    
    // Poll & token management
    
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public PollTO getPollTO() {
        return pollTO;
    }
    
    public boolean isHasParticipated() {
        return hasParticipated;
    }
    
    public boolean isPreview() {
        return preview;
    }
    
    public String getFormattedStartDate() {
        if (pollTO == null) {
            return "";
        }
        
        return localeBean.getDateFormatForCurrentLocale().format(pollTO.getStartDate());
    }
    
    public String getFormattedEndDate() {
        if (pollTO == null) {
            return "";
        }
        
        return localeBean.getDateFormatForCurrentLocale().format(pollTO.getEndDate());
    }
    
    public String getItemInfoString(ItemTO item) {
        if (item.isYesNo()) {
            return localeBean.getMessage("Vote.info.yesNo");
        } else {
            if (item.getM() == 1) {
                return localeBean.getMessage("Vote.info.1ofN", 
                        item.getOptions().size() + 1); // +1 because void!
            } else {
                return localeBean.getMessage("Vote.info.MofN", item.getM());
            }
        }
    }
    
    /**
     * This method is used to preview a non-public poll.
     * 
     * @param poll The poll to preview.
     */
    public void enablePreview(PollTO poll) {
        pollTO = poll;
        pollTO.sort();
        hasParticipated = false;
        preview = true;
    }
    
    /**
     * Tries to load a poll by a token. The token was set by the 
     * {@link JumpToIndexFilter} from the input field on the page.
     * 
     * @return The logical view id of the next page.
     */
    public String tryParticipate() {        
        // The user hasn't participated yet
        hasParticipated = false;
        preview = false;
        
        try {
            // Request the poll for the given token
            pollTO = participateLogic.getPollByToken(token);
            pollTO.sort();
        } catch (Exception ex) {
            Throwable root = ExceptionUtil.getRootAsObject(ex);
            
            if (root instanceof IllegalStateException) {
                // Poll not RUNNING, only started
                Date startDate = new Date(Long.parseLong(root.getMessage()));
                    localeBean.addError("Index.error.pollNotRunning", startDate);
            } else if (root instanceof NullPointerException) {
                // No poll for this token
                localeBean.addError("Index.error.pollExpired");
            } else /* IllegalArgumentException */ {
                // No poll for this token
                localeBean.addError("Index.error.pollNotFound");
            }
            
            token = null;
            pollTO = null;
            return null;
        }
        
        // Go voting!
        return "vote";
    }
    
    /**
     * Cancels a participation and removes all references inside this 
     * backing bean. If the bean is in preview mode no action is performed.
     * 
     * @return The logical view id of the next page.
     */
    public String cancelParticipation() {
        // In preview mode do nothing on cancel
        if (preview) {
            return "self";
        }
        
        pollTO = null;
        token = null;
        hasParticipated = false;
        return "index";
    }
    
    public void finishParticipation() {
        cancelParticipation();
    }
    
    /**
     * Tries to participate on the poll with all items set to void. It calls
     * {@link ParticipateBean.doPaticipate()}.
     * 
     * @return The logical view id of the next page.
     */
    public String doParticipateVoid() {
        // Set the void options for all
        for (ItemTO item : pollTO.getItems()) {
            item.setVotedVoid(true);
            for (ItemOptionTO itemOption : item.getOptions()) {
                itemOption.setSelected(false);
            }
        }
        
        // Run normal participation
        return doParticipate();
    }
    
    /**
     * Tries to save the result of a participation. Before this action is performed,
     * the submitted vote(s) are checked for correctness. If the bean is in preview
     * mode no action is performed.
     * 
     * @return The logical view id of the next page.
     */
    public String doParticipate() {
        boolean hasErrors = false;
        
        // Check if there are any error messages to display
        for (ItemTO item : pollTO.getItems()) {
            if (!item.isValidVote()) {
                if (item.isYesNo()) {
                    addMessage(item, localeBean.getMessage("Vote.error.invalidYesNo"));
                } else {
                    addMessage(item, localeBean.getMessage("Vote.error.invalidMofN", item.getM()));
                }
                hasErrors |= true;
            }
        }
        
        // Don't do any further steps
        if (preview) {
            return "self";
        }
        
        // On error stay at the current page
        if (hasErrors) {
            localeBean.addError("Vote.error.invalidFields");
            return "vote";
        }
        
        // If there are no validation errors, the data can be given to the
        // business logic to count them
        try {
            if(!participateLogic.participate(pollTO, token)) {
                throw new IllegalStateException();
            }
        } catch (Exception ex) {
            if (getRootAsObject(ex) instanceof IllegalStateException) {
                localeBean.addError("Vote.error.notFilled");
            } else { // IllegalStateException
                localeBean.addError("Vote.error.unkownId");
            }
            return "vote";
        }
        
        // Everything finished: thank you!
        cancelParticipation();
        hasParticipated = true;
        return "thankyou";
    }
    
    /**
     * Returns a random string as name for the token field. This random string
     * contains the characters <code>t0kEn</code> as identifiation.
     * 
     * @return A random string.
     */
    public String getUID() {
        Random r = new Random(42);
        String id = UUID.randomUUID().toString().replaceAll("-", "");
        int rand = r.nextInt(id.length() - 5);
        id = id.substring(0, rand) + "t0kEn" + 
                id.substring(rand + 1);
        return id;
    }
    
    /**
     * Returns the token which was provided in the URL as GET parameter to
     * prefill the token field.
     * Note: on PUBLIC browsers, the URLs are cached and the token is also 
     * cached when using this method.
     * 
     * @return The token provided or <code>null</code>.
     */
    public String getTokenFromRequest() {
        return FacesContext
                .getCurrentInstance()
                .getExternalContext()
                .getRequestParameterMap()
                .get("token");
    }
    
}
