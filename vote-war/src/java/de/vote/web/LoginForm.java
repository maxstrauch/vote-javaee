package de.vote.web;

import de.vote.LocaleBean;
import de.vote.logic.AdministrationLogic;
import de.vote.logic.to.PrincipalTO;
import java.io.Serializable;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * This backing bean is responsible for the login page and manages principal
 * logins and logouts.
 * 
 * @author Daniel Vivas Estevao
 * @author maximilianstrauch
 * @since 2014
 */
@SessionScoped
@Named
public class LoginForm implements Serializable {
    
    @EJB
    private AdministrationLogic adminLogic;
    
    @Inject
    private VoteSession voteSession;
    
    @Inject
    private LocaleBean localeBean;
    
    private String name, passwd;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPasswd() {
        return passwd;
    }

    public void setPasswd(String passwd) {
        this.passwd = passwd;
    }
    
    public String cancel() {
        name = null;
        passwd = null;
        return "index";
    }
    
    /**
     * Tries to login the current principal with the given credentials. Iff 
     * the user is authenticated by the realm, the principal object is requested
     * from the EJB tier and stored inside the session to provide immediate
     * access to the currently signed in principal.
     * 
     * @return The logical view id of the next view to display.
     */
    public String login() {
        // Check input
        if (empty(name) || empty(passwd)) {
            localeBean.addError("Login.error.invalidCredentials");
            return "self";
        }
        
        // Try to login the principal
        try {
            FacesContext context = FacesContext.getCurrentInstance();
            HttpServletRequest request = (HttpServletRequest) context
                    .getExternalContext()
                    .getRequest();
            
            // Try to login
            request.login(name, passwd);
            
            // If success, register the principal in the session
            try {
                PrincipalTO principalTo = adminLogic.getPrincipal(name);
                voteSession.setPrincipal(principalTo);
            } catch (Exception ex) {
                // Destroy the session on this kind of exception
                HttpSession session = (HttpSession) FacesContext
                            .getCurrentInstance()
                            .getExternalContext()
                            .getSession(false);
                session.invalidate();
                localeBean.addError("Login.error.internalError");
                return "self";
            }
            
            return "welcome";
        } catch (Exception ex) {
            passwd = null;
            localeBean.addError("Login.error.invalidData");
            return "self";
        }
    }
    
    /**
     * Invalidates the current user session and performs a logout.
     * 
     * @return The logical view id of the next page to display.
     */
    public String logout() {
        // Invalidate session and goto login page
        ExternalContext externalContext = FacesContext
                .getCurrentInstance()
                .getExternalContext();
        externalContext.invalidateSession();
        return "login";
    }
    
    /**
     * Tests if the given string is empty.
     * 
     * @param str The {@link String} to test
     * @return <code>true</code> iff the string is empty and otherwise 
     * <code>false</code>.
     */
    public static boolean empty(String str) {
        return str == null || str.trim().isEmpty();
    }
    
}
