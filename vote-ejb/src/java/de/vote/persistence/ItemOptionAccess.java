package de.vote.persistence;

import de.vote.persistence.entities.ItemOption;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;

/**
 * Implementation of {@link AbstractAccess} to access the {@link ItemOption} 
 * entites.
 * 
 * @author Daniel Vivas Estevao
 * @author maximilianstrauch
 */
@Stateless
@LocalBean
public class ItemOptionAccess extends AbstractAccess<ItemOption> {

    public ItemOptionAccess() {
        super(ItemOption.class);
    }
    
}
