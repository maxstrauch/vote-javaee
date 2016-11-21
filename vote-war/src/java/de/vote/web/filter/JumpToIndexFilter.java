package de.vote.web.filter;

import de.vote.web.ParticipateBean;
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
 * This filter is generally responsible for the frontend and has two specific
 * tasks:
 * <ol>
 *  <li>iff the index page is displayed and a token is submitted, this 
 *  filter is responsible for transfering the token value to the token field
 *  of the {@link ParticipateBean}.</li>
 *  <li>it prevents the view of the pages vote or thank-you if no poll is
 *  set or was actually finished.</li>
 * </ol>
 * 
 * @author Daniel Vivas Estevao
 * @author maximilianstrauch
 */
public class JumpToIndexFilter implements Filter {

    @Inject
    private ParticipateBean participateBean;
    
    @Override
    public void init(FilterConfig filterConfig) throws ServletException { }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, 
            FilterChain chain) throws IOException, ServletException {

        // If there is no instance of ParticipateBean
        if (participateBean == null) {
            chain.doFilter(request, response);
            return;
        }
        
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        String page = getRequestedPage(req);
        
        // Inject the value of the variable input field on the index page
        // into the participate backing bean
        if ("index".equals(page)) {
            // Find the right key
            for (String key : req.getParameterMap().keySet()) {
                if (key.matches(".*t0kEn.*")) {
                    participateBean.setToken(req.getParameter(key));
                    break;
                }
            }
        }
        
        // Check if there is a direct call of pages
        if (
            // Page "vote.xhtml"
            ("vote".equals(page) && participateBean.getPollTO() == null) ||
            // Page "thankyou.xhtml"
            ("thankyou".equals(page) && !participateBean.isHasParticipated())
        ) {
            resp.sendRedirect(req.getContextPath() + "/index.xhtml");
            return;
        }
        
        // Otherwise everything is fine and the next filter can be called ...
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() { }
    
    public static final String getRequestedPage(HttpServletRequest request) {
        String uri = request.getRequestURI();
        
        // If a XHTML page is requested, return its name
        if (uri.endsWith(".xhtml")) {
            return uri.substring(uri.lastIndexOf('/') + 1, uri.length() - 6);
        }
        
        // Otherwise return an empty string
        return "";
    }
    
}
