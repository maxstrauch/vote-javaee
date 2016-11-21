package de.vote.organizer.web;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Simple e-mail address finder utility method.
 * 
 * @author Daniel Vivas Estevao
 * @author maximilianstrauch
 */
public class EMailUtil {
    
    /**
     * Pattern for e-mail-addresses. It is not 100% accurate but will work
     * for the most adresses.
     */
    public static final Pattern EMAIL_PATTERN = Pattern.compile(
            "([_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+"
                    + "(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,}))");
    
    /**
     * Finds all mail addresses in a given string.
     * 
     * @param text The text to inspect or <code>null</code>.
     * @return A non-empty list of all mail addresses found in the given
     * text.
     */
    public static final List<String> find(String text) {
        List<String> finds = new ArrayList<>();
        
        if (text == null || text.isEmpty()) {
            return finds;
        }
        
        Matcher matcher = EMAIL_PATTERN.matcher(text);
        
        while (matcher.find()) {
            finds.add(matcher.group(1));
        }
        
        return finds;
    }
    
}
