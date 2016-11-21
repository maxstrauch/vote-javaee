package de.vote.logic;

import de.vote.persistence.entities.Poll;
import de.vote.persistence.entities.Principal;
import javax.ejb.Remote;

/**
 * Enum containing order attributes for list ordering.
 * 
 * @author Daniel Vivas Estevao
 * @author maximilianstrauch
 */
@Remote
public enum EntityOrder {
    TITLE("title", Poll.class),
    USERNAME("username", Principal.class);
    
    private final String field;
    private final Class<?>[] validClasses;
    
    private EntityOrder(String field, Class<?>...validClassed) {
        this.field = field;
        this.validClasses = validClassed;
    }
    
    /**
     * Checks if this attribute order is supported by a given entity class.
     * 
     * @param clazz The entity class.
     * @return <code>true</code> iff supported, otherwise <code>false</code>.
     */
    public boolean isValidFor(Class<?> clazz) {
        if (validClasses == null || validClasses.length < 1) {
            return false;
        }
        
        for (Class<?> clazz1 : validClasses) {
            if (clazz == clazz1) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return field;
    }
    
}
