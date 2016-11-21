package de.vote.logic.to;

import de.vote.persistence.entities.Principal;

/**
 * TO for {@link Principal}.
 * 
 * @author Daniel Vivas Estevao
 * @author maximilianstrauch
 */
public class PrincipalTO extends AbstractTO {

    private String username, realname, email;
    
    private boolean admin, enabled, thridPartyAuthorized;

    public PrincipalTO(Integer id) {
        super(id);
    }
    
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRealname() {
        return realname;
    }

    public void setRealname(String realname) {
        this.realname = realname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isThridPartyAuthorized() {
        return thridPartyAuthorized;
    }

    public void setThridPartyAuthorized(boolean thridPartyAuthorized) {
        this.thridPartyAuthorized = thridPartyAuthorized;
    }

    public boolean isValid() {
        if (username == null || realname == null || email == null) {
            return false;
        }
        
        return !username.isEmpty() && !realname.isEmpty() && !email.isEmpty();
    }
    
    public boolean equals(PrincipalTO to) {
        return to != null && username.equals(to.username);
    }

    @Override
    public String toString() {
        if (realname != null) {
            return realname + " (" + username + ")";
        }
        
        return username;
    }
    
}
