package de.vote;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Named;

/**
 * This backing bean manages the locale of the current user session. It provides
 * beneath the locale features like date formatting and access to the 
 * resource bundle of the current locale.
 * 
 * @author Daniel Vivas Estevao
 * @author maximilianstrauch
 */
@SessionScoped
@Named
public class LocaleBean implements Serializable {
    
    private List<Locale> supportedLocales;
    
    private ResourceBundle lang;
    
    private Locale locale;
    
    @PostConstruct
    public void init() {
        // Populate locales
        supportedLocales = new ArrayList<>();
        supportedLocales.add(Locale.GERMAN);
        supportedLocales.add(Locale.ENGLISH);
    }

    public boolean isValid() {
        return locale != null;
    }
    
    public Locale getLocale() {
        if (locale == null) {
            setLocale(Locale.GERMAN);
        }
        
        return locale;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
        
        try {
            lang = ResourceBundle.getBundle("de.vote.Language", getLocale());
        } catch (Exception ex) {
            System.err.println("No ressource bundle for locale " 
                    + locale + ": " + ex);
        }
    }

    public List<Locale> getSupportedLocales() {
        return supportedLocales;
    }

    public void setSupportedLocales(List<Locale> supportedLocales) {
        this.supportedLocales = supportedLocales;
    }
    
    /**
     * Returns a date formatter for the current locale
     * 
     * @return A date formatter for the current locale.
     */
    public DateFormat getDateFormatForCurrentLocale() {
        Locale current = getLocale();
        if (current.equals(Locale.ENGLISH)) {
            return new SimpleDateFormat("MM/dd/yyyy HH:mm");
        } else {
            // Fallback to German format
            return new SimpleDateFormat("dd.MM.yyyy HH:mm");
        }
    }
    
    /**
     * Returns a message from the ressource bundle of the current locale.
     * 
     * @param key The message key.
     * @param args Arguments to insert into the locale. If a {@link Date} object
     * is passed, it gets automatically converted to a string with the right
     * format according to the current locale.
     * @return The message.
     */
    public String getMessage(String key, Object...args) {
        Object[] parsedArgs = new Object[args.length];
        for (int i = 0; i < args.length; i++) {
            if (args[i].getClass() == Date.class) {
                parsedArgs[i] = getDateFormatForCurrentLocale().format((Date) args[i]);
            } else {
                parsedArgs[i] = String.valueOf(args[i]);
            }
        }
        
        try {
            return MessageFormat.format(lang.getString(key), parsedArgs);
        } catch (Exception ex) {
            return "???" + key + "???";
        }
    }
    
    /**
     * Adds a success message to the current {@link FacesContext} with severity
     * <code>FacesMessage.SEVERITY_INFO</code>.
     * 
     * @param key The message key.
     * @param args Arguments to insert into the message.
     */
    public void addSuccess(String key, Object...args) {
        FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(
                        FacesMessage.SEVERITY_INFO,
                        getMessage(key, args),
                        null
                )
        );
    }
    
    /**
     * Adds an error message to the current {@link FacesContext} with severity
     * <code>FacesMessage.SEVERITY_ERROR</code>.
     * 
     * @param key The message key.
     * @param args Arguments to insert into the message.
     */
    public void addError(String key, Object...args) {
        FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(
                        FacesMessage.SEVERITY_ERROR,
                        getMessage(key, args),
                        null
                )
        );
    }
    
    /**
     * Shorthand to format a date.
     * 
     * @param date Date object to format.
     * @return Formatted date string.
     */
    public String formatDate(Date date) {
        return getDateFormatForCurrentLocale().format(date);
    }
    
    /**
     * Shorthand to format a boolean. This method requires the message keys
     * <code>Bool.true</code> and <code>Bool.false</code>.
     * 
     * @param bool Boolean to format.
     * @return Formatted boolean string.
     */
    public String formatBoolean(boolean bool) {
        return lang.getString(bool ? "Bool.true" : "Bool.false");
    }
    
}
