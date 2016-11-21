package de.vote.organizer.web;

import de.vote.logic.OrganizationLogic;
import de.vote.logic.to.PollResultTO;
import de.vote.logic.to.PollTO;
import java.io.Serializable;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

/**
 * This is the backing bean for the page to display the results of a poll.
 * 
 * @author Daniel Vivas Estevao
 * @author maximilianstrauch
 */
@SessionScoped
@Named
public class ViewResults implements Serializable {
    
    @EJB
    private OrganizationLogic organLogic;
    
    private String id, shareHyperlink;
    
    private PollResultTO pollResult;

    public String getId() {
        return id;
    }
    
    /**
     * Sets the results id. On public access this method is used to set the
     * results id and lookup the poll results.
     * 
     * @param id The ID.
     */
    public void setId(String id) {
        this.id = id;
        
        // Check if a stranger wants access to the result
        if (id != null && !id.trim().isEmpty()) {
            try {
                pollResult = organLogic.getResultsByResultsId(id);
                shareHyperlink = pollResult.getId();
            } catch (Exception ex) {
                // Don't watch
                pollResult = null;
                shareHyperlink = null;
            }
        }
    }
    
    /**
     * Sets a poll to view the result private, where private means to show
     * the result only to the current sigend in principal.
     * 
     * @param poll The poll to display the results of.
     */
    public void setPoll(PollTO poll) {
        try {
            pollResult = organLogic.getResults(poll.getId());
            shareHyperlink = pollResult.getId();
        } catch (Exception ex) {
            // Don't watch
            pollResult = null;
            shareHyperlink = null;
        }
    }
    
    public PollResultTO getPollResult() {
        return pollResult;
    }
    
    public boolean hasPollResult() {
        return pollResult != null;
    }
    
    /**
     * Returns a hyperlink which points to the page with a public results id.
     * 
     * @return The URL to this page or <code>null</code> if the results are
     * not public.
     */
    public String getShareHyperlink() {
        if (shareHyperlink == null) {
            return null;
        }
        
        HttpServletRequest origRequest = (HttpServletRequest) FacesContext.
                getCurrentInstance().
                getExternalContext()
                .getRequest();
        
        return origRequest.getRequestURL().toString() + "?id=" + shareHyperlink;
    }
    
    public boolean hasShareHyperlink() {
        return shareHyperlink != null && !shareHyperlink.isEmpty();
    }
    
}
