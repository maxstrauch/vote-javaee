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
 * Backing bean to display a list of all closed polls (FINISHED) and perform
 * some actions on this polls.
 * 
 * @author Daniel Vivas Estevao
 * @author maximilianstrauch
 */
@SessionScoped
@Named
public class ClosedPolls extends AbstractPageableTable<PollTO> implements Serializable {

    @EJB
    private OrganizationLogic organLogic;
    
    @EJB
    private AdministrationLogic adminLogic;
    
    @Inject
    private LocaleBean localeBean;
    
    @Inject
    private VoteSession voteSession;
    
    @Inject
    private ViewResults viewResults;
    
    @Override
    protected int load(int start, int count, List<PollTO> entryList) {
        int uid = voteSession.getPrincipal().getId();
        entryList.addAll(organLogic.getPolls(
                uid,
                OrganizationLogic.PollType.CLOSED, 
                start, 
                count, 
                EntityOrder.TITLE)
        );
        return organLogic.getTotalPollCount(uid, OrganizationLogic.PollType.CLOSED);
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
    
    public String togglePublish(PollTO poll) {
        try {
            String id = organLogic.togglePublishPollResults(poll.getId());
            poll.setResultsId(id);
        } catch (Exception ex) {
            localeBean.addError("Dashboard.error.cantPublish");
        }
        return "self";
    }
    
    public String sendResultsToParticipants(PollTO poll) {
        try {
            organLogic.sendPollResults(poll.getId(), localeBean.getLocale());
        } catch (Exception ex) {
            localeBean.addError("Dashboard.error.cantSendResults");
        }
        return "self";
    }
    
    public String gotoResult(PollTO poll) {   
        viewResults.setPoll(poll);
        return "results";
    }

}
