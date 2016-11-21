package de.vote.persistence;

import de.vote.logic.EntityOrder;
import de.vote.persistence.entities.AbstractEntity;
import java.util.List;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

/**
 * Implementations of {@link AbstractAccess} provide access to the database for
 * a JPA entity. This class provides varios helper methods.
 * 
 * @author Daniel Vivas Estevao
 * @author maximilianstrauch
 * @param <T> Type of the implementation. The type must be a child of 
 * {@link AbstractEntity} i.e. a JPA entity.
 */
public abstract class AbstractAccess<T extends AbstractEntity> {
    
    @PersistenceContext(name = "vote-ejbPU")
    private EntityManager em;
    
    /**
     * Type of the implementation.
     */
    private final Class<T> type;
    
    public AbstractAccess(Class<T> type) {
        this.type = type;
    }

    public EntityManager getEntityManager() {
        return em;
    }
    
    /**
     * Stores an object in the database.
     * 
     * @param obj The object to store.
     */
    public void store(T obj) {
        em.persist(obj);
        em.flush();
    }
    
    /**
     * Finds an object in the database.
     * 
     * @param id The id of the object.
     * @return The Object.
     * @throws IllegalArgumentException Is thrown if no object could be found
     * with the given id.
     */
    public T find(int id) throws IllegalArgumentException {
        T obj = getEntityManager().find(type, id);
        if (obj == null) {
            throw new IllegalArgumentException("Cannot find element with id #" + id);
        }
        return obj;
    }
    
    /**
     * Lists all objects for this type.
     * 
     * @param start The start index.
     * @param count The number of objects to retrieve or -1 for all objects. 
     * @param order The order attribute.
     * @return The list of objects.
     * @throws IllegalArgumentException Is thrown if one of the attributes is
     * invalid.
     */
    public List<T> getAll(int start, int count, EntityOrder order) 
            throws IllegalArgumentException {
        return getAll(start, count, null, order, null, null);
    }
    
    /**
     * Retrives the total number of elements, of the object type T, in the
     * database.
     * 
     * @return Total number of objects T available.
     */
    public int getTotalCount() {
        return getTotalCount(null, null, null);
    }
    
    /**
     * Internal method to retrieve a list of objects with various arguments.
     * 
     * @param start The startindex to begin.
     * @param count The number of objects to retrieve or -1 for all objects. 
     * @param fromAdd Further arguments for the FROM statement which need to
     * start with a ", ".
     * @param order The order attribute.
     * @param where JPQL WHERE arguments without the WHERE statement. The alias
     * for this object in JPQL is "o".
     * @param params Parameters for the WHERE argument.
     * @return The list of elements retrieved
     * @throws IllegalArgumentException Is thrown if one of the attributes is
     * invalid.
     */
    protected List<T> getAll(int start, int count, String fromAdd, 
            EntityOrder order, String where, Map<String, Object> params) 
            throws IllegalArgumentException {
        
        // Check if order attribute is valid
        if (order != null && !order.isValidFor(type)) {
            throw new IllegalArgumentException("Illegal order attribute " + order + " for " + type);
        }
        
        // Create statement
        String jpqlStmt = "SELECT o "
                + "FROM " + type.getSimpleName() + " o" + (fromAdd == null ? "" : fromAdd);
        
        if (where != null) {
            jpqlStmt += " WHERE " + where;
        }
        
        if (order != null) {
            jpqlStmt += " ORDER BY o." + order.toString();
        }
        
        TypedQuery<T> ccp = getEntityManager().createQuery(jpqlStmt, type);
        
        // Add parameters
        if (params != null) {
            for (String key : params.keySet()) {
                ccp.setParameter(key, params.get(key));
            }
        }
        
        // Get results
        List<T> all;
        if (start + count < start) {
             all = ccp.getResultList();
        } else {
             all = ccp
                .setFirstResult(start)
                .setMaxResults(count)
                .getResultList();
        }
        return all;
    }
    
    /**
     * Internal method to retrieve the count of elements of this type T in the
     * database.
     * 
     * @param where JPQL WHERE arguments without the WHERE statement. The alias
     * for this object in JPQL is "o".
     * @param fromAdd Further arguments for the FROM statement which need to
     * start with a ", ".
     * @param params Parameters for the WHERE argument.
     * @return The number of elements of this type T in the database.
     */
    protected int getTotalCount(String where, String fromAdd, 
            Map<String, Object> params) {
        // Create statement
        TypedQuery<Long> ccp = getEntityManager().createQuery(
                "SELECT COUNT(o) "
                + "FROM " + type.getSimpleName() + " o" + (fromAdd == null ? "" : fromAdd)
                + (where == null ? "" : " WHERE " + where), Long.class);
        
        if (params != null) {
            for (String key : params.keySet()) {
                ccp.setParameter(key, params.get(key));
            }
        }
        
        return ccp.getSingleResult().intValue();
    }
    
    /**
     * Deletes an object T from the database.
     * 
     * @param object The object to delete (with set id field).
     * @return <code>true</code> if the object was removed and otherwise
     * <code>false</code>.
     */
    public boolean delete(T object) {
        return delete(object.getId());
    }
    
    /**
     * Deletes an object from the database of the type T with the given id.
     * 
     * @param id The id of the object T to delete.
     * @return <code>true</code> if the object was removed and otherwise
     * <code>false</code>.
     */
    public boolean delete(int id) {
        if (id < 0) {
            return false;
        }
        
        T obj = getEntityManager().find(type, id);
        
        if (obj == null) {
            return false;
        }
        
        getEntityManager().remove(obj);
        getEntityManager().flush();
        
        return true;
    }
    
}
