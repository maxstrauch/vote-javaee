package de.vote.persistence.entities;

import de.vote.logic.to.PrincipalTO;
import javax.persistence.Entity;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToMany;

/**
 * The entity for a principal.
 * 
 * @author Daniel Vivas Estevao
 * @author maximilianstrauch
 */
@Entity
public class Principal extends AbstractEntity<PrincipalTO> {

    @ManyToMany(mappedBy = "organizers", cascade = CascadeType.PERSIST)
    private Set<Poll> polls;
    
    @Enumerated(EnumType.STRING)
    private Role principalRole;
    
    private String username, realname, email, encryptedPassword, salt;
    
    private boolean thirdPartyAuthorized;
    
    public enum Role {
        ADMINISTRATOR, ORGANIZER;
    }

    public Principal() {
        principalRole = Role.ORGANIZER;
        encryptedPassword = null;
        salt = null;
    }

    public Set<Poll> getPolls() {
        return polls;
    }

    public void setPolls(Set<Poll> param) {
        this.polls = param;
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

    public String getEncryptedPassword() {
        return encryptedPassword;
    }

    public void setEncryptedPassword(String encryptedPassword) {
        this.encryptedPassword = encryptedPassword;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public Role getPrincipalRole() {
        return principalRole;
    }

    public void setPrincipalRole(Role role) {
        this.principalRole = role;
    }

    public boolean isThirdPartyAuthorized() {
        return thirdPartyAuthorized;
    }

    public void setThirdPartyAuthorized(boolean thirdPartyAuthorized) {
        this.thirdPartyAuthorized = thirdPartyAuthorized;
    }

    /**
     * Tests if the user is enabled.
     * 
     * @return <code>true</code> if the user is enabled and otherwise 
     * <code>false</code> is returned.
     */
    public boolean isEnabled() {
        return true;
    }
    
    @Override
    public PrincipalTO getTO(boolean deepCopy) {
        PrincipalTO to = new PrincipalTO(id);
        to.setRealname(realname);
        to.setUsername(username);
        to.setEmail(email);
        to.setAdmin(principalRole == Role.ADMINISTRATOR);
        to.setEnabled(isEnabled());
        to.setThridPartyAuthorized(thirdPartyAuthorized);
        return to;
    }
    
}