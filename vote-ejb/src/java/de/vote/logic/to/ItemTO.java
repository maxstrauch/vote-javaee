package de.vote.logic.to;

import de.vote.logic.to.PollTO.ValidationResult;
import java.util.ArrayList;
import java.util.List;

/**
 * TO for {@link Item}.
 * 
 * @author Daniel Vivas Estevao
 * @author maximilianstrauch
 */
public class ItemTO extends AbstractTO implements Comparable<ItemTO> {
    
    private String title;
    
    private int m, index;
    
    private List<ItemOptionTO> options;
    
    /**
     * Is set when the user voted this item void.
     */
    private boolean votedVoid;
    
    private boolean yesNo;
    
    public ItemTO(Integer id) {
        super(id);
    }
    
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getM() {
        return m;
    }

    public void setM(int m) {
        this.m = m;
    }

    public List<ItemOptionTO> getOptions() {
        if (options == null) {
            options = new ArrayList<>();
        }
        return options;
    }

    public void setOptions(List<ItemOptionTO> options) {
        this.options = options;
    }

    public boolean isVotedVoid() {
        return votedVoid;
    }

    public void setVotedVoid(boolean votedVoid) {
        this.votedVoid = votedVoid;
    }

    public boolean isYesNo() {
        return yesNo;
    }

    public void setYesNo(boolean yesNo) {
        this.yesNo = yesNo;
    }

    public Integer getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }
    
    /**
     * Tests if this item is valid, e.g. has valid properties. This method
     * cascades and thecks all options, too.
     * 
     * @return The {@link ValidationResult}.
     */
    public ValidationResult isValid() {
        if (title == null || title.trim().isEmpty()) {
            return ValidationResult.ITEM_NO_TITLE;
        }
        
        if (!yesNo && (getM() < 1 || getM() > options.size())) {
            return ValidationResult.ITEM_INVALID_M;
        }
        
        if (getOptions().size() < 1) {
            return ValidationResult.ITEM_TOO_LESS_OPTIONS;
        }
        
        boolean isValid = true;
        ValidationResult result;
        for (ItemOptionTO itemOptionTO : getOptions()) {
            isValid &= (result = itemOptionTO.isValid()) == ValidationResult.VALID;
            
            if (!isValid) {
                return result;
            }
        }
        
        return ValidationResult.VALID;
    }
    
    /**
     * Checks whether this item is a valid vote. This method checks, in contrast
     * to {@link ItemTO.isValid()}, if the vote set by participant is valid and
     * does not violate the constraint of the item type.
     * 
     * @return <code>true</code> iff this item object has a correct user 
     * selection based on the type of this item, e.g. yes-no-options have only
     * one option selected; otherwise this method will return <code>false</code>.
     */
    public boolean isValidVote() {
        int selectedOptions = 0;

        // For both cases all selected options need to be counted
        for (ItemOptionTO option : options) {
            if (option.isSelected()) {
                selectedOptions++;
            }
        }
        
        if (yesNo) {
            // On yes-no-items only one option can be checked OR 
            // the item can be set to voted void            
            if (votedVoid) {
                selectedOptions++; // Void counts as one selected item
            }

            // A yes-no-item is valid if exactly one option is selected
            return selectedOptions == 1;
        } else {
            // M-of-N items need a selection of 0 < M <= N options or the
            // selection of vote void
            
            if (votedVoid && selectedOptions > 0) {
                // Either the item is voted void or other items are selected
                return false;
            }
            
            if (votedVoid && selectedOptions == 0) {
                return true;
            }
            
            // Check if the number of selected options is in range
            return 0 < selectedOptions && selectedOptions <= m;
        }
    }
    
    @Override
    public int compareTo(ItemTO o) {
        return index == o.index ? 0 : index - o.index;
    }    

}
