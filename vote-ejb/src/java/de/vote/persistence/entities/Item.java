package de.vote.persistence.entities;

import de.vote.logic.impl.TOConverter;
import de.vote.logic.to.ItemTO;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;

/**
 * The entity for items of a poll.
 * 
 * @author Daniel Vivas Estevao
 * @author maximilianstrauch
 */
@Entity
public class Item extends AbstractEntity<ItemTO> {
    
    private String title;
    
    private ItemType type;
    
    private Integer m, voidTally, index;
    
    @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.REMOVE})
    private Set<ItemOption> options;
    
    public enum ItemType{
        YES_NO, ONE_OF_N, M_OF_N;
    }

    public Item() { }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public ItemType getType() {
        return type;
    }

    public void setType(ItemType type) {
        // Set m correct
        if (type == ItemType.YES_NO || type == ItemType.ONE_OF_N) {
            this.m = 1;
        }
        
        this.type = type;
    }

    public Integer getM() {
        return m;
    }

    public void setM(Integer m) {
        this.m = m;
    }

    public Integer getVoidTally() {
        return voidTally;
    }

    public void setVoidTally(Integer voidTally) {
        this.voidTally = voidTally;
    }

    public Set<ItemOption> getOptions() {
        if (options == null) {
            options = new HashSet<>();
        }
        return options;
    }

    public void setOptions(Set<ItemOption> options) {
        this.options = options;
    }
    
    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    /**
     * Finds an option for a given id.
     * 
     * @param id The id of the option to find.
     * @return <code>null</code> if this item has no option with the given id 
     * and otherwise the {@link ItemOption} with that id is returned.
     */
    public ItemOption findOptionById(Integer id) {
        for (ItemOption option : getOptions()) {
            if (Objects.equals(option.getId(), id)) {
                return option;
            }
        }
        return null;
    }
    
    /**
     * Adds a void vote to this item.
     */
    public void addVoidVote() {
        if (voidTally == null) {
            voidTally = 0;
        }
        
        voidTally++;
    }
    
    @Override
    public ItemTO getTO(boolean deepCopy) {
        ItemTO to = new ItemTO(id);
        to.setTitle(title);
        to.setM(m == null ? 0 : m);
        to.setYesNo(type == ItemType.YES_NO);
        to.setIndex(index == null ? 0 : index);
        
        if (deepCopy) {
            to.setOptions(TOConverter.convertDeep(options));
        }
        return to;
    }

}
