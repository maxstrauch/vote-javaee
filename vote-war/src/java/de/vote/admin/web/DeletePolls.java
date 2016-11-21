package de.vote.admin.web;

import de.vote.LocaleBean;
import de.vote.logic.AdministrationLogic;
import de.vote.logic.EntityOrder;
import de.vote.logic.to.PollTO;
import de.vote.web.AbstractPageableTable;
import java.io.Serializable;
import java.util.List;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

/**
 * Simple backing bean to list all polls and provide the ability to delete
 * a poll.
 * 
 * @author Daniel Vivas Estevao
 * @author maximilianstrauch
 */
@SessionScoped
@Named
public class DeletePolls extends AbstractPageableTable<PollTO> implements Serializable {

    @EJB
    private AdministrationLogic adminLogic;
    
    @Inject
    private LocaleBean localeBean;

    @Override
    protected int load(int start, int count, List<PollTO> entryList) {
        entryList.addAll(adminLogic.getPolls(start, count, EntityOrder.TITLE));
        return adminLogic.getTotalPollCount();
    }
    
    public void delete(PollTO poll) {
        // Try to delete the poll
        try {
            adminLogic.deletePoll(poll.getId());
            localeBean.addSuccess("DeletePolls.info.deleted", poll.getTitle());
            
            // Invalidate the list to rearrange
            invalidate();
        } catch (Exception ex) {
            localeBean.addError("DeletePolls.error.cantDelete");
        }
    }
    
}
