package de.vote.logic.to;

import java.io.Serializable;
import java.util.Objects;

/**
 * Prototype of a transfer object ("TO"). A TO is a plain java object following
 * the Java Beans convention of getters and setters. This classes also provide
 * some simple methods perfoming basic actions like validation of objects. The
 * reason therfore is that it is easier to place the validation logic, which
 * is different for each TO, in these objects than in an extra class.
 * 
 * @author Daniel Vivas Estevao
 * @author maximilianstrauch
 */
public class AbstractTO implements Serializable {
    
    private Integer id;
    
    public AbstractTO(Integer id) {
        this.id = id;
    }

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
        if (obj instanceof AbstractTO) {
            return Objects.equals(((AbstractTO) obj).id, id);
        }
        return false;
    }
    
}
