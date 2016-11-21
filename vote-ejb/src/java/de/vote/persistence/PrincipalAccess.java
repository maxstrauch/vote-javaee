package de.vote.persistence;

import de.vote.persistence.entities.Principal;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;

/**
 * Implementation of {@link AbstractAccess} to access {@link Principal} entities.
 * The implementation provides also further methods for principal manipulation.
 * 
 * @author Daniel Vivas Estevao
 * @author maximilianstrauch
 */
@Stateless
@LocalBean
public class PrincipalAccess extends AbstractAccess<Principal> {

    public PrincipalAccess() {
        super(Principal.class);
    }
    
    /**
     * Tests whether the given username is already existing or not.
     * 
     * @param username The username to test.
     * @return <code>true</code> iff the username is existing and otherwise
     * <code>false</code>.
     * @throws IllegalArgumentException Is thrown if the given username is
     * invalid (e.g. null).
     */
    public boolean isUsernameExisiting(String username) throws IllegalArgumentException {
        if (username == null) {
            throw new IllegalArgumentException("Invalid username given");
        }
        
        return getEntityManager().createQuery(
                "SELECT COUNT(p) FROM Principal p "
                        + "WHERE p.username LIKE :username", Long.class)
                .setParameter("username", username)
                .getSingleResult()
                .intValue() != 0;
    }
    
    /**
     * Looks up a principal by its name.
     * 
     * @param username The username of the principal to lookup.
     * @return The {@link Principal} object.
     * @throws IllegalArgumentException Is thrown if the given username is
     * invalid.
     */
    public Principal getByUsername(String username) throws IllegalArgumentException {
        if (username == null) {
            throw new IllegalArgumentException("Invalid username given");
        }
        
        try {
            Principal p = getEntityManager().createQuery(
                "SELECT p FROM Principal p "
                        + "WHERE p.username LIKE :username", Principal.class)
                .setParameter("username", username)
                .getSingleResult();
            
            if (p == null) {
                throw new IllegalArgumentException("No such user with that name");
            }
            
            return p;
        } catch (Exception ex) {
            throw new IllegalArgumentException("No such user");
        }
    }
    
}
