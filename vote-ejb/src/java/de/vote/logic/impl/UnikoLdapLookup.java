package de.vote.logic.impl;

import de.vote.persistence.entities.Principal;
import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

/**
 * Simple helper class to lookup some data from LDAP.
 * 
 * @author Maximilian Strauch
 * @author riediger
 * @since 2015
 */
public class UnikoLdapLookup {
    
    /**
     * Tries to find more data for a principal (by its user name) in the LDAP
     * directory of the University and adds the data if available. Added data
     * are: full user name (first, last name), e-mail address.
     * 
     * @see Based on Dr. Riediger's UnikoLdapLookup#lookupPerson(String)
     * implementation.
     * @param p the principal to pupulate.
     */
    public static void populate(Principal p) {
        String uid = p.getUsername();
        Hashtable<String, String> env = new Hashtable<>();
        String sp = "com.sun.jndi.ldap.LdapCtxFactory";
        env.put(Context.INITIAL_CONTEXT_FACTORY, sp);

        String ldapUrl = "ldaps://ldap.uni-koblenz.de";
        env.put(Context.PROVIDER_URL, ldapUrl);

        DirContext dctx = null;
        try {
            dctx = new InitialDirContext(env);
            String base = "ou=people,ou=koblenz,dc=uni-koblenz-landau,dc=de";

            SearchControls sc = new SearchControls();
            String[] attributeFilter = {"uid", "sn", "givenname"};
            sc.setReturningAttributes(attributeFilter);
            sc.setSearchScope(SearchControls.SUBTREE_SCOPE);

            String filter = "(&(objectClass=Person)(uid=" + uid + "))";
            NamingEnumeration results = dctx.search(base, filter, sc);
            if (results.hasMore()) {
                SearchResult sr = (SearchResult) results.next();
                Attributes attrs = sr.getAttributes();
                Attribute a = attrs.get("uid");
                if (a != null) {
                    uid = (String) a.get();
                    p.setEmail(uid + "@uni-koblenz.de");
                }
                a = attrs.get("sn");
                String fullName = "";
                if (a != null) {
                    fullName = (String) a.get();
                }
                a = attrs.get("givenname");
                if (a != null) {
                    if (fullName.isEmpty()) {
                        fullName = (String) a.get();
                    } else {
                        fullName = ((String) a.get()) + " " + fullName;
                    }
                    p.setRealname(fullName);
                }
            }
        } catch (NamingException ex) {
            Logger.getLogger(UnikoLdapLookup.class.getName()).log(
                Level.SEVERE, null, ex);
            
            
            System.out.println(""
                    + "ERRR : " + ex);
            
        } finally {
            if (dctx != null) {
                try {
                    dctx.close();
                } catch (NamingException ex) {
                    Logger.getLogger(UnikoLdapLookup.class.getName())
                            .log(Level.SEVERE, null, ex);
                }
            }
        }
    }
    
}
