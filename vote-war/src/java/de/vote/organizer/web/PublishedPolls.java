package de.vote.organizer.web;

import de.vote.LocaleBean;
import de.vote.logic.AdministrationLogic;
import de.vote.logic.EntityOrder;
import de.vote.logic.OrganizationLogic;
import de.vote.logic.to.PollTO;
import de.vote.web.AbstractPageableTable;
import de.vote.web.VoteSession;
import java.io.Serializable;
import java.util.List;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

/**
 * Simple backing bean to display a list of published polls (STARTED, RUNNING)
 * and provide some methods to operate on them.
 * 
 * @author Daniel Vivas Estevao
 * @author maximilianstrauch
 */
@SessionScoped
@Named
public class PublishedPolls extends AbstractPageableTable<PollTO> implements Serializable {

    @EJB
    private OrganizationLogic organLogic;
    
    @EJB
    private AdministrationLogic adminLogic;
    
    @Inject
    private EditPoll editPoll;
    
    @Inject
    private VoteSession voteSession;
    
    @Inject
    private LocaleBean localeBean;
    
    @Inject
    private ClosedPolls closedPolls;
    
    @Override
    protected int load(int start, int count, List<PollTO> entryList) {
        int uid = voteSession.getPrincipal().getId();
        entryList.addAll(organLogic.getPolls(
                uid,
                OrganizationLogic.PollType.PUBLISHED, 
                start, 
                count, 
                EntityOrder.TITLE)
        );
        return organLogic.getTotalPollCount(uid, OrganizationLogic.PollType.PUBLISHED);
    }
    
    public String edit(PollTO poll) {
        editPoll.editPoll(poll);
        return "edit";
    }
    
    public String delete(PollTO poll) {
        try {
            adminLogic.deletePoll(poll.getId());
            invalidate();
        } catch (Exception ex) {
            localeBean.addError("Dashboard.error.cantDelete");
        }
        return "self";
    }
    
    public String cancel(PollTO poll) {
        try {
            organLogic.cancelPoll(poll.getId());
            invalidate();
            closedPolls.invalidate();
        } catch (Exception ex) {
            localeBean.addError("Dashboard.error.cantCancel");
        }
        return "self";
    }
    
}
