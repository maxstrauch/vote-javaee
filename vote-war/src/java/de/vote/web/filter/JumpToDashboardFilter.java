package de.vote.web.filter;

import de.vote.organizer.web.EditPoll;
import static de.vote.web.filter.JumpToIndexFilter.getRequestedPage;
import java.io.IOException;
import javax.inject.Inject;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * A simple filter which prevents the user from viewing the poll edit page
 * without a poll set to edit.
 * 
 * @author Daniel Vivas Estevao
 * @author maximilianstrauch
 */
public class JumpToDashboardFilter implements Filter {

    @Inject
    private EditPoll editPoll;
    
    @Override
    public void init(FilterConfig filterConfig) throws ServletException { }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, 
            FilterChain chain) throws IOException, ServletException {
        // If there is no instance of ParticipateBean
        if (editPoll == null) {
            chain.doFilter(request, response);
            return;
        }
        
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        String page = getRequestedPage(req);
        
        // Check if there is a direct call of pages
        if (
            // Page "edit-poll.xhtml"
            ("edit-poll".equals(page) && editPoll.getPoll() == null)
        ) {
            resp.sendRedirect(req.getContextPath() + "/organizer/dashboard.xhtml");
            return;
        }
        
        // Otherwise everything is fine and the next filter can be called ...
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() { }
    
}
