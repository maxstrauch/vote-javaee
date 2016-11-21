package de.vote.web;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.enterprise.context.RequestScoped;
import javax.inject.Named;

/**
 * Simply some version information.
 * 
 * @author Daniel Vivas Estevao
 * @author maximilianstrauch
 */
@RequestScoped
@Named
public class Version implements Serializable {
    
    /**
     * The current version string of the Vote! system.
     */
    private static final String ID = "1.84";
    
    /**
     * The current year.
     */
    private static final String YEAR = new SimpleDateFormat("yyyy")
            .format(new Date());
    
    /**
     * Returns the version information for this version.
     * 
     * @return a version string.
     */
    public String getId() {
        return ID + " &raquo;Afterlife&laquo; &#x00a9; " + YEAR;
    }
    
}
