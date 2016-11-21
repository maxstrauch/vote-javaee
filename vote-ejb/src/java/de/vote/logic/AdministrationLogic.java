package de.vote.logic;

import de.vote.logic.to.PollTO;
import de.vote.logic.to.PrincipalTO;
import de.vote.persistence.entities.Poll;
import java.util.List;
import java.util.Locale;
import javax.ejb.Remote;

/**
 * Facade of the EJB tier containing methods for administrational purposes.
 * 
 * @author Daniel Vivas Estevao
 * @author maximilianstrauch
 */
@Remote
public interface AdministrationLogic {

    // Poll list interface
    
    /**
     * Returns all polls currently available in the system.
     * 
     * @param start Start index in the list. Iff -1 is supplied, all polls are
     * returned.
     * @param count Number of polls to return (if avialable).
     * @param order Attribute to order by.
     * @return The non <code>null</code> list of polls for the given arguments.
     */
    public List<PollTO> getPolls(int start, int count, EntityOrder order);
    
    /**
     * Counts all {@link Poll} obejects which are present in the entire
     * system.
     * 
     * @return Number of all {@link Poll} objects in the system.
     */
    public int getTotalPollCount();
    
    /**
     * Deletes the {@link Poll} object with the given ID from the database
     * including all voting results, items with options, tokens and 
     * participants.
     * 
     * @param pid The ID of the {@link Poll} to delete.
     * @throws IllegalArgumentException Is thrown iff there is no poll
     * existing for the given ID.
     */
    public void deletePoll(int pid) throws IllegalArgumentException;
    
    // Principal management interface
    
    /**
     * Lists all principals in the system.
     * 
     * @param start Start index in the list. Iff -1 is supplied, all principals 
     * are returned.
     * @param count Number of principal objects to return (if avialable).
     * @param order Attribute to order by.
     * @return The non <code>null</code> list of polls for the given arguments.
     */
    public List<PrincipalTO> getPrincipals(int start, int count, EntityOrder order);
    
    /**
     * Returns the number of principals registered in the system.
     * 
     * @return The total number of principals.
     */
    public int getTotalPrincipalCount();
    
    /**
     * Finds a principal by its username.
     * 
     * @param username The username of the principal to find.
     * @return The principal.
     * @throws IllegalArgumentException Is thrown if no principal could be
     * found for the given username.
     */
    public PrincipalTO getPrincipal(String username) throws IllegalArgumentException;
    
}
