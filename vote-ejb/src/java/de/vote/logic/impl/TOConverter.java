package de.vote.logic.impl;

import de.vote.logic.to.AbstractTO;
import de.vote.logic.to.ItemOptionTO;
import de.vote.logic.to.ItemTO;
import de.vote.logic.to.ParticipantTO;
import de.vote.logic.to.PollTO;
import de.vote.logic.to.PrincipalTO;
import de.vote.persistence.ItemAccess;
import de.vote.persistence.ItemOptionAccess;
import de.vote.persistence.ParticipantAccess;
import de.vote.persistence.PrincipalAccess;
import de.vote.persistence.entities.AbstractEntity;
import de.vote.persistence.entities.Item;
import de.vote.persistence.entities.ItemOption;
import de.vote.persistence.entities.Participant;
import de.vote.persistence.entities.Poll;
import de.vote.persistence.entities.Principal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.inject.Inject;

/**
 * This utility class can be used to transfer entity classes into TOs and
 * copy data from TOs back into entity classes.
 * For the entity to TO conversion the static methods can be used and for the
 * other direction the using class needs an injected instance.
 * 
 * @author Daniel Vivas Estevao
 * @author maximilianstrauch
 */
@Stateless
@LocalBean
public class TOConverter {
    
    @Inject
    private PrincipalAccess principalAccess;
    
    @Inject
    private ItemAccess itemAccess;
    
    @Inject
    private ItemOptionAccess itemOptionAccess;
    
    @Inject
    private ParticipantAccess participantAccess;
    
    /**
     * Fills a {@link Poll} object with the data from a {@PollTO} object with
     * respect to the current state of the poll (e.g. on STARTED or RUNNING
     * polls only a subset is updated). This method cascades and cuases also
     * the items to be update (if available on the current state).
     * 
     * @param poll The {@link Poll} to update.
     * @param pollTo The source object with the data to copy.
     * @param index Conversation index number.
     */
    public void fillByTO(Poll poll, PollTO pollTo, int index) {
        // Ignore
        if (poll.getPollState() == Poll.PollState.FINISHED) {
            return;
        }
        
        // Set two attributes
        poll.setEndDate(pollTo.getEndDate());
        poll.setReminderBeforeEnd(pollTo.getReminderBeforeEnd());
        
        // Update organizer list
        poll.getOrganizers().clear();
        for (PrincipalTO organTO : pollTo.getOrganizers()) {
            poll.getOrganizers().add(
                    principalAccess.find(organTO.getId())
            );
        }
        
        // Return if only a subset needs to be updated
        if (poll.getPollState() == Poll.PollState.STARTED || 
                poll.getPollState() == Poll.PollState.RUNNING) {
            return;
        }
        
        // Update other values
        poll.setTitle(pollTo.getTitle());
        poll.setDescription(pollTo.getDescription());
        poll.setStartDate(pollTo.getStartDate());
        poll.setParticipationTracking(pollTo.isParticipationTracking());
        
        // Update participants
        Set<Participant> allParticipants = new HashSet<>(poll.getParticipants());
        int index1 = 0;
        for (ParticipantTO participantTo : pollTo.getParticipants()) {
            Participant participant = poll.findParticipantByEMail(participantTo.getEmail());
            
            if (participant == null || participantTo.getId() < 0) {
                participant = new Participant();
            }
            
            fillByTO(participant, participantTo, index1++);
            poll.getParticipants().add(participant);
            allParticipants.remove(participant);
        }
        
        for (Participant participant : allParticipants) {
            poll.getParticipants().remove(participant);
            participantAccess.delete(participant);
        }

        // Update items
        Set<Item> allItems = new HashSet<>(poll.getItems());
        index1 = 0;
        for (ItemTO itemTo : pollTo.getItems()) {
            Item item = poll.findItemById(itemTo.getId());
            
            if (item == null || itemTo.getId() < 0) {
                // Item not found: create new
                item = new Item();
            }
            
            fillByTO(item, itemTo, index1++);
            poll.getItems().add(item);
            allItems.remove(item);
        }
        
        // Remove all items which are removed by the user
        for (Item item : allItems) {
            poll.getItems().remove(item);
            itemAccess.delete(item);
        }
    }
    
    /**
     * Fills a {@link Item} object with the data from a {@ItemTO} object. This
     * method also cascades and causes all options to be updated.
     * 
     * @param item The {@link Item} to update.
     * @param itemTo The source object with the data to copy.
     * @param index Conversation index number.
     */
    public void fillByTO(Item item, ItemTO itemTo, int index) {
        item.setTitle(itemTo.getTitle());
        item.setVoidTally(0);
        item.setIndex(index);
        
        if (itemTo.isYesNo()) {
            item.setType(Item.ItemType.YES_NO);
            item.setM(1);
        } else {
            if (itemTo.getM() == 1) {
                item.setType(Item.ItemType.ONE_OF_N);
                item.setM(1);
            } else {
                item.setType(Item.ItemType.M_OF_N);
                item.setM(itemTo.getM());
            }
        }
        
        // Update options
        Set<ItemOption> allItemOptions = new HashSet<>(item.getOptions());
        int index1 = 0;
        for (ItemOptionTO itemOptionTo : itemTo.getOptions()) {
            ItemOption itemOption = item.findOptionById(itemOptionTo.getId());
            
            if (itemOption == null || itemOption.getId() < 0) {
                // Item not found: create new
                itemOption = new ItemOption();
            }
            
            fillByTO(itemOption, itemOptionTo, index1++);
            item.getOptions().add(itemOption);
            allItemOptions.remove(itemOption);
        }
        
        // Remove all items which are removed by the user
        for (ItemOption itemOption : allItemOptions) {
            item.getOptions().remove(itemOption);
            itemOptionAccess.delete(itemOption);
        }
    }
    
    /**
     * Fills a {@link ItemOption} object with the data from a {@ItemOptionTO} object.
     * 
     * @param itemOption The {@link ItemOption} to update.
     * @param itemOptionTo The source object with the data to copy.
     * @param index Conversation index number.
     */
    public void fillByTO(ItemOption itemOption, ItemOptionTO itemOptionTo, int index) {
        itemOption.setShortName(itemOptionTo.getShortName());
        itemOption.setDescription(itemOptionTo.getDescription());
        itemOption.setTally(0);
        itemOption.setIndex(index);
    }
    
    /**
     * Fills a {@link Participant} object with the data from a {@ParticipantTO} object.
     * 
     * @param participant The {@link Participant} to update.
     * @param participantTO The source object with the data to copy.
     * @param index Conversation index number.
     */
    public void fillByTO(Participant participant, ParticipantTO participantTO, int index) {
        participant.setEmail(participantTO.getEmail());
    }
    
    /**
     * Fills a {@link Principal} object with the data from a {@PrincipalTO} object.
     * 
     * @param principal The {@link Principal} to update.
     * @param principalTO The source object with the data to copy.
     * @param index Conversation index number.
     */
    public void fillByTO(Principal principal, PrincipalTO principalTO, int index) {
        principal.setUsername(principalTO.getUsername());
        principal.setEmail(principalTO.getEmail());
        principal.setRealname(principalTO.getRealname());
        principal.setPrincipalRole(principalTO.isAdmin() ? 
                Principal.Role.ADMINISTRATOR : Principal.Role.ORGANIZER);
    }
    
    /**
     * Converts a entity object into a transfer object, performing a FLAT copy.
     * 
     * @param <X> The input object must extend {@link AbstractEntity} so that
     * the converter methods for transfer objects are available.
     * @param <Y> The output object must extend {@link AbstractTO} since we want
     * transfer objects as result.
     * @param input The input entity object to convert.
     * @return The generated and converted transfer object.
     */
    public static <X extends AbstractEntity<Y>,Y extends AbstractTO> Y convert(X input) {
        return input.getTO();
    }
    
    /**
     * Converts a set of entity objects into a list of transfer objects, performing
     * a FLAT copy.
     * 
     * @param <X> The input object must extend {@link AbstractEntity} so that
     * the converter methods for transfer objects are available.
     * @param <Y> The output object must extend {@link AbstractTO} since we want
     * transfer objects as result.
     * @param input The set of entity objects.
     * @return A non-null list of converted objects.
     */
    public static <X extends AbstractEntity<Y>,Y extends AbstractTO> List<Y> 
            convert(Iterable<X> input) {
        List<Y> result = new ArrayList<>();
        for (X x : input) {
            result.add(x.getTO());
        }
        return result;
    }
    
    /**
     * Converts a set of entity objects into a list of transfer objects, performing
     * a DEEP copy - as the name indicates.
     * 
     * @param <X> The input object must extend {@link AbstractEntity} so that
     * the converter methods for transfer objects are available.
     * @param <Y> The output object must extend {@link AbstractTO} since we want
     * transfer objects as result.
     * @param input The set of entity objects.
     * @return A non-null list of converted objects.
     */
    public static <X extends AbstractEntity<Y>,Y extends AbstractTO> List<Y> 
            convertDeep(Iterable<X> input) {
        List<Y> result = new ArrayList<>();
        for (X x : input) {
            result.add(x.getTO(true));
        }
        return result;
    }
    
}
