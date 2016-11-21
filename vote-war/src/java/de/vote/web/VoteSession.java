package de.vote.web;

import de.vote.logic.to.PrincipalTO;
import java.io.Serializable;
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;

/**
 * Simple backing bean to hold the principal object of a principal who sigend
 * in successfully. This bean provides immediate access to the user data
 * within a session.
 * 
 * @author Daniel Vivas Estevao
 * @author maximilianstrauch
 */
@SessionScoped
@Named
public class VoteSession implements Serializable {

    private PrincipalTO principal;

    public PrincipalTO getPrincipal() {
        return principal;
    }

    public void setPrincipal(PrincipalTO principal) {
        this.principal = principal;
    }
    
    public boolean isSignedIn() {
        return principal != null && principal.getId() > -1;
    }
    
    public boolean isAdmin() {
        return principal != null && principal.isAdmin();
    }
    
    public String getName() {
        return principal == null ? "" : principal.toString();
    }

}
