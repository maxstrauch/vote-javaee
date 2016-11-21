package de.vote.web;

/**
 * Little helper class to help juggle with exceptions.
 * 
 * @author Daniel Vivas Estevao
 * @author maximilianstrauch
 */
public class ExceptionUtil {

    /**
     * Finds out the reason or cause of the given throwable (exception).
     * 
     * @param t A throwable object, e.g. exception, with a very long stack
     * trace.
     * @return The reason of the throwable or <code>null</code> if no 
     * throwable was given.
     */
    public static final Throwable getRootAsObject(Throwable t) {
        if (t == null) {
            return null;
        }
        
        Throwable cause = t;
        do {
            if (cause.getCause() == null) {
                return cause;
            } else {
                cause = cause.getCause();
            }
        } while (true);
    }
    
}
