package de.vote.logic.impl;

import de.vote.logic.AdministrationLogic;
import de.vote.logic.EntityOrder;
import de.vote.logic.to.PollTO;
import de.vote.logic.to.PrincipalTO;
import de.vote.persistence.PollAccess;
import de.vote.persistence.PrincipalAccess;
import de.vote.persistence.entities.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;

/**
 * Implementation of the {@link AdminstrationLogic} interface containing
 * all the business methods for administrational tasks.
 * 
 * @author Daniel Vivas Estevao
 * @author Maximilian Strauch
 * @since 2014
 */
@Stateless
public class AdministrationLogicImpl implements AdministrationLogic {

    @EJB
    private PollAccess pollAccess;
    
    @EJB
    private PrincipalAccess principalAccess;

    // Used to determine the role of the calling user (see getPrincipal())
    @Resource
    private SessionContext ctx;
    
    // Poll list interface
    
    // Only admins can list _all_ polls
    @RolesAllowed(value = {"ADMINISTRATOR"})
    @Override
    public List<PollTO> getPolls(int start, int count, EntityOrder order) {
        return TOConverter.convertDeep(pollAccess.getAll(start, count, order));
    }

    @RolesAllowed(value = {"ADMINISTRATOR"})
    @Override
    public int getTotalPollCount() {
        return pollAccess.getTotalCount();
    }

    // Also organizers can delete polls
    @RolesAllowed(value = {"ORGANIZER", "ADMINISTRATOR"})
    @Override
    public void deletePoll(int id) throws IllegalArgumentException {
        if (!pollAccess.delete(id)) {
            throw new IllegalArgumentException("ID #" + id + " not valid");
        }
    }

    // Principal management interface
    
    @RolesAllowed(value = {"ADMINISTRATOR"})
    @Override
    public List<PrincipalTO> getPrincipals(int start, int count, EntityOrder order) {
        List<Principal> principals = principalAccess.getAll(start, count, order);
        
        // Convert result into transfer objects
        List<PrincipalTO> tos = new ArrayList<>();
        for (Principal principal : principals) {
            tos.add(principal.getTO());
        }
        return tos;
    }

    @RolesAllowed(value = {"ADMINISTRATOR"})
    @Override
    public int getTotalPrincipalCount() {
        return principalAccess.getTotalCount();
    }
    
    // Everybody needs to request its principal object on login
    @RolesAllowed(value = {"ORGANIZER", "ADMINISTRATOR"})
    @Override
    public PrincipalTO getPrincipal(String username) throws IllegalArgumentException {
        // Lookup the principal object
        Principal principal;
        if (!principalAccess.isUsernameExisiting(username)) {
            principal = new Principal();
            principal.setUsername(username);
            principal.setThirdPartyAuthorized(true);
            if (ctx.isCallerInRole("ADMINISTRATOR")) {
                principal.setPrincipalRole(Principal.Role.ADMINISTRATOR);
            } else {
                principal.setPrincipalRole(Principal.Role.ORGANIZER);
            }
            
            // Populate through LDAP
            try {
                UnikoLdapLookup.populate(principal);
            } catch (Exception ex) {
                /* Don't watch */
                Logger.getLogger(AdministrationLogicImpl.class.getName())
                        .log(Level.INFO, "Can't get LDAP-Data for " + username, 
                                ex);
            }
            principalAccess.store(principal);
        } else {
            // User is already imported into the database
            principal = principalAccess.getByUsername(username);
        }
        
        // Apply the role (for security)
        if (ctx.isCallerInRole("ADMINISTRATOR")) {
            principal.setPrincipalRole(Principal.Role.ADMINISTRATOR);
        } else {
            principal.setPrincipalRole(Principal.Role.ORGANIZER);
        }
        
        return principal.getTO();
    }
    
}
