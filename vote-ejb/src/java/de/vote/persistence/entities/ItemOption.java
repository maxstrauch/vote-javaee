package de.vote.persistence.entities;

import de.vote.logic.to.ItemOptionTO;
import javax.persistence.Entity;

/**
 * The entity for an option of an item.
 * 
 * @author Daniel Vivas Estevao
 * @author maximilianstrauch
 */
@Entity
public class ItemOption extends AbstractEntity<ItemOptionTO> {
    
    private String shortName, description;
    
    private Integer tally, index;
    
    public ItemOption() { }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getTally() {
        return tally;
    }

    public void setTally(Integer tally) {
        this.tally = tally;
    }

    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    /**
     * Adds a vote for this option.
     */
    public void addVote() {
        if (tally == null) {
            tally = 0;
        }

        tally++;
    }

    @Override
    public ItemOptionTO getTO(boolean deepCopy) {
        ItemOptionTO to = new ItemOptionTO(id);
        to.setShortName(shortName);
        to.setDescription(description);
        to.setIndex(index == null ? 0 : index);
        return to;
    }
    
}
