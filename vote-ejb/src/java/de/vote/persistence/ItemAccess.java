package de.vote.persistence;

import de.vote.persistence.entities.Item;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;

/**
 * Implementation of {@link AbstractAccess} to access the {@link Item} entites.
 * 
 * @author Daniel Vivas Estevao
 * @author maximilianstrauch
 */
@Stateless
@LocalBean
public class ItemAccess extends AbstractAccess<Item> {

    public ItemAccess() {
        super(Item.class);
    }
    
}
