package de.vote.persistence.entities;

import java.io.Serializable;
import java.util.Objects;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

/**
 * Abstract JPA entity.
 * 
 * @author Daniel Vivas Estevao
 * @author maximilianstrauch
 * @param <T> Type of the corresponding transfer object for this entity. If
 * no TO is avialable for an entity, this type can be {@link Void}.
 */
@MappedSuperclass
public abstract class AbstractEntity<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    protected Integer id;
    
    public AbstractEntity() { }
    
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Override
    public int hashCode() {
        int hash = 21;
        hash = 42 * hash + Objects.hashCode(this.id);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof AbstractEntity)) {
            return false;
        }
        
        // No ID present -> unequal!
        if (id == null || ((AbstractEntity) obj).id == null || 
                id < 0 || ((AbstractEntity) obj).id < 0) {
            return false;
        }
        
        return Objects.equals(id, ((AbstractEntity) obj).id);
    }
 
    /**
     * Creates a flat TO for this entity. This call is equivalent to
     * {@link AbstractEntity.getTO(false)}.
     * 
     * @return The created transfer object.
     */
    public final T getTO() {
        return getTO(false);
    }
    
    /**
     * Creates a TO for this entity.
     * 
     * @param deepCopy If <code>true</code> a deep object copy is created and
     * if <code>false</code> a flat object copy is created. The meaning of deep
     * and flat is defined by the implementing class. For example a deep copy of
     * {@link Poll} includes all {@link Item}s and therfore all {@link ItemOption}s.
     * A flat copy copies only the values of {@link Poll}.
     * @return The created TO.
     */
    public abstract T getTO(boolean deepCopy);
    
}
