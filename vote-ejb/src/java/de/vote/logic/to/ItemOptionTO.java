package de.vote.logic.to;

import de.vote.logic.to.PollTO.ValidationResult;
import de.vote.persistence.entities.ItemOption;

/**
 * TO for the entity class {@link ItemOption}.
 * 
 * @author Daniel Vivas Estevao
 * @author maximilianstrauch
 */
public class ItemOptionTO extends AbstractTO implements Comparable<ItemOptionTO> {
    
    private String shortName, description;
    
    private boolean selected;
    
    private int index;
    
    public ItemOptionTO(Integer id) {
        super(id);
    }
    
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

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }
    
    /**
     * Tests if this option is valid, e.g. has valid properties.
     * 
     * @return Returns the result of the validation as {@link ValidationResult}.
     */
    public ValidationResult isValid() {
        if (shortName == null || shortName.trim().isEmpty()) {
            return ValidationResult.OPTION_NO_SHORT_NAME;
        }
        return ValidationResult.VALID;
    }
    
    @Override
    public int compareTo(ItemOptionTO o) {
        return index == o.index ? 0 : index - o.index;
    }
    
}
