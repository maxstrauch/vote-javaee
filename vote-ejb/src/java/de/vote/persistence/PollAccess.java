package de.vote.persistence;

import de.vote.logic.EntityOrder;
import de.vote.logic.OrganizationLogic.PollType;
import de.vote.persistence.entities.Poll;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;

/**
 * Implementation of {@link PollAccess} to access {@link Poll} entites. This
 * class implements also further operations which are performed with polls in
 * the database.
 * 
 * @author Daniel Vivas Estevao
 * @author maximilianstrauch
 */
@Stateless
@LocalBean
public class PollAccess extends AbstractAccess<Poll> {

    public PollAccess() {
        super(Poll.class);
    }
    
    /**
     * Creates the JPQL WHERE argument string and fills the parameters map for
     * a given {@link PollType}. The following types are known:
     * <ul>
     *  <li><code>PollType.ALL</code>: no specific WHERE argument for poll type
     *  selection is created.</li>
     *  <li><code>PollType.PUBLISHED</code>: published polls are in the state 
     *  STARTED or RUNNING and their end date is greater than now (future).</li>
     *  <li><code>PollType.UNPUBLISHED</code>: unpublished polls are in the
     *  state PREPARED.</li>
     *  <li><code>PollType.CLOSED</code>: closed polls are in the state FINISHED
     *  or their end date is before now.</li>
     * </ul>
     * 
     * @param type The type of the polls to look for.
     * @param uid The id of the principal to look for or -1 to look for all.
     * @param params A parameter map, non-null.
     * @return The created JPQL WHERE argument.
     */
    private String getWhereArgumentByType(PollType type, int uid, Map<String, Object> params) {
        if (uid > -1) {
            params.put("dynamicWhereArg0", uid);
        }
        
        String whereArg0 = "t.id = :dynamicWhereArg0 AND ";
        
        if (type == PollType.ALL) {
            
            return uid > -1 ? whereArg0.substring(0, 24) : "";
            
        } else if (type == PollType.PUBLISHED) {
            params.put("dynamicWhereArg1", Poll.PollState.STARTED);
            params.put("dynamicWhereArg2", Poll.PollState.RUNNING);
            params.put("dynamicWhereArg3", new Date(System.currentTimeMillis()));
            
            return (uid > -1 ? whereArg0 : "")
                    + "(o.pollState = :dynamicWhereArg1 OR "
                    + "o.pollState = :dynamicWhereArg2) AND o.endDate >= :dynamicWhereArg3";
        } else if (type == PollType.UNPUBLISHED) {
            params.put("dynamicWhereArg1", Poll.PollState.PREPARED);
            
            return (uid > -1 ? whereArg0 : "") + "o.pollState = :dynamicWhereArg1";
        } else if (type == PollType.CLOSED) {
            params.put("dynamicWhereArg1", Poll.PollState.FINISHED);
            params.put("dynamicWhereArg2", new Date(System.currentTimeMillis()));
            
            return (uid > -1 ? whereArg0 : "")
                    + "(o.pollState = :dynamicWhereArg1 OR o.endDate < :dynamicWhereArg2)";
        }
        
        return null; // Otherwise no argument
    }
    
    /**
     * Retrieves a list of polls of a certain type of a certain user in a
     * certain quantity.
     * 
     * @param type Type of the polls to look for.
     * @param uid Id of the principal to look for.
     * @param start The start index.
     * @param count The number of objects to retrieve or -1 for all objects. 
     * @param order The order attribut of the elements.
     * @return The list of resulting objects of type T.
     * @throws IllegalArgumentException Is thrown by 
     * {@link AbstractAccess.getAll(...)}.
     */
    public List<Poll> getAllByType(PollType type, int uid, int start, 
            int count, EntityOrder order) throws IllegalArgumentException {
        Map<String, Object> params = new HashMap<>();
        return super.getAll(
                start, 
                count, 
                ", IN(o.organizers) AS t",
                order, 
                getWhereArgumentByType(type, uid, params), 
                params.isEmpty() ? null : params
        );
    }
    
    /**
     * Calculates the total number of polls of a certain type for a certain
     * principal.
     * 
     * @param type Type of the polls to look for.
     * @param uid Id of the principal to look for.
     * @return The number of polls for this principal.
     * @throws IllegalArgumentException Is thrown by 
     * {@link AbstractAccess.getTotalCount(...)}.
     */
    public int getTotalCountByType(PollType type, int uid) throws IllegalArgumentException {
        Map<String, Object> params = new HashMap<>();
        return super.getTotalCount(
                getWhereArgumentByType(type, uid, params),
                ", IN(o.organizers) AS t",
                params.isEmpty() ? null : params
        );
    }
    
    /**
     * Checks if a given poll title is existing.
     * 
     * @param title The title to check.
     * @return <code>true</code> iff the given title does not exist in the 
     * database and otherwise <code>false</code> is returned.
     * @throws IllegalArgumentException Is thrown if the title is invalid.
     */
    public boolean isPollTitleExisiting(String title) throws IllegalArgumentException {
        if (title == null) {
            throw new IllegalArgumentException("Invalid title given");
        }
        
        return getEntityManager().createQuery(
                "SELECT COUNT(p) FROM Poll p "
                        + "WHERE p.title LIKE :title", Long.class)
                .setParameter("title", title)
                .getSingleResult()
                .intValue() != 0;
    }
    
    /**
     * Retrieves a {@link Poll} entity by a given token.
     * 
     * @param token The token which is not invalidated and is related to a 
     * poll.
     * @return The poll object.
     * @throws IllegalArgumentException Is thrown if a poll for the token could 
     * not be found in the database.
     */
    public Poll getPollByToken(String token) throws IllegalArgumentException {
        if (token == null) {
            throw new IllegalArgumentException("Invalid token!");
        }
        
        String jpql = "SELECT p FROM Poll p, IN(p.tokens) AS t " +
            "WHERE t.token LIKE :theToken AND t.invalid = false";
        
        // Get the polls
        List<Object> polls = getEntityManager()
                .createQuery(jpql)
                .setParameter("theToken", token)
                .getResultList();
        
        // Check result
        if (polls == null || polls.size() != 1) {
            if (polls != null && polls.size() > 1) {
                // PANIC! 
                System.err.println("FATAL: There are multiple polls with the same token!");
            }
            
            throw new IllegalArgumentException("No token found.");
        }
        
        // Get the poll
        return (Poll) polls.get(0);
    }
    
    /**
     * Retrieves a {@link Poll} entity by a given resultsId.
     * 
     * @param resultsId The resultsId for the poll.
     * @return The poll object.
     * @throws IllegalArgumentException Is thrown if the resultsId is invalid
     * or no poll could be found for this results id.
     */
    public Poll getPollByResultsId(String resultsId) throws IllegalArgumentException {
        if (resultsId == null) {
            throw new IllegalArgumentException("Invalid result ID");
        }
        
        // Get the polls
        List<Object> polls = getEntityManager()
                .createQuery("SELECT p FROM Poll p WHERE p.resultsId = :resultsId")
                .setParameter("resultsId", resultsId)
                .getResultList();
        
        // Check result
        if (polls == null || polls.size() != 1) {
            if (polls != null && polls.size() > 1) {
                // PANIC! 
                System.err.println("FATAL: There are multiple polls with the same token!");
            }
            
            throw new IllegalArgumentException("No token found.");
        }
        
        // Get the poll
        return (Poll) polls.get(0);
    }
    
}
