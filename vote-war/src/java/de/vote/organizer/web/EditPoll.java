package de.vote.organizer.web;

import de.vote.LocaleBean;
import de.vote.logic.OrganizationLogic;
import de.vote.logic.to.ItemOptionTO;
import de.vote.logic.to.ItemTO;
import de.vote.logic.to.ParticipantSetTO;
import de.vote.logic.to.ParticipantTO;
import de.vote.logic.to.PollTO;
import de.vote.logic.to.PrincipalTO;
import de.vote.web.VoteSession;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

/**
 * This backing bean provides all features needed to edit a poll or create
 * a new one.
 * 
 * @author Daniel Vivas Estevao
 * @author maximilianstrauch
 */
@SessionScoped
@Named
public class EditPoll implements Serializable {
    
    @EJB
    private OrganizationLogic organLogic;
    
    @Inject
    private LocaleBean locale;
    
    @Inject
    private VoteSession voteSession;
    
    private PollTO poll;
    
    public PollTO getPoll() {
        return poll;
    }
    
    // Attributes for participation management
    
    /**
     * Raw input text for mail addresses from other applications or added
     * by hand.
     */
    private String rawEMailInput;
    
    /**
     * IDs of all currently selected participants.
     */
    private Integer[] selectedParticipants;
    
    /**
     * ID of the currently selected participant set.
     */
    private Integer selectedParticipantSet;
    
    /**
     * All participant sets avialable (should be loaded at the beginning).
     */
    private List<ParticipantSetTO> participantSets;
    
    // Attributes for organizer manipulation
    
    /**
     * The IDs of the selected organizer principals in the main list.
     */
    private Integer[] selectedOrganizers;
    
    /**
     * ID of the organizer to add (selected).
     */
    private Integer addOrganizer;
    
    /**
     * All selectedOrganizers selectable by the user.
     */
    private List<PrincipalTO> allOrganizers;    
    
    // Methods for participation management
    
    public String getRawEMailInput() {
        return rawEMailInput;
    }

    public void setRawEMailInput(String rawEMailInput) {
        this.rawEMailInput = rawEMailInput;
    }
    

    public Integer[] getSelectedParticipants() {
        return selectedParticipants;
    }

    public void setSelectedParticipants(Integer[] selectedParticipants) {
        this.selectedParticipants = selectedParticipants;
    }
    
    public List<ParticipantSetTO> getParticipantSets() {
        return participantSets;
    }
    
    public Integer getSelectedParticipantSet() {
        return selectedParticipantSet;
    }

    public void setSelectedParticipantSet(Integer selectedParticipantSet) {
        this.selectedParticipantSet = selectedParticipantSet;
    }
    
    public void doRecognizeRawEMails() {
        List<String> newParticipants = EMailUtil.find(rawEMailInput);
        for (String newParticipant : newParticipants) {
            ParticipantTO o = new ParticipantTO();
            o.setEmail(newParticipant);
            if (!poll.getParticipants().contains(o)) {
                poll.getParticipants().add(o);
            }
        }
        
        // Clear textfield
        rawEMailInput = null;
    }
    
    public void doDeleteParticipants() {
        if (selectedParticipants == null || selectedParticipants.length < 1) {
            return;
        }
        
        // Find the participants to delete
        List<ParticipantTO> removeCandidates = new ArrayList<>();
        for (Integer id : selectedParticipants) {
            for (ParticipantTO participant : poll.getParticipants()) {
                if (Objects.equals(participant.getId(), id)) {
                    removeCandidates.add(participant);
                }
            }
        }
        
        // Delete elements
        for (ParticipantTO participant : removeCandidates) {
            poll.getParticipants().remove(participant);
        }
    }
    
    public void doAddParticipantSet() {
        if (selectedParticipantSet == null) {
            return; // No selection
        }
        
        // Find the set to add
        for (ParticipantSetTO participantSet : participantSets) {
            if (Objects.equals(participantSet.getId(), selectedParticipantSet)) {
                List<ParticipantTO> all = participantSet.getParticipants();
                
                for (ParticipantTO participant : all) {
                    if (!poll.getParticipants().contains(participant)) {
                        poll.getParticipants().add(participant);
                    }
                }
            }
        }
    }
    
    // Methods for organizer manipulation
    
    public Integer[] getSelectedOrganizers() {
        return selectedOrganizers;
    }

    public void setSelectedOrganizers(Integer[] selectedOrganizers) {
        this.selectedOrganizers = selectedOrganizers;
    }

    public Integer getAddOrganizer() {
        return addOrganizer;
    }

    public void setAddOrganizer(Integer addOrganizers) {
        this.addOrganizer = addOrganizers;
    }

    public List<PrincipalTO> getAllOrganizers() {
        if (allOrganizers == null) {
            allOrganizers = organLogic.getAllOrganizers();
        }
        return allOrganizers;
    }

    public void doAddOrganizer() {
        List<PrincipalTO> all = getAllOrganizers();
        for (PrincipalTO organ : all) {
            if (Objects.equals(organ.getId(), addOrganizer)) {
                if (!poll.getOrganizers().contains(organ)) {
                    poll.getOrganizers().add(organ);
                }
            }
        }
    }
    
    public void doDeleteOrganizers() {
        // Step 1: find all selectedOrganizers to remove
        List<PrincipalTO> removeCandidates = new ArrayList<>();
        for (PrincipalTO organ : poll.getOrganizers()) {
            for (Integer id : selectedOrganizers) {
                if (Objects.equals(organ.getId(), id)) {
                    removeCandidates.add(organ);
                }
            }
        }
        
        // Step 2: remove them
        for (PrincipalTO organ : removeCandidates) {
            if (organ.equals(voteSession.getPrincipal())) {
                locale.addError("EditPoll.error.cantDeleteMyself");
            } else {
                // Delete only other organizers, not me!
                poll.getOrganizers().remove(organ);
            }
        }
    }
    
    // Item and ItemOption management
    
    public void doAddItem(PollTO poll, boolean isYesNo) {
        // Create new item
        ItemTO item = new ItemTO(poll.getItems().size());
        poll.getItems().add(item);
        
        // If yes-no-question add predefined options
        if (isYesNo) {
            item.setYesNo(true);
            item.setM(1);
            doAddOption(item);
            doAddOption(item);
            item.getOptions().get(0).setShortName(
                    locale.getMessage("Bool.true")
            );
            item.getOptions().get(1).setShortName(
                    locale.getMessage("Bool.false")
            );
        } else {
            item.setM(1);
            
            // At least one option is needed
            doAddOption(item);
        }
    }
    
    public void doAddOption(ItemTO item) {
        // Simply add an option
        ItemOptionTO option = new ItemOptionTO(item.getOptions().size());
        item.getOptions().add(option);
    }
    
    public void doDeleteItem(ItemTO item) {
        // Delete only if there are more than 1 items
        if (poll.getItems().size() > 1) {
            poll.getItems().remove(item);
        } else {
            locale.addError("EditPoll.error.cantDeleteItem");
        }
    }
    
    public void doDeleteOption(ItemTO item, ItemOptionTO option) {
        // Delete option only if there are more than one option listed
        if (item != null && item.getOptions().size() > 1) {
            item.getOptions().remove(option);
        } else {
            locale.addError("EditPoll.error.cantDeleteOption");
        }
    }
    
    // "Main" actions & management

    /**
     * Tests whether the current poll is new or already existing.
     * 
     * @return <code>true</code> iff the poll is existing and only going to
     * be edited and otherwise <code>false</code> for complete new polls.
     */
    public boolean isEditMode() {
        return poll.getId() > -1;
    }

    /**
     * Checks if the current edit mode is restricted.
     * 
     * @return <code>true</code> iff the poll is already published (STARTED)
     * and not all properties can be edited and otherwise <code>false</code>.
     */
    public boolean isRestrictedMode() {
        return poll.isPublished();
    }
    
    // Main actions
    
    /**
     * Tries to save the current poll.
     * 
     * @param publish If <code>true</code> the poll is published after saving.
     * The poll is only saved if <code>false</code> was given.
     * @return The logical view id of the next page.
     */
    public String save(boolean publish) {
        // Check for errors
        PollTO.ValidationResult result = poll.isValid();
        if (result != PollTO.ValidationResult.VALID) {
            locale.addError("EditPoll.error." + result.name().toUpperCase());
            return "self"; // Stay at the site
        }
        
        // Try to persist
        try {
            int pid = organLogic.persistPoll(poll);
            poll.setId(pid); // Update the id
        } catch (Exception ex) {
            locale.addError("EditPoll.error.notSaved");
            return "self";
        }
        
        if (publish) {
            // Publish the poll directly
            try {
                organLogic.publishPoll(poll.getId(), locale.getLocale());
            } catch (Exception ex) {
                locale.addError("EditPoll.error.notPublished");
                return "self";
            }
        }
        
        // Clear data
        return cancel();
    }
    
    /**
     * Clears the fields of this backing bean.
     * 
     * @return Logical identifiactor for the dashboard page.
     */
    public String cancel() {
        poll = null;
        rawEMailInput = null;
        selectedParticipants = null;
        selectedParticipantSet = null;
        participantSets = null;
        selectedOrganizers = null;
        addOrganizer = null;
        allOrganizers = null;
        return "dashboard"; // Go back to the dashboard   
    }
    
    // Outside interface to apply polls to the edit page to let them
    // be edited by the user
    
    public void editPoll(PollTO poll) {
        // Get reference to poll
        this.poll = organLogic.getPoll(poll.getId());
        if (this.poll == null) {
            // An error occurred
            return;
        }
        
        this.poll.sort();
        
        // Load all system users
        allOrganizers = organLogic.getAllOrganizers();
        
        // If not published we can load also the ParticipantSetTOs,
        // because this part is editable
        if (!poll.isPublished()) {
            participantSets = organLogic.getParticipantSets(
                voteSession.getPrincipal().getId());
        } else {
            participantSets = null;
        }
    }
    
    public String newPoll() {
        // Create new poll object
        poll = new PollTO(-1);
        poll.getOrganizers().add(voteSession.getPrincipal());
        doAddItem(poll, false); // ...and fill with a default item
        
        // Load all participant sets
        participantSets = organLogic.getParticipantSets(
                voteSession.getPrincipal().getId());
        
        // Load all system users
        allOrganizers = organLogic.getAllOrganizers();
        
        return "edit"; // Move to edit page
    }
    
}
