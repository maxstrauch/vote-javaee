package de.vote.web;

import java.util.Iterator;
import javax.faces.FacesException;
import javax.faces.application.NavigationHandler;
import javax.faces.application.ViewExpiredException;
import javax.faces.context.ExceptionHandler;
import javax.faces.context.ExceptionHandlerFactory;
import javax.faces.context.ExceptionHandlerWrapper;
import javax.faces.context.FacesContext;
import javax.faces.event.ExceptionQueuedEvent;
import javax.faces.event.ExceptionQueuedEventContext;

/**
 * This factory creates an exception handler which handles 
 * {@link javax.faces.application.ViewExpiredException}s gracefully by navigating
 * back to the index page.
 * 
 * @see https://weblogs.java.net/blog/edburns/archive/2009/09/03/dealing-gracefully-viewexpiredexception-jsf2
 * @author Daniel Vivas Estevao
 * @author maximilianstrauch
 */
public class ViewExpiredExceptionExceptionHandlerFactory extends ExceptionHandlerFactory {

    private final ExceptionHandlerFactory parent;

    public ViewExpiredExceptionExceptionHandlerFactory(ExceptionHandlerFactory parent) {
        this.parent = parent;
    }

    @Override
    public ExceptionHandler getExceptionHandler() {
        return new ViewExpiredExceptionExceptionHandler(parent.getExceptionHandler());
    }

    public class ViewExpiredExceptionExceptionHandler extends ExceptionHandlerWrapper {

        private final ExceptionHandler wrapped;

        public ViewExpiredExceptionExceptionHandler(ExceptionHandler wrapped) {
            this.wrapped = wrapped;
        }

        @Override
        public ExceptionHandler getWrapped() {
            return this.wrapped;
        }

        @Override
        public void handle() throws FacesException {
            for (Iterator<ExceptionQueuedEvent> i = getUnhandledExceptionQueuedEvents().iterator(); i.hasNext();) {
                ExceptionQueuedEvent event = i.next();
                ExceptionQueuedEventContext context = (ExceptionQueuedEventContext) event.getSource();
                Throwable t = context.getException();
                
                // On view expired exception
                if (t instanceof ViewExpiredException) {
                    try {
                        // Navigate back home
                        FacesContext fc = FacesContext.getCurrentInstance();
                        NavigationHandler nav = fc.getApplication().getNavigationHandler();
                        nav.handleNavigation(fc, null, "index");
                        fc.renderResponse();
                    } finally {
                        i.remove();
                    }
                }
            }
            
            // Handle others
            getWrapped().handle();
        }
    }

}
