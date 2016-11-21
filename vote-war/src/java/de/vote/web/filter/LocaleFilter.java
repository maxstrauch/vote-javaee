package de.vote.web.filter;

import de.vote.LocaleBean;
import java.io.IOException;
import javax.inject.Inject;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

/**
 * This filter is responsible for setting the locale for the current user 
 * session, if not already set, from the locale of the HTTP request. The
 * locale is managed in {@link LocaleBean}.
 * 
 * @author Daniel Vivas Estevao
 * @author maximilianstrauch
 */
public class LocaleFilter implements Filter {

    @Inject
    private LocaleBean localeBean;
    
    @Override
    public void init(FilterConfig filterConfig) throws ServletException { }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, 
            FilterChain chain) throws IOException, ServletException {
        
        // Set the locale if not already set
        if (!localeBean.isValid()) {
            HttpServletRequest req = (HttpServletRequest) request;
            localeBean.setLocale(req.getLocale());
        }
        
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() { }
    
}
