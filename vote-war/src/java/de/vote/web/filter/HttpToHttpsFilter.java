package de.vote.web.filter;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This filter redirects all users using a unsecure HTTP connection to a secure
 * HTTPS connection.
 * 
 * @author Daniel Vivas Estevao
 * @author maximilianstrauch
 */
public class HttpToHttpsFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException { }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, 
            FilterChain chain) throws IOException, ServletException {
        
        // Redirect the user to HTTPS if he is using plain HTTP
        HttpServletRequest requ = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        if ("http".equalsIgnoreCase(requ.getScheme())) {
            resp.sendRedirect(
                "https://" + request.getServerName() + ":8181"  
                    + requ.getRequestURI()
            );
            return;
        }
        
        // Otherwise everything is fine and the next filter can be called ...
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() { }
    
}
