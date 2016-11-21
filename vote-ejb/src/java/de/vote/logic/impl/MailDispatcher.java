package de.vote.logic.impl;

import de.vote.persistence.entities.Participant;
import de.vote.persistence.entities.Principal;
import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Date;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.faces.context.FacesContext;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;

/**
 * A simple mail dispatcher class. This class handles all mail traffic from
 * the Vote! system. It retrives mail bodies from the resource bundle with
 * the correct locale from <code>de.vote.resources.Mails</code>, formats them
 * by inserting the arguments and sends them via the Java Mail API.
 * 
 * Please note, that all mail related methods are not annotated with @Async in
 * the Vote! system.
 * 
 * @author Daniel Vivas Estevao
 * @author maximilianstrauch
 */
@Stateless
@LocalBean
public class MailDispatcher implements Serializable {
    
    @Resource(lookup = "VoteMail")
    private Session mailSession;
    
    // Public interface methods for sending mails

    /**
     * Sends a mail to a principal.
     * 
     * @param to Target principal.
     * @param resourceKey The message key.
     * @param locale The locale of the mail.
     * @param args Mail arguments.
     * @return Returns <code>true</code> iff the mail was send and otherwise
     * <code>false</code>.
     */
    public boolean sendMail(Principal to, String resourceKey, 
            Locale locale, Object...args) {
        return sendMail(
                to.getEmail(), 
                null, 
                resourceKey, 
                locale, 
                args
        );
    }
    
    /**
     * Sends a mail to a poll participant.
     * 
     * @param to Participant of a poll.
     * @param by Sending principal.
     * @param resourceKey The message key.
     * @param locale The locale of the mail.
     * @param args Mail arguments.
     * @return Returns <code>true</code> iff the mail was send and otherwise
     * <code>false</code>.
     */
    public boolean sendMail(Participant to, Principal by, String resourceKey, 
            Locale locale, Object...args) {
        return sendMail(
                to.getEmail(), 
                by == null ? null : by.getEmail(), 
                resourceKey, 
                locale, 
                args
        );
    }
    
    // Internal methods
    
    /**
     * Internal wrapper method for the internal send mail method which consumes
     * all exceptions, loggs them to the console and returns a flat boolean.
     * 
     * @param to Mail recipient.
     * @param by Mail sender.
     * @param resourceKey Mail message key.
     * @param locale The locale to use for the mail message or <code>null</code>.
     * @param args The arguments for the mail.
     * @return Returns <code>true</code> iff the mail was send and otherwise
     * <code>false</code>.
     */
    private boolean sendMail(String to, String by, 
            String resourceKey, Locale locale, Object...args) {
        try {
            _sendMail(
                    to, 
                    by == null ? null : by, 
                    resourceKey, 
                    locale, 
                    args
            );
            return true;
        } catch (Exception ex) {
            Logger.getLogger(getClass().getName())
                    .log(Level.SEVERE, "Cannot send a mail", ex);
            return false;
        }
    }
    
    /**
     * Internal send mail method.
     * 
     * @param to Mail recipient.
     * @param by Mail sender.
     * @param resourceKey Mail message key.
     * @param locale The locale to use for the mail message or <code>null</code>.
     * @param args The arguments for the mail.
     * @throws Exception Any exception that is trown by the Java Mail API
     * (e.g. syntax errors during creation, transport I/O errors ...)
     */
    private void _sendMail(String to, String by, String resourceKey, 
            Locale locale, Object...args) throws Exception {
        // Create a new message
        Message msg = new MimeMessage(mailSession);
        msg.setSubject(formatMailString(resourceKey + ".title", locale));
        msg.setSentDate(new Date());
        if (by != null) {
            try {
                msg.setReplyTo(InternetAddress.parse(by, false));
            } catch (Exception ex) {
                Logger.getLogger(getClass().getName())
                    .log(Level.SEVERE, "Cannot set Reply-To field in mail");
            }
        }
        
        if (to == null || to.isEmpty()) {
            throw new IllegalArgumentException("Target mail address invalid");
        }
        
        msg.setRecipients(Message.RecipientType.TO, 
                InternetAddress.parse(to, false));
        
        msg.setText(formatMailString(resourceKey, locale, args));
        
        // Send the message
        Transport.send(msg);
    }
    
    /**
     * Formats a mail with arguments.
     * 
     * @param resourceKey The mail message key.
     * @param locale The locale of the mail. If <code>null</code> the fallback
     * locale (GERMAN) is used.
     * @param args The arguments to place inside the mail.
     * @return The formatted mail string.
     */
    private String formatMailString(String resourceKey, Locale locale, Object...args) {
        ResourceBundle bundle = ResourceBundle.getBundle(
                "de.vote.resources.Mails", 
                locale == null ? Locale.GERMAN : locale
        );
        String mail = bundle.getString(resourceKey);
        if (mail == null) {
            throw new IllegalArgumentException("Mail resource not found!");
        }
        return MessageFormat.format(mail, args);
    }
    
    /**
     * Returns the base URL of the current web application.
     * 
     * @return The base URL with an ending slash.
     */
    public static final String getAppURL() {
        // Get the request
        HttpServletRequest request = (HttpServletRequest) FacesContext
                .getCurrentInstance()
                .getExternalContext()
                .getRequest();
        
        // Assemble the URL
        String url = request.getScheme() + "://" + request.getServerName() + ":" 
                + request.getServerPort() 
                + (
                    request.getContextPath().startsWith("/") ? 
                        request.getContextPath() :
                        "/" + request.getContextPath()
                );
        return url.endsWith("/") ? url : url + "/";
    }
    
}
